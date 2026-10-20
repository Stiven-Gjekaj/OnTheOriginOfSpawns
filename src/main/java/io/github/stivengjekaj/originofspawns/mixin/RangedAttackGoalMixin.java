package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applies the throw rate and the throw range of the genes to the ranged attack of a mob, for example the trident of
 * the drowned.
 */
@Mixin(RangedAttackGoal.class)
public abstract class RangedAttackGoalMixin {
	@Shadow
	@Final
	private Mob mob;

	@Shadow
	private LivingEntity target;

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(F)I"))
	private int originofspawns$throwRateAfterAttack(int ticks) {
		return GeneBehaviors.scaleThrowInterval(mob, ticks);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I"))
	private int originofspawns$throwRateAtStart(int ticks) {
		return GeneBehaviors.scaleThrowInterval(mob, ticks);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/ai/goal/RangedAttackGoal;attackRadiusSqr:F"))
	private float originofspawns$throwRangeSquared(float radiusSquared) {
		float multiplier = GeneBehaviors.throwRangeMultiplier(mob, target);
		return radiusSquared * multiplier * multiplier;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/ai/goal/RangedAttackGoal;attackRadius:F"))
	private float originofspawns$throwRange(float radius) {
		return radius * GeneBehaviors.throwRangeMultiplier(mob, target);
	}
}
