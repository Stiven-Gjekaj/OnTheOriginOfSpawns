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

	/**
	 * Finds the moment when a player looks away from the creaking that the look held still. Then the look away genes act:
	 * the self effect after and speed after genes with the action {@code look_away}, and each blink strike.
	 *
	 * @return true if a player looked away now
	 */
	public static boolean tickLook(Creaking creaking) {
		boolean watched = watched(creaking);
		boolean before = creaking.getAttachedOrElse(ModAttachments.WATCHED, false);
		if (watched != before) {
			creaking.setAttached(ModAttachments.WATCHED, watched);
		}
		if (watched || !before) {
			return false;
		}
		GeneBehaviors.selfEffectAfter(creaking, GeneEffect.Action.LOOK_AWAY);
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.BlinkStrike blink && !creaking.level().getEntitiesOfClass(
					net.minecraft.world.entity.player.Player.class, creaking.getBoundingBox().inflate(blink.range()),
					player -> !player.isSpectator() && !player.isCreative()).isEmpty()) {
				creaking.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_COOLING_DOWN);
			}
		}
		return true;
	}

	/**
	 * Returns the creaking that the heart at the place protects now, or empty. Only the server knows it.
	 */
	public static java.util.Optional<Creaking> protector(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
		return level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.CreakingHeartBlockEntity heart
				? ((io.github.stivengjekaj.originofspawns.mixin.CreakingHeartBlockEntityAccessor) heart).originofspawns$getCreakingProtector()
				: java.util.Optional.empty();
	}

	/**
	 * Returns the product of the break time multipliers of the hard heart genes of the creaking.
	 */
	public static float heartBreakTime(Creaking creaking) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.HardHeart heart) {
				multiplier *= heart.breakTime();
			}
		}
		return multiplier;
	}

	/**
	 * Returns true if a hard heart gene of the creaking stops an explosion from breaking its heart.
	 */
	public static boolean heartBlastProof(Creaking creaking) {
		return GeneEffects.of(creaking).anyMatch(effect -> effect instanceof GeneEffect.HardHeart heart && heart.blastProof());
	}

	/**
	 * Runs the effects that act when the heart of the creaking breaks from a player or a blast, before the creaking dies.
	 */
	public static void onHeartBreak(Creaking creaking, net.minecraft.world.damagesource.DamageSource source) {
		if (!(creaking.level() instanceof net.minecraft.server.level.ServerLevel level)) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.HeartThorns thorns
					&& source.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
				player.hurtServer(level, level.damageSources().thorns(creaking), thorns.damage());
			} else if (effect instanceof GeneEffect.HeartBurst burst) {
				level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,
						net.minecraft.world.level.block.Blocks.PALE_OAK_WOOD.defaultBlockState()), creaking.getX(), creaking.getY(0.5),
						creaking.getZ(), 40, burst.radius() / 2.0, 0.5, burst.radius() / 2.0, 0.2);
				for (net.minecraft.world.entity.player.Player player : level.getEntitiesOfClass(
						net.minecraft.world.entity.player.Player.class, creaking.getBoundingBox().inflate(burst.radius()),
						player -> player.distanceTo(creaking) <= burst.radius())) {
					player.hurtServer(level, level.damageSources().mobAttack(creaking), burst.damage());
				}
			}
		}
	}

	/**
	 * The range from its heart in which a vanilla creaking walks.
	 */
	public static final double VANILLA_HEART_RANGE = 32.0;

	/**
	 * The smallest range from its heart.
	 */
	private static final double MIN_HEART_RANGE = 4.0;

	/**
	 * The heart lets its creaking go this many blocks past its walk range. Vanilla is 34 for 32.
	 */
	public static final double HEART_RANGE_SLACK = 2.0;

	/**
	 * Returns the range from its heart in which the creaking walks, after its heart leash genes.
	 */
	public static double heartRange(Creaking creaking) {
		double less = 0.0;
		for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
			if (effect instanceof GeneEffect.HeartLeash leash) {
				less += leash.less();
			}
		}
		return Math.max(MIN_HEART_RANGE, VANILLA_HEART_RANGE - less);
	}
}
