package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

/**
 * Applies the throw rate, the throw range, and the keep distance effects of the genes to the bow attack of a mob,
 * for example a skeleton.
 */
@Mixin(RangedBowAttackGoal.class)
public abstract class RangedBowAttackGoalMixin {
	@Shadow
	@Final
	private Monster mob;

	@Shadow
	private int attackTime;

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;attackIntervalMin:I", opcode = Opcodes.GETFIELD))
	private int originofspawns$shotRate(int ticks) {
		return GeneBehaviors.scaleThrowInterval(mob, ticks);
	}

	// The tick has three checks with 20: the see time, the strafe time, and the draw time. The draw time is the third.
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 20, ordinal = 2))
	private int originofspawns$drawTime(int ticks) {
		return GeneBehaviors.drawTicks(mob, ticks);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;attackRadiusSqr:F", opcode = Opcodes.GETFIELD))
	private float originofspawns$shotRange(float radiusSquared) {
		float multiplier = GeneBehaviors.throwRangeMultiplier(mob, mob.getTarget());
		return radiusSquared * multiplier * multiplier;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;strafingBackwards:Z", opcode = Opcodes.GETFIELD))
	private boolean originofspawns$keepDistance(boolean backwards) {
		return backwards || GeneBehaviors.keepsDistance(mob);
	}

	// The first write of the attack time in the tick comes after a shot. The second write counts the time down.
	@Inject(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER,
			target = "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;attackTime:I"))
	private void originofspawns$barrage(CallbackInfo info) {
		GeneBehaviors.barrage(mob, mob.getTarget(), attackTime);
	}
}
