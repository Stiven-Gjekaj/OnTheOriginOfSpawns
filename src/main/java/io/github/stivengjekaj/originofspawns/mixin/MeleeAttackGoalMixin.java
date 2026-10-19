package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
	@Shadow
	@Final
	protected PathfinderMob mob;

	@Inject(method = "getAttackInterval", at = @At("RETURN"), cancellable = true)
	private void originofspawns$scaleInterval(CallbackInfoReturnable<Integer> info) {
		if (mob.hasAttached(ModAttachments.GENES)) {
			info.setReturnValue(Math.max(1, Math.round(info.getReturnValueI() * GeneBehaviors.attackIntervalMultiplier(mob))));
		}
	}
}
