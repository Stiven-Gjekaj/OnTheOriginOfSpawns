package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a piglin or a piglin brute in its render state, and gives it the texture with the marks of its genes.
 * The piglin region map has the size of the adult texture, 64 x 64 with the outer layer, so the marks use the adult
 * layout. A baby has its own texture, so it gets no marks.
 */
@Mixin(PiglinRenderer.class)
public abstract class PiglinRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/piglin/AbstractPiglin;Lnet/minecraft/client/renderer/entity/state/PiglinRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(AbstractPiglin mob, PiglinRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null || mob.isBaby() ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(mob).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/PiglinRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(PiglinRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
