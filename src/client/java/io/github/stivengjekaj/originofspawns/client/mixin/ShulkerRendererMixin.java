package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Shulker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a shulker in its render state, and gives it the texture with the marks of its genes, for each of
 * its 17 colors. Each color has its own texture, and each texture has the size of the shulker region map, 64 x 64. A
 * shulker has no babies, so the marks use the adult layout.
 */
@Mixin(ShulkerRenderer.class)
public abstract class ShulkerRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Shulker;Lnet/minecraft/client/renderer/entity/state/ShulkerRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Shulker shulker, ShulkerRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = shulker.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(shulker).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/ShulkerRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(ShulkerRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
