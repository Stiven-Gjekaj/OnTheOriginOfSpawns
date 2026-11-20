package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.Shulker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerAccessor {
	@Invoker("isClosed")
	boolean originofspawns$isClosed();

	@Invoker("setRawPeekAmount")
	void originofspawns$setRawPeekAmount(int amount);

	@Invoker("teleportSomewhere")
	boolean originofspawns$teleportSomewhere();
}
