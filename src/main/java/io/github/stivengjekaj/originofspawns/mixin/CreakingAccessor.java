package io.github.stivengjekaj.originofspawns.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.creaking.Creaking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creaking.class)
public interface CreakingAccessor {
	@Accessor("CAN_MOVE")
	static EntityDataAccessor<Boolean> originofspawns$canMoveData() {
		throw new AssertionError();
	}
}
