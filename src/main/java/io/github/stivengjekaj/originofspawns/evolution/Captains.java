package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;

/**
 * The captains of patrols and of raid waves. Only a pillager with a gene of the Captain family can be a captain. Only
 * a captain wears the ominous banner, and only the death of a captain gives Bad Omen.
 */
public final class Captains {
	/**
	 * The main family of the genes that let a pillager be a captain.
	 */
	public static final Identifier CAPTAIN_FAMILY = OriginOfSpawns.id("pillager/captain");
	/**
	 * The members of one patrol spawn in this radius of each other.
	 */
	private static final double PATROL_RADIUS = 16.0;

	private Captains() {
	}

	/**
	 * Returns true if the mob carries a gene whose main family is the Captain family.
	 */
	public static boolean canLead(Mob mob) {
		CarriedGenes carried = mob.getAttached(ModAttachments.GENES);
		if (carried == null) {
			return false;
		}
		Registry<Gene> genes = mob.level().registryAccess().lookupOrThrow(ModRegistries.GENE);
		return carried.genes().stream().map(genes::getValue)
				.anyMatch(gene -> gene != null && CAPTAIN_FAMILY.equals(gene.family()));
	}

	/**
	 * Chooses the captain when a patrol member or a raider first joins the world. The game chooses its captain before
	 * the genes exist, so the choice changes here: a captain with no Captain gene is not a captain, and a pillager with
	 * a Captain gene becomes the captain if its patrol or its wave has none.
	 */
	public static void onJoin(PatrollingMonster mob, ServerLevel level) {
		boolean canLead = canLead(mob);
		if (mob instanceof Raider raider && raider.getCurrentRaid() != null) {
			Raid raid = raider.getCurrentRaid();
			Raider leader = raid.getLeader(raider.getWave());
			if (leader == raider && !canLead) {
				raid.removeLeader(raider.getWave());
				demote(mob);
			} else if (leader == null && canLead) {
				mob.setPatrolLeader(true);
				raid.setLeader(raider.getWave(), raider);
			}
			return;
		}
		boolean inPatrol = mob.getAttached(ModAttachments.SPAWN_REASON) == EntitySpawnReason.PATROL;
		if (mob.isPatrolLeader() && !canLead) {
			demote(mob);
		} else if (!mob.isPatrolLeader() && canLead && inPatrol && patrol(mob, level).stream()
				.noneMatch(PatrollingMonster::isPatrolLeader)) {
			mob.setPatrolLeader(true);
			mob.setItemSlot(EquipmentSlot.HEAD, banner(level));
			mob.setDropChance(EquipmentSlot.HEAD, 2.0F);
		}
		// The members of a patrol go to the place of the patrol, also when the patrol has no captain to lead them.
		if (inPatrol && !mob.hasPatrolTarget()) {
			patrol(mob, level).stream().filter(PatrollingMonster::hasPatrolTarget).findFirst()
					.ifPresent(other -> mob.setPatrolTarget(other.getPatrolTarget()));
		}
	}

	private static java.util.List<PatrollingMonster> patrol(PatrollingMonster mob, ServerLevel level) {
		return level.getEntitiesOfClass(PatrollingMonster.class, mob.getBoundingBox().inflate(PATROL_RADIUS),
				other -> other != mob && other.isAlive());
	}

	/**
	 * Takes the captain role and the ominous banner from the mob.
	 */
	private static void demote(PatrollingMonster mob) {
		mob.setPatrolLeader(false);
		if (ItemStack.matches(mob.getItemBySlot(EquipmentSlot.HEAD), banner((ServerLevel) mob.level()))) {
			mob.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
		}
	}

	/**
	 * Returns true if the stack is the ominous banner.
	 */
	public static boolean isBanner(ServerLevel level, ItemStack stack) {
		return ItemStack.matches(stack, banner(level));
	}

	private static ItemStack banner(ServerLevel level) {
		return Raid.getOminousBannerInstance(level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN));
	}
}
