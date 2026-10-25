package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.LongJump;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The hop genes change the long jump of a breeze, as they change the hop of a cube.
 */
@Mixin(LongJump.class)
public abstract class LongJumpMixin {
	/**
	 * The distance multiplier changes the speed along the ground, and the height multiplier changes the speed up.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/breeze/Breeze;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	private void originofspawns$jump(Breeze breeze, Vec3 velocity, Operation<Void> original) {
		float[] hop = GeneCubes.hop(breeze);
		original.call(breeze, new Vec3(velocity.x * hop[1], velocity.y * hop[2], velocity.z * hop[1]));
	}

	/**
	 * When the breeze lands from a long jump, its landing genes act. The game stops to discard the friction of the
	 * breeze only at the landing.
	 */
	@Inject(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/breeze/Breeze;setDiscardFriction(Z)V", ordinal = 1))
	private void originofspawns$land(ServerLevel level, Breeze breeze, long timestamp, CallbackInfo info) {
		GeneCubes.onLand(breeze, level);
	}

	/**
	 * The wait multiplier changes the rest after a jump: 10 ticks, or 2 ticks after the breeze is hurt.
	 */
	@ModifyConstant(method = "tick", constant = {@Constant(longValue = 10L), @Constant(longValue = 2L)})
	private long originofspawns$rest(long ticks, ServerLevel level, Breeze breeze, long timestamp) {
		return Math.max(1L, Math.round(ticks * GeneCubes.hop(breeze)[0]));
	}
}
