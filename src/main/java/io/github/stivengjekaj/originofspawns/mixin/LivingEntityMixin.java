package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float originofspawns$modifyDamageTaken(float amount, ServerLevel level, DamageSource source) {
		return GeneEffects.modifyDamageTaken((LivingEntity) (Object) this, source, amount);
	}

	@Inject(method = "baseTick", at = @At("TAIL"))
	private void originofspawns$tickGenes(CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.level() instanceof ServerLevel level && self.hasAttached(ModAttachments.GENES)) {
			GeneEffects.tick(self, level);
		}
	}
}
