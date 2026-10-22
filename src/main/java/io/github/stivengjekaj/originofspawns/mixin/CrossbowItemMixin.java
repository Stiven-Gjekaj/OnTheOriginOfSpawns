package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
	/**
	 * The draw time and the shot rate of the genes change the load time of the crossbow of a mob. The goal of the mob
	 * and the crossbow use the same time, so the crossbow is full when the mob lets go.
	 */
	@ModifyReturnValue(method = "getChargeDuration", at = @At("RETURN"))
	private static int originofspawns$loadTime(int ticks, ItemStack crossbow, LivingEntity user) {
		return user instanceof Mob mob && mob.hasAttached(ModAttachments.GENES) ? GeneBehaviors.drawTicks(mob, ticks) : ticks;
	}

	/**
	 * The arrow genes of a mob act on the arrows of its crossbow, as on the arrows of a bow.
	 */
	@Inject(method = "createProjectile", at = @At("RETURN"))
	private void originofspawns$armBolt(Level level, LivingEntity shooter, ItemStack heldItem, ItemStack projectile,
			boolean isCrit, CallbackInfoReturnable<Projectile> info) {
		if (shooter instanceof Mob mob && mob.hasAttached(ModAttachments.GENES)) {
			GeneBehaviors.armArrow(mob, info.getReturnValue());
		}
	}

	/**
	 * The arrow spread of the genes changes the spread of the crossbow shot of a mob.
	 */
	@ModifyVariable(method = "performShooting", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private float originofspawns$spread(float uncertainty, Level level, LivingEntity shooter, InteractionHand hand,
			ItemStack weapon, float power) {
		return shooter instanceof Mob mob && mob.hasAttached(ModAttachments.GENES)
				? uncertainty * GeneBehaviors.arrowSpreadMultiplier(mob) : uncertainty;
	}
}
