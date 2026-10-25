package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractWindCharge.class)
public abstract class AbstractWindChargeMixin {
	/**
	 * The wind charge damage genes of the mob that shot the charge add to the damage of its hit.
	 */
	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
			index = 2)
	private float originofspawns$damage(float damage) {
		Mob shooter = GeneBlasts.shooter((AbstractWindCharge) (Object) this);
		return shooter == null ? damage : GeneBehaviors.windChargeDamage(shooter, damage);
	}
}
