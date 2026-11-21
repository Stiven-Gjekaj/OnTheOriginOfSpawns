package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Animal.class)
public abstract class AnimalMixin {
	/**
	 * Records that a baby comes from its parents. A bred baby gets genes from the pool, also when it is tame at birth,
	 * as a bred tame wolf is.
	 */
	@WrapOperation(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private void originofspawns$recordBreeding(ServerLevel level, Entity baby, Operation<Void> original) {
		baby.setAttached(ModAttachments.SPAWN_REASON, EntitySpawnReason.BREEDING);
		original.call(level, baby);
	}
}
