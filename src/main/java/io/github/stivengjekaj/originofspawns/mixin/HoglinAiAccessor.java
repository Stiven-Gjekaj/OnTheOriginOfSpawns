package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HoglinAi.class)
public interface HoglinAiAccessor {
	@Invoker("piglinsOutnumberHoglins")
	static boolean originofspawns$piglinsOutnumberHoglins(Hoglin body) {
		throw new AssertionError();
	}
}
