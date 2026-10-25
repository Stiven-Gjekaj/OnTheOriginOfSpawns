package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCubeMob.class)
public interface AbstractCubeMobAccessor {
	/**
	 * Runs the touch attack of the cube on the target.
	 */
	@Invoker("dealDamage")
	void originofspawns$dealDamage(LivingEntity target);

	/**
	 * Returns a wait between two hops, in ticks.
	 */
	@Invoker("getJumpDelay")
	int originofspawns$jumpDelay();

	/**
	 * Returns the number of pieces of a split.
	 */
	@Invoker("getSplitCount")
	int originofspawns$splitCount();
}
