package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.sounds.SoundEvent;

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
		if (self.hasAttached(ModAttachments.GENES)
				&& (GeneCombat.dodges(self, source) || GeneCombat.protectedWhileDown(self, source)
						|| GeneCombat.parries(self, source) || GeneTeleports.fromBlast(self, source)
						|| GeneCombat.blastHeals(self, source, amount) || GeneCombat.sunShaded(self, source))) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
	private void originofspawns$undying(DamageSource source, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasAttached(ModAttachments.GENES) && (GeneCombat.undying(self) || GeneCombat.reassemble(self, source))) {
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

	@Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
	private void originofspawns$senses(ServerLevel level, Entity looker, CallbackInfoReturnable<Double> info) {
		if (looker != null && looker.hasAttached(ModAttachments.GENES)) {
			info.setReturnValue(GeneBehaviors.visibility((LivingEntity) (Object) this, looker, info.getReturnValueD()));
		}
	}

	@Inject(method = "applyItemBlocking", at = @At("HEAD"), cancellable = true)
	private void originofspawns$pierceShields(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Float> info) {
		// No block also means no damage to the shield.
		if (source.getDirectEntity() instanceof Projectile && source.getEntity() instanceof LivingEntity shooter
				&& GeneCombat.has(shooter, GeneEffect.PierceShields.class) || GeneBlasts.piercesShields(source)) {
			info.setReturnValue(0.0F);
		}
	}

	@Inject(method = "applyItemBlocking", at = @At("RETURN"))
	private void originofspawns$recordShieldBlock(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Float> info) {
		if (info.getReturnValueF() > 0.0F && (Object) this instanceof Player) {
			((LivingEntity) (Object) this).setAttached(ModAttachments.SHIELD_BLOCK, level.getGameTime());
		}
	}

	@ModifyExpressionValue(method = "hurtServer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getDeathSound()Lnet/minecraft/sounds/SoundEvent;"))
	private SoundEvent originofspawns$silentDeath(SoundEvent sound) {
		GeneEffect.Silence silence = GeneBehaviors.silence((LivingEntity) (Object) this);
		return silence != null && silence.death() ? null : sound;
	}

	@ModifyExpressionValue(method = "playHurtSound", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;"))
	private SoundEvent originofspawns$silentHurt(SoundEvent sound) {
		GeneEffect.Silence silence = GeneBehaviors.silence((LivingEntity) (Object) this);
		return silence != null && silence.hurt() ? null : sound;
	}

	@Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
	private void originofspawns$climb(CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		// A climber climbs while it walks into a wall, like a spider.
		if (!info.getReturnValueZ() && self.horizontalCollision && self.hasAttached(ModAttachments.GENES)
				&& GeneCombat.has(self, GeneEffect.Climb.class) && !GeneCombat.touchesGlass(self)) {
			info.setReturnValue(true);
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
				if (mob.tickCount % 10 == 0) {
					Evolution.trackLooks(mob, level);
				}
			}
		}
	}

	@Inject(method = "isSensitiveToWater", at = @At("RETURN"), cancellable = true)
	private void originofspawns$waterSensitive(CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && GeneCombat.has((LivingEntity) (Object) this, GeneEffect.WaterSensitive.class)) {
			info.setReturnValue(true);
		}
	}

	/**
	 * A projectile of a mob with genes that a shield stops can stop the shield. Vanilla stops a shield only at the
	 * hit of a mob that holds an axe.
	 */
	@Inject(method = "applyItemBlocking", at = @At("RETURN"))
	private void originofspawns$projectileShieldBreak(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Float> info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (info.getReturnValueF() > 0.0F && source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile
				&& source.getEntity() instanceof net.minecraft.world.entity.Mob mob && mob != self
				&& mob.hasAttached(ModAttachments.GENES)) {
			io.github.stivengjekaj.originofspawns.evolution.GeneCubes.shieldBreak(mob, level, self, self.getItemBlockingWith(), true);
		}
	}
}
