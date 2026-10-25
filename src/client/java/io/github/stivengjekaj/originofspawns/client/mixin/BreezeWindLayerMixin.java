package io.github.stivengjekaj.originofspawns.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.visual.VisualLayer;

import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Draws the wind of a breeze with the wind texture with the gene layers that have the wind target. These layers
 * cover all of the wind texture.
 */
@Mixin(BreezeWindLayer.class)
public abstract class BreezeWindLayerMixin {
	@ModifyArg(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/BreezeRenderState;FF)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;breezeWind(Lnet/minecraft/resources/Identifier;FF)Lnet/minecraft/client/renderer/rendertype/RenderType;"),
			index = 0)
	private Identifier originofspawns$geneWind(Identifier texture, @Local(argsOnly = true) BreezeRenderState state) {
		return GeneTextures.textureFor(texture, state.getData(GeneTextures.GENES), GeneTextures.Layout.ADULT,
				VisualLayer.Target.WIND);
	}
}
