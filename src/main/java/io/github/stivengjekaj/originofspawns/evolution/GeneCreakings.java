package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.phys.Vec3;

/**
 * The genes of a creaking: its twitch while a player looks at it, its heart, its wake, and its stare.
 */
public final class GeneCreakings {
	private GeneCreakings() {
	}

	/**
	 * Returns true while a player looks at the awake creaking and holds it still.
	 */
	public static boolean watched(Creaking creaking) {
		return creaking.isActive() && !creaking.canMove();
	}

	/**
	 * Lets a watched creaking take one step toward its target, if the shortest interval of its watched step genes is over.
	 *
	 * @return true if the creaking took a step
	 */
	public static boolean watchedStep(Creaking creaking) {
		var interval = GeneEffects.of(creaking).filter(GeneEffect.WatchedStep.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.WatchedStep) effect).intervalSeconds()).min();
		LivingEntity target = creaking.getTarget();
		if (interval.isEmpty() || target == null || !watched(creaking)) {
			return false;
		}
		long now = creaking.level().getGameTime();
		long last = creaking.getAttachedOrElse(ModAttachments.STEP_TIME, Long.MIN_VALUE / 2);
		if (now - last < Math.round(interval.getAsDouble() * 20.0)) {
			return false;
		}
		Vec3 toTarget = target.position().subtract(creaking.position()).multiply(1.0, 0.0, 1.0);
		if (toTarget.lengthSqr() < 1.0) {
			return false;
		}
		creaking.setAttached(ModAttachments.STEP_TIME, now);
		creaking.move(MoverType.SELF, toTarget.normalize());
		return true;
	}

	/**
	 * Returns true while the creaking moves, also while a player looks at it.
	 */
	public static boolean free(Creaking creaking) {
		return creaking.getAttachedOrElse(ModAttachments.FREE_UNTIL, Long.MIN_VALUE) > creaking.level().getGameTime();
	}

	/**
	 * Lets the creaking move for the seconds, also while a player looks at it.
	 */
	public static void freeFor(Creaking creaking, float seconds) {
		long until = creaking.level().getGameTime() + Math.round(seconds * 20.0F);
		creaking.setAttached(ModAttachments.FREE_UNTIL, Math.max(until, creaking.getAttachedOrElse(ModAttachments.FREE_UNTIL, 0L)));
	}

	/**
	 * Runs the effects that act when a player hurts the creaking: each twitch on hurt gene whose cooldown is over lets it
	 * move for a short time.
	 */
	public static void onPlayerHurt(Creaking creaking) {
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.TwitchOnHurt twitch && GeneCombat.ready(creaking, twitch, twitch.cooldownSeconds())) {
				GeneCombat.startCooldown(creaking, twitch);
				freeFor(creaking, twitch.seconds());
			}
		}
	}

	/**
	 * Returns the players in the radius of the heart of the creaking that a creaking can see: not in creative or
	 * spectator mode. A creaking with no heart has none.
	 */
	public static java.util.List<net.minecraft.world.entity.player.Player> playersNearHeart(Creaking creaking, double radius) {
		net.minecraft.core.BlockPos home = creaking.getHomePos();
		if (home == null) {
			return java.util.List.of();
		}
		Vec3 center = Vec3.atCenterOf(home);
		return creaking.level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,
				new net.minecraft.world.phys.AABB(home).inflate(radius), player -> player.isAlive() && !player.isSpectator()
						&& !player.isCreative() && player.position().distanceTo(center) <= radius);
	}

	/**
	 * Returns true when a player came in the radius of the heart for the gene since the last check. The creaking keeps the
	 * state of each gene.
	 */
	public static boolean playerCameToHeart(Creaking creaking, GeneEffect effect, double radius) {
		boolean near = !playersNearHeart(creaking, radius).isEmpty();
		java.util.Map<GeneEffect, Boolean> visited = new java.util.HashMap<>(
				creaking.getAttachedOrElse(ModAttachments.HEART_VISITED, java.util.Map.of()));
		boolean before = visited.getOrDefault(effect, false);
		visited.put(effect, near);
		creaking.setAttached(ModAttachments.HEART_VISITED, visited);
		return near && !before;
	}

	/**
	 * Lets the creaking move for a short time when a player comes near its heart, if the cooldown of the gene is over.
	 *
	 * @return true if the creaking got the move now
	 */
	public static boolean heartTwitch(Creaking creaking, GeneEffect.HeartTwitch twitch) {
		if (!playerCameToHeart(creaking, twitch, twitch.radius()) || !GeneCombat.ready(creaking, twitch, twitch.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(creaking, twitch);
		freeFor(creaking, twitch.seconds());
		return true;
	}

	/**
	 * Returns true if the look of the player does not stop the awake creaking, after its look genes.
	 */
	public static boolean ignoresLook(Creaking creaking, net.minecraft.world.entity.player.Player player) {
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.IgnoreFarLookInDark dark && player.distanceTo(creaking) > dark.distance()
					&& creaking.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, creaking.blockPosition())
							<= dark.maxLight()) {
				return true;
			}
			if (effect instanceof GeneEffect.IgnoreShieldLook && player.isBlocking()) {
				return true;
			}
			if (effect instanceof GeneEffect.IgnoreLookAfterHit fear) {
				Long hit = creaking.getAttachedOrElse(ModAttachments.CREAKING_HITS, java.util.Map.of()).get(player.getUUID());
				if (hit != null && creaking.level().getGameTime() - hit <= Math.round(fear.seconds() * 20.0F)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Keeps the time of a hit of a creaking with a look gene on a player.
	 */
	public static void onHit(Creaking creaking, net.minecraft.world.entity.Entity target) {
		if (target instanceof net.minecraft.world.entity.player.Player player
				&& GeneEffects.of(creaking).anyMatch(GeneEffect.IgnoreLookAfterHit.class::isInstance)) {
			java.util.Map<java.util.UUID, Long> hits = new java.util.HashMap<>(
					creaking.getAttachedOrElse(ModAttachments.CREAKING_HITS, java.util.Map.of()));
			hits.put(player.getUUID(), creaking.level().getGameTime());
			creaking.setAttached(ModAttachments.CREAKING_HITS, hits);
		}
	}
}
