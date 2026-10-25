package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Puts the genes of a slime or a magma cube in its render state.
 */
@Mixin(AbstractCubeMobRenderer.class)
public abstract class AbstractCubeMobRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/cubemob/AbstractCubeMob;Lnet/minecraft/client/renderer/entity/state/SlimeRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractGenes(AbstractCubeMob cube, SlimeRenderState state, float partialTick,
			CallbackInfo info) {
		CarriedGenes genes = cube.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(cube).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}
}
