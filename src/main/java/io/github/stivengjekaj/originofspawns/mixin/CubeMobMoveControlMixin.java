package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.world.entity.monster.cubemob.AbstractCubeMob$CubeMobMoveControl")
public abstract class CubeMobMoveControlMixin {
	/**
	 * The genes change the speed of the cube in the air, so the hop goes farther. The speed in the air is the last
	 * speed that the move control sets.
	 */
	@ModifyArg(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/cubemob/AbstractCubeMob;setSpeed(F)V", ordinal = 2))
	private float originofspawns$hopDistance(float speed) {
		return speed * GeneCubes.hop(((MoveControlAccessor) (Object) this).originofspawns$mob())[1];
	}
}
