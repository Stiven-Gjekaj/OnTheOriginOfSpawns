package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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
		Creaking self = (Creaking) (Object) this;
		// A hidden heart shows no trail and grows no resin.
		if (!GeneCombat.has(self, GeneEffect.HiddenHeart.class)) {
			original.call(heart);
		}
		GeneCreakings.onPlayerHurt(self);
	}

	/**
	 * A creaking that a gene lets move, moves also while a player looks at it. Else a still after look gene holds it
	 * still for a short time after a look.
	 */
	@Inject(method = "checkCanMove", at = @At("RETURN"), cancellable = true)
	private void originofspawns$free(CallbackInfoReturnable<Boolean> info) {
		Creaking self = (Creaking) (Object) this;
		if (GeneCreakings.free(self)) {
			info.setReturnValue(true);
		} else if (GeneCreakings.stillAfterLook(self, !info.getReturnValueZ())) {
			info.setReturnValue(false);
		}
	}

	/**
	 * A look gene of an awake creaking can stop the look of a player from holding it still. The look still wakes it.
	 */
	@WrapOperation(method = "checkCanMove", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/creaking/Creaking;isLookingAtMe(Lnet/minecraft/world/entity/LivingEntity;DZZ[D)Z"))
	private boolean originofspawns$ignoreLook(Creaking self, LivingEntity viewer, double cone, boolean adjust, boolean through,
			double[] heights, Operation<Boolean> original) {
		if (self.isActive() && viewer instanceof Player player && GeneCreakings.ignoresLook(self, player)) {
			return false;
		}
		return original.call(self, viewer, cone, adjust, through, heights);
	}

	/**
	 * A look wake range gene changes the distance in which a look wakes the creaking.
	 */
	@ModifyConstant(method = "checkCanMove", constant = @Constant(doubleValue = 144.0))
	private double originofspawns$lookWakeRange(double vanilla) {
		return GeneCreakings.lookWakeRangeSqr((Creaking) (Object) this, vanilla);
	}
}
