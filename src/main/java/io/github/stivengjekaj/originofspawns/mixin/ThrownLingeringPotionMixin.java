package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownLingeringPotion.class)
public abstract class ThrownLingeringPotionMixin {
	/**
	 * The genes of the owner make the cloud wider. The cloud gets smaller over its life from this start radius.
	 */
	@ModifyArg(method = "onHitAsPotion", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadius(F)V", ordinal = 0))
	private float originofspawns$cloudRadius(float radius) {
		return radius * GeneBehaviors.splashMultiplier(((ThrownLingeringPotion) (Object) this).getOwner());
	}
}
