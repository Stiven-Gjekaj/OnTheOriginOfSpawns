package io.github.stivengjekaj.originofspawns.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.monster.Shulker$ShulkerAttackGoal")
public interface ShulkerAttackGoalAccessor {
	@Accessor("attackTime")
	int originofspawns$attackTime();
}
