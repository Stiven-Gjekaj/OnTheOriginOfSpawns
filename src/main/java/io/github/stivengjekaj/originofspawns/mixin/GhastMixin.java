package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Ghast.class)
public abstract class GhastMixin {
	/**
	 * A fireball that a player hits back does 1000 damage to a ghast. A return ward changes this damage.
	 */
	@ModifyConstant(method = "hurtServer", constant = @Constant(floatValue = 1000.0F))
	private float originofspawns$returnWard(float reflected, ServerLevel level, DamageSource source, float damage) {
		return GeneCombat.returnedFireballDamage((Ghast) (Object) this, reflected, damage);
	}
}
