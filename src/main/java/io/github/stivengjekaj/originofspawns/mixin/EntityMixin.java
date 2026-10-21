package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	/**
	 * The vanilla strike sets the entity on fire and hurts it. A mob with lightning immunity skips both. The creeper
	 * calls this before it becomes charged, so it still becomes charged.
	 */
	@Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
	private void originofspawns$lightningImmunity(ServerLevel level, LightningBolt bolt, CallbackInfo info) {
		if (GeneCombat.has((Entity) (Object) this, GeneEffect.LightningImmunity.class)) {
			info.cancel();
		}
	}

	/**
	 * A loud mob makes louder steps. The zombie and the skeleton have their own steps, so this is for other mobs, for
	 * example the creeper.
	 */
	@ModifyConstant(method = {"playStepSound", "playCombinationStepSounds"}, constant = @Constant(floatValue = 0.15F))
	private float originofspawns$stepVolume(float volume) {
		Entity self = (Entity) (Object) this;
		return self instanceof LivingEntity living && self.hasAttached(ModAttachments.GENES)
				? volume * GeneBehaviors.volumeMultiplier(living) : volume;
	}
}
