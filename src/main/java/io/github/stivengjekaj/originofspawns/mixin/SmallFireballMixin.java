package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The genes of the mob that shoots a small fireball change what the fireball does where it hits.
 */
@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin {
	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
	private float originofspawns$fireTime(float seconds) {
		Mob shooter = GeneBlasts.shooter((SmallFireball) (Object) this);
		return shooter == null ? seconds : GeneBehaviors.fireballFireSeconds(shooter, seconds);
	}
}
