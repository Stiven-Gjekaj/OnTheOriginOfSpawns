package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Hoglin.class)
public abstract class HoglinMixin {
	/**
	 * The conversion time genes change the time in the Overworld before a hoglin turns into a zoglin.
	 */
	@ModifyConstant(method = "customServerAiStep", constant = @Constant(intValue = 300))
	private int originofspawns$conversionTime(int ticks) {
		return GeneBehaviors.conversionTicks((Hoglin) (Object) this, ticks);
	}

	/**
	 * The hit genes run on the hit of the tusks, and the throw genes change the throw. The hit does not use the hit
	 * of the other mobs.
	 */
	@WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/hoglin/HoglinBase;hurtAndThrowTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean originofspawns$tuskHit(ServerLevel level, LivingEntity body, LivingEntity target,
			Operation<Boolean> original) {
		ItemStack shield = target.getItemBlockingWith();
		Vec3 before = target.getDeltaMovement();
		boolean hurt = original.call(level, body, target);
		GeneCombat.afterTuskHit((Mob) body, level, target, before, shield, hurt);
		return hurt;
	}
}
