package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import io.github.stivengjekaj.originofspawns.mixin.CreeperAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The effects of genes that act on the blast of a mob: its size, the fuse of a creeper, and what the blast leaves
 * behind.
 */
public final class GeneBlasts {
	/**
	 * The shortest fuse that the genes give.
	 */
	private static final int MIN_FUSE_TICKS = 3;

	private GeneBlasts() {
	}

	/**
	 * Returns the product of the blast size multipliers of the mob whose states are true now.
	 */
	public static float sizeMultiplier(Mob mob) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.BlastSize size && GeneCombat.test(mob, size.when())) {
				multiplier *= size.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Returns the ticks of the fuse of the creeper after its fuse time multipliers. The genes do not make the fuse
	 * shorter than 3 ticks, because the game divides the swell by the fuse less 2 ticks.
	 */
	public static int fuseTicks(Creeper creeper, int ticks) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(creeper).toList()) {
			if (effect instanceof GeneEffect.FuseTime fuse) {
				multiplier *= fuse.multiplier();
			}
		}
		return multiplier == 1.0F ? ticks : Math.max(Math.min(ticks, MIN_FUSE_TICKS), Math.round(ticks * multiplier));
	}

	/**
	 * Runs the effects that act after the blast of the mob, at the place of the blast.
	 *
	 * @param pos the center of the blast
	 * @param radius the radius of the blast, after the charge and the genes
	 */
	public static void afterBlast(Mob mob, ServerLevel level, Vec3 pos, float radius) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.BlastCloud cloud -> blastCloud(level, pos, cloud, radius);
				case GeneEffect.ChainReaction chain when mob instanceof Creeper creeper
						&& !creeper.getAttachedOrElse(ModAttachments.CHAIN_LIT, false) -> chainReaction(creeper, level, chain);
				case GeneEffect.BlastLightning lightning -> {
					LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
					if (bolt != null) {
						bolt.snapTo(pos.x, pos.y, pos.z);
						bolt.setAttached(ModAttachments.NO_CHARGE, true);
						level.addFreshEntity(bolt);
					}
				}
				case GeneEffect.Aftershock aftershock ->
					// A creeper is gone then, but it stays the cause, so the damage effects of its genes still act.
					// This plain blast does not run the effects after a blast, so it causes no more aftershocks.
					DelayedTasks.schedule(level, Math.round(aftershock.seconds() * 20.0F), () -> level.explode(mob,
							pos.x, pos.y, pos.z, radius * aftershock.fraction(), Level.ExplosionInteraction.MOB));
				case GeneEffect.BlastShieldStop stop -> {
					for (Player player : level.getEntitiesOfClass(Player.class, AABB.ofSize(pos, radius * 4.0, radius * 4.0,
							radius * 4.0), player -> player.position().distanceTo(pos) <= radius * 2.0F)) {
						ItemStack shield = player.getItemBlockingWith();
						if (shield != null && shield.has(DataComponents.BLOCKS_ATTACKS)) {
							shield.get(DataComponents.BLOCKS_ATTACKS).disable(level, player, stop.seconds(), shield);
						}
					}
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Lights the fuses of the creepers near the creeper whose fuses do not burn yet, and marks them as lit by a chain.
	 */
	private static void chainReaction(Creeper creeper, ServerLevel level, GeneEffect.ChainReaction chain) {
		for (Creeper other : level.getEntitiesOfClass(Creeper.class, creeper.getBoundingBox().inflate(chain.radius()),
				other -> other != creeper && other.isAlive() && !other.isIgnited() && other.getSwellDir() <= 0
						&& other.distanceTo(creeper) <= chain.radius())) {
			other.setAttached(ModAttachments.CHAIN_LIT, true);
			other.ignite();
		}
	}

	private static void blastCloud(ServerLevel level, Vec3 pos, GeneEffect.BlastCloud cloud, float radius) {
		AreaEffectCloud area = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
		int duration = Math.max(1, Math.round(cloud.seconds() * 20.0F));
		area.setRadius(radius);
		area.setDuration(duration);
		area.setRadiusPerTick(-radius / duration);
		int effectTicks = Math.round(cloud.effectSeconds().orElse(cloud.seconds()) * 20.0F);
		area.addEffect(new MobEffectInstance(cloud.effect(), effectTicks, cloud.amplifier()));
		level.addFreshEntity(area);
	}

	/**
	 * Returns the mob with genes whose own blast is the source, or null. The blast of a creeper has the creeper as
	 * the direct entity and as the cause. The blast of a large fireball has the fireball as the direct entity and the
	 * mob that shot it as the cause.
	 */
	public static Mob blaster(DamageSource source) {
		return source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() instanceof Mob mob
				&& (source.getDirectEntity() == mob || source.getDirectEntity() instanceof LargeFireball)
				&& mob.hasAttached(ModAttachments.GENES) ? mob : null;
	}

	/**
	 * Returns the mob with genes that shot the projectile, or null. A projectile that a player hits back has the
	 * player as its owner, so it returns null.
	 */
	public static Mob shooter(Projectile projectile) {
		return projectile.getOwner() instanceof Mob mob && mob.hasAttached(ModAttachments.GENES) ? mob : null;
	}

	/**
	 * Runs the effects that act on an entity that the blast of a mob damaged.
	 */
	public static void onBlastHit(LivingEntity victim, DamageSource source, float damage, boolean blocked) {
		Mob mob = blaster(source);
		if (mob == null || mob == victim || blocked || damage <= 0.0F) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.BlastEffect blast) {
				victim.addEffect(new MobEffectInstance(blast.effect(), Math.round(blast.seconds() * 20.0F),
						blast.amplifier()), mob);
			}
		}
	}

	/**
	 * Returns true if the source is the blast of a mob whose blasts go through shields.
	 */
	public static boolean piercesShields(DamageSource source) {
		Mob mob = blaster(source);
		return mob != null && GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.PierceShields pierce
				&& pierce.blasts());
	}

	/**
	 * Makes the creeper a charged creeper, with no lightning.
	 */
	public static void charge(Creeper creeper) {
		creeper.getEntityData().set(CreeperAccessor.originofspawns$poweredData(), true);
	}
}
