package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.camel.Camel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camel.class)
public abstract class CamelMixin {
	/**
	 * After the push of a dash, the dash genes change the push. The side that moves the camel runs this: the client
	 * for a player that rides it, and the server for a mob that rides it.
	 */
	@Inject(method = "executeRidersJump", at = @At("TAIL"))
	private void originofspawns$dashMotion(CallbackInfo info) {
		Camel self = (Camel) (Object) this;
		if (self.hasAttached(ModAttachments.GENES)) {
			GeneBehaviors.dashMotion(self);
		}
	}

	/**
	 * The server learns that the player that rides the camel starts a dash. The dash genes act on it.
	 */
	@Inject(method = "handleStartJump", at = @At("TAIL"))
	private void originofspawns$playerDash(CallbackInfo info) {
		Camel self = (Camel) (Object) this;
		if (self.level() instanceof ServerLevel level && self.hasAttached(ModAttachments.GENES)) {
			GeneBehaviors.onDash(self, level);
		}
	}
}
