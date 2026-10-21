package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.monster.Creeper;

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
				default -> {
				}
			}
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
}
