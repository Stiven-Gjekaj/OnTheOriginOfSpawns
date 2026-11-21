package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the hard heart genes of a creaking make its heart slower to break.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void originofspawns$hardHeart(BlockState state, Player player, BlockGetter level, BlockPos pos,
			CallbackInfoReturnable<Float> info) {
		if (state.is(Blocks.CREAKING_HEART)) {
			GeneCreakings.protector(level, pos).ifPresent(creaking ->
					info.setReturnValue(info.getReturnValueF() / GeneCreakings.heartBreakTime(creaking)));
		}
	}
}
