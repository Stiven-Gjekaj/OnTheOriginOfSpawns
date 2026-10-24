package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
public abstract class EndermanMixin {
	/**
	 * A gene can aim the random teleport of the enderman at its target. With no free place there, the vanilla random
	 * teleport runs.
	 */
	@Inject(method = "teleport()Z", at = @At("HEAD"), cancellable = true)
	private void originofspawns$aimedTeleport(CallbackInfoReturnable<Boolean> info) {
		Enderman self = (Enderman) (Object) this;
		LivingEntity target = GeneTeleports.aimedTarget(self);
		if (target != null && self.level() instanceof ServerLevel level && GeneTeleports.near(self, level, target)) {
			info.setReturnValue(true);
		}
	}
}
