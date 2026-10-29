package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the laser of a guardian with its genes. A vanilla guardian locks on its target, charges for 80 ticks, and
 * then hits the target with magic damage and with its attack.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Guardian$GuardianAttackGoal")
public abstract class GuardianAttackGoalMixin {
	@Shadow
	@Final
	private Guardian guardian;

	@Shadow
	private int attackTime;

	/**
	 * At the lock, a guardian whose next laser is ready skips the charge: the laser fires on the next tick. The swoop
	 * call genes call the other guardians to the same target.
	 */
	@Inject(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Guardian;setActiveAttackTarget(I)V", shift = At.Shift.AFTER))
	private void originofspawns$lock(CallbackInfo info) {
		GeneBehaviors.swoopCall(guardian);
		if (guardian.getAttachedOrElse(ModAttachments.LASER_READY, false)) {
			guardian.removeAttached(ModAttachments.LASER_READY);
			attackTime = guardian.getAttackDuration() - 1;
		}
	}

	/**
	 * While the laser charges, the charge damage genes hurt the target.
	 */
	@Inject(method = "tick", at = @At("TAIL"))
	private void originofspawns$chargeDamage(CallbackInfo info) {
		LivingEntity target = guardian.getTarget();
		if (target != null && attackTime > 0 && attackTime < guardian.getAttackDuration()
				&& guardian.level() instanceof ServerLevel level) {
			GeneBehaviors.chargeDamage(guardian, level, target, attackTime);
		}
	}

	/**
	 * A guardian with a laser lock keeps its target for a short time out of sight.
	 */
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Guardian;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean originofspawns$laserLock(boolean sees) {
		return GeneBehaviors.laserSight(guardian, sees);
	}

	/**
	 * The magic damage of the laser is a laser hit.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private boolean originofspawns$laserMagic(LivingEntity target, ServerLevel level, DamageSource source, float damage,
			Operation<Boolean> original) {
		return GeneCombat.asLaser(() -> original.call(target, level, source, damage));
	}

	/**
	 * The attack of the guardian at the end of the charge is a laser hit too.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Guardian;doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean originofspawns$laserAttack(Guardian guardian, ServerLevel level, Entity target, Operation<Boolean> original) {
		boolean hit = GeneCombat.asLaser(() -> original.call(guardian, level, target));
		if (target instanceof LivingEntity living) {
			GeneCombat.splitLaser(guardian, level, living, level.getDifficulty() == net.minecraft.world.Difficulty.HARD ? 3.0F : 1.0F);
		}
		return hit;
	}
}
