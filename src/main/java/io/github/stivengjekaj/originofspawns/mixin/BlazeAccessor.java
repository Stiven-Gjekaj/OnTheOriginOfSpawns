package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Blaze;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Blaze.class)
public interface BlazeAccessor {
	/**
	 * The blocks under the eyes of its target at which a blaze stops to rise. The game picks a new one each 5 seconds.
	 */
	@Accessor("allowedHeightOffset")
	void originofspawns$setHeightOffset(float offset);

	@Accessor("nextHeightOffsetChangeTick")
	void originofspawns$setNextHeightOffsetChange(int tick);

	/**
	 * Runs the server step of the blaze in which it rises to its target.
	 */
	@Invoker("customServerAiStep")
	void originofspawns$serverStep(ServerLevel level);
}
