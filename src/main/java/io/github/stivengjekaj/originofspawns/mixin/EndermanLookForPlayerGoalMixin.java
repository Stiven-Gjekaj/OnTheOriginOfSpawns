package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enderman;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets = "net.minecraft.world.entity.monster.Enderman$EndermanLookForPlayerGoal")
public abstract class EndermanLookForPlayerGoalMixin {
	@Shadow
	@Final
	private Enderman enderman;

	/**
	 * The genes change the distance in which the enderman sees a stare and gets angry.
	 */
	@WrapOperation(method = "canUse", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Enderman$EndermanLookForPlayerGoal;getFollowDistance()D"))
	private double originofspawns$stareRange(@Coerce TargetGoal goal, Operation<Double> original) {
		return original.call(goal) * GeneCombat.stareRangeMultiplier(this.enderman);
	}
}
