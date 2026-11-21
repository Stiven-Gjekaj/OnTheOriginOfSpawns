package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a wolf in its render state, and gives it the texture with the marks of its genes. The wolf region
 * map has the size of the adult wolf texture, 64 x 32. The wild, the tame, and the angry texture of each variant have
 * the same layout, so the marks fit each of them. The collar and the armor draw over the marks. A baby wolf has its own
 * model and a 32 x 32 texture with another layout, so a baby gets no gene marks and no gene models.
 */
@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/wolf/Wolf;Lnet/minecraft/client/renderer/entity/state/WolfRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Wolf wolf, WolfRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = wolf.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null || state.isBaby ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(wolf).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/WolfRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(WolfRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
