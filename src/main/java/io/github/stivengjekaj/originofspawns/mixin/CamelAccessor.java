package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camel.class)
public interface CamelAccessor {
	/**
	 * Runs the vanilla dash of a camel, as when the player that rides it jumps. The amount 1 is a full dash.
	 */
	@Invoker("executeRidersJump")
	void originofspawns$dash(float amount, Vec3 input);
}
