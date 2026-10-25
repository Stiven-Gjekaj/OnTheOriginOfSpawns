package io.github.stivengjekaj.originofspawns.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws the outer gel of a slime with the texture with the marks of its genes. The vanilla layer uses the plain slime
 * texture, so a mark on the gel does not show without this.
 */
@Mixin(SlimeOuterLayer.class)
public abstract class SlimeOuterLayerMixin {
	@ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/SlimeRenderState;FF)V",
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/SlimeRenderer;SLIME_LOCATION:Lnet/minecraft/resources/Identifier;"))
	private Identifier originofspawns$geneGel(Identifier texture, PoseStack pose, SubmitNodeCollector collector, int light,
			SlimeRenderState state, float yRot, float xRot) {
		return GeneTextures.textureFor(texture, state.getData(GeneTextures.GENES), GeneTextures.Layout.ADULT);
	}
}
