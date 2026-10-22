package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.Captains;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.raid.Raider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raider.class)
public abstract class RaiderMixin {
	/**
	 * Only a pillager with a Captain gene can pick up the ominous banner and lead its raid wave.
	 */
	@Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
	private void originofspawns$onlyCaptainsTakeTheBanner(ServerLevel level, ItemEntity entity, CallbackInfo info) {
		Raider self = (Raider) (Object) this;
		if (Captains.isBanner(level, entity.getItem()) && !Captains.canLead(self)) {
			info.cancel();
		}
	}

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
