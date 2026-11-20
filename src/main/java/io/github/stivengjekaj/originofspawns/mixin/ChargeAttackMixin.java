package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ChargeAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the vanilla charge attack of a nautilus act as a melee hit for the genes of the mob.
 */
@Mixin(ChargeAttack.class)
public abstract class ChargeAttackMixin {
	/**
	 * Before the charge hits, a shield break gene stops the shield of the target, so that the charge hits.
	 */
	@Inject(method = "dealDamageToTarget", at = @At("HEAD"))
	private void originofspawns$shieldBreak(ServerLevel level, Animal body, LivingEntity target, CallbackInfo info) {
		if (body.hasAttached(ModAttachments.GENES)) {
			GeneCubes.shieldBreak(body, level, target, target.getItemBlockingWith(), false);
		}
	}

	/**
	 * After the charge hits, the melee hit genes of the mob act on the target.
	 */
	@Inject(method = "dealDamageToTarget", at = @At("TAIL"))
	private void originofspawns$hitGenes(ServerLevel level, Animal body, LivingEntity target, CallbackInfo info) {
		if (body.hasAttached(ModAttachments.GENES)) {
			GeneCombat.onHit(body, target);
		}
	}

	/**
	 * After the charge ends, the self effect after genes act, and each charge rate gene makes the time to the next
	 * charge shorter.
	 */
	@Inject(method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;J)V",
			at = @At("TAIL"))
	private void originofspawns$chargeRate(ServerLevel level, Animal body, long timestamp, CallbackInfo info) {
		if (!body.hasAttached(ModAttachments.GENES)) {
			return;
		}
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.selfEffectAfter(body, GeneEffect.Action.CHARGE);
		float rate = 1.0F;
		for (GeneEffect effect : GeneEffects.of(body).toList()) {
			if (effect instanceof GeneEffect.ChargeRate charge) {
				rate *= charge.multiplier();
			}
		}
		if (rate != 1.0F) {
			float divisor = rate;
			body.getBrain().getMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS).ifPresent(ticks ->
					body.getBrain().setMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS, Math.max(1, Math.round(ticks / divisor))));
		}
	}

	/**
	 * Each charge push gene multiplies the push of the charge on the target.
	 */
	@WrapOperation(method = "dealKnockBack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/animal/Animal;causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/damagesource/DamageSource;FZ)V"))
	private void originofspawns$chargePush(Animal body, Entity target, float knockback, Vec3 movement, DamageSource source,
			float damage, boolean fromEffect, Operation<Void> original) {
		float push = knockback;
		if (body.hasAttached(ModAttachments.GENES)) {
			for (GeneEffect effect : GeneEffects.of(body).toList()) {
				if (effect instanceof GeneEffect.ChargePush charge) {
					push *= charge.multiplier();
				}
			}
		}
		original.call(body, target, push, movement, source, damage, fromEffect);
	}
}
