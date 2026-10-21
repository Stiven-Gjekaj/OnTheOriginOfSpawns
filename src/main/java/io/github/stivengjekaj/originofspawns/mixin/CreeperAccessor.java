package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Creeper.class)
public interface CreeperAccessor {
	/**
	 * The synced flag of a charged creeper. Only lightning sets it in vanilla.
	 */
	@Accessor("DATA_IS_POWERED")
	static EntityDataAccessor<Boolean> originofspawns$poweredData() {
		throw new AssertionError();
	}

	/**
	 * Makes the blast of the creeper now, as at the end of its fuse.
	 */
	@Invoker("explodeCreeper")
	void originofspawns$explode();
}
