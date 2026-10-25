package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.Shoot;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Changes the wind charge shot of a breeze with its genes. A vanilla breeze charges for 15 ticks before a shot, and
 * waits 10 ticks after it.
 */
@Mixin(Shoot.class)
public abstract class BreezeShootMixin {
	/**
	 * The genes of the breeze change the wind charge of the shot before the game adds it to the level.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
	private Projectile originofspawns$shot(Projectile charge, ServerLevel level, ItemStack stack, double x, double y, double z,
			float power, float uncertainty, Operation<Projectile> original) {
		if (!(charge.getOwner() instanceof Breeze breeze)) {
			return original.call(charge, level, stack, x, y, z, power, uncertainty);
		}
		Projectile shot = original.call(charge, level, stack, x, y, z, power, uncertainty * GeneBehaviors.arrowSpreadMultiplier(breeze));
		if (shot instanceof AbstractHurtingProjectile hurting) {
			GeneBehaviors.fireballSpeed(breeze, hurting);
		}
		return shot;
	}

	@ModifyExpressionValue(method = "start", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/monster/breeze/Shoot;SHOOT_INITIAL_DELAY_TICKS:I"))
	private int originofspawns$charge(int ticks, ServerLevel level, Breeze breeze, long timestamp) {
		return GeneBehaviors.scaleThrowInterval(breeze, ticks);
	}

	@ModifyExpressionValue(method = "stop", at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/monster/breeze/Shoot;SHOOT_COOLDOWN_TICKS:I"))
	private int originofspawns$cooldown(int ticks, ServerLevel level, Breeze breeze, long timestamp) {
		return GeneBehaviors.scaleThrowInterval(breeze, ticks);
	}
}
