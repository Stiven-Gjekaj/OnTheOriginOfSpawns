package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {
	/**
	 * The genes of the endermite can raise the chance that the pearl makes one.
	 */
	@ModifyExpressionValue(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"))
	private float originofspawns$pearlChance(float roll) {
		ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
		return self.level() instanceof ServerLevel level && self.getOwner() != null
				? GeneBehaviors.pearlRoll(level, self.getOwner(), roll) : roll;
	}
}
