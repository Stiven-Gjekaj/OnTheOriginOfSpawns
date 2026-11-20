package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneShulkers;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.monster.Shulker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the genes of a shulker change its shell and its teleports.
 */
@Mixin(Shulker.class)
public abstract class ShulkerMixin {
	@Inject(method = "teleportSomewhere", at = @At("RETURN"))
	private void originofspawns$afterTeleport(CallbackInfoReturnable<Boolean> info) {
		Shulker self = (Shulker) (Object) this;
		if (info.getReturnValueZ() && !self.level().isClientSide() && self.hasAttached(ModAttachments.GENES)) {
			GeneShulkers.afterTeleport(self);
		}
	}

	/**
	 * A shulker that stays closed after a teleport does not open.
	 */
	@Inject(method = "setRawPeekAmount", at = @At("HEAD"), cancellable = true)
	private void originofspawns$staysShut(int amount, CallbackInfo info) {
		Shulker self = (Shulker) (Object) this;
		if (amount > 0 && !self.level().isClientSide() && GeneShulkers.shut(self)) {
			info.cancel();
		}
	}
}
