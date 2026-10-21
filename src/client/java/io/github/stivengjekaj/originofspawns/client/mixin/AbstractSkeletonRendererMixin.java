package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Puts the genes of a skeleton, a stray, or another skeleton species in its render state.
 */
@Mixin(AbstractSkeletonRenderer.class)
public abstract class AbstractSkeletonRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(AbstractSkeleton skeleton, SkeletonRenderState state, float partialTick,
			CallbackInfo info) {
		CarriedGenes genes = skeleton.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(skeleton).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}
}
