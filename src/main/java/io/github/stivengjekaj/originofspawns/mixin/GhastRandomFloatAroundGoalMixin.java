package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Ghast.RandomFloatAroundGoal.class)
public abstract class GhastRandomFloatAroundGoalMixin {
	@Shadow
	@Final
	private Mob ghast;

	/**
	 * The float height genes raise the place that the ghast floats to while it has a target.
	 */
	@ModifyArg(method = "start", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/control/MoveControl;setWantedPosition(DDDD)V"), index = 1)
	private double originofspawns$floatHeight(double y) {
		return GeneBehaviors.floatHeight(ghast, y);
	}
}
