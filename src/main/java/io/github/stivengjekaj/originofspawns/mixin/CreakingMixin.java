package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;

import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the genes of a creaking change its twitch, its stop when a player looks at it, and its heart.
 */
@Mixin(Creaking.class)
public abstract class CreakingMixin {
	/**
	 * When a player hurts a creaking that a heart protects, the hurt genes of the creaking act.
	 */
	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/CreakingHeartBlockEntity;creakingHurt()V"))
	private void originofspawns$playerHurt(CreakingHeartBlockEntity heart, Operation<Void> original) {
		original.call(heart);
		GeneCreakings.onPlayerHurt((Creaking) (Object) this);
	}

	/**
	 * A creaking that a gene lets move, moves also while a player looks at it.
	 */
	@Inject(method = "checkCanMove", at = @At("RETURN"), cancellable = true)
	private void originofspawns$free(CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && GeneCreakings.free((Creaking) (Object) this)) {
			info.setReturnValue(true);
		}
	}
}
