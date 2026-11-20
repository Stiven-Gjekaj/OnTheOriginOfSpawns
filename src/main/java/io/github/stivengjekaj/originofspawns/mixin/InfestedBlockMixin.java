package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.evolution.GeneSilverfish;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.InfestedBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The silverfish that comes out of an infested block gets the genes of the silverfish that hid there.
 */
@Mixin(InfestedBlock.class)
public abstract class InfestedBlockMixin {
	@Inject(method = "spawnInfestation", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private void originofspawns$emerge(ServerLevel level, BlockPos pos, CallbackInfo info, @Local Silverfish silverfish) {
		GeneSilverfish.emerge(level, pos, silverfish);
	}
}
