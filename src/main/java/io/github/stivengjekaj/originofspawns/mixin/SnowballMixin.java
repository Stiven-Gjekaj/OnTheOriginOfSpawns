package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets snowballs hurt a mob with a gene that is weak to snowballs, as they hurt a blaze.
 */
@Mixin(Snowball.class)
public abstract class SnowballMixin {
	@WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
	private void originofspawns$snowballDamage(Entity target, DamageSource source, float damage, Operation<Void> original) {
		original.call(target, source, GeneCombat.snowballDamage(target, damage));
	}
}
