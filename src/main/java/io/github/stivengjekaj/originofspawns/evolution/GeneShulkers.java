package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;

/**
 * The genes of a shulker: its bullets, its shell, its teleports, and its split.
 */
public final class GeneShulkers {
	private GeneShulkers() {
	}

	/**
	 * Returns the speed multiplier of the bullets of the owner: the product of its bullet speed genes. With no owner, it
	 * is 1.
	 */
	public static double bulletSpeed(Entity owner) {
		double multiplier = 1.0;
		if (owner != null) {
			for (GeneEffect effect : GeneEffects.of(owner).toList()) {
				if (effect instanceof GeneEffect.BulletSpeed speed) {
					multiplier *= speed.multiplier();
				}
			}
		}
		return multiplier;
	}

	/**
	 * Returns the Levitation of a bullet of the owner, after its bullet levitation genes: the longest time and the highest
	 * level of the genes and of the vanilla effect.
	 */
	public static MobEffectInstance levitation(Entity owner, MobEffectInstance vanilla) {
		int ticks = vanilla.getDuration();
		int amplifier = vanilla.getAmplifier();
		boolean changed = false;
		if (owner != null) {
			for (GeneEffect effect : GeneEffects.of(owner).toList()) {
				if (effect instanceof GeneEffect.BulletLevitation levitation) {
					changed = true;
					ticks = Math.max(ticks, levitation.seconds().map(seconds -> Math.round(seconds * 20.0F)).orElse(0));
					amplifier = Math.max(amplifier, levitation.amplifier().orElse(0));
				}
			}
		}
		return changed ? new MobEffectInstance(vanilla.getEffect(), ticks, amplifier) : vanilla;
	}
}
