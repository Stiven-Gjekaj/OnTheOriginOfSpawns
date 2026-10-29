package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Guardian;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Guardian.class)
public abstract class GuardianMixin {
	/**
	 * The laser charge genes change the ticks of the charge of the laser. The client draws the beam with the same
	 * ticks.
	 */
	@Inject(method = "getAttackDuration", at = @At("RETURN"), cancellable = true)
	private void originofspawns$laserCharge(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(GeneBehaviors.laserChargeTicks((Guardian) (Object) this, info.getReturnValueI()));
	}
}
