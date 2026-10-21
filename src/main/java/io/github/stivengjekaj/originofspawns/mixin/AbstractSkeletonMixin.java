package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {
	/**
	 * Multiplies the spread of the arrow by the arrow spread effects of the genes.
	 */
	@ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"),
			index = 7)
	private float originofspawns$arrowSpread(float spread) {
		return spread * GeneBehaviors.arrowSpreadMultiplier((AbstractSkeleton) (Object) this);
	}

	@Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noShotWhileDown(LivingEntity target, float power, CallbackInfo info) {
		if (GeneCombat.collapsed((AbstractSkeleton) (Object) this)) {
			info.cancel();
		}
	}

	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	private void originofspawns$silentSteps(BlockPos pos, BlockState state, CallbackInfo info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((AbstractSkeleton) (Object) this);
		if (silence != null && silence.steps()) {
			info.cancel();
		}
	}

	@ModifyConstant(method = "playStepSound", constant = @Constant(floatValue = 0.15F))
	private float originofspawns$stepVolume(float volume) {
		return volume * GeneBehaviors.volumeMultiplier((AbstractSkeleton) (Object) this);
	}

	@ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),
			index = 1)
	private float originofspawns$shotVolume(float volume) {
		return volume * GeneBehaviors.volumeMultiplier((AbstractSkeleton) (Object) this);
	}

	@Inject(method = "performRangedAttack", at = @At("TAIL"))
	private void originofspawns$fanShot(LivingEntity target, float power, CallbackInfo info) {
		GeneBehaviors.fanShot((AbstractSkeleton) (Object) this, target);
	}

	@Inject(method = "getArrow", at = @At("RETURN"))
	private void originofspawns$arrowEffects(ItemStack arrow, float power, ItemStack bow,
			CallbackInfoReturnable<AbstractArrow> info) {
		GeneEffects.of((AbstractSkeleton) (Object) this).forEach(effect -> {
			if (effect instanceof GeneEffect.GhostArrows ghost) {
				info.getReturnValue().setAttached(ModAttachments.GHOST_ARROW, ghost.blocks());
			}
		});
		if (info.getReturnValue() instanceof Arrow tipped) {
			GeneEffects.of((AbstractSkeleton) (Object) this).forEach(effect -> {
				if (effect instanceof GeneEffect.ArrowEffect arrowEffect) {
					tipped.addEffect(new MobEffectInstance(arrowEffect.effect(), Math.round(arrowEffect.seconds() * 20.0F),
							arrowEffect.amplifier()));
				}
			});
		}
	}
}
