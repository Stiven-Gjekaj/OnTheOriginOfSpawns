package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSkeleton.class)
public interface AbstractSkeletonAccessor {
	@Accessor("bowGoal")
	RangedBowAttackGoal<AbstractSkeleton> originofspawns$bowGoal();

	@Invoker("getArrow")
	AbstractArrow originofspawns$getArrow(ItemStack arrow, float power, ItemStack bow);
}
