package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ChargeAttack;
import net.minecraft.world.entity.animal.Animal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the vanilla charge attack of a nautilus act as a melee hit for the genes of the mob.
 */
@Mixin(ChargeAttack.class)
public abstract class ChargeAttackMixin {
	/**
	 * Before the charge hits, a shield break gene stops the shield of the target, so that the charge hits.
	 */
	@Inject(method = "dealDamageToTarget", at = @At("HEAD"))
	private void originofspawns$shieldBreak(ServerLevel level, Animal body, LivingEntity target, CallbackInfo info) {
		if (body.hasAttached(ModAttachments.GENES)) {
			GeneCubes.shieldBreak(body, level, target, target.getItemBlockingWith(), false);
		}
	}

	/**
	 * After the charge hits, the melee hit genes of the mob act on the target.
	 */
	@Inject(method = "dealDamageToTarget", at = @At("TAIL"))
	private void originofspawns$hitGenes(ServerLevel level, Animal body, LivingEntity target, CallbackInfo info) {
		if (body.hasAttached(ModAttachments.GENES)) {
			GeneCombat.onHit(body, target);
		}
	}
}
