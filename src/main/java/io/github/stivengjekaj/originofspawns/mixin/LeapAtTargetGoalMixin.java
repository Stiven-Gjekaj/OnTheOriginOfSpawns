package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeapAtTargetGoal.class)
public abstract class LeapAtTargetGoalMixin {
	@Shadow
	@Final
	private Mob mob;

	/**
	 * The vanilla leap of a spider or a wolf counts as a leap for the genes.
	 */
	@Inject(method = "start", at = @At("TAIL"))
	private void originofspawns$markLeap(CallbackInfo info) {
		GeneBehaviors.markLeap(mob);
	}
}
