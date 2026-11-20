package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.state.NautilusRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a zombie nautilus in its render state, and gives it the texture with the marks of its genes. The
 * temperate and the warm texture have one layout of 128 x 128, so one region map serves both, and each base texture
 * gets its own baked texture. A zombie nautilus has no babies, so the marks use the adult layout.
 */
@Mixin(ZombieNautilusRenderer.class)
public abstract class ZombieNautilusRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/nautilus/ZombieNautilus;Lnet/minecraft/client/renderer/entity/state/NautilusRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(ZombieNautilus nautilus, NautilusRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = nautilus.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(nautilus).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/NautilusRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(NautilusRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
