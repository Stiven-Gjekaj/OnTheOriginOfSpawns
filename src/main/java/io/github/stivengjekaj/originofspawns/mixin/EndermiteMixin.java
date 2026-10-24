package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Endermite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Endermite.class)
public abstract class EndermiteMixin {
	/**
	 * The genes change the life of the endermite. Vanilla gives it 2400 ticks.
	 */
	@ModifyConstant(method = "aiStep", constant = @Constant(intValue = 2400))
	private int originofspawns$lifespan(int ticks) {
		return GeneBehaviors.lifespan((Endermite) (Object) this, ticks);
	}
}
