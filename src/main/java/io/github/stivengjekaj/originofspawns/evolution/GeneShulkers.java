package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
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

	/**
	 * Closes a shulker after a teleport for the time of its longest shut after teleport gene.
	 */
	public static void afterTeleport(Shulker shulker) {
		var seconds = GeneEffects.of(shulker).filter(GeneEffect.ShutAfterTeleport.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.ShutAfterTeleport) effect).seconds()).max();
		if (seconds.isEmpty()) {
			return;
		}
		shulker.removeAttached(ModAttachments.SHUT_UNTIL);
		// The vanilla teleport closes the lid with no armor. The closed shell gets its armor here.
		((io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker).originofspawns$setRawPeekAmount(0);
		shulker.setAttached(ModAttachments.SHUT_UNTIL, shulker.level().getGameTime() + Math.round(seconds.getAsDouble() * 20.0));
	}

	/**
	 * Returns true while a shulker stays closed after a teleport.
	 */
	public static boolean shut(Shulker shulker) {
		return shulker.getAttachedOrElse(ModAttachments.SHUT_UNTIL, Long.MIN_VALUE) > shulker.level().getGameTime();
	}

	/**
	 * Ends the closed time after a teleport. A shulker with a target opens again, as its attack opens it.
	 */
	public static void tickShut(Shulker shulker) {
		Long until = shulker.getAttached(ModAttachments.SHUT_UNTIL);
		if (until != null && shulker.level().getGameTime() >= until) {
			shulker.removeAttached(ModAttachments.SHUT_UNTIL);
			if (shulker.getTarget() != null) {
				((io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker).originofspawns$setRawPeekAmount(100);
			}
		}
	}

	/**
	 * The places that a sight teleport tries each second.
	 */
	private static final int SIGHT_TRIES = 16;

	/**
	 * Counts the time that the shulker does not see its target. After the time of the gene, it teleports to a place that
	 * sees the target.
	 *
	 * @return true if the shulker teleported now
	 */
	public static boolean sightTeleport(Shulker shulker, ServerLevel level, GeneEffect.SightTeleport sight) {
		LivingEntity target = shulker.getTarget();
		if (target == null || !target.isAlive() || shulker.hasLineOfSight(target)) {
			shulker.removeAttached(ModAttachments.BLIND_SINCE);
			return false;
		}
		long now = level.getGameTime();
		long since = shulker.getAttachedOrElse(ModAttachments.BLIND_SINCE, now);
		shulker.setAttached(ModAttachments.BLIND_SINCE, since);
		if (now - since < Math.round(sight.seconds() * 20.0F) || !teleportInSight(shulker, level, target)) {
			return false;
		}
		shulker.removeAttached(ModAttachments.BLIND_SINCE);
		return true;
	}

	/**
	 * Teleports the shulker to a place in 8 blocks that sees the target, with the checks of the vanilla teleport. The
	 * shulker keeps its target.
	 *
	 * @return true if the shulker found a place
	 */
	public static boolean teleportInSight(Shulker shulker, ServerLevel level, LivingEntity target) {
		var access = (io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker;
		BlockPos old = shulker.blockPosition();
		for (int i = 0; i < SIGHT_TRIES; i++) {
			BlockPos place = old.offset(net.minecraft.util.Mth.randomBetweenInclusive(shulker.getRandom(), -8, 8),
					net.minecraft.util.Mth.randomBetweenInclusive(shulker.getRandom(), -8, 8),
					net.minecraft.util.Mth.randomBetweenInclusive(shulker.getRandom(), -8, 8));
			if (place.getY() <= level.getMinY() || !level.isEmptyBlock(place) || !level.getWorldBorder().isWithinBounds(place)
					|| !level.noCollision(shulker, new net.minecraft.world.phys.AABB(place).deflate(1.0E-6))) {
				continue;
			}
			net.minecraft.core.Direction face = access.originofspawns$findAttachableSurface(place);
			if (face == null || level.getBlockState(place.relative(face))
					.is(net.minecraft.tags.BlockTags.SHULKER_DOES_NOT_TELEPORT_TO)) {
				continue;
			}
			net.minecraft.world.phys.Vec3 eyes = net.minecraft.world.phys.Vec3.atCenterOf(place);
			if (level.clip(new net.minecraft.world.level.ClipContext(eyes, target.getEyePosition(),
					net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE,
					shulker)).getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
				continue;
			}
			shulker.unRide();
			access.originofspawns$setAttachFace(face);
			shulker.playSound(net.minecraft.sounds.SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
			shulker.setPos(place.getX() + 0.5, place.getY(), place.getZ() + 0.5);
			level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.TELEPORT, old,
					net.minecraft.world.level.gameevent.GameEvent.Context.of(shulker));
			level.levelEvent(2016, old, net.minecraft.util.BlockUtil.packDifferenceInPosition(old, shulker.blockPosition(), 8, 8, 8));
			afterTeleport(shulker);
			return true;
		}
		return false;
	}

	/**
	 * Lets a hurt shulker split, after the chance of each split on hurt gene. The split is the vanilla split from a
	 * bullet. The shulker splits one time at most.
	 *
	 * @return true if a split gene acted. The vanilla split can still fail
	 */
	public static boolean splitOnHurt(Mob mob) {
		if (!(mob instanceof Shulker shulker) || !shulker.isAlive()) {
			return false;
		}
		for (GeneEffect effect : GeneEffects.of(shulker).toList()) {
			if (effect instanceof GeneEffect.SplitOnHurt split && shulker.getRandom().nextFloat() < split.chance()) {
				((io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker).originofspawns$hitByShulkerBullet();
				return true;
			}
		}
		return false;
	}

	/**
	 * The vanilla limit of the split of a shulker.
	 */
	public static final int VANILLA_SPLIT_LIMIT = 5;

	/**
	 * Returns the limit of the split of the shulker: the largest split limit gene, or the vanilla limit.
	 */
	public static int splitLimit(Shulker shulker) {
		int limit = VANILLA_SPLIT_LIMIT;
		for (GeneEffect effect : GeneEffects.of(shulker).toList()) {
			if (effect instanceof GeneEffect.SplitLimit split) {
				limit = Math.max(limit, split.limit());
			}
		}
		return limit;
	}
}
