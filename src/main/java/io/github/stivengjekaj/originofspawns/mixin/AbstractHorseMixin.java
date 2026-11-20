package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
	/**
	 * The server learns that the player that rides the horse starts a jump. The client moves the horse.
	 */
	@Inject(method = "handleStartJump", at = @At("TAIL"))
	private void originofspawns$playerJump(CallbackInfo info) {
		AbstractHorse self = (AbstractHorse) (Object) this;
		if (self.level() instanceof ServerLevel level && self.hasAttached(ModAttachments.GENES)) {
			GeneBehaviors.onJump(self, level);
		}
	}
}
