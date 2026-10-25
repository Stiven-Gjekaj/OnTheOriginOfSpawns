package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BreezeWindCharge.class)
public abstract class BreezeWindChargeMixin {
	/**
	 * The blast size genes of the mob that shot the wind charge multiply the radius of its blast. The vanilla radius
	 * is 3 blocks. The push at each distance comes from the radius, so a larger blast also pushes more.
	 */
	@ModifyArg(method = "explode", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/util/random/WeightedList;Lnet/minecraft/core/Holder;)V"),
			index = 6)
	private float originofspawns$blastSize(float radius) {
		Mob shooter = GeneBlasts.shooter((BreezeWindCharge) (Object) this);
		return shooter == null ? radius : radius * GeneBlasts.sizeMultiplier(shooter);
	}
}
