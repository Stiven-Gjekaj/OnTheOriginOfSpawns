package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

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
}
