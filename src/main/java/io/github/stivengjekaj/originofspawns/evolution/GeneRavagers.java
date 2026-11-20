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

	/**
	 * The ticks of a roar before its blast, as the vanilla roar after a stun.
	 */
	private static final int ROAR_TICKS = 20;

	/**
	 * Starts a roar of the ravager now, as the vanilla roar at the end of a stun: the roar sound, and the blast of the
	 * roar half a second later. A ravager that roars or is stunned now does not start a second roar.
	 *
	 * @return true if the ravager started a roar
	 */
	public static boolean roarNow(Ravager ravager) {
		if (!ravager.isAlive() || ravager.getRoarTick() > 0 || ravager.getStunnedTick() > 0) {
			return false;
		}
		ravager.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
		((io.github.stivengjekaj.originofspawns.mixin.RavagerAccessor) ravager).originofspawns$setRoarTick(ROAR_TICKS);
		return true;
	}

	/**
	 * Starts a roar of the hurt ravager for each roar on hurt gene whose kind of hit matches and whose cooldown is over.
	 *
	 * @return true if the ravager started a roar
	 */
	public static boolean roarOnHurt(net.minecraft.world.entity.Mob mob, net.minecraft.world.damagesource.DamageSource source) {
		if (!(mob instanceof Ravager ravager) || source.getDirectEntity() == null) {
			return false;
		}
		boolean projectile = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarOnHurt roar && roar.hits().includes(projectile)
					&& GeneCombat.ready(ravager, roar, roar.cooldownSeconds()) && roarNow(ravager)) {
				GeneCombat.startCooldown(ravager, roar);
				return true;
			}
		}
		return false;
	}

	/**
	 * After the mob kills a player, each roar on kill gene heals it, and the ravager roars.
	 *
	 * @return true if a roar on kill gene acted
	 */
	public static boolean onPlayerKilled(net.minecraft.world.entity.Mob killer) {
		if (!(killer instanceof Ravager ravager)) {
			return false;
		}
		boolean acted = false;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarOnKill roar) {
				ravager.heal(roar.heal());
				acted = true;
			}
		}
		if (acted) {
			roarNow(ravager);
		}
		return acted;
	}

	/**
	 * The vanilla chance that a shield block stuns a ravager.
	 */
	public static final double VANILLA_STUN_CHANCE = 0.5;

	/**
	 * Returns the chance that a shield block stuns the ravager: the largest stun chance gene, or the vanilla chance.
	 */
	public static double stunChance(Ravager ravager) {
		var chance = GeneEffects.of(ravager).filter(GeneEffect.StunChance.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.StunChance) effect).chance()).max();
		return chance.orElse(VANILLA_STUN_CHANCE);
	}
}
