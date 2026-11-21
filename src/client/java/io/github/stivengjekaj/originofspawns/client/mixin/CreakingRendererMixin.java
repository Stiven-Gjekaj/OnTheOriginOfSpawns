package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.CreakingRenderer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.creaking.Creaking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a creaking in its render state, and gives it the texture with the marks of its genes. The creaking
 * region map has the size of the creaking texture, 64 x 64, and a creaking has no babies, so the marks use the adult
 * layout. The glow layer of the eyes keeps its own texture.
 */
@Mixin(CreakingRenderer.class)
public abstract class CreakingRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/creaking/Creaking;Lnet/minecraft/client/renderer/entity/state/CreakingRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Creaking creaking, CreakingRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = creaking.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(creaking).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/CreakingRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(CreakingRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
