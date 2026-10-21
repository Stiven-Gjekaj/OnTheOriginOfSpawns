package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

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
}
