package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.Shoot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Changes the wind charge shot of a breeze with its genes. A vanilla breeze charges for 15 ticks before a shot, and
 * waits 10 ticks after it.
 */
@Mixin(Shoot.class)
public abstract class BreezeShootMixin {
	@ModifyExpressionValue(method = "start", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/monster/breeze/Shoot;SHOOT_INITIAL_DELAY_TICKS:I"))
	private int originofspawns$charge(int ticks, ServerLevel level, Breeze breeze, long timestamp) {
		return GeneBehaviors.scaleThrowInterval(breeze, ticks);
	}

	@ModifyExpressionValue(method = "stop", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/monster/breeze/Shoot;SHOOT_COOLDOWN_TICKS:I"))
	private int originofspawns$cooldown(int ticks, ServerLevel level, Breeze breeze, long timestamp) {
		return GeneBehaviors.scaleThrowInterval(breeze, ticks);
	}
}
