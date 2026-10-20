package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {
	/**
	 * Multiplies the spread of the arrow by the arrow spread effects of the genes.
	 */
	@ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"),
			index = 7)
	private float originofspawns$arrowSpread(float spread) {
		return spread * GeneBehaviors.arrowSpreadMultiplier((AbstractSkeleton) (Object) this);
	}
}
