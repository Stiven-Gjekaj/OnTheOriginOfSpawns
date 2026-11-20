package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;

/**
 * The genes of a ravager: its roar, its stun, its charge, and its rider.
 */
public final class GeneRavagers {
	/**
	 * The range of the vanilla roar, in blocks.
	 */
	public static final double VANILLA_ROAR_RANGE = 4.0;

	private GeneRavagers() {
	}

	/**
	 * Returns the range of the roar of the ravager: the largest roar range gene, or the vanilla range.
	 */
	public static double roarRange(Ravager ravager) {
		double range = VANILLA_ROAR_RANGE;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarRange roar) {
				range = Math.max(range, roar.blocks());
			}
		}
		return range;
	}

	/**
	 * Returns the damage of the roar of the ravager after its roar damage genes whose state is true.
	 */
	public static float roarDamage(Ravager ravager, float damage) {
		float result = damage;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarDamage roar && GeneCombat.test(ravager, roar.when())) {
				result *= roar.multiplier();
			}
		}
		return result;
	}

	/**
	 * Gives the roar effects of the ravager to an entity that its roar hurt.
	 */
	public static void roarHit(Ravager ravager, LivingEntity target) {
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarEffect roar && !roar.allies()) {
				target.addEffect(new MobEffectInstance(roar.effect(), Math.round(roar.seconds() * 20.0F), roar.amplifier()),
						ravager);
			}
		}
	}

	/**
	 * Gives the ally roar effects of the ravager to the other raiders in their radius, after a roar.
	 *
	 * @return the number of effects that the raiders got
	 */
	public static int roarAllies(Ravager ravager) {
		int given = 0;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarEffect roar && roar.allies()) {
				for (Raider raider : ravager.level().getEntitiesOfClass(Raider.class,
						ravager.getBoundingBox().inflate(roar.radius()), other -> other != ravager && other.isAlive())) {
					raider.addEffect(new MobEffectInstance(roar.effect(), Math.round(roar.seconds() * 20.0F), roar.amplifier()),
							ravager);
					given++;
				}
			}
		}
		return given;
	}
}
