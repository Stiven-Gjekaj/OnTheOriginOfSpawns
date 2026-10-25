package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Blaze;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blaze.class)
public abstract class BlazeMixin {
	/**
	 * A blaze rises while the eyes of its target are higher than its own eyes and a random offset. The float height
	 * genes add their blocks to the height of the eyes of the target, so the blaze stays this much higher.
	 */
	/**
	 * After the vanilla rise, the keep above genes make the blaze rise over the mobs of their lists.
	 */
	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void originofspawns$keepAbove(ServerLevel level, CallbackInfo info) {
		GeneBehaviors.keepAbove((Blaze) (Object) this, level);
	}

	@ModifyExpressionValue(method = "customServerAiStep", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D"))
	private double originofspawns$floatHeight(double eyeY) {
		return eyeY + GeneBehaviors.floatBlocks((Blaze) (Object) this);
	}
}
