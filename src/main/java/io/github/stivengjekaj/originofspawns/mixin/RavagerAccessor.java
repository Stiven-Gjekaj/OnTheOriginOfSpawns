package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.Ravager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ravager.class)
public interface RavagerAccessor {
	@Invoker("roar")
	void originofspawns$roar();

	@Accessor("roarTick")
	void originofspawns$setRoarTick(int ticks);

	@Accessor("stunnedTick")
	void originofspawns$setStunnedTick(int ticks);
}
