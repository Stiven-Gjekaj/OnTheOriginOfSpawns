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
	 * Returns true if the creaking must stay still after a look. A look that holds the awake creaking still starts the
	 * time again. After the look, a still after look gene holds the creaking still for its seconds.
	 *
	 * @param held true if the vanilla look check holds the creaking still
	 */
	public static boolean stillAfterLook(Creaking creaking, boolean held) {
		long now = creaking.level().getGameTime();
		if (held) {
			creaking.setAttached(ModAttachments.LOOK_TIME, now);
			return false;
		}
		if (!creaking.isActive()) {
			return false;
		}
		var seconds = GeneEffects.of(creaking).filter(GeneEffect.StillAfterLook.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.StillAfterLook) effect).seconds()).max();
		Long last = creaking.getAttached(ModAttachments.LOOK_TIME);
		return seconds.isPresent() && last != null && now - last < Math.round(seconds.getAsDouble() * 20.0);
	}

	/**
	 * Returns true if the eyes of the creaking do not glow: it has a dark eyes gene, it can move, and it does not die
	 * with its heart.
	 */
	public static boolean darkEyes(Creaking creaking) {
		return !creaking.isTearingDown() && creaking.canMove() && GeneCombat.has(creaking, GeneEffect.DarkEyes.class);
	}

	/**
	 * Plays a step sound of the creaking only to the players in the smallest range of its quiet steps genes.
	 *
	 * @return the players that hear the step, or null if the creaking has no quiet steps gene
	 */
	public static java.util.List<net.minecraft.server.level.ServerPlayer> quietStep(Creaking creaking,
			net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
		var blocks = GeneEffects.of(creaking).filter(GeneEffect.QuietSteps.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.QuietSteps) effect).blocks()).min();
		if (blocks.isEmpty() || !(creaking.level() instanceof net.minecraft.server.level.ServerLevel level)) {
			return null;
		}
		java.util.List<net.minecraft.server.level.ServerPlayer> players = level.getPlayers(
				player -> player.distanceTo(creaking) <= blocks.getAsDouble());
		if (creaking.isSilent()) {
			return java.util.List.of();
		}
		var packet = new net.minecraft.network.protocol.game.ClientboundSoundPacket(
				net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), creaking.getSoundSource(),
				creaking.getX(), creaking.getY(), creaking.getZ(), volume, pitch, level.getRandom().nextLong());
		for (net.minecraft.server.level.ServerPlayer player : players) {
			player.connection.send(packet);
		}
		return players;
	}

	/**
	 * Returns the square of the distance in which a look wakes the creaking: the largest range of its look wake range
	 * genes, or the vanilla value.
	 */
	public static double lookWakeRangeSqr(Creaking creaking, double vanilla) {
		var blocks = GeneEffects.of(creaking).filter(GeneEffect.LookWakeRange.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.LookWakeRange) effect).blocks()).max();
		return blocks.isPresent() ? blocks.getAsDouble() * blocks.getAsDouble() : vanilla;
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

	/**
	 * Sends the creaking back to its heart when a player comes near the heart, if the cooldown of the gene is over. An
	 * awake creaking stops its attack first.
	 *
	 * @return true if the creaking walks back now
	 */
	public static boolean heartGuard(Creaking creaking, GeneEffect.HeartGuard guard) {
		if (!playerCameToHeart(creaking, guard, guard.radius()) || !GeneCombat.ready(creaking, guard, guard.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(creaking, guard);
		if (creaking.isActive()) {
			creaking.deactivate();
		}
		creaking.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
				new net.minecraft.world.entity.ai.memory.WalkTarget(creaking.getHomePos(), guard.speed(), 1));
		return true;
	}

	/**
	 * Returns true if the creaking that the heart protects has a dark heart gene.
	 */
	public static boolean darkHeart(net.minecraft.world.level.block.entity.CreakingHeartBlockEntity heart) {
		return ((io.github.stivengjekaj.originofspawns.mixin.CreakingHeartBlockEntityAccessor) heart)
				.originofspawns$getCreakingProtector()
				.filter(creaking -> GeneEffects.of(creaking).anyMatch(GeneEffect.DarkHeart.class::isInstance)).isPresent();
	}

	/**
	 * Wakes a sleeping creaking at the nearest player in the radius of its heart.
	 *
	 * @return the player that woke the creaking, or null
	 */
	public static net.minecraft.world.entity.player.Player heartSense(Creaking creaking, GeneEffect.HeartSense sense) {
		if (creaking.isActive()) {
			return null;
		}
		net.minecraft.world.entity.player.Player nearest = playersNearHeart(creaking, sense.radius()).stream()
				.min(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(creaking))).orElse(null);
		if (nearest != null) {
			creaking.activate(nearest);
		}
		return nearest;
	}

	/**
	 * Wakes the sleeping creaking at the only player in the radius of the lone watch gene.
	 *
	 * @return the player that woke the creaking, or null
	 */
	public static net.minecraft.world.entity.player.Player loneWatch(Creaking creaking, GeneEffect.LoneWatch watch) {
		if (creaking.isActive()) {
			return null;
		}
		java.util.List<net.minecraft.world.entity.player.Player> players = creaking.level().getEntitiesOfClass(
				net.minecraft.world.entity.player.Player.class, creaking.getBoundingBox().inflate(watch.radius()),
				player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
						&& player.distanceTo(creaking) <= watch.radius());
		if (players.size() != 1) {
			return null;
		}
		creaking.activate(players.getFirst());
		return players.getFirst();
	}

	/**
	 * The distance from a creaking to its heart is 34 blocks at most, so a search for creakings round lit TNT adds it.
	 */
	private static final double HEART_REACH = 34.0;

	/**
	 * Wakes each creaking with a blast sense gene whose heart is in the radius of the lit TNT. Each creaking targets the
	 * nearest player in its follow range.
	 *
	 * @return the number of creakings that woke
	 */
	public static int onTntLit(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity tnt) {
		int woke = 0;
		for (Creaking creaking : level.getEntitiesOfClass(Creaking.class, tnt.getBoundingBox().inflate(64.0 + HEART_REACH),
				creaking -> creaking.isAlive() && creaking.getHomePos() != null)) {
			for (GeneEffect effect : GeneEffects.of(creaking).toList()) {
				if (effect instanceof GeneEffect.BlastSense sense
						&& Vec3.atCenterOf(creaking.getHomePos()).distanceTo(tnt.position()) <= sense.radius()) {
					net.minecraft.world.entity.player.Player player = level.getNearestPlayer(creaking.getX(), creaking.getY(),
							creaking.getZ(), creaking.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE),
							candidate -> candidate instanceof net.minecraft.world.entity.player.Player p && !p.isSpectator()
									&& !p.isCreative());
					if (player != null) {
						creaking.activate(player);
						woke++;
					}
					break;
				}
			}
		}
		return woke;
	}
}
