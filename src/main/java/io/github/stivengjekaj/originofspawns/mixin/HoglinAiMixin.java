package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GenePiglins;

import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoglinAi.class)
public abstract class HoglinAiMixin {
	/**
	 * A hoglin with no fear of piglins does not run from a larger group of piglins.
	 */
	@ModifyReturnValue(method = "piglinsOutnumberHoglins", at = @At("RETURN"))
	private static boolean originofspawns$noFearOfPiglins(boolean outnumber, Hoglin body) {
		return outnumber && !GenePiglins.fearless(body, GeneEffect.NoFear.Fear.PIGLIN);
	}
}
