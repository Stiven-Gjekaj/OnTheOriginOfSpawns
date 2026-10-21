package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Does not draw a mob or an arrow that a gene hides from far away, when the camera is farther than the distance.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
	private <E extends Entity> void originofspawns$hiddenFromAfar(E entity, Frustum frustum, double cameraX, double cameraY,
			double cameraZ, float partialTick, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ()) {
			GeneEffects.hiddenBeyond(entity).ifPresent(blocks -> {
				if (entity.distanceToSqr(cameraX, cameraY, cameraZ) > blocks * blocks) {
					info.setReturnValue(false);
				}
			});
		}
	}
}
