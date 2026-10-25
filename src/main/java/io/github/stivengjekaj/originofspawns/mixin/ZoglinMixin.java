package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Optional;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(Zoglin.class)
public abstract class ZoglinMixin {
	/**
	 * The hit genes run on the hit of the tusks, and the throw genes change the throw. The hit does not use the hit
	 * of the other mobs.
	 */
	@WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/hoglin/HoglinBase;hurtAndThrowTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean originofspawns$tuskHit(ServerLevel level, LivingEntity body, LivingEntity target,
			Operation<Boolean> original) {
		ItemStack shield = target.getItemBlockingWith();
		Vec3 before = target.getDeltaMovement();
		boolean hurt = original.call(level, body, target);
		GeneCombat.afterTuskHit((Mob) body, level, target, before, shield, hurt);
		return hurt;
	}

	/**
	 * A zoglin with the player first gene takes a player in the radius first, if it sees one.
	 */
	@ModifyReturnValue(method = "findNearestValidAttackTarget", at = @At("RETURN"))
	private static Optional<? extends LivingEntity> originofspawns$playerFirst(Optional<? extends LivingEntity> nearest,
			ServerLevel level, Mob mob) {
		var radius = io.github.stivengjekaj.originofspawns.evolution.GeneEffects.of(mob)
				.filter(io.github.stivengjekaj.originofspawns.definition.GeneEffect.PlayerFirst.class::isInstance)
				.mapToDouble(effect -> ((io.github.stivengjekaj.originofspawns.definition.GeneEffect.PlayerFirst) effect).radius())
				.max();
		if (radius.isEmpty()) {
			return nearest;
		}
		Optional<LivingEntity> player = mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
				.orElse(NearestVisibleLivingEntities.empty())
				.findClosest(target -> target instanceof Player && mob.distanceTo(target) <= radius.getAsDouble()
						&& Sensor.isEntityAttackable(level, mob, target));
		return player.isPresent() ? player : nearest;
	}
}
