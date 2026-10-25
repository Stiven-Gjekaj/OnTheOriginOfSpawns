package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.LongJump;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LongJump.class)
public interface LongJumpAccessor {
	/**
	 * Runs one step of the long jump of the breeze: the jump at the end of the inhale, or the landing.
	 */
	@Invoker("tick")
	void originofspawns$tick(ServerLevel level, Breeze breeze, long timestamp);
}
