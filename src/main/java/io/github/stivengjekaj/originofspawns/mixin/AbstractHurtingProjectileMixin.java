package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin {
	/**
	 * Before the fireball moves, the seeker genes of the mob that shot it turn it to the target of the mob.
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void originofspawns$seek(CallbackInfo info) {
		AbstractHurtingProjectile self = (AbstractHurtingProjectile) (Object) this;
		if (!self.level().isClientSide()) {
			Mob shooter = GeneBlasts.shooter(self);
			if (shooter != null) {
				GeneBehaviors.seek(shooter, self);
			}
		}
	}
}
