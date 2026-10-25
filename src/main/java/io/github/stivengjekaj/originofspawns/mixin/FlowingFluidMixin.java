package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
	/**
	 * The fluid of a gene does not flow. It goes away at its time.
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void originofspawns$holdGeneFluid(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState,
			CallbackInfo info) {
		if (TemporaryBlocks.isTemporary(level, pos)) {
			info.cancel();
		}
	}
}
