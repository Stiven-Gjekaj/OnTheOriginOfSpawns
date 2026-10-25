package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a ghast in its render state, and gives it the texture with the marks of its genes. The ghast has
 * two textures, one with a closed face and one with the open face of a shot. Both have the size of the ghast region
 * map, 128 x 64, and one region map serves both, so the marks go on the texture that the renderer picks.
 */
@Mixin(GhastRenderer.class)
public abstract class GhastRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Ghast;Lnet/minecraft/client/renderer/entity/state/GhastRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Ghast ghast, GhastRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = ghast.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(ghast).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/GhastRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(GhastRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
