package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives the vindicator the texture with the marks of its genes. The illager region map has the size of the vindicator
 * texture, so the marks use the adult layout.
 */
@Mixin(VindicatorRenderer.class)
public abstract class VindicatorRendererMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(IllagerRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
