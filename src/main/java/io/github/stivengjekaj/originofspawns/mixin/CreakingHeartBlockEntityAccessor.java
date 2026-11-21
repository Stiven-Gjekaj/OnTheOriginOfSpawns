package io.github.stivengjekaj.originofspawns.mixin;

import java.util.Optional;

import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreakingHeartBlockEntity.class)
public interface CreakingHeartBlockEntityAccessor {
	@Invoker("getCreakingProtector")
	Optional<Creaking> originofspawns$getCreakingProtector();
}
