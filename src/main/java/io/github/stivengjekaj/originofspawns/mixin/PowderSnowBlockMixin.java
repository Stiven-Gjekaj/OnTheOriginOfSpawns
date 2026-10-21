package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a mob with a gene that walks on powder snow stand on it, as with leather boots.
 */
@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {
	@Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true)
	private static void originofspawns$walkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && entity instanceof LivingEntity living
				&& GeneCombat.has(living, GeneEffect.WalkOnPowderSnow.class)) {
			info.setReturnValue(true);
		}
	}
}
