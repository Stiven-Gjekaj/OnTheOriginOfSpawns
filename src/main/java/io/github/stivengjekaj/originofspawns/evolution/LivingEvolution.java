package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.LurkGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Lets a mob that lives a long time change one gene toward its pool, but only where no player sees it.
 * A mob with a name tag never changes. This changes only the mob, not the pool.
 */
public final class LivingEvolution {
	/**
	 * The ticks between two checks: about one minute.
	 */
	public static final int CHECK_TICKS = 1200;

	/**
	 * The age that a mob needs: one Minecraft day.
	 */
	public static final long MIN_AGE = 24000L;

	/**
	 * The chance at each check that a mob that can change, changes.
	 */
	public static final float CHANCE = 0.05F;

	/**
	 * No player may be in this distance.
	 */
	public static final double PLAYER_RADIUS = 32.0;

	/**
	 * A player in this distance with a line of sight to the mob sees it.
	 */
	public static final double SIGHT_RANGE = 128.0;

	private LivingEvolution() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(LivingEvolution::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % CHECK_TICKS != 0) {
			return;
		}
		for (ServerLevel level : server.getAllLevels()) {
			List<Mob> mobs = new ArrayList<>();
			for (Entity entity : level.getAllEntities()) {
				if (entity instanceof Mob mob && canChange(mob, level)) {
					mobs.add(mob);
				}
			}
			for (Mob mob : mobs) {
				if (mob.getRandom().nextFloat() < CHANCE) {
					evolve(mob, level);
				}
			}
		}
	}

	/**
	 * Tells if a mob can change its genes now: it has genes, it has no name tag, it is old enough, no player is near,
	 * and no player can see it.
	 */
	public static boolean canChange(Mob mob, ServerLevel level) {
		if (!mob.isAlive() || mob.hasCustomName() || !mob.hasAttached(ModAttachments.GENES)) {
			return false;
		}
		Long born = mob.getAttached(ModAttachments.BORN);
		if (born == null || level.getGameTime() - born < MIN_AGE) {
			return false;
		}
		for (ServerPlayer player : level.players()) {
			double distance = player.distanceTo(mob);
			if (distance <= PLAYER_RADIUS || distance <= SIGHT_RANGE && player.hasLineOfSight(mob)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Swaps the weakest gene of the mob for the gene that its pool favors most, if the pool favors that gene more.
	 *
	 * @return true if the mob changed
	 */
	public static boolean evolve(Mob mob, ServerLevel level) {
		CarriedGenes old = mob.getAttached(ModAttachments.GENES);
		if (old == null || old.genes().isEmpty()) {
			return false;
		}
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		GenePools pools = GenePools.get(level.getServer());
		Identifier weakest = old.genes().stream()
				.min(Comparator.comparingDouble(id -> pools.frequency(species, category, id))).orElseThrow();
		Optional<Identifier> best = Optional.empty();
		float bestFrequency = pools.frequency(species, category, weakest);
		for (Map.Entry<ResourceKey<Gene>, Gene> entry : level.registryAccess().lookupOrThrow(ModRegistries.GENE).entrySet()) {
			Identifier id = entry.getKey().identifier();
			float frequency = pools.frequency(species, category, id);
			if (entry.getValue().carriedBy(mob.getType()) && !old.genes().contains(id) && frequency > bestFrequency) {
				best = Optional.of(id);
				bestFrequency = frequency;
			}
		}
		if (best.isEmpty()) {
			return false;
		}
		List<Identifier> genes = new ArrayList<>(old.genes());
		genes.set(genes.indexOf(weakest), best.get());
		replace(mob, level, old, Evolution.carry(level, mob, genes));
		Gene gained = level.registryAccess().lookupOrThrow(ModRegistries.GENE).getValue(best.get());
		if (gained != null) {
			GeneBehaviors.onGain(mob, gained.effects().toList());
		}
		return true;
	}

	/**
	 * Gives a mob new genes: it changes the attributes, keeps the part of health that the mob had, and installs the
	 * goals again.
	 */
	private static void replace(Mob mob, ServerLevel level, CarriedGenes old, CarriedGenes next) {
		float health = mob.getHealth() / mob.getMaxHealth();
		GeneEffects.removeAttributes(mob, GeneEffects.sources(level.registryAccess(), old));
		mob.setAttached(ModAttachments.GENES, next);
		GeneEffects.applyAttributes(mob, GeneEffects.sources(level.registryAccess(), next));
		mob.setHealth(mob.getMaxHealth() * health);
		((MobAccessor) mob).originofspawns$goalSelector().removeAllGoals(
				goal -> goal instanceof LeapGoal || goal instanceof LurkGoal || goal instanceof SiegeGoal);
		mob.setGlowingTag(false);
		GeneBehaviors.onLoss(mob, GeneEffects.sources(level.registryAccess(), old), 
				GeneEffects.sources(level.registryAccess(), next));
		GeneBehaviors.installGoals(mob);
	}
}
