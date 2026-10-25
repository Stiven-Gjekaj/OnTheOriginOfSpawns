package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.item.ProjectileWeaponItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BehaviorUtils.class)
public abstract class BehaviorUtilsMixin {
	/**
	 * The throw range genes change the shot range of a mob with a brain, such as a piglin with a crossbow. The
	 * crossbow range is 8 blocks in vanilla.
	 */
	@WrapOperation(method = "isWithinAttackRange", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getDefaultProjectileRange()I"))
	private static int originofspawns$shotRange(ProjectileWeaponItem weapon, Operation<Integer> original, Mob body,
			LivingEntity target, int margin) {
		int blocks = original.call(weapon);
		return body.hasAttached(ModAttachments.GENES)
				? Math.round(blocks * GeneBehaviors.throwRangeMultiplier(body, target)) : blocks;
	}
}
