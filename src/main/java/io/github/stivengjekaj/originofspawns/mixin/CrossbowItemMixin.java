package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
}
