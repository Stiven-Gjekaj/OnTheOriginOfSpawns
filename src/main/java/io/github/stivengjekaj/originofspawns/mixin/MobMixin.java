package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
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
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void originofspawns$ignoredTarget(LivingEntity target, CallbackInfo info) {
		Mob self = (Mob) (Object) this;
		if (target != null && !self.level().isClientSide() && target.hasAttached(ModAttachments.GENES)
				&& GeneCombat.ignores(self, target)) {
			info.cancel();
		}
	}

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

	@Inject(method = "finalizeSpawn", at = @At("HEAD"))
	private void originofspawns$recordSpawnReason(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> info) {
		((Mob) (Object) this).setAttached(ModAttachments.SPAWN_REASON, reason);
	}

	/**
	 * A species in the tag {@code originofspawns:no_random_equipment} gets no random armor and no random enchantments.
	 * Its natural weapon, such as the bow of a skeleton, comes from its own class and stays.
	 */
	@Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noRandomArmor(RandomSource random, DifficultyInstance difficulty, CallbackInfo info) {
		if (((Mob) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_RANDOM_EQUIPMENT)) {
			info.cancel();
		}
	}

	@Inject(method = "populateDefaultEquipmentEnchantments", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noRandomEnchantments(ServerLevelAccessor level, RandomSource random,
			DifficultyInstance difficulty, CallbackInfo info) {
		if (((Mob) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_RANDOM_EQUIPMENT)) {
			info.cancel();
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
		GeneCombat.onHit(self, target);
		if (target instanceof Player) {
			CombatRecord record = self.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
			if (!record.damagedPlayer()) {
				self.setAttached(ModAttachments.COMBAT, record.withDamagedPlayer());
			}
		}
	}
}
