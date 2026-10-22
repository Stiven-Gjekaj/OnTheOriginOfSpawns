package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {
	@Accessor("goalSelector")
	GoalSelector originofspawns$goalSelector();

	@Accessor("targetSelector")
	GoalSelector originofspawns$targetSelector();

	/**
	 * Picks up the item, as the mob does when it walks over it.
	 */
	@Invoker("pickUpItem")
	void originofspawns$pickUpItem(ServerLevel level, ItemEntity item);
}
