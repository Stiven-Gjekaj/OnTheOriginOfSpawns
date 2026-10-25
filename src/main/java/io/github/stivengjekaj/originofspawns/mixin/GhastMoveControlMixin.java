package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastMoveControl")
public abstract class GhastMoveControlMixin {
	/**
	 * The flying speed genes multiply the flying speed attribute that the move control of a ghast uses.
	 */
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Mob;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
	private double originofspawns$flyingSpeed(double speed) {
		return speed * GeneBehaviors.flyingSpeedMultiplier(((MoveControlAccessor) this).originofspawns$mob());
	}
}
