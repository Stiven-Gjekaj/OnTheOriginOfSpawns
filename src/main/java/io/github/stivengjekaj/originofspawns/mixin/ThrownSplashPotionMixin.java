package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrownSplashPotion.class)
public abstract class ThrownSplashPotionMixin {
	/**
	 * The genes of the owner make the splash wider. Vanilla uses 4 blocks for the width of the splash and for the drop
	 * of its strength with the distance.
	 */
	@ModifyConstant(method = "onHitAsPotion", constant = @Constant(doubleValue = 4.0))
	private double originofspawns$splashWidth(double blocks) {
		return blocks * GeneBehaviors.splashMultiplier(((ThrownSplashPotion) (Object) this).getOwner());
	}

	/**
	 * The genes of the owner make the splash higher.
	 */
	@ModifyConstant(method = "onHitAsPotion", constant = @Constant(doubleValue = 2.0))
	private double originofspawns$splashHeight(double blocks) {
		return blocks * GeneBehaviors.splashMultiplier(((ThrownSplashPotion) (Object) this).getOwner());
	}

	/**
	 * Vanilla compares the square of the distance with 16.
	 */
	@ModifyConstant(method = "onHitAsPotion", constant = @Constant(doubleValue = 16.0))
	private double originofspawns$splashReach(double squared) {
		float multiplier = GeneBehaviors.splashMultiplier(((ThrownSplashPotion) (Object) this).getOwner());
		return squared * multiplier * multiplier;
	}
}
