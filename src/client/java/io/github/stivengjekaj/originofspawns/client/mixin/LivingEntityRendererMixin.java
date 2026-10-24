package io.github.stivengjekaj.originofspawns.client.mixin;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws a mob with a translucent gene in a translucent render type, with the opacity of the gene. It also puts the
 * genes of an endermite in its render state.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@ModifyVariable(method = "getRenderType", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private boolean originofspawns$translucent(boolean translucent, LivingEntityRenderState state) {
		return translucent || state.getData(GeneTextures.OPACITY) != null;
	}

	@Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
	private void originofspawns$opacity(LivingEntityRenderState state, CallbackInfoReturnable<Integer> info) {
		Float opacity = state.getData(GeneTextures.OPACITY);
		if (opacity != null) {
			int tint = info.getReturnValueI();
			info.setReturnValue(ARGB.color(Math.round(opacity * 255.0F), ARGB.red(tint), ARGB.green(tint), ARGB.blue(tint)));
		}
	}

	/**
	 * Puts the genes of an endermite in its render state. The endermite renderer has no render state method of its
	 * own, so the genes come from here.
	 */
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
			at = @At("TAIL"))
	private void originofspawns$extractEndermiteGenes(LivingEntity entity, LivingEntityRenderState state, float partialTick,
			CallbackInfo info) {
		if (!(entity instanceof Endermite endermite)) {
			return;
		}
		CarriedGenes genes = endermite.getAttached(ModAttachments.GENES);
		state.setData(GeneTextures.GENES, genes == null ? List.of() : genes.genes());
		state.setData(GeneTextures.OPACITY, null);
		if (genes != null) {
			GeneEffects.translucentOpacity(endermite).ifPresent(opacity -> state.setData(GeneTextures.OPACITY, opacity));
		}
	}
}
