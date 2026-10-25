package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.breeze.Breeze;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the genes of a breeze in its render state, and gives it the texture with the marks of its genes. The breeze
 * region map has the size of the breeze texture, 32 x 32, so the marks use the adult layout. The wind has its own
 * texture, and the wind layer puts the layers with the wind target on it.
 */
@Mixin(BreezeRenderer.class)
public abstract class BreezeRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/breeze/Breeze;Lnet/minecraft/client/renderer/entity/state/BreezeRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(Breeze breeze, BreezeRenderState state, float partialTick, CallbackInfo info) {
		CarriedGenes genes = breeze.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(breeze).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/BreezeRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("RETURN"), cancellable = true)
	private void originofspawns$geneTexture(BreezeRenderState state, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(GeneTextures.textureFor(info.getReturnValue(), state.getData(GeneTextures.GENES),
				GeneTextures.Layout.ADULT));
	}
}
