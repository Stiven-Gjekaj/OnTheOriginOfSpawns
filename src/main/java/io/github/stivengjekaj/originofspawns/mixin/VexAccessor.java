package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.Vex;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vex.class)
public interface VexAccessor {
	@Accessor("hasLimitedLife")
	boolean originofspawns$hasLimitedLife();

	/**
	 * Returns the ticks of life that the vex has left.
	 */
	@Accessor("limitedLifeTicks")
	int originofspawns$limitedLifeTicks();
}
