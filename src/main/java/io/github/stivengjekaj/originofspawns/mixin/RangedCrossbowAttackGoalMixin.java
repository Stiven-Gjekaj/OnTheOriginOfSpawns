package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedCrossbowAttackGoal.class)
public abstract class RangedCrossbowAttackGoalMixin {
	@Shadow
	@Final
	private Monster mob;

	@Shadow
	private int attackDelay;

	// The first write of the attack delay in the tick comes after the load. The second write counts the delay down.
	@Inject(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER,
			target = "Lnet/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal;attackDelay:I"))
	private void originofspawns$shotRate(CallbackInfo info) {
		attackDelay = GeneBehaviors.scaleThrowInterval(mob, attackDelay);
	}
}
