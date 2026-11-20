package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.evolution.GeneSilverfish;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A silverfish that hides in a stone block in the vanilla way keeps its genes in the block.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishMergeWithStoneGoal")
public abstract class SilverfishMergeWithStoneGoalMixin {
	@Inject(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;discard()V"))
	private void originofspawns$keepGenes(CallbackInfo info, @Local BlockPos pos) {
		PathfinderMob mob = ((RandomStrollGoalAccessor) this).originofspawns$mob();
		if (mob.level() instanceof ServerLevel level) {
			GeneSilverfish.hide(mob, level, pos);
		}
	}
}
