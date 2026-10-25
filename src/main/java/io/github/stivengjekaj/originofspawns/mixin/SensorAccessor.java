package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Sensor.class)
public interface SensorAccessor {
	/**
	 * Runs the sensor one time for the body, as the brain does.
	 */
	@Invoker("doTick")
	void originofspawns$doTick(ServerLevel level, LivingEntity body);
}
