package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
	/**
	 * The rocket blast of a mob with a larger blast reaches farther. The radius is 5 blocks in vanilla.
	 */
	@ModifyConstant(method = "dealExplosionDamage", constant = @Constant(doubleValue = 5.0))
	private double originofspawns$blastRadius(double radius) {
		return radius * GeneBehaviors.rocketBlastMultiplier(((FireworkRocketEntity) (Object) this).getOwner());
	}

	/**
	 * The square of the radius, which the blast also checks.
	 */
	@ModifyConstant(method = "dealExplosionDamage", constant = @Constant(doubleValue = 25.0))
	private double originofspawns$blastRadiusSquared(double radiusSquared) {
		float multiplier = GeneBehaviors.rocketBlastMultiplier(((FireworkRocketEntity) (Object) this).getOwner());
		return radiusSquared * multiplier * multiplier;
	}
}
