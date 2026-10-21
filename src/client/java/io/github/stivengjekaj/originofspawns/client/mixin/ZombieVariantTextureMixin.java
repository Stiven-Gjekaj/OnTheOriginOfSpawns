package io.github.stivengjekaj.originofspawns.client.mixin;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives the husk and the drowned the texture with the marks of their genes. Their renderers choose their own texture,
 * so the hook on the renderer of all zombies does not reach them.
 */
@Mixin({HuskRenderer.class, DrownedRenderer.class})
public abstract class ZombieVariantTextureMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(ZombieRenderState state, CallbackInfoReturnable<Identifier> info) {
		GeneTextures.Layout layout = state.isBaby ? GeneTextures.Layout.BABY
				: Boolean.TRUE.equals(state.getData(GeneTextures.SPLIT_LIMBS)) ? GeneTextures.Layout.SPLIT_LIMBS
				: GeneTextures.Layout.ADULT;
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES), layout));
	}
}
