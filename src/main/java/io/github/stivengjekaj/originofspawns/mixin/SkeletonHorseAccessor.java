package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.animal.equine.SkeletonHorse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkeletonHorse.class)
public interface SkeletonHorseAccessor {
	/**
	 * The ticks that a skeleton horse trap waited for a player.
	 */
	@Accessor("trapTime")
	void originofspawns$setTrapTime(int ticks);
}
