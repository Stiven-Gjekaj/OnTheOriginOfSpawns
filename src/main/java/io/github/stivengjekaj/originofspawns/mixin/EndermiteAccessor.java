package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.Endermite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Endermite.class)
public interface EndermiteAccessor {
	/**
	 * Returns the age of the endermite, in ticks.
	 */
	@Accessor("life")
	int originofspawns$life();

	@Accessor("life")
	void originofspawns$setLife(int ticks);
}
