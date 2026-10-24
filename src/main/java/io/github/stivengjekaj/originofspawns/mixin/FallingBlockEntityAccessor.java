package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
	/**
	 * Makes a falling block at the place, with no change to the world. The vanilla way removes the block at the place.
	 */
	@Invoker("<init>")
	static FallingBlockEntity originofspawns$create(Level level, double x, double y, double z, BlockState state) {
		throw new AssertionError();
	}
}
