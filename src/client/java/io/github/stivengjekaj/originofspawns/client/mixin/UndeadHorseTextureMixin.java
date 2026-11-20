package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives a zombie horse or a skeleton horse the texture with the marks of its genes. The region maps have the size of
 * the adult textures, 64 x 64. A baby keeps its vanilla texture.
 */
@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseTextureMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(EquineRenderState state, CallbackInfoReturnable<Identifier> info) {
		if (!state.isBaby) {
			info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
					GeneTextures.Layout.ADULT));
		}
	}
}
