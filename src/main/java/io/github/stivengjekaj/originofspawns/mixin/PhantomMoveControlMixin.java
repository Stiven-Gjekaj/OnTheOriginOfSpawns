package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomMoveControl")
public abstract class PhantomMoveControlMixin {
	/**
	 * The flying speed genes change the speed that the phantom wants in its circle and its swoop.
	 */
	@ModifyArgs(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"))
	private void originofspawns$flyingSpeed(Args args) {
		double multiplier = GeneBehaviors.flyingSpeedMultiplier(((MoveControlAccessor) this).originofspawns$mob());
		if (multiplier != 1.0) {
			args.set(0, (double) args.get(0) * multiplier);
			args.set(1, (double) args.get(1) * multiplier);
			args.set(2, (double) args.get(2) * multiplier);
		}
	}
}
