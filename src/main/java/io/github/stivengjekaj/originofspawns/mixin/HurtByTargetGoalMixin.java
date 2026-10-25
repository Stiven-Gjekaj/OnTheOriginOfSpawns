package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneAnger;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HurtByTargetGoal.class)
public abstract class HurtByTargetGoalMixin {
	/**
	 * The anger range genes make the call for help of a hurt mob reach farther.
	 */
	@ModifyVariable(method = "alertOthers", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	private double originofspawns$angerRange(double within) {
		return within * GeneAnger.rangeMultiplier(((TargetGoalAccessor) this).originofspawns$mob());
	}
}
