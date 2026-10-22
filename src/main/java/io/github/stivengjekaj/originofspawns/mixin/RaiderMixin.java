package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.raid.Raider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raider.class)
public abstract class RaiderMixin {
	/**
	 * The raid lets a raider go before the death event, so the death records here that it was in a raid.
	 */
	@Inject(method = "die", at = @At("HEAD"))
	private void originofspawns$recordRaidDeath(DamageSource source, CallbackInfo info) {
		Raider self = (Raider) (Object) this;
		if (self.getCurrentRaid() != null) {
			self.setAttached(ModAttachments.RAID_DEATH, true);
		}
	}
}
