package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	/**
	 * The spider has its own steps, so the silence and the volume of the genes act here.
	 */
	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	private void originofspawns$silentSteps(BlockPos pos, BlockState state, CallbackInfo info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Spider) (Object) this);
		if (silence != null && silence.steps()) {
			info.cancel();
		}
	}

	@ModifyConstant(method = "playStepSound", constant = @Constant(floatValue = 0.15F))
	private float originofspawns$stepVolume(float volume) {
		return volume * GeneBehaviors.volumeMultiplier((Spider) (Object) this);
	}
}
