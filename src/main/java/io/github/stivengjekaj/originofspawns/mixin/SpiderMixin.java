package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.Evolution;

import net.minecraft.world.entity.monster.spider.Spider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Spider.class)
public abstract class SpiderMixin {
	/**
	 * A species in the tag {@code originofspawns:no_vanilla_jockeys} does not spawn with a skeleton on its back.
	 * Skeleton jockeys come only from the Spider Rider gene. The roll of 1 in 100 never gives 0 here.
	 */
	@ModifyExpressionValue(method = "finalizeSpawn", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
	private int originofspawns$noVanillaJockey(int roll) {
		if (((Spider) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_VANILLA_JOCKEYS)) {
			return 1;
		}
		return roll;
	}
}
