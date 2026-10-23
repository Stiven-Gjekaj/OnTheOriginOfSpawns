package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Witch.class)
public abstract class WitchMixin {
	/**
	 * A witch with gene effects throws its potion with the changes of its genes, and with the arrow spread of its genes.
	 * The potions that heal a raider do not change.
	 */
	@WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
	private Projectile originofspawns$throwPotion(Projectile.ProjectileFactory<?> factory, ServerLevel level,
			ItemStack stack, LivingEntity source, double x, double y, double z, float power, float uncertainty,
			Operation<Projectile> original, LivingEntity target, float attackPower) {
		if (target instanceof Raider || GeneEffects.of(source).findAny().isEmpty()) {
			return original.call(factory, level, stack, source, x, y, z, power, uncertainty);
		}
		return GeneBehaviors.throwPotion((Witch) source, level, stack, x, y, z, power,
				uncertainty * GeneBehaviors.arrowSpreadMultiplier(source));
	}
}
