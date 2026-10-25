package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractPiglin.class)
public abstract class AbstractPiglinMixin {
	/**
	 * The conversion time genes change the time in the Overworld before a piglin or a piglin brute turns into a
	 * zombified piglin.
	 */
	@ModifyConstant(method = "customServerAiStep", constant = @Constant(intValue = 300))
	private int originofspawns$conversionTime(int ticks) {
		return GeneBehaviors.conversionTicks((AbstractPiglin) (Object) this, ticks);
	}
}
