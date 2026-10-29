package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Guardian.class)
public abstract class GuardianMixin {
	/**
	 * The spike genes change the hit of the spikes on an attacker.
	 */
	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private boolean originofspawns$spikes(LivingEntity attacker, ServerLevel level, DamageSource source, float damage,
			Operation<Boolean> original) {
		return original.call(attacker, level, source, GeneBehaviors.spikeDamage((Guardian) (Object) this, damage));
	}

	/**
	 * The laser charge genes change the ticks of the charge of the laser. The client draws the beam with the same
	 * ticks.
	 */
	@Inject(method = "getAttackDuration", at = @At("RETURN"), cancellable = true)
	private void originofspawns$laserCharge(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(GeneBehaviors.laserChargeTicks((Guardian) (Object) this, info.getReturnValueI()));
	}
}
