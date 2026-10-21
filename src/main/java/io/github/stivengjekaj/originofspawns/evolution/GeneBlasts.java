package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import io.github.stivengjekaj.originofspawns.mixin.CreeperAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * The effects of genes that act on the blast of a creeper: its size, its fuse, and what it leaves behind.
 */
public final class GeneBlasts {
	private GeneBlasts() {
	}

	/**
	 * Returns the product of the blast size multipliers of the creeper whose states are true now.
	 */
	public static float sizeMultiplier(Creeper creeper) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(creeper).toList()) {
			if (effect instanceof GeneEffect.BlastSize size && GeneCombat.test(creeper, size.when())) {
				multiplier *= size.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Returns the ticks of the fuse of the creeper after its fuse time multipliers. The fuse lasts 1 tick or more.
	 */
	public static int fuseTicks(Creeper creeper, int ticks) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(creeper).toList()) {
			if (effect instanceof GeneEffect.FuseTime fuse) {
				multiplier *= fuse.multiplier();
			}
		}
		return multiplier == 1.0F ? ticks : Math.max(1, Math.round(ticks * multiplier));
	}

	/**
	 * Runs the effects that act after the blast of the creeper, at the place of the blast.
	 *
	 * @param radius the radius of the blast, after the charge and the genes
	 */
	public static void afterBlast(Creeper creeper, ServerLevel level, float radius) {
		for (GeneEffect effect : GeneEffects.of(creeper).toList()) {
			switch (effect) {
				case GeneEffect.BlastCloud cloud -> blastCloud(creeper, level, cloud, radius);
				case GeneEffect.ChainReaction chain when !creeper.getAttachedOrElse(ModAttachments.CHAIN_LIT, false) ->
						chainReaction(creeper, level, chain);
				case GeneEffect.Aftershock aftershock -> {
					double x = creeper.getX();
					double y = creeper.getY();
					double z = creeper.getZ();
					// The creeper is gone then, but it stays the cause, so the damage effects of its genes still act.
					// This plain blast does not run the effects after a blast, so it causes no more aftershocks.
					DelayedTasks.schedule(level, Math.round(aftershock.seconds() * 20.0F), () -> level.explode(creeper,
							x, y, z, radius * aftershock.fraction(), Level.ExplosionInteraction.MOB));
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

	private static void blastCloud(Creeper creeper, ServerLevel level, GeneEffect.BlastCloud cloud, float radius) {
		AreaEffectCloud area = new AreaEffectCloud(level, creeper.getX(), creeper.getY(), creeper.getZ());
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
	 * the direct entity and as the cause.
	 */
	public static Mob blaster(DamageSource source) {
		return source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() instanceof Mob mob
				&& source.getDirectEntity() == mob && mob.hasAttached(ModAttachments.GENES) ? mob : null;
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
