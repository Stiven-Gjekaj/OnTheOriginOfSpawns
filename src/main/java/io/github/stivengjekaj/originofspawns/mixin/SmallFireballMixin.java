package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The genes of the mob that shoots a small fireball change what the fireball does where it hits.
 */
@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin {
	@Inject(method = "onHit", at = @At("HEAD"))
	private void originofspawns$hit(HitResult hit, CallbackInfo info) {
		SmallFireball self = (SmallFireball) (Object) this;
		Mob shooter = GeneBlasts.shooter(self);
		if (shooter != null && self.level() instanceof ServerLevel level) {
			GeneBehaviors.smallFireballHit(shooter, self, level);
		}
	}

	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
	private float originofspawns$fireTime(float seconds) {
		Mob shooter = GeneBlasts.shooter((SmallFireball) (Object) this);
		return shooter == null ? seconds : GeneBehaviors.fireballFireSeconds(shooter, seconds);
	}
}
