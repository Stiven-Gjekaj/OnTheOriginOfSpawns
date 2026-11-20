package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneShulkers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lets the genes of the shulker that shot a bullet change the bullet.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin {
	/**
	 * Each bullet speed gene of the owner multiplies the speed of each step of the bullet.
	 */
	@ModifyConstant(method = "selectNextMoveDirection", constant = @Constant(doubleValue = 0.15))
	private double originofspawns$bulletSpeed(double speed) {
		return speed * GeneShulkers.bulletSpeed(((ShulkerBullet) (Object) this).getOwner());
	}

	/**
	 * Each bullet levitation gene of the owner makes the Levitation of the hit longer or stronger.
	 */
	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
			index = 0)
	private MobEffectInstance originofspawns$levitation(MobEffectInstance levitation) {
		return GeneShulkers.levitation(((ShulkerBullet) (Object) this).getOwner(), levitation);
	}
}
