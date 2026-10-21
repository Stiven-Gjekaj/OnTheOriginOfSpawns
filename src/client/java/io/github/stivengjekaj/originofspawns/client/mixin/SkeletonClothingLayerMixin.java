package io.github.stivengjekaj.originofspawns.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.visual.VisualLayer;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the gene layers with the outer target on the cloth of the stray, and on the other skeleton species with a
 * cloth layer.
 */
@Mixin(SkeletonClothingLayer.class)
public abstract class SkeletonClothingLayerMixin {
	@WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/SkeletonRenderState;FF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/SkeletonClothingLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/Model;Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;II)V"))
	private void originofspawns$geneCloth(Model model, Identifier texture, PoseStack pose, SubmitNodeCollector collector,
			int light, LivingEntityRenderState state, int color, int order, Operation<Void> original) {
		Identifier baked = GeneTextures.textureFor(texture, state.getData(GeneTextures.GENES), GeneTextures.Layout.ADULT,
				VisualLayer.Target.OUTER);
		original.call(model, baked, pose, collector, light, state, color, order);
	}
}
