package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a projectile that a gene dodges go through the mob, as if the mob were not there.
 */
@Mixin(Projectile.class)
public abstract class ProjectileMixin {
	@Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
	private void originofspawns$passThrough(Entity target, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ() && target instanceof LivingEntity living && living.hasAttached(ModAttachments.GENES)
				&& GeneCombat.dodges(living, (Entity) (Object) this)) {
			info.setReturnValue(false);
		}
	}
}
