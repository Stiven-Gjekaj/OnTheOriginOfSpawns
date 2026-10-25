package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ZombifiedPiglin.class)
public interface ZombifiedPiglinAccessor {
	@Invoker("alertOthers")
	void originofspawns$alertOthers();
}
