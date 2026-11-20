package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneRavagers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the genes of a ravager change its roar and its stun.
 */
@Mixin(Ravager.class)
public abstract class RavagerMixin {
	/**
	 * A roar range gene changes the range of the roar.
	 */
	@ModifyConstant(method = "roar", constant = @Constant(doubleValue = 4.0))
	private double originofspawns$roarRange(double range) {
		return GeneRavagers.roarRange((Ravager) (Object) this);
	}

	/**
	 * A roar damage gene changes the damage of the roar.
	 */
	@ModifyConstant(method = "roar", constant = @Constant(floatValue = 6.0F))
	private float originofspawns$roarDamage(float damage) {
		return GeneRavagers.roarDamage((Ravager) (Object) this, damage);
	}

	/**
	 * Each roar effect gene gives its effect to each entity that the roar hurts.
	 */
	@WrapOperation(method = "roar", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private boolean originofspawns$roarHit(LivingEntity target, ServerLevel level, DamageSource source, float damage,
			Operation<Boolean> original) {
		boolean hurt = original.call(target, level, source, damage);
		if (hurt) {
			GeneRavagers.roarHit((Ravager) (Object) this, target);
		}
		return hurt;
	}

	/**
	 * Each ally roar effect gene gives its effect to the raiders near the ravager after a roar.
	 */
	@Inject(method = "roar", at = @At("TAIL"))
	private void originofspawns$roarAllies(CallbackInfo info) {
		Ravager self = (Ravager) (Object) this;
		if (self.isAlive() && !self.level().isClientSide()) {
			GeneRavagers.roarAllies(self);
		}
	}

	/**
	 * A stun chance gene changes the chance that a shield block stuns the ravager.
	 */
	@ModifyConstant(method = "blockedByItem", constant = @Constant(doubleValue = 0.5))
	private double originofspawns$stunChance(double chance) {
		return GeneRavagers.stunChance((Ravager) (Object) this);
	}
}
