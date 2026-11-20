package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import net.minecraft.world.entity.animal.equine.SkeletonHorse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SkeletonHorse.class)
public abstract class SkeletonHorseMixin {
	/**
	 * Each trap life gene multiplies the time before a trap that no player finds goes away. The vanilla time is 18000
	 * ticks, 15 minutes.
	 */
	@ModifyConstant(method = "aiStep", constant = @Constant(intValue = 18000))
	private int originofspawns$trapLife(int ticks) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of((SkeletonHorse) (Object) this).toList()) {
			if (effect instanceof GeneEffect.TrapLife life) {
				multiplier *= life.multiplier();
			}
		}
		return Math.round(ticks * multiplier);
	}
}
