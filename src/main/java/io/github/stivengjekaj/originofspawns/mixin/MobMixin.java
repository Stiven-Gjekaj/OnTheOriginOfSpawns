package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Inject(method = "setTarget", at = @At("HEAD"))
	private void originofspawns$recordTarget(LivingEntity target, CallbackInfo info) {
		Mob self = (Mob) (Object) this;
		if (target instanceof Player && !self.level().isClientSide()) {
			CombatRecord record = self.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
			if (!record.targetedPlayer()) {
				self.setAttached(ModAttachments.COMBAT, record.withTargetedPlayer());
			}
		}
	}

	@Inject(method = "burnUndead", at = @At("HEAD"))
	private void originofspawns$recordSunBurn(CallbackInfo info) {
		Evolution.markSunBurn((Mob) (Object) this);
	}

	@Inject(method = "doHurtTarget", at = @At("RETURN"))
	private void originofspawns$recordHit(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ()) {
			return;
		}
		Mob self = (Mob) (Object) this;
		GeneEffects.onHit(self, target);
		if (target instanceof Player) {
			CombatRecord record = self.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
			if (!record.damagedPlayer()) {
				self.setAttached(ModAttachments.COMBAT, record.withDamagedPlayer());
			}
		}
	}
}
