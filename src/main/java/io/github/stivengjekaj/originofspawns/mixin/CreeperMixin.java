package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.world.entity.monster.Creeper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
	/**
	 * The radius of the last blast of the creeper, after the charge and the genes.
	 */
	@Unique
	private float originofspawns$blastRadius;

	/**
	 * The genes multiply the radius that the game gives the blast. This radius already has the charge in it.
	 */
	@ModifyArg(method = "explodeCreeper", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),
			index = 4)
	private float originofspawns$blastSize(float radius) {
		originofspawns$blastRadius = radius * GeneBlasts.sizeMultiplier((Creeper) (Object) this);
		return originofspawns$blastRadius;
	}
}
