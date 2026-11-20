package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Puts the genes of a zombie horse or a skeleton horse in its render state. The undead horse renderer has the render
 * state of all horses, so this runs only for it. A baby has a different texture layout, and it comes only from a spawn
 * egg, so a baby gets no gene marks and no gene models.
 */
@Mixin(AbstractHorseRenderer.class)
public abstract class UndeadHorseRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/AbstractHorse;Lnet/minecraft/client/renderer/entity/state/EquineRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(AbstractHorse horse, EquineRenderState state, float partialTick, CallbackInfo info) {
		if (!((Object) this instanceof UndeadHorseRenderer)) {
			return;
		}
		CarriedGenes genes = horse.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null || state.isBaby ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(horse).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}
}
