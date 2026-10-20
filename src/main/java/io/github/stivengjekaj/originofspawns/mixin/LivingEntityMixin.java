package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float originofspawns$modifyDamageTaken(float amount, ServerLevel level, DamageSource source) {
		return GeneCombat.modifyDamage((LivingEntity) (Object) this, source, amount);
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void originofspawns$dodge(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES) && GeneCombat.dodges(self, source)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
	private void originofspawns$undying(DamageSource source, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES) && GeneCombat.undying(self)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
	private void originofspawns$effectImmunity(MobEffectInstance instance, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES) && GeneCombat.immune(self, instance)) {
			info.setReturnValue(false);
		}
	}

	@ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
	private float originofspawns$healMultiplier(float amount) {
		LivingEntity self = (LivingEntity) (Object) this;
		return self.hasAttached(ModAttachments.GENES) ? GeneCombat.healAmount(self, amount) : amount;
	}

	@Inject(method = "heal", at = @At("TAIL"))
	private void originofspawns$shareHealing(float amount, CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES)) {
			GeneCombat.shareHealing(self, amount);
		}
	}

	@Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
	private void originofspawns$leechKeepsItsXp(ServerLevel level, Entity killer, CallbackInfoReturnable<Integer> info) {
		// A leech drops only the XP that it took, when it dies.
		if (GeneCombat.has((LivingEntity) (Object) this, GeneEffect.LeechXp.class)) {
			info.setReturnValue(0);
		}
	}

	@Inject(method = "getSoundVolume", at = @At("RETURN"), cancellable = true)
	private void originofspawns$loud(CallbackInfoReturnable<Float> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES)) {
			info.setReturnValue(info.getReturnValueF() * GeneBehaviors.volumeMultiplier(self));
		}
	}

	@Inject(method = "baseTick", at = @At("TAIL"))
	private void originofspawns$tickGenes(CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.level() instanceof ServerLevel level && self.hasAttached(ModAttachments.GENES)) {
			GeneEffects.tick(self, level);
			if (self instanceof Mob mob) {
				GeneBehaviors.tick(mob, level);
				if (mob.tickCount % 20 == 0) {
					Evolution.trackSpotter(mob, level);
				}
			}
		}
	}
}
