package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.animal.wolf.Wolf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Gives the tests the sound volume of a wolf.
 */
@Mixin(Wolf.class)
public interface WolfAccessor {
	@Invoker("getSoundVolume")
	float originofspawns$soundVolume();
}
