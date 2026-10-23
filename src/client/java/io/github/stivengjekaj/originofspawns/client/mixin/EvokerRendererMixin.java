package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives the evoker the texture with the marks of its genes. The illager region map has the size of the evoker
 * texture, so the marks use the adult layout.
 */
@Mixin(EvokerRenderer.class)
public abstract class EvokerRendererMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/EvokerRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(EvokerRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
