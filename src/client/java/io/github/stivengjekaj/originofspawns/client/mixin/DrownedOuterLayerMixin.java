package io.github.stivengjekaj.originofspawns.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the gene marks on the outer layer of the drowned too. The outer layer covers almost all of the body, so the
 * marks on the body texture alone do not show.
 */
@Mixin(DrownedOuterLayer.class)
public abstract class DrownedOuterLayerMixin {
	@WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ZombieRenderState;FF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/DrownedOuterLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/Model;Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;II)V"))
	private void originofspawns$geneOuterLayer(Model model, Identifier texture, PoseStack pose, SubmitNodeCollector collector,
			int light, LivingEntityRenderState state, int color, int order, Operation<Void> original) {
		Identifier baked = texture;
		if (state instanceof ZombieRenderState zombie) {
			GeneTextures.Layout layout = zombie.isBaby ? GeneTextures.Layout.BABY : GeneTextures.Layout.SPLIT_LIMBS;
			baked = GeneTextures.textureFor(texture, zombie.getData(GeneTextures.GENES), layout);
		}
		original.call(model, baked, pose, collector, light, state, color, order);
	}
}
