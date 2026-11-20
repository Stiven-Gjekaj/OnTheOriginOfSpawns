package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
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

	/**
	 * A snap does not come in this many ticks after a shot.
	 */
	public static final long SNAP_SHOT_TICKS = 20L;

	/**
	 * Snaps the lid of the shulker on the nearest player next to it, if the lid is open, the shulker did not shoot in the
	 * last second, and the cooldown of the gene is over.
	 *
	 * @return true if the shulker snapped
	 */
	public static boolean lidSnap(Shulker shulker, ServerLevel level, GeneEffect.LidSnap snap) {
		long now = level.getGameTime();
		if (!GeneCombat.test(shulker, GeneEffect.When.SHELL_OPEN)
				|| now - shulker.getAttachedOrElse(ModAttachments.SHOT_TIME, Long.MIN_VALUE / 2) < SNAP_SHOT_TICKS
				|| !GeneCombat.ready(shulker, snap, snap.cooldownSeconds())) {
			return false;
		}
		Player player = level.getEntitiesOfClass(Player.class, shulker.getBoundingBox().inflate(1.0),
				other -> other.isAlive() && !other.isSpectator() && !other.isCreative())
				.stream().min(java.util.Comparator.comparingDouble(other -> other.distanceToSqr(shulker))).orElse(null);
		if (player == null) {
			return false;
		}
		GeneCombat.startCooldown(shulker, snap);
		shulker.playSound(net.minecraft.sounds.SoundEvents.SHULKER_CLOSE, 1.0F, 1.5F);
		if (player.hurtServer(level, level.damageSources().mobAttack(shulker), snap.damage())) {
			GeneCombat.onHit(shulker, player);
		}
		return true;
	}

	/**
	 * The range of a teleport of a mob that is not a shulker, as the vanilla teleport of a shulker.
	 */
	public static final double TELEPORT_RANGE = 8.0;

	/**
	 * Teleports the hurt mob, after the chance of each teleport on hurt gene. The mob teleports one time at most.
	 *
	 * @return true if the mob teleported
	 */
	public static boolean teleportOnHurt(Mob mob, ServerLevel level) {
		if (!mob.isAlive()) {
			return false;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.TeleportOnHurt teleport && mob.getRandom().nextFloat() < teleport.chance()) {
				return mob instanceof Shulker shulker
						? ((io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker).originofspawns$teleportSomewhere()
						: GeneTeleports.away(mob, level, TELEPORT_RANGE);
			}
		}
		return false;
	}
}
