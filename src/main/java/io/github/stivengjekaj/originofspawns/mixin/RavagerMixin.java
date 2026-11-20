package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneRavagers;

import net.minecraft.world.entity.monster.Ravager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lets the genes of a ravager change its roar and its stun.
 */
@Mixin(Ravager.class)
public abstract class RavagerMixin {
	/**
	 * A roar range gene changes the range of the roar.
	 */
	@ModifyConstant(method = "roar", constant = @Constant(doubleValue = 4.0))
	private double originofspawns$roarRange(double range) {
		return GeneRavagers.roarRange((Ravager) (Object) this);
	}

	/**
	 * A roar damage gene changes the damage of the roar.
	 */
	@ModifyConstant(method = "roar", constant = @Constant(floatValue = 6.0F))
	private float originofspawns$roarDamage(float damage) {
		return GeneRavagers.roarDamage((Ravager) (Object) this, damage);
	}
}
