package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

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

	/**
	 * Each vanilla teleport of the enderman marks its time, for the first hit after a teleport.
	 */
	@Inject(method = "teleport(DDD)Z", at = @At("RETURN"))
	private void originofspawns$markTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
		Enderman self = (Enderman) (Object) this;
		if (info.getReturnValueZ() && self.level() instanceof ServerLevel level) {
			self.setAttached(ModAttachments.TELEPORT_TIME, level.getGameTime());
		}
	}

	/**
	 * A gene lets the enderman see the stare of a player with a carved pumpkin.
	 */
	@WrapOperation(method = "isBeingStaredBy", at = @At(value = "INVOKE",
			target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
	private boolean originofspawns$throughPumpkin(Predicate<Object> disguise, Object player, Operation<Boolean> original) {
		return original.call(disguise, player)
				|| GeneCombat.has((Enderman) (Object) this, GeneEffect.StareThroughPumpkin.class);
	}
}
