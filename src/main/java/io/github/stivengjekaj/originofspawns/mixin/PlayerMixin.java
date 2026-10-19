package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.Evolution;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
	/**
	 * The game decides a critical hit before the damage. This marks the target, so that its death can know it.
	 */
	@Inject(method = "canCriticalAttack", at = @At("RETURN"))
	private void originofspawns$recordCritical(Entity target, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ()) {
			Evolution.markCritical(target);
		}
	}
}
