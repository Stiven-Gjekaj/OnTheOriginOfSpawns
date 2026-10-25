package io.github.stivengjekaj.originofspawns.mixin;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GenePiglins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PiglinSpecificSensor.class)
public abstract class PiglinSpecificSensorMixin {
	/**
	 * A piglin with the gold armor attack gene sees a player in gold armor as a target, with its chance.
	 */
	@WrapOperation(method = "doTick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isWearingSafeArmor(Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean originofspawns$goldArmor(LivingEntity entity, Operation<Boolean> original,
			@Local(argsOnly = true) LivingEntity body) {
		return original.call(entity) && !(entity instanceof Player player && GenePiglins.attacksInGold(body, player));
	}

	/**
	 * A piglin with no fear of soul fire finds no repellent.
	 */
	@WrapOperation(method = "doTick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/sensing/PiglinSpecificSensor;findNearestRepellent(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;"))
	private Optional<BlockPos> originofspawns$soulFire(ServerLevel level, LivingEntity body, Operation<Optional<BlockPos>> original) {
		return GenePiglins.fearless(body, GeneEffect.NoFear.Fear.SOUL_FIRE) ? Optional.empty() : original.call(level, body);
	}

	/**
	 * A piglin with no fear of zombified piglins does not see them as a danger.
	 */
	@WrapOperation(method = "doTick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isZombified(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean originofspawns$zombified(Entity entity, Operation<Boolean> original,
			@Local(argsOnly = true) LivingEntity body) {
		return original.call(entity) && !GenePiglins.fearless(body, GeneEffect.NoFear.Fear.ZOMBIFIED_PIGLIN);
	}
}
