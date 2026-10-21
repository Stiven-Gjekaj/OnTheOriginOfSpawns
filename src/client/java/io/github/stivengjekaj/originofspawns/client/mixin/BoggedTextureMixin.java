package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives the bogged the texture with the marks of its genes. The bogged has the skeleton body.
 */
@Mixin(BoggedRenderer.class)
public abstract class BoggedTextureMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/BoggedRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(BoggedRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
