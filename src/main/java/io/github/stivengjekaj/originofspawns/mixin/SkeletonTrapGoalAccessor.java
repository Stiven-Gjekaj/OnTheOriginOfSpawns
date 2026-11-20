package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkeletonTrapGoal.class)
public interface SkeletonTrapGoalAccessor {
	/**
	 * Makes one more tame skeleton horse of the trap, as the vanilla trap makes its three horses.
	 */
	@Invoker("createHorse")
	AbstractHorse originofspawns$createHorse(DifficultyInstance difficulty);

	/**
	 * Makes a skeleton with an iron helmet and an enchanted bow for the horse, as the vanilla trap does.
	 */
	@Invoker("createSkeleton")
	Skeleton originofspawns$createSkeleton(DifficultyInstance difficulty, AbstractHorse horse);
}
