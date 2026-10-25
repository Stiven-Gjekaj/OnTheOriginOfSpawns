package io.github.stivengjekaj.originofspawns.mixin;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GenePiglins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.sensing.HoglinSpecificSensor;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoglinSpecificSensor.class)
public abstract class HoglinSpecificSensorMixin {
	/**
	 * A hoglin finds no repellent of a fear that its genes stop: warped fungus, a nether portal, or a respawn anchor.
	 */
	@WrapOperation(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/hoglin/Hoglin;)V",
			at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/sensing/HoglinSpecificSensor;findNearestRepellent(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/hoglin/Hoglin;)Ljava/util/Optional;"))
	private Optional<BlockPos> originofspawns$fears(HoglinSpecificSensor sensor, ServerLevel level, Hoglin body,
			Operation<Optional<BlockPos>> original) {
		return GenePiglins.hoglinRepellent(level, body);
	}
}
