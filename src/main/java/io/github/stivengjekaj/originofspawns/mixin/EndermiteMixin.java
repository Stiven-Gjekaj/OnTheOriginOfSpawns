package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Endermite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
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

	/**
	 * The endermites of the genes come out when the endermite dies of age.
	 */
	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Endermite;discard()V"))
	private void originofspawns$ageSpawn(Endermite self, Operation<Void> original) {
		if (self.level() instanceof ServerLevel level) {
			GeneBehaviors.ageSpawn(self, level);
		}
		original.call(self);
	}
}
