package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
	/**
	 * The lava of a gene starts no fires near it.
	 */
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noGeneLavaFire(ServerLevel level, BlockPos pos, FluidState fluidState, RandomSource random,
			CallbackInfo info) {
		if (TemporaryBlocks.isTemporary(level, pos)) {
			info.cancel();
		}
	}
}
