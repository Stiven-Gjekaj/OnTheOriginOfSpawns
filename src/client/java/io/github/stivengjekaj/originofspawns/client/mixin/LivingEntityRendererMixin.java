package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws a mob with a translucent gene in a translucent render type, with the opacity of the gene.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@ModifyVariable(method = "getRenderType", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private boolean originofspawns$translucent(boolean translucent, LivingEntityRenderState state) {
		return translucent || state.getData(GeneTextures.OPACITY) != null;
	}

	@Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
	private void originofspawns$opacity(LivingEntityRenderState state, CallbackInfoReturnable<Integer> info) {
		Float opacity = state.getData(GeneTextures.OPACITY);
		if (opacity != null) {
			int tint = info.getReturnValueI();
			info.setReturnValue(ARGB.color(Math.round(opacity * 255.0F), ARGB.red(tint), ARGB.green(tint), ARGB.blue(tint)));
		}
	}
}
