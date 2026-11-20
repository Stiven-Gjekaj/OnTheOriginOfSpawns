package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.world.entity.monster.Ravager;

/**
 * The genes of a ravager: its roar, its stun, its charge, and its rider.
 */
public final class GeneRavagers {
	/**
	 * The range of the vanilla roar, in blocks.
	 */
	public static final double VANILLA_ROAR_RANGE = 4.0;

	private GeneRavagers() {
	}

	/**
	 * Returns the range of the roar of the ravager: the largest roar range gene, or the vanilla range.
	 */
	public static double roarRange(Ravager ravager) {
		double range = VANILLA_ROAR_RANGE;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarRange roar) {
				range = Math.max(range, roar.blocks());
			}
		}
		return range;
	}
}
