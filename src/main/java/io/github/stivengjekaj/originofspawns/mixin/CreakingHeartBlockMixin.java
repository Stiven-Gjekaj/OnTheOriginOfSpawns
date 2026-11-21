package io.github.stivengjekaj.originofspawns.mixin;

import java.util.function.BiConsumer;

import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the genes of the creaking that a heart protects keep the heart from a blast.
 */
@Mixin(CreakingHeartBlock.class)
public abstract class CreakingHeartBlockMixin {
	@Inject(method = "onExplosionHit", at = @At("HEAD"), cancellable = true)
	private void originofspawns$blastProof(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
			BiConsumer<ItemStack, BlockPos> onHit, CallbackInfo info) {
		if (GeneCreakings.protector(level, pos).filter(GeneCreakings::heartBlastProof).isPresent()) {
			info.cancel();
		}
	}
}
