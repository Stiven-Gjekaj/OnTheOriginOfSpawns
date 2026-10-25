package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
	/**
	 * The fire of a gene does not spread, does not burn blocks, and does not burn out. It goes away at its time.
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void originofspawns$holdGeneFire(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
			CallbackInfo info) {
		if (TemporaryBlocks.isTemporary(level, pos)) {
			info.cancel();
		}
	}
}
