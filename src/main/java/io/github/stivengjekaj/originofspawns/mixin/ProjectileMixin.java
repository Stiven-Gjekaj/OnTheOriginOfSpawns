package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a projectile that a gene dodges go through the mob, as if the mob were not there. The projectiles of a mob with
 * genes also do not hit each other, so that the fireballs of one shot do not explode at the mob.
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

	/**
	 * A player does not turn back a large fireball of a mob with a hard fireball.
	 */
	@Inject(method = "deflect(Lnet/minecraft/world/entity/projectile/ProjectileDeflection;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityReference;ZLnet/minecraft/world/phys/Vec3;)Z",
			at = @At("HEAD"), cancellable = true)
	private void originofspawns$hardFireball(ProjectileDeflection deflection, Entity deflecting,
			EntityReference<Entity> newOwner, boolean byAttack, Vec3 power, CallbackInfoReturnable<Boolean> info) {
		if ((Object) this instanceof LargeFireball fireball && deflecting instanceof Player
				&& GeneBlasts.shooter(fireball) instanceof Mob shooter
				&& GeneEffects.of(shooter).anyMatch(GeneEffect.HardFireball.class::isInstance)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
	private void originofspawns$notTheSameShooter(Entity target, CallbackInfoReturnable<Boolean> info) {
		Entity owner = ((Projectile) (Object) this).getOwner();
		if (info.getReturnValueZ() && owner != null && owner.hasAttached(ModAttachments.GENES)
				&& target instanceof Projectile other && other.getOwner() == owner) {
			info.setReturnValue(false);
		}
	}
}
