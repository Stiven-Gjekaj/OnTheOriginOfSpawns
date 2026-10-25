package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The blast genes of the mob that shoots a large fireball act on the blast of the fireball: its size, and the effects
 * after the blast. A fireball that a player hits back has the player as its owner, so it has no genes.
 */
@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin {
	@Unique
	private static final String EXPLODE =
			"Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V";

	/**
	 * The radius of the blast of the fireball, after the genes.
	 */
	@Unique
	private float originofspawns$blastRadius;

	@ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = EXPLODE), index = 4)
	private float originofspawns$blastSize(float radius) {
		Mob shooter = GeneBlasts.shooter((LargeFireball) (Object) this);
		originofspawns$blastRadius = shooter == null ? radius : radius * GeneBlasts.sizeMultiplier(shooter);
		return originofspawns$blastRadius;
	}

	@Inject(method = "onHit", at = @At(value = "INVOKE", target = EXPLODE, shift = At.Shift.AFTER))
	private void originofspawns$afterBlast(HitResult hit, CallbackInfo info) {
		LargeFireball self = (LargeFireball) (Object) this;
		Mob shooter = GeneBlasts.shooter(self);
		if (shooter != null && self.level() instanceof ServerLevel level) {
			GeneBlasts.afterBlast(shooter, level, self.position(), originofspawns$blastRadius);
		}
	}
}
