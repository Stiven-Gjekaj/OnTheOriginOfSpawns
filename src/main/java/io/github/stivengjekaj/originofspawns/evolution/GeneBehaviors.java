package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.LurkGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

/**
 * The effects of genes and families that change behavior: goals, effects each tick, spawns, and sounds.
 */
public final class GeneBehaviors {
	/**
	 * The blocks that a burrowing mob can rise through.
	 */

	private static final int BURROW_DEPTH = 2;
	private static final float CHICKEN_JOCKEY_CHANCE = 0.05F;
	private static final int TICK_INTERVAL = 20;
	/**
	 * The speed that swim vertical adds for each point of its down multiplier above 1, each tick.
	 */
	private static final double SINK_STEP = 0.01;
	private static final double PULL_STEP = 0.25;
	private static final double HOLD_DISTANCE = 1.5;

	private GeneBehaviors() {
	}

	/**
	 * Adds the goals of the effects to a mob. The game does not save goals, so this runs each time the mob loads.
	 */
	public static void installGoals(Mob mob) {
		freeFromRise(mob);
		var goals = ((MobAccessor) mob).originofspawns$goalSelector();
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.Leap leap -> goals.addGoal(1, new LeapGoal(mob, leap));
				case GeneEffect.Lurk lurk -> goals.addGoal(0, new LurkGoal(mob, lurk));
				case GeneEffect.Siege siege -> {
					if (mob instanceof Zombie zombie) {
						zombie.setCanBreakDoors(true);
					}
					goals.addGoal(1, new BreakDoorGoal(mob, difficulty -> true));
					goals.addGoal(1, new SiegeGoal(mob, siege));
				}
				case GeneEffect.Glowing glowing when glowing.when() == GeneEffect.When.ALWAYS -> mob.setGlowingTag(true);
				case GeneEffect.NoWeaponDrop noDrop -> mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
				default -> {
				}
			}
		}
	}

	/**
	 * Runs the effects that act one time, when a mob first spawns with its genes.
	 */
	public static void onSpawn(Mob mob, ServerLevel level, EntitySpawnReason reason) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.PickUpItems pickUp -> mob.setCanPickUpLoot(true);
				case GeneEffect.TridentChance trident -> rollTrident(mob, trident);
				case GeneEffect.Runt runt when mob instanceof Zombie zombie && mob.getRandom().nextFloat() < runt.chance() ->
						makeRunt(zombie, level, runt);
				case GeneEffect.Brood brood when reason == EntitySpawnReason.NATURAL -> spawnBrood(mob, level, brood);
				case GeneEffect.Burrow burrow when reason == EntitySpawnReason.NATURAL -> burrow(mob, level, burrow);
				default -> {
				}
			}
		}
	}

	/**
	 * Gives the mob a trident with the chance of the effect. A trident takes the place of an empty hand or of a
	 * fishing rod, so that the chance adds to the vanilla chance.
	 */
	private static void rollTrident(Mob mob, GeneEffect.TridentChance trident) {
		ItemStack hand = mob.getMainHandItem();
		if ((hand.isEmpty() || hand.is(Items.FISHING_ROD)) && mob.getRandom().nextFloat() < trident.chance()) {
			mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
		}
	}

	/**
	 * Runs the spawn effects of a gene that a living mob gains, when they also fit a mob that already lives.
	 * Effects that only fit a new mob, such as a baby, a brood, or a burrow, do not run.
	 */
	public static void onGain(Mob mob, List<GeneEffect> effects) {
		for (GeneEffect effect : effects) {
			switch (effect) {
				case GeneEffect.PickUpItems pickUp -> mob.setCanPickUpLoot(true);
				case GeneEffect.TridentChance trident -> rollTrident(mob, trident);
				default -> {
				}
			}
		}
	}

	/**
	 * Undoes the lasting changes of the effects that a mob loses: door breaking and item pickup.
	 */
	public static void onLoss(Mob mob, List<GeneEffects.Source> before, List<GeneEffects.Source> after) {
		if (has(before, GeneEffect.Siege.class) && !has(after, GeneEffect.Siege.class)) {
			((MobAccessor) mob).originofspawns$goalSelector().removeAllGoals(goal -> goal instanceof BreakDoorGoal);
			if (mob instanceof Zombie zombie) {
				zombie.setCanBreakDoors(false);
			}
		}
		if (has(before, GeneEffect.PickUpItems.class) && !has(after, GeneEffect.PickUpItems.class)
				&& mob.getType().builtInRegistryHolder().is(Evolution.NO_ITEM_PICKUP)) {
			mob.setCanPickUpLoot(false);
		}
	}

	private static boolean has(List<GeneEffects.Source> sources, Class<? extends GeneEffect> type) {
		return sources.stream().flatMap(source -> source.effects().stream()).anyMatch(type::isInstance);
	}

	private static final Identifier RUNT_HEALTH = OriginOfSpawns.id("runt_health");

	private static void makeRunt(Zombie zombie, ServerLevel level, GeneEffect.Runt runt) {
		zombie.setBaby(true);
		AttributeInstance health = zombie.getAttribute(Attributes.MAX_HEALTH);
		if (health != null && !health.hasModifier(RUNT_HEALTH)) {
			health.addPermanentModifier(new AttributeModifier(RUNT_HEALTH,
					runt.babyHealthMultiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			zombie.setHealth(zombie.getMaxHealth());
		}
		if (zombie.getRandom().nextFloat() < CHICKEN_JOCKEY_CHANCE) {
			Chicken chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.JOCKEY);
			if (chicken != null) {
				chicken.snapTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), 0.0F);
				chicken.setChickenJockey(true);
				level.addFreshEntity(chicken);
				zombie.startRiding(chicken);
			}
		}
	}

	private static void spawnBrood(Mob mob, ServerLevel level, GeneEffect.Brood brood) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		for (int i = 0; i < brood.extra(); i++) {
			if (!(mob.getType().create(level, EntitySpawnReason.REINFORCEMENT) instanceof Mob extra)) {
				return;
			}
			extra.snapTo(mob.getX() + mob.getRandom().nextInt(3) - 1, mob.getY(), mob.getZ() + mob.getRandom().nextInt(3) - 1,
					mob.getRandom().nextFloat() * 360.0F, 0.0F);
			// The brood has the genes of the first mob. Its genes are set before it joins, so it does not roll new ones.
			extra.setAttached(ModAttachments.GENES, genes);
			GeneEffects.applyAttributes(extra, GeneEffects.sources(level.registryAccess(), genes));
			level.addFreshEntity(extra);
			// The extra mob joins with its genes, so the load does not run its spawn effects. They run here.
			onSpawn(extra, level, EntitySpawnReason.REINFORCEMENT);
		}
	}

	/**
	 * Puts the mob under the ground near a player, so that it rises. With near player, the mob first moves next to
	 * the nearest player.
	 *
	 * @return true if the mob went under the ground
	 */
	public static boolean burrow(Mob mob, ServerLevel level, GeneEffect.Burrow burrow) {
		Player player = level.getNearestPlayer(mob, burrow.playerRadius());
		if (burrow.nightOnly() && level.isBrightOutside() || player == null) {
			return false;
		}
		BlockPos feet = burrow.nearPlayer() ? spotNear(mob, level, player.blockPosition(), burrow) : mob.blockPosition();
		if (feet == null || !canBurrow(level, feet, burrow)) {
			return false;
		}
		if (burrow.nearPlayer()) {
			mob.setPos(feet.getX() + 0.5, feet.getY() - BURROW_DEPTH, feet.getZ() + 0.5);
		} else {
			mob.setPos(mob.getX(), mob.getY() - BURROW_DEPTH, mob.getZ());
		}
		mob.noPhysics = true;
		mob.setNoGravity(true);
		mob.setAttached(ModAttachments.STATE, mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE)
				.withRiseTicks(Math.max(1, Math.round(burrow.riseSeconds() * 20.0F))));
		return true;
	}

	/**
	 * Brings a burrowing mob up to the surface when its rise stopped, for example because its chunk unloaded.
	 * The game does not save the rise, so a mob that loads with no gravity inside the ground is stuck.
	 */
	public static void freeFromRise(Mob mob) {
		if (!mob.isNoGravity() || mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE).riseTicks() > 0
				|| !GeneCombat.has(mob, GeneEffect.Burrow.class) || mob.level().noCollision(mob)) {
			return;
		}
		for (int up = 1; up <= BURROW_DEPTH + 1; up++) {
			double y = Math.floor(mob.getY()) + up;
			if (mob.level().noCollision(mob, mob.getBoundingBox().move(0.0, y - mob.getY(), 0.0))) {
				mob.setPos(mob.getX(), y, mob.getZ());
				break;
			}
		}
		mob.noPhysics = false;
		mob.setNoGravity(false);
	}

	private static boolean canBurrow(ServerLevel level, BlockPos feet, GeneEffect.Burrow burrow) {
		for (int depth = 1; depth <= BURROW_DEPTH; depth++) {
			if (!level.getBlockState(feet.below(depth)).is(burrow.blocks())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds a place next to the player where the mob can rise: the ground is of the burrow blocks, and the mob fits.
	 */
	private static BlockPos spotNear(Mob mob, ServerLevel level, BlockPos center, GeneEffect.Burrow burrow) {
		for (int distance = 1; distance <= 2; distance++) {
			for (int dx = -distance; dx <= distance; dx++) {
				for (int dz = -distance; dz <= distance; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) {
						continue;
					}
					for (int dy = 1; dy >= -1; dy--) {
						BlockPos feet = center.offset(dx, dy, dz);
						if (canBurrow(level, feet, burrow) && level.noCollision(mob, mob.getDimensions(mob.getPose())
								.makeBoundingBox(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5))) {
							return feet;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Runs the behavior effects each tick, on the server.
	 */
	public static void tick(Mob mob, ServerLevel level) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		if (state.riseTicks() > 0) {
			rise(mob, level, state);
			return;
		}
		if (level.getGameTime() < state.stunUntil()) {
			mob.getNavigation().stop();
			mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
		}
		List<GeneEffect> effects = GeneEffects.of(mob).toList();
		updateSpeed(mob, effects);
		for (GeneEffect effect : effects) {
			switch (effect) {
				case GeneEffect.WaterPull pull -> waterPull(mob, level, pull);
				case GeneEffect.SwimVertical swim -> swimVertical(mob, swim);
				case GeneEffect.DragUnder drag -> drag(mob, level, drag);
				default -> {
				}
			}
		}
		if (mob.tickCount % TICK_INTERVAL != 0) {
			return;
		}
		long now = level.getGameTime();
		for (GeneEffect effect : effects) {
			switch (effect) {
				case GeneEffect.Regenerate regenerate -> {
					long combat = mob.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).combatTime();
					boolean idle = combat < 0L || now - combat >= Math.round(regenerate.idleSeconds() * 20.0F);
					if (idle && GeneCombat.test(mob, regenerate.when())
							&& now % Math.max(1, Math.round(regenerate.intervalSeconds() * 20.0F)) < TICK_INTERVAL) {
						mob.heal(regenerate.amount());
					}
				}
				case GeneEffect.Aura aura when now % Math.max(1, Math.round(aura.intervalSeconds() * 20.0F)) < TICK_INTERVAL -> {
					AABB area = mob.getBoundingBox().inflate(aura.radius());
					for (Player player : level.getEntitiesOfClass(Player.class, area,
							player -> !aura.inWaterOnly() || player.isInWater())) {
						// With no time of its own, the effect lasts until the next renewal, so that it does not end.
						int ticks = aura.seconds().map(seconds -> Math.round(seconds * 20.0F))
								.orElse(Math.round(aura.intervalSeconds() * 20.0F) + TICK_INTERVAL);
						player.addEffect(new MobEffectInstance(aura.effect(), ticks, aura.amplifier()), mob);
					}
					if (aura.particles()) {
						level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY(0.5), mob.getZ(),
								4, 0.4, 0.5, 0.4, 0.0);
					}
				}
				case GeneEffect.AttractGolems attract -> attractGolems(mob, level, attract);
				case GeneEffect.Sidestep sidestep -> sidestep(mob, level, sidestep);
				case GeneEffect.ThrowEffect thrown -> throwEffect(mob, level, thrown);
				case GeneEffect.CallOnSight call when mob.getTarget() instanceof Player player -> callOnSight(mob, level, call, player);
				case GeneEffect.FrenziedTargeting frenzy
						when now % Math.max(1, Math.round(frenzy.intervalSeconds() * 20.0F)) < TICK_INTERVAL
								&& mob.getRandom().nextFloat() < frenzy.chance() -> {
					AABB area = mob.getBoundingBox().inflate(frenzy.radius());
					// A zombie does not target its kin, such as husks and drowned.
					level.getEntitiesOfClass(Mob.class, area, other -> other.getType() != mob.getType() && other.isAlive()
							&& !(mob instanceof Zombie && other instanceof Zombie))
							.stream().min((a, b) -> Double.compare(a.distanceToSqr(mob), b.distanceToSqr(mob)))
							.ifPresent(mob::setTarget);
				}
				case GeneEffect.Hunt hunt -> hunt(mob, level, hunt);
				case GeneEffect.Lure lure -> lure(mob, level, lure);
				case GeneEffect.HarmWhen harm when now % Math.max(1, Math.round(harm.intervalSeconds() * 20.0F)) < TICK_INTERVAL
						&& GeneCombat.test(mob, harm.when()) -> mob.hurtServer(level, level.damageSources().dryOut(), harm.amount());
				case GeneEffect.ScavengeTridents scavenge when mob.getMainHandItem().isEmpty() ->
						scavengeTrident(mob, level, scavenge);
				case GeneEffect.Glowing glowing when glowing.when() != GeneEffect.When.ALWAYS ->
						mob.setGlowingTag(GeneCombat.test(mob, glowing.when()));
				default -> {
				}
			}
		}
	}

	/**
	 * Pulls the players in water near the mob a small step toward it.
	 */
	public static void waterPull(Mob mob, ServerLevel level, GeneEffect.WaterPull pull) {
		AABB area = mob.getBoundingBox().inflate(pull.radius());
		// A player in creative or spectator mode is invulnerable, and the pull does not move it.
		for (Player player : level.getEntitiesOfClass(Player.class, area, player -> player.isInWater()
				&& !player.getAbilities().invulnerable && player.distanceTo(mob) <= pull.radius())) {
			Vec3 toMob = mob.position().subtract(player.position());
			if (toMob.lengthSqr() > 1.0) {
				player.push(toMob.normalize().scale(pull.strength()));
				player.syncVelocity = true;
			}
		}
	}

	/**
	 * Changes the vertical speed of a mob in water. It sinks faster, and it swims up slower.
	 */
	public static void swimVertical(Mob mob, GeneEffect.SwimVertical swim) {
		if (!mob.isInWater()) {
			return;
		}
		Vec3 speed = mob.getDeltaMovement();
		double y = speed.y > 0.0 ? speed.y * Math.min(swim.up(), 1.0F) : speed.y - SINK_STEP * Math.max(swim.down() - 1.0F, 0.0F);
		mob.setDeltaMovement(speed.x, y, speed.z);
	}

	/**
	 * Takes a trident that a player threw near the mob into its empty hand.
	 *
	 * @return true if the mob took a trident
	 */
	public static boolean scavengeTrident(Mob mob, ServerLevel level, GeneEffect.ScavengeTridents scavenge) {
		AABB area = mob.getBoundingBox().inflate(scavenge.radius());
		for (ThrownTrident trident : level.getEntitiesOfClass(ThrownTrident.class, area,
				trident -> trident.isAlive() && trident.getOwner() instanceof Player)) {
			ItemStack stack = trident.getPickupItemStackOrigin().copy();
			if (!stack.isEmpty()) {
				mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
				// The mob keeps the trident of a player, so it must not despawn.
				mob.setPersistenceRequired();
				if (GeneCombat.has(mob, GeneEffect.NoWeaponDrop.class)) {
					mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
				}
				trident.discard();
				return true;
			}
		}
		return false;
	}

	/**
	 * Puts the lure of the mob out when a player comes near, and shows it again after the reset time.
	 * The lure that goes out arms the next hit of the mob.
	 */
	public static void lure(Mob mob, ServerLevel level, GeneEffect.Lure lure) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		boolean lit = state.lureNearTime() < 0L || now - state.lureNearTime() >= Math.round(lure.resetSeconds() * 20.0F);
		boolean near = level.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), lure.radius(),
				EntitySelector.NO_SPECTATORS) != null;
		if (near) {
			mob.setAttached(ModAttachments.STATE, state.withLure(now, state.lureArmed() || lit));
		} else if (lit) {
			Vec3 light = mob.getEyePosition().add(mob.getLookAngle().scale(0.6)).add(0.0, 0.3, 0.0);
			level.sendParticles(ParticleTypes.GLOW, light.x, light.y, light.z, 3, 0.05, 0.05, 0.05, 0.0);
		}
	}

	/**
	 * Returns how well the looker sees the target, after the senses of the looker.
	 *
	 * @param visibility the visibility in vanilla, from 0 to 1
	 */
	public static double visibility(LivingEntity target, Entity looker, double visibility) {
		double result = visibility;
		double distance = target.distanceTo(looker);
		for (GeneEffect effect : GeneEffects.of(looker).toList()) {
			switch (effect) {
				case GeneEffect.SeeInvisible see when target.isInvisible() && distance <= see.radius() ->
						result = Math.max(result, 1.0);
				case GeneEffect.SneakBlind blind when target.isCrouching() && distance > blind.radius() -> {
					return 0.0;
				}
				default -> {
				}
			}
		}
		return result;
	}

	/**
	 * Returns the attack interval of a ranged goal after the throw rates of the mob.
	 */
	public static int scaleThrowInterval(Mob mob, int ticks) {
		float rate = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ThrowRate throwRate) {
				rate *= throwRate.multiplier();
			}
		}
		return rate == 1.0F ? ticks : Math.max(1, Math.round(ticks / rate));
	}

	/**
	 * Returns the product of the throw range multipliers of the mob for the target.
	 */
	public static float throwRangeMultiplier(Mob mob, LivingEntity target) {
		if (target == null) {
			return 1.0F;
		}
		boolean wet = target.isInWater() || target.getVehicle() instanceof AbstractBoat;
		GeneEffect.ThrowRange.Target kind = wet ? GeneEffect.ThrowRange.Target.WET : GeneEffect.ThrowRange.Target.DRY;
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ThrowRange range && range.target() == kind) {
				multiplier *= range.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Moves the entity down, up to the number of blocks. A block stops the move.
	 */
	public static void pullDown(LivingEntity entity, float blocks) {
		double moved = 0.0;
		while (moved + PULL_STEP <= blocks && entity.level().noCollision(entity,
				entity.getBoundingBox().move(0.0, -(moved + PULL_STEP), 0.0))) {
			moved += PULL_STEP;
		}
		if (moved > 0.0) {
			entity.teleportTo(entity.getX(), entity.getY() - moved, entity.getZ());
		}
	}

	/**
	 * Sinks the mob and the player that it holds. The drag stops when the time ends or the player leaves the water.
	 */
	private static void drag(Mob mob, ServerLevel level, GeneEffect.DragUnder drag) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		if (state.dragTarget() < 0) {
			return;
		}
		if (level.getGameTime() >= state.dragUntil() || !(level.getEntity(state.dragTarget()) instanceof LivingEntity held)
				|| !held.isAlive() || !held.isInWater()) {
			mob.setAttached(ModAttachments.STATE, state.withDrag(-1, -1L));
			return;
		}
		Vec3 sink = new Vec3(0.0, -drag.sinkSpeed(), 0.0);
		held.setDeltaMovement(sink);
		held.syncVelocity = true;
		mob.setDeltaMovement(sink);
		if (mob.distanceTo(held) > HOLD_DISTANCE) {
			mob.setPos(held.getX(), held.getY(), held.getZ());
		}
	}

	/**
	 * Moves the mob to the side when a player looks at it from far away. It moves only to a safe place.
	 */
	public static boolean sidestep(Mob mob, ServerLevel level, GeneEffect.Sidestep sidestep) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		if (state.sidestepTime() >= 0L && now - state.sidestepTime() < Math.round(sidestep.cooldownSeconds() * 20.0F)) {
			return false;
		}
		for (Player player : level.players()) {
			if (player.isSpectator() || !LurkGoal.isWatchedFromAfar(mob, player, sidestep.range())) {
				continue;
			}
			// The side is at a right angle to the line from the player to the mob.
			Vec3 toMob = mob.position().subtract(player.position()).multiply(1.0, 0.0, 1.0).normalize();
			Vec3 side = new Vec3(-toMob.z, 0.0, toMob.x).scale(mob.getRandom().nextBoolean() ? 1.0 : -1.0);
			for (Vec3 direction : List.of(side, side.scale(-1.0))) {
				Vec3 target = mob.position().add(direction.scale(sidestep.distance()));
				for (int dy = 1; dy >= -2; dy--) {
					BlockPos feet = BlockPos.containing(target.x, mob.getY() + dy, target.z);
					if (isSafe(mob, level, feet)) {
						level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY(0.5), mob.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
						mob.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
						mob.setAttached(ModAttachments.STATE, state.withSidestepTime(now));
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}

	private static boolean isSafe(Mob mob, ServerLevel level, BlockPos feet) {
		if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
				|| !level.getFluidState(feet).isEmpty() || !level.getFluidState(feet.below()).isEmpty()) {
			return false;
		}
		return level.noCollision(mob, mob.getDimensions(mob.getPose()).makeBoundingBox(
				feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5));
	}

	/**
	 * Throws at the target of the mob, which gives the target a status effect.
	 */
	public static boolean throwEffect(Mob mob, ServerLevel level, GeneEffect.ThrowEffect thrown) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) > thrown.range() || !mob.hasLineOfSight(target)
				|| state.throwTime() >= 0L && now - state.throwTime() < Math.round(thrown.cooldownSeconds() * 20.0F)) {
			return false;
		}
		target.addEffect(new MobEffectInstance(thrown.effect(), Math.round(thrown.seconds() * 20.0F), thrown.amplifier()), mob);
		Vec3 from = mob.getEyePosition();
		Vec3 step = target.getEyePosition().subtract(from).scale(0.1);
		for (int i = 0; i <= 10; i++) {
			Vec3 point = from.add(step.scale(i));
			level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
					point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.0);
		}
		mob.setAttached(ModAttachments.STATE, state.withThrowTime(now));
		return true;
	}

	/**
	 * Turns the iron golems near the mob to the mob, unless they already attack a mob that attracts golems.
	 */
	public static void attractGolems(Mob mob, ServerLevel level, GeneEffect.AttractGolems attract) {
		for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, mob.getBoundingBox().inflate(attract.radius()),
				IronGolem::isAlive)) {
			LivingEntity target = golem.getTarget();
			boolean busy = target != null && target.isAlive() && GeneCombat.has(target, GeneEffect.AttractGolems.class);
			if (!busy && golem.canAttack(mob)) {
				golem.setTarget(mob);
			}
		}
	}

	private static void callOnSight(Mob mob, ServerLevel level, GeneEffect.CallOnSight call, Player player) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		if (state.callTime() >= 0L && now - state.callTime() < Math.round(call.cooldownSeconds() * 20.0F)) {
			return;
		}
		AABB area = mob.getBoundingBox().inflate(call.radius());
		for (Mob ally : level.getEntitiesOfClass(Mob.class, area, other -> other.getType() == mob.getType())) {
			ally.setTarget(player);
		}
		mob.setAttached(ModAttachments.STATE, state.withCallTime(now));
	}

	private static void rise(Mob mob, ServerLevel level, BehaviorState state) {
		GeneEffect.Burrow burrow = GeneEffects.of(mob).filter(GeneEffect.Burrow.class::isInstance)
				.map(GeneEffect.Burrow.class::cast).findFirst().orElse(new GeneEffect.Burrow(24.0F, 2.0F));
		int total = Math.max(1, Math.round(burrow.riseSeconds() * 20.0F));
		int left = state.riseTicks() - 1;
		// Each tick moves the same part of the depth, so the mob ends on the surface.
		mob.setPos(mob.getX(), mob.getY() + (double) BURROW_DEPTH / total, mob.getZ());
		mob.setDeltaMovement(0.0, 0.0, 0.0);
		BlockPos surface = BlockPos.containing(mob.getX(), mob.getY() + BURROW_DEPTH - 0.5, mob.getZ());
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(surface.below())),
				mob.getX(), surface.getY(), mob.getZ(), 4, 0.3, 0.1, 0.3, 0.0);
		if (left <= 0) {
			mob.noPhysics = false;
			mob.setNoGravity(false);
			mob.setPos(mob.getX(), Math.round(mob.getY()), mob.getZ());
			long stunUntil = level.getGameTime() + Math.round(burrow.stunSeconds() * 20.0F);
			mob.setAttached(ModAttachments.STATE, state.withRiseTicks(0).withStunUntil(stunUntil));
			return;
		}
		mob.setAttached(ModAttachments.STATE, state.withRiseTicks(left));
	}

	private static void hunt(Mob mob, ServerLevel level, GeneEffect.Hunt hunt) {
		if (mob.getTarget() instanceof Player) {
			return;
		}
		double follow = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
		double range = Math.max(hunt.invisibleRadius(), follow * hunt.woundedRangeMultiplier());
		AABB area = mob.getBoundingBox().inflate(range);
		for (Player player : level.getEntitiesOfClass(Player.class, area, Player::isAlive)) {
			if (!player.isCreative() && !player.isSpectator() && isHunted(mob, player, hunt)) {
				mob.setTarget(player);
				return;
			}
		}
	}

	/**
	 * Tells if a hunting mob finds the player: invisible near it, or wounded in its range.
	 */
	public static boolean isHunted(Mob mob, Player player, GeneEffect.Hunt hunt) {
		double follow = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
		double distance = player.distanceTo(mob);
		boolean invisible = player.isInvisible() && distance <= hunt.invisibleRadius();
		boolean wounded = player.getHealth() < player.getMaxHealth() / 2.0F
				&& distance <= follow * hunt.woundedRangeMultiplier();
		return invisible || wounded;
	}

	private static void updateSpeed(Mob mob, List<GeneEffect> effects) {
		int speedIndex = 0;
		int attributeIndex = 0;
		for (GeneEffect effect : effects) {
			if (effect instanceof GeneEffect.SpeedWhen when) {
				toggle(mob, mob.getAttribute(Attributes.MOVEMENT_SPEED), OriginOfSpawns.id("speed_when/" + speedIndex++),
						GeneCombat.test(mob, when.when()), when.multiplier() - 1.0,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			} else if (effect instanceof GeneEffect.AttributeWhen when) {
				toggle(mob, mob.getAttribute(when.attribute()), OriginOfSpawns.id("attribute_when/" + attributeIndex++),
						GeneCombat.test(mob, when.when()), when.amount(), when.operation());
			}
		}
	}

	/**
	 * Adds a short-lived modifier while a state is true, and removes it when the state is false.
	 */
	private static void toggle(Mob mob, AttributeInstance attribute, Identifier id, boolean active, double amount,
			AttributeModifier.Operation operation) {
		if (attribute == null) {
			return;
		}
		if (active && !attribute.hasModifier(id)) {
			attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
		} else if (!active && attribute.hasModifier(id)) {
			attribute.removeModifier(id);
		}
	}

	/**
	 * Returns the product of the attack interval multipliers of the mob.
	 */
	public static float attackIntervalMultiplier(LivingEntity mob) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.AttackInterval interval) {
				multiplier *= interval.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Returns the silence effect of the mob, or null if it has none. Two silence effects join.
	 */
	public static GeneEffect.Silence silence(LivingEntity mob) {
		GeneEffect.Silence result = null;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.Silence silence) {
				result = result == null ? silence : new GeneEffect.Silence(result.idle() || silence.idle(),
						result.hurt() || silence.hurt(), result.steps() || silence.steps(), result.death() || silence.death());
			}
		}
		return result;
	}

	public static float volumeMultiplier(LivingEntity mob) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.Loud loud) {
				multiplier *= loud.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * A player that a mob with raise victim killed rises as a mob of its species, with its genes and the head of
	 * the player.
	 */
	public static void raiseVictim(Mob killer, Player victim, ServerLevel level) {
		if (!GeneCombat.has(killer, GeneEffect.RaiseVictim.class)
				|| !(killer.getType().create(level, EntitySpawnReason.CONVERSION) instanceof Mob raised)) {
			return;
		}
		CarriedGenes genes = killer.getAttachedOrElse(ModAttachments.GENES, CarriedGenes.NONE);
		raised.snapTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), 0.0F);
		raised.setAttached(ModAttachments.GENES, genes);
		GeneEffects.applyAttributes(raised, GeneEffects.sources(level.registryAccess(), genes));
		ItemStack head = new ItemStack(Items.PLAYER_HEAD);
		head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(victim.getGameProfile()));
		raised.setItemSlot(EquipmentSlot.HEAD, head);
		raised.setDropChance(EquipmentSlot.HEAD, 0.0F);
		raised.setPersistenceRequired();
		level.addFreshEntity(raised);
	}
}
