package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

/**
 * The genes of a shulker: its bullets, its shell, its teleports, and its split.
 */
public final class GeneShulkers {
	private GeneShulkers() {
	}

	/**
	 * Returns the speed multiplier of the bullets of the owner: the product of its bullet speed genes. With no owner, it
	 * is 1.
	 */
	public static double bulletSpeed(Entity owner) {
		double multiplier = 1.0;
		if (owner != null) {
			for (GeneEffect effect : GeneEffects.of(owner).toList()) {
				if (effect instanceof GeneEffect.BulletSpeed speed) {
					multiplier *= speed.multiplier();
				}
			}
		}
		return multiplier;
	}

	/**
	 * Returns the Levitation of a bullet of the owner, after its bullet levitation genes: the longest time and the highest
	 * level of the genes and of the vanilla effect.
	 */
	public static MobEffectInstance levitation(Entity owner, MobEffectInstance vanilla) {
		int ticks = vanilla.getDuration();
		int amplifier = vanilla.getAmplifier();
		boolean changed = false;
		if (owner != null) {
			for (GeneEffect effect : GeneEffects.of(owner).toList()) {
				if (effect instanceof GeneEffect.BulletLevitation levitation) {
					changed = true;
					ticks = Math.max(ticks, levitation.seconds().map(seconds -> Math.round(seconds * 20.0F)).orElse(0));
					amplifier = Math.max(amplifier, levitation.amplifier().orElse(0));
				}
			}
		}
		return changed ? new MobEffectInstance(vanilla.getEffect(), ticks, amplifier) : vanilla;
	}

	/**
	 * Counts an attack of the shulker. If a double shot gene makes this attack a double shot, it shoots one more bullet at
	 * the target now.
	 *
	 * @return true if the shulker shot one more bullet
	 */
	public static boolean countShot(Shulker shulker, LivingEntity target) {
		int count = shulker.getAttachedOrElse(ModAttachments.SHOT_COUNT, 0) + 1;
		shulker.setAttached(ModAttachments.SHOT_COUNT, count);
		var every = GeneEffects.of(shulker).filter(GeneEffect.DoubleShot.class::isInstance)
				.mapToInt(effect -> ((GeneEffect.DoubleShot) effect).every()).min();
		if (every.isEmpty() || count % every.getAsInt() != 0 || target == null) {
			return false;
		}
		shulker.level().addFreshEntity(new ShulkerBullet(shulker.level(), shulker, target, shulker.getAttachFace().getAxis()));
		return true;
	}
}
