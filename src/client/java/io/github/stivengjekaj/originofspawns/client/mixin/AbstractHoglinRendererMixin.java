package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Puts the genes of a hoglin or a zoglin in its render state. A baby has its own texture, so it gets no marks.
 */
@Mixin(AbstractHoglinRenderer.class)
public abstract class AbstractHoglinRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/client/renderer/entity/state/HoglinRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Mob mob, HoglinRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null || mob.isBaby() ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(mob).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}
}
