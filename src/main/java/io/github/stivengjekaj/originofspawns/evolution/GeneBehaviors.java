package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.mixin.VexAccessor;
import io.github.stivengjekaj.originofspawns.mixin.WitchAccessor;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.LurkGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeDoorGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.AbstractSkeletonAccessor;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import io.github.stivengjekaj.originofspawns.mixin.RangedBowAttackGoalAccessor;
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
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.RangedAttackMob;
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
	 * The color of the star of the rocket of a mob.
	 */
	private static final int RED_STAR = 0xB3312C;
	/**
	 * A sack holds as many stacks as the inventory of a pillager.
	 */
	private static final int SACK_SLOTS = 5;
	/**
	 * The speed of a rocket that a mob shoots, the same as from a crossbow.
	 */
	private static final float ROCKET_SPEED = 1.6F;
	/**
	 * A mob that moves faster than this, in blocks each tick, walks.
	 */
	private static final double WALK_SPEED_SQR = 1.0E-4;
	/**
	 * The look tolerance of the enderman.
	 */
	private static final double GAZE_TOLERANCE = 0.025;
	/**
	 * A player aims at a mob when its view is in about 14 degrees of the mob.
	 */
	private static final double AIM_COS = 0.97;
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
		if (GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.Silence silence && silence.all())) {
			mob.setSilent(true);
		}
		var goals = ((MobAccessor) mob).originofspawns$goalSelector();
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.Leap leap -> goals.addGoal(1, new LeapGoal(mob, leap));
				case GeneEffect.Lurk lurk -> goals.addGoal(0, new LurkGoal(mob, lurk));
				case GeneEffect.Siege siege -> {
					if (mob instanceof Zombie zombie) {
						zombie.setCanBreakDoors(true);
					}
					goals.addGoal(1, new SiegeDoorGoal(mob, Math.round(siege.doorSeconds() * 20.0F)));
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
		if (!mob.hasAttached(ModAttachments.HOME)) {
			mob.setAttached(ModAttachments.HOME, mob.blockPosition());
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.PickUpItems pickUp -> mob.setCanPickUpLoot(true);
				case GeneEffect.SpawnEnchantment enchant when !mob.getMainHandItem().isEmpty() ->
						level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
								.get(enchant.enchantment()).ifPresent(holder -> mob.getMainHandItem().enchant(holder, enchant.level()));
				case GeneEffect.TridentChance trident -> rollTrident(mob, trident);
				case GeneEffect.Runt runt when mob.getRandom().nextFloat() < runt.chance() -> makeRunt(mob, level, runt);
				case GeneEffect.Brood brood when broodReason(reason) ->
						spawnBrood(mob, level, brood.extra(), EntitySpawnReason.REINFORCEMENT, brood.poolGenes(), brood.babyChance());
				case GeneEffect.PatrolSize patrol when reason == EntitySpawnReason.PATROL ->
						spawnBrood(mob, level, patrol.extra(), EntitySpawnReason.PATROL, false, 0.0F);
				case GeneEffect.Jockey jockey when reason == EntitySpawnReason.NATURAL
						&& mob.getRandom().nextFloat() < jockey.chance() -> mount(mob, level, jockey);
				// An endermite comes from an ender pearl, an event, so the burrow also runs at an event.
				case GeneEffect.Burrow burrow when reason == EntitySpawnReason.NATURAL
						|| reason == EntitySpawnReason.TRIGGERED -> burrow(mob, level, burrow);
				case GeneEffect.SpawnSize spawn when mob instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob cube
						&& broodReason(reason) && reason != EntitySpawnReason.TRIGGERED -> GeneCubes.spawnSize(cube, spawn);
				case GeneEffect.ChargedSpawn charged when mob instanceof Creeper creeper
						&& mob.getRandom().nextFloat() < charged.chance() -> GeneBlasts.charge(creeper);
				default -> {
				}
			}
		}
	}

	/**
	 * Returns true if a spawn for the reason gives a brood: a natural spawn, a spawn from a spawner or a trial spawner,
	 * or a spawn from an event. Some mobs, such as the cave spider, come mostly from spawners, and the endermite comes
	 * from an ender pearl. The extra mobs of a brood are reinforcements, so they give no brood of their own.
	 */
	private static boolean broodReason(EntitySpawnReason reason) {
		return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.SPAWNER
				|| reason == EntitySpawnReason.TRIAL_SPAWNER || reason == EntitySpawnReason.TRIGGERED;
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

	/**
	 * Returns the ticks in the Overworld after which the mob turns into its Nether kin, after its conversion time
	 * genes. The longest time counts.
	 */
	public static int conversionTicks(Mob mob, int ticks) {
		return GeneEffects.of(mob).filter(GeneEffect.ConversionTime.class::isInstance)
				.mapToInt(effect -> Math.round(((GeneEffect.ConversionTime) effect).seconds() * 20.0F)).max().orElse(ticks);
	}

	/**
	 * Makes the mob a baby with the health of the gene. A baby zombie or a baby zombified piglin can ride a chicken,
	 * as in vanilla. A baby piglin does not.
	 */
	private static void makeRunt(Mob baby, ServerLevel level, GeneEffect.Runt runt) {
		baby.setBaby(true);
		AttributeInstance health = baby.getAttribute(Attributes.MAX_HEALTH);
		if (health != null && !health.hasModifier(RUNT_HEALTH)) {
			health.addPermanentModifier(new AttributeModifier(RUNT_HEALTH,
					runt.babyHealthMultiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			baby.setHealth(baby.getMaxHealth());
		}
		if (baby instanceof Zombie && baby.getRandom().nextFloat() < CHICKEN_JOCKEY_CHANCE) {
			Chicken chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.JOCKEY);
			if (chicken != null) {
				chicken.snapTo(baby.getX(), baby.getY(), baby.getZ(), baby.getYRot(), 0.0F);
				chicken.setChickenJockey(true);
				level.addFreshEntity(chicken);
				baby.startRiding(chicken);
			}
		}
	}

	/**
	 * Spawns the count of extra mobs with the genes of the mob near it. The reason is the reason of their vanilla
	 * spawn: a reinforcement for a brood, or a patrol for the extra member of a patrol.
	 */
	private static void spawnBrood(Mob mob, ServerLevel level, int count, EntitySpawnReason spawnReason,
			boolean poolGenes, float babyChance) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		for (int i = 0; i < count; i++) {
			if (!(mob.getType().create(level, EntitySpawnReason.REINFORCEMENT) instanceof Mob extra)) {
				return;
			}
			extra.snapTo(mob.getX() + mob.getRandom().nextInt(3) - 1, mob.getY(), mob.getZ() + mob.getRandom().nextInt(3) - 1,
					mob.getRandom().nextFloat() * 360.0F, 0.0F);
			// The vanilla spawn gives the extra mob its natural gear, for example the bow of a skeleton.
			extra.finalizeSpawn(level, level.getCurrentDifficultyAt(extra.blockPosition()), spawnReason, null);
			if (babyChance > 0.0F) {
				extra.setBaby(mob.getRandom().nextFloat() < babyChance);
			}
			// An extra mob of a patrol follows the patrol, to the same place.
			if (mob instanceof PatrollingMonster leader && extra instanceof PatrollingMonster member
					&& leader.getPatrolTarget() != null) {
				member.setPatrolTarget(leader.getPatrolTarget());
			}
			if (poolGenes) {
				// The extra mob joins with no genes, so the load rolls new genes from the pool, as for a new mob.
				level.addFreshEntity(extra);
				continue;
			}
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
		if (state.collapseUntil() >= 0L && level.getGameTime() >= state.collapseUntil()) {
			standUp(mob, level, state);
			state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		}
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
		scaleBrainCooldown(mob, level);
		for (GeneEffect effect : effects) {
			switch (effect) {
				case GeneEffect.WaterPull pull -> waterPull(mob, level, pull);
				case GeneEffect.SwimVertical swim -> swimVertical(mob, swim);
				case GeneEffect.DragUnder drag -> drag(mob, level, drag);
				case GeneEffect.Lurk lurk when lurk.ignite() && mob instanceof Creeper creeper && !creeper.isIgnited()
						&& mob.getTarget() instanceof Player player && mob.distanceTo(player) <= lurk.radius()
						&& mob.hasLineOfSight(player) -> creeper.ignite();
				case GeneEffect.Drink drink when mob instanceof Witch witch -> drink(witch, level, drink);
				case GeneEffect.LavaExitFire fire when mob.isInLava() -> mob.setAttached(ModAttachments.LAVA_TIME, level.getGameTime());
				case GeneEffect.RunStrike run -> countRun(mob);
				case GeneEffect.JumpShot shot -> jumpShot(mob, level);
				case GeneEffect.TouchReach reach when mob instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob cube ->
						GeneCubes.reachHit(cube);
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
				case GeneEffect.GazeEffect gaze when now % Math.max(1, Math.round(gaze.intervalSeconds() * 20.0F)) < TICK_INTERVAL -> {
					AABB area = mob.getBoundingBox().inflate(gaze.range());
					for (Player player : level.getEntitiesOfClass(Player.class, area, player -> !player.isSpectator()
							&& mob.isLookingAtMe(player, GAZE_TOLERANCE, true, false, mob.getEyeY())
							&& player.hasLineOfSight(mob))) {
						player.addEffect(new MobEffectInstance(gaze.effect(), Math.round(gaze.seconds() * 20.0F),
								gaze.amplifier()), mob);
					}
				}
				case GeneEffect.SkullShot skull when mob.getTarget() instanceof Player player && mob.hasLineOfSight(player) -> {
					double distance = mob.distanceTo(player);
					long last = mob.getAttachedOrElse(ModAttachments.SKULL_TIME, Long.MIN_VALUE);
					if (distance >= skull.minRange() && distance <= skull.maxRange()
							&& (last == Long.MIN_VALUE || now - last >= Math.round(skull.cooldownSeconds() * 20.0F))) {
						mob.setAttached(ModAttachments.SKULL_TIME, now);
						shootSkull(mob, level, player);
					}
				}
				case GeneEffect.Regrow regrow when mob instanceof Bogged bogged && bogged.isSheared() -> {
					long sheared = mob.getAttachedOrElse(ModAttachments.SHEAR_TIME, now);
					if (!mob.hasAttached(ModAttachments.SHEAR_TIME)) {
						mob.setAttached(ModAttachments.SHEAR_TIME, now);
					}
					if (now - sheared >= Math.round(regrow.seconds() * 20.0F)) {
						bogged.setSheared(false);
						bogged.heal(regrow.heal());
						mob.removeAttached(ModAttachments.SHEAR_TIME);
					}
				}
				case GeneEffect.FreezeAura freeze when GeneCombat.test(mob, freeze.when()) -> {
					AABB area = mob.getBoundingBox().inflate(freeze.radius());
					for (Player player : level.getEntitiesOfClass(Player.class, area, Player::canFreeze)) {
						// The freeze stops 1 second past full, so that it does not build up with no end.
						int most = player.getTicksRequiredToFreeze() + 2 * TICK_INTERVAL;
						player.setTicksFrozen(Math.min(most, player.getTicksFrozen() + freeze.rate()));
					}
				}
				case GeneEffect.Aura aura when now % Math.max(1, Math.round(aura.intervalSeconds() * 20.0F)) < TICK_INTERVAL
						&& GeneCombat.test(mob, aura.when()) -> {
					AABB area = mob.getBoundingBox().inflate(aura.radius());
					// With no time of its own, the effect lasts until the next renewal, so that it does not end.
					int ticks = aura.seconds().map(seconds -> Math.round(seconds * 20.0F))
							.orElse(Math.round(aura.intervalSeconds() * 20.0F) + TICK_INTERVAL);
					List<? extends LivingEntity> targets = aura.allies()
							? level.getEntitiesOfClass(Mob.class, area, other -> other.getType() == mob.getType()
									&& (!aura.inWaterOnly() || other.isInWater()))
							: level.getEntitiesOfClass(Player.class, area, player -> !aura.inWaterOnly() || player.isInWater());
					for (LivingEntity target : targets) {
						target.addEffect(new MobEffectInstance(aura.effect(), ticks, aura.amplifier()), mob);
					}
					if (aura.particles()) {
						level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY(0.5), mob.getZ(),
								4, 0.4, 0.5, 0.4, 0.0);
					}
				}
				case GeneEffect.AttractGolems attract -> attractGolems(mob, level, attract);
				case GeneEffect.Sidestep sidestep -> sidestep(mob, level, sidestep);
				case GeneEffect.ThrowEffect thrown -> throwEffect(mob, level, thrown);
				case GeneEffect.WebTrail trail -> webTrail(mob, level, trail);
				case GeneEffect.FireTrail trail -> fireTrail(mob, level, trail);
				case GeneEffect.WebShot shot -> webShot(mob, level, shot);
				case GeneEffect.RocketShot shot -> rocketShot(mob, level, shot);
				case GeneEffect.FireballShot shot -> fireballShot(mob, level, shot);
				case GeneEffect.FireballRing ring -> fireballRing(mob, level, ring);
				case GeneEffect.FireFeed feed -> fireFeed(mob, feed);
				case GeneEffect.Dive dive -> dive(mob, dive);
				case GeneEffect.BoilOff boil -> boilOff(mob, level, boil);
				case GeneEffect.Hex hex -> hex(mob, level, hex);
				case GeneEffect.HealThrow heal -> healThrow(mob, level, heal);
				case GeneEffect.EatItems eat -> eatItems(mob, level, eat);
				case GeneEffect.HuntMobs hunt when mob.getTarget() == null -> huntMobs(mob, level, hunt);
				case GeneEffect.FireAura aura when now % Math.max(1, Math.round(aura.intervalSeconds() * 20.0F)) < TICK_INTERVAL ->
						fireAura(mob, level, aura);
				case GeneEffect.Grow grow when mob instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob cube ->
						GeneCubes.grow(cube, level, grow);
				case GeneEffect.Bud bud when mob instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob cube ->
						GeneCubes.bud(cube, level, bud);
				case GeneEffect.TeleportToTarget teleport -> GeneTeleports.toTarget(mob, level, teleport);
				case GeneEffect.StareEffect stare -> stareEffect(mob, level, stare);
				case GeneEffect.BlockThrow thrown when mob instanceof Enderman enderman ->
						GeneThrows.throwBlock(enderman, level, thrown);
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
				case GeneEffect.ChargeWhen charge when mob instanceof Creeper creeper && !creeper.isPowered()
						&& GeneCombat.test(mob, charge.when()) -> GeneBlasts.charge(creeper);
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
	 * Returns true if the mob wants to walk back from its target now, because the target is too near.
	 */
	public static boolean keepsDistance(Mob mob) {
		LivingEntity target = mob.getTarget();
		if (target == null) {
			return false;
		}
		double distance = mob.distanceTo(target);
		return GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.KeepDistance keep
				&& distance < keep.blocks());
	}

	/**
	 * Returns the product of the arrow spread multipliers of the mob.
	 */
	public static float arrowSpreadMultiplier(LivingEntity mob) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ArrowSpread spread) {
				multiplier *= spread.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Returns true if the player draws a bow, a crossbow, or a trident in the range, and looks at the mob.
	 */
	public static boolean isAimedAt(Mob mob, Player player, float range) {
		if (!player.isUsingItem() || player.distanceTo(mob) > range || !player.hasLineOfSight(mob)) {
			return false;
		}
		ItemStack item = player.getUseItem();
		if (!(item.is(Items.BOW) || item.is(Items.CROSSBOW) || item.is(Items.TRIDENT))) {
			return false;
		}
		Vec3 toMob = mob.getEyePosition().subtract(player.getEyePosition()).normalize();
		return player.getViewVector(1.0F).dot(toMob) > AIM_COS;
	}

	/**
	 * Puts the mob on the back of a new mob of the mount type.
	 */
	public static void mount(Mob mob, ServerLevel level, GeneEffect.Jockey jockey) {
		if (jockey.mount().create(level, EntitySpawnReason.JOCKEY) instanceof Mob mount) {
			mount.snapTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), 0.0F);
			level.addFreshEntity(mount);
			mob.startRiding(mount);
		}
	}

	/**
	 * When a skeleton with barrage shoots at a player, the skeletons near it that target the same player and can see
	 * the player get the same wait to the next shot. Then the group draws and shoots at the same time. A skeleton
	 * that releases its own arrow in this tick keeps its shot.
	 */
	public static void barrage(Mob mob, LivingEntity target, int attackTime) {
		if (!(target instanceof Player)) {
			return;
		}
		var barrage = GeneEffects.of(mob).filter(GeneEffect.Barrage.class::isInstance)
				.map(GeneEffect.Barrage.class::cast).findFirst();
		if (barrage.isEmpty()) {
			return;
		}
		List<AbstractSkeleton> group = mob.level().getEntitiesOfClass(AbstractSkeleton.class,
				mob.getBoundingBox().inflate(barrage.get().radius()),
				other -> other != mob && other.getType() == mob.getType() && other.getTarget() == target
						&& other.hasLineOfSight(target) && !GeneCombat.collapsed(other));
		if (group.size() + 1 < barrage.get().group()) {
			return;
		}
		for (AbstractSkeleton other : group) {
			if (other.isUsingItem() && other.getTicksUsingItem() >= drawTicks(other, 20) - 1) {
				continue;
			}
			other.stopUsingItem();
			((RangedBowAttackGoalAccessor) ((AbstractSkeletonAccessor) other).originofspawns$bowGoal())
					.originofspawns$setAttackTime(attackTime);
		}
	}

	private static void standUp(Mob mob, ServerLevel level, BehaviorState state) {
		float health = GeneEffects.of(mob).filter(GeneEffect.Reassemble.class::isInstance)
				.map(effect -> ((GeneEffect.Reassemble) effect).health()).findFirst().orElse(0.5F);
		mob.setHealth(Math.max(1.0F, mob.getMaxHealth() * health));
		mob.setAttached(ModAttachments.STATE, state.withCollapseUntil(-1L));
		level.sendParticles(new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM,
				Items.BONE), mob.getX(), mob.getY(0.5), mob.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
	}

	/**
	 * Shoots a wither skull from the eyes of the mob at the middle of the target.
	 */
	public static void shootSkull(Mob mob, ServerLevel level, LivingEntity target) {
		Vec3 aim = new Vec3(target.getX() - mob.getX(), target.getY(0.5) - mob.getEyeY(), target.getZ() - mob.getZ())
				.normalize();
		var skull = new net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull(level, mob, aim);
		skull.setPos(mob.getX() + aim.x, mob.getEyeY(), mob.getZ() + aim.z);
		level.addFreshEntity(skull);
	}

	/**
	 * After a bow shot of a mob with a fan shot, shoots the other arrows of the fan, if the cooldown is over and the
	 * state is true.
	 */
	public static void fanShot(AbstractSkeleton mob, LivingEntity target) {
		fanShot(mob, target, () -> ((AbstractSkeletonAccessor) mob).originofspawns$getArrow(new ItemStack(Items.ARROW), 1.0F,
				mob.getMainHandItem()));
	}

	/**
	 * After a crossbow shot of a mob with a fan shot, such as a piglin, shoots the other bolts of the fan. The bolts
	 * get the arrow genes of the mob, as its own bolt does.
	 */
	public static void crossbowFanShot(Mob mob, LivingEntity target) {
		fanShot(mob, target, () -> {
			var bolt = new net.minecraft.world.entity.projectile.arrow.Arrow(mob.level(), mob, new ItemStack(Items.ARROW),
					mob.getMainHandItem());
			bolt.setCritArrow(true);
			armArrow(mob, bolt);
			return bolt;
		});
	}

	/**
	 * Returns the fan shot of the mob whose state is true, if its cooldown is over, and starts the cooldown.
	 */
	private static java.util.Optional<GeneEffect.FanShot> readyFanShot(Mob mob, ServerLevel level) {
		var fan = GeneEffects.of(mob).filter(GeneEffect.FanShot.class::isInstance).map(GeneEffect.FanShot.class::cast)
				.filter(effect -> GeneCombat.test(mob, effect.when())).findFirst();
		if (fan.isEmpty()) {
			return fan;
		}
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.FAN_SHOT_TIME, Long.MIN_VALUE);
		if (last != Long.MIN_VALUE && now - last < Math.round(fan.get().cooldownSeconds() * 20.0F)) {
			return java.util.Optional.empty();
		}
		mob.setAttached(ModAttachments.FAN_SHOT_TIME, now);
		return fan;
	}

	/**
	 * Returns the turn in radians of the extra shot with the index. The shots go to each side in turn: +1, -1, +2, -2,
	 * and so on, times the angle.
	 */
	private static float fanAngle(GeneEffect.FanShot fan, int index) {
		int step = index / 2 + 1;
		return (index % 2 == 0 ? step : -step) * fan.angle() * Mth.DEG_TO_RAD;
	}

	/**
	 * Returns the turns in radians of the extra shots of the fan shot of the mob, if its state is true and its cooldown
	 * is over, and starts the cooldown. Else it returns no turns.
	 */
	public static float[] fanShotTurns(Mob mob, ServerLevel level) {
		var fan = readyFanShot(mob, level);
		if (fan.isEmpty()) {
			return new float[0];
		}
		float[] turns = new float[fan.get().arrows() - 1];
		for (int i = 0; i < turns.length; i++) {
			turns[i] = fanAngle(fan.get(), i);
		}
		return turns;
	}

	/**
	 * After a shot of a ghast with a fan shot, shoots the other fireballs of the fan. Each extra fireball is the main
	 * one, turned about the center of the ghast. It has the same blast power, so it follows the rules of the main one.
	 */
	public static void ghastFanShot(Ghast ghast, LargeFireball main) {
		if (!(ghast.level() instanceof ServerLevel level)) {
			return;
		}
		var fan = readyFanShot(ghast, level);
		if (fan.isEmpty()) {
			return;
		}
		Vec3 direction = main.getDeltaMovement().normalize();
		Vec3 offset = main.position().subtract(ghast.position());
		for (int i = 0; i < fan.get().arrows() - 1; i++) {
			float angle = fanAngle(fan.get(), i);
			LargeFireball fireball = new LargeFireball(level, ghast, direction.yRot(angle), ghast.getExplosionPower());
			Vec3 turned = offset.yRot(angle);
			fireball.setPos(ghast.getX() + turned.x, main.getY(), ghast.getZ() + turned.z);
			level.addFreshEntity(fireball);
		}
	}

	/**
	 * After a shot of a large fireball, shoots the small fireballs of the small fireball fans of the mob.
	 */
	public static void smallFireballFan(Mob mob, LargeFireball main) {
		if (!(mob.level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 direction = main.getDeltaMovement().normalize();
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.SmallFireballFan fan) {
				for (int i = 0; i < fan.count(); i++) {
					float angle = (i - (fan.count() - 1) / 2.0F) * fan.angle() * Mth.DEG_TO_RAD;
					SmallFireball fireball = new SmallFireball(level, mob, direction.yRot(angle));
					fireball.setPos(main.position());
					level.addFreshEntity(fireball);
				}
			}
		}
	}

	private static void fanShot(Mob mob, LivingEntity target, java.util.function.Supplier<AbstractArrow> makeArrow) {
		if (!(mob.level() instanceof ServerLevel level) || target == null) {
			return;
		}
		var fan = readyFanShot(mob, level);
		if (fan.isEmpty()) {
			return;
		}
		int extra = fan.get().arrows() - 1;
		for (int i = 0; i < extra; i++) {
			float angle = fanAngle(fan.get(), i);
			AbstractArrow arrow = makeArrow.get();
			double dx = target.getX() - mob.getX();
			double dz = target.getZ() - mob.getZ();
			double dy = target.getY(0.3333333333333333) - arrow.getY();
			double distance = Math.sqrt(dx * dx + dz * dz);
			double turnedX = dx * Mth.cos(angle) - dz * Mth.sin(angle);
			double turnedZ = dx * Mth.sin(angle) + dz * Mth.cos(angle);
			arrow.shoot(turnedX, dy + distance * 0.2, turnedZ, 1.6F, 14 - level.getDifficulty().getId() * 4);
			level.addFreshEntity(arrow);
		}
	}

	/**
	 * Returns the draw time of a bow after the draw time and the throw rate of the mob. The throw rate changes the
	 * draw and the wait after a shot together, so that the full time of a shot changes by the rate.
	 */
	public static int drawTicks(Mob mob, int ticks) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.DrawTime draw) {
				multiplier *= draw.multiplier();
			} else if (effect instanceof GeneEffect.ThrowRate rate && GeneCombat.test(mob, rate.when())) {
				multiplier /= rate.multiplier();
			}
		}
		return multiplier == 1.0F ? ticks : Math.max(1, Math.round(ticks * multiplier));
	}

	/**
	 * Makes the blaze rise while it is lower than the top of the nearest mob of a keep above list and the blocks of
	 * the gene. The rise is the vanilla rise of a blaze to its target.
	 */
	public static void keepAbove(Mob mob, ServerLevel level) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.KeepAbove above) {
				var near = level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(above.radius()),
						other -> other != mob && other.isAlive() && above.mobs().contains(other.getType().builtInRegistryHolder())
								&& other.distanceTo(mob) <= above.radius());
				var nearest = near.stream().min(java.util.Comparator.comparingDouble(other -> other.distanceToSqr(mob)));
				if (nearest.isPresent() && mob.getY() < nearest.get().getY() + nearest.get().getBbHeight() + above.blocks()) {
					Vec3 movement = mob.getDeltaMovement();
					mob.setDeltaMovement(movement.add(0.0, (0.3 - movement.y) * 0.3, 0.0));
					mob.needsSync = true;
					return;
				}
			}
		}
	}

	/**
	 * Returns the largest number of blocks of the float height genes of the mob, or 0.
	 */
	public static float floatBlocks(Mob mob) {
		float blocks = 0.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.FloatHeight height) {
				blocks = Math.max(blocks, height.blocks());
			}
		}
		return blocks;
	}

	/**
	 * Returns the height of a place that the mob floats to, after its float height genes. While the mob has a target,
	 * the place is at least the largest number of blocks above the target.
	 */
	public static double floatHeight(Mob mob, double y) {
		LivingEntity target = mob.getTarget();
		if (target == null) {
			return y;
		}
		double result = y;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.FloatHeight height) {
				result = Math.max(result, target.getY() + height.blocks());
			}
		}
		return result;
	}

	/**
	 * Runs the effects of the genes of the mob where its small fireball hits: the blast and the clouds.
	 */
	public static void smallFireballHit(Mob mob, SmallFireball fireball, ServerLevel level) {
		float power = 0.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.FireballBlast blast) {
				power = Math.max(power, blast.power());
			} else if (effect instanceof GeneEffect.FireballCloud cloud) {
				var area = new net.minecraft.world.entity.AreaEffectCloud(level, fireball.getX(), fireball.getY(),
						fireball.getZ());
				area.setOwner(mob);
				area.setRadius(cloud.radius());
				area.setDuration(Math.max(1, Math.round(cloud.cloudSeconds() * 20.0F)));
				area.addEffect(new MobEffectInstance(cloud.effect(), Math.round(cloud.seconds() * 20.0F), cloud.amplifier()));
				level.addFreshEntity(area);
			}
		}
		if (power > 0.0F) {
			level.explode(fireball, fireball.getX(), fireball.getY(), fireball.getZ(), power, false,
					net.minecraft.world.level.Level.ExplosionInteraction.NONE);
		}
	}

	/**
	 * Returns the seconds of fire of a small fireball of the mob, after its fireball fire time genes. The fire is not
	 * shorter than 0 seconds.
	 */
	public static float fireballFireSeconds(Mob mob, float seconds) {
		float result = seconds;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.FireballFireTime fire) {
				result += fire.seconds();
			}
		}
		return Math.max(0.0F, result);
	}

	/**
	 * Turns the water source blocks in the radius of the mob to air, with the fizz of water on lava, if the
	 * {@code mobGriefing} gamerule is on.
	 *
	 * @return the number of blocks that turned to steam
	 */
	public static int boilOff(Mob mob, ServerLevel level, GeneEffect.BoilOff boil) {
		if (!level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)) {
			return 0;
		}
		int reach = Mth.ceil(boil.radius());
		int boiled = 0;
		Vec3 center = mob.position();
		for (BlockPos pos : BlockPos.betweenClosed(mob.blockPosition().offset(-reach, -reach, -reach),
				mob.blockPosition().offset(reach, reach, reach))) {
			var fluid = level.getFluidState(pos);
			if (fluid.isSource() && fluid.is(net.minecraft.tags.FluidTags.WATER)
					&& Vec3.atCenterOf(pos).distanceTo(center) <= boil.radius() + 0.5
					&& level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
				level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
				level.levelEvent(1501, pos, 0);
				boiled++;
			}
		}
		return boiled;
	}

	/**
	 * Makes the mob dive at its target, if the target is in range and lower than the mob, and the cooldown is over.
	 *
	 * @return true if the mob dived
	 */
	public static boolean dive(Mob mob, GeneEffect.Dive dive) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) > dive.range() || target.getY() >= mob.getY()
				|| !mob.hasLineOfSight(target) || !GeneCombat.ready(mob, dive, dive.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(mob, dive);
		Vec3 toTarget = new Vec3(target.getX(), target.getY(0.5), target.getZ()).subtract(mob.position());
		mob.setDeltaMovement(toTarget.normalize().scale(dive.speed()));
		mob.needsSync = true;
		return true;
	}

	/**
	 * Heals the mob if its target burns in the radius. The behavior tick runs this one time each second.
	 *
	 * @return true if the mob healed
	 */
	public static boolean fireFeed(Mob mob, GeneEffect.FireFeed feed) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || !target.isOnFire() || mob.distanceTo(target) > feed.radius()) {
			return false;
		}
		mob.heal(feed.amount());
		return true;
	}

	/**
	 * At the top of a long jump, makes a breeze shoot a wind charge at its target, one time in each jump. The top is
	 * the first tick of the jump in which the breeze does not rise.
	 *
	 * @return true if the breeze shot
	 */
	public static boolean jumpShot(Mob mob, ServerLevel level) {
		if (!(mob instanceof net.minecraft.world.entity.monster.breeze.Breeze breeze)
				|| breeze.getPose() != net.minecraft.world.entity.Pose.LONG_JUMPING) {
			mob.removeAttached(ModAttachments.JUMP_SHOT_DONE);
			return false;
		}
		// A breeze keeps its target in its brain.
		LivingEntity target = breeze.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
				.orElse(null);
		if (breeze.getAttachedOrElse(ModAttachments.JUMP_SHOT_DONE, false) || breeze.getDeltaMovement().y > 0.0
				|| target == null || !target.isAlive()) {
			return false;
		}
		breeze.setAttached(ModAttachments.JUMP_SHOT_DONE, true);
		var charge = new net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge(breeze, level);
		double dx = target.getX() - breeze.getX();
		double dy = target.getY(0.3) - breeze.getFiringYPosition();
		double dz = target.getZ() - breeze.getZ();
		net.minecraft.world.entity.projectile.Projectile.spawnProjectileUsingShoot(charge, level, ItemStack.EMPTY, dx, dy, dz,
				0.7F, (5 - level.getDifficulty().getId() * 4) * arrowSpreadMultiplier(breeze));
		fireballSpeed(breeze, charge);
		return true;
	}

	/**
	 * Returns the damage of the hit of a wind charge of the mob, after its wind charge damage genes. The damage is not
	 * less than 0.
	 */
	public static float windChargeDamage(Mob mob, float damage) {
		float result = damage;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.WindChargeDamage extra) {
				result += extra.extra();
			}
		}
		return Math.max(0.0F, result);
	}

	/**
	 * Returns the push of the blast of a wind charge on the entity, after the genes of the mob that shot the charge.
	 * Other blasts do not change.
	 *
	 * @param source the direct source of the blast
	 */
	public static Vec3 windPush(Entity source, Entity pushed, Vec3 knockback) {
		if (!(source instanceof net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge charge)
				|| !(GeneBlasts.shooter(charge) instanceof Mob mob)) {
			return knockback;
		}
		double multiplier = 1.0;
		double lift = 0.0;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.WindPush push
					&& push.targets().map(targets -> targets.contains(pushed.getType().builtInRegistryHolder())).orElse(true)) {
				multiplier *= push.multiplier();
			} else if (effect instanceof GeneEffect.WindLift windLift && pushed instanceof LivingEntity
					&& knockback.lengthSqr() > 0.0) {
				// A speed up of v takes an entity about v * v / (2 * 0.08) blocks high.
				lift = Math.max(lift, Math.sqrt(2.0 * 0.08 * windLift.blocks()));
			}
		}
		return knockback.scale(multiplier).add(0.0, lift, 0.0);
	}

	/**
	 * Gives the blast effects of the mob that shot a wind charge to each living entity that the blast of the charge
	 * pushes. The blast of a wind charge does no damage, so the effects go with the push. A blast effect with a radius
	 * acts only on the blasts that do damage.
	 */
	public static void windBlastEffects(Entity source, Entity pushed, Vec3 push) {
		if (!(source instanceof net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge charge)
				|| !(GeneBlasts.shooter(charge) instanceof Mob mob) || !(pushed instanceof LivingEntity living)
				|| living == mob || push.lengthSqr() <= 0.0) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.BlastEffect blast && blast.radius().isEmpty()) {
				living.addEffect(new MobEffectInstance(blast.effect(), Math.round(blast.seconds() * 20.0F), blast.amplifier()), mob);
			}
		}
	}

	/**
	 * Shoots the ring of small fireballs of the mob, if the mob has a target and the cooldown is over.
	 *
	 * @return true if the mob shot the ring
	 */
	public static boolean fireballRing(Mob mob, ServerLevel level, GeneEffect.FireballRing ring) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || !GeneCombat.ready(mob, ring, ring.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(mob, ring);
		for (int i = 0; i < ring.count(); i++) {
			float angle = i * Mth.TWO_PI / ring.count();
			SmallFireball fireball = new SmallFireball(level, mob, new Vec3(Mth.cos(angle), 0.0, Mth.sin(angle)));
			fireball.setPos(fireball.getX(), mob.getY(0.5) + 0.5, fireball.getZ());
			level.addFreshEntity(fireball);
		}
		return true;
	}

	/**
	 * Turns the fireball of the mob to the center of the target of the mob, at most the largest angle of its seeker
	 * genes. The speed of the fireball stays.
	 */
	public static void seek(Mob mob, AbstractHurtingProjectile fireball) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive()) {
			return;
		}
		float degrees = 0.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.SeekerFireballs seeker) {
				degrees = Math.max(degrees, seeker.degrees());
			}
		}
		Vec3 motion = fireball.getDeltaMovement();
		double speed = motion.length();
		Vec3 wanted = new Vec3(target.getX(), target.getY(0.5), target.getZ()).subtract(fireball.position());
		if (degrees <= 0.0F || speed < 1.0E-6 || wanted.lengthSqr() < 1.0E-6) {
			return;
		}
		Vec3 now = motion.normalize();
		Vec3 to = wanted.normalize();
		double angle = Math.acos(Mth.clamp(now.dot(to), -1.0, 1.0));
		double turn = Math.min(angle, degrees * Mth.DEG_TO_RAD);
		Vec3 side = to.subtract(now.scale(now.dot(to)));
		if (side.lengthSqr() < 1.0E-9) {
			return;
		}
		Vec3 turned = now.scale(Math.cos(turn)).add(side.normalize().scale(Math.sin(turn)));
		fireball.setDeltaMovement(turned.scale(speed));
	}

	/**
	 * Multiplies the speed of a new fireball of the mob by its fireball speed genes. A fireball speeds up by its
	 * acceleration each tick, so the acceleration changes too.
	 */
	public static void fireballSpeed(Mob mob, AbstractHurtingProjectile fireball) {
		double multiplier = 1.0;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.FireballSpeed speed) {
				multiplier *= speed.multiplier();
			}
		}
		if (multiplier != 1.0) {
			fireball.accelerationPower *= multiplier;
			fireball.setDeltaMovement(fireball.getDeltaMovement().scale(multiplier));
		}
	}

	/**
	 * Turns the fireball with the index in the burst of the blaze to its place in the burst fan of the blaze.
	 */
	public static void turnInBurstFan(Mob blaze, SmallFireball fireball, int index) {
		int count = 3 + burstExtra(blaze);
		if (count < 2) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(blaze).toList()) {
			if (effect instanceof GeneEffect.BurstFan fan) {
				float angle = ((float) index / (count - 1) - 0.5F) * fan.angle() * Mth.DEG_TO_RAD;
				fireball.setDeltaMovement(fireball.getDeltaMovement().yRot(angle));
				return;
			}
		}
	}

	/**
	 * Returns the number of extra fireballs in each burst of a blaze. A burst has at least one fireball.
	 */
	public static int burstExtra(Mob blaze) {
		int extra = 0;
		for (GeneEffect effect : GeneEffects.of(blaze).toList()) {
			if (effect instanceof GeneEffect.BurstCount count) {
				extra += count.extra();
			}
		}
		return Math.max(-2, extra);
	}

	/**
	 * Returns the ticks that a ghast waits after a shot, after its throw rates. The full time from one shot to the
	 * next is the wait and the charge of 20 ticks. The throw rate changes the full time.
	 */
	public static int ghastShotWait(Mob ghast, int wait) {
		return Math.max(0, scaleThrowInterval(ghast, wait + GHAST_CHARGE_TICKS) - GHAST_CHARGE_TICKS);
	}

	/**
	 * The ticks that a ghast charges before each shot.
	 */
	private static final int GHAST_CHARGE_TICKS = 20;

	/**
	 * Returns the attack interval of a ranged goal after the throw rates of the mob.
	 */
	public static int scaleThrowInterval(Mob mob, int ticks) {
		float rate = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ThrowRate throwRate && GeneCombat.test(mob, throwRate.when())) {
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
			if (effect instanceof GeneEffect.ThrowRange range && GeneCombat.test(mob, range.when())
					&& (range.target() == kind || range.target() == GeneEffect.ThrowRange.Target.ANY)) {
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
			boolean seen = sidestep.aimed() ? isAimedAt(mob, player, sidestep.range())
					: LurkGoal.isWatchedFromAfar(mob, player, sidestep.range());
			if (player.isSpectator() || !seen) {
				continue;
			}
			// The side is at a right angle to the line from the player to the mob.
			Vec3 toMob = mob.position().subtract(player.position()).multiply(1.0, 0.0, 1.0).normalize();
			Vec3 side = new Vec3(-toMob.z, 0.0, toMob.x).scale(mob.getRandom().nextBoolean() ? 1.0 : -1.0);
			// A mob in the air, such as a phantom or a ghast, steps aside in the air first, at the same height.
			if (!mob.onGround()) {
				for (Vec3 direction : List.of(side, side.scale(-1.0))) {
					Vec3 target = mob.position().add(direction.scale(sidestep.distance()));
					if (level.noCollision(mob, mob.getDimensions(mob.getPose()).makeBoundingBox(target.x, target.y, target.z))) {
						level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY(0.5), mob.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
						mob.teleportTo(target.x, target.y, target.z);
						mob.setAttached(ModAttachments.STATE, state.withSidestepTime(now));
						return true;
					}
				}
			}
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
		if (target == null || !target.isAlive() || mob.distanceTo(target) < thrown.minRange()
				|| mob.distanceTo(target) > thrown.range() || !mob.hasLineOfSight(target)
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
	 * Leaves fire at the place of the mob while it stands on the ground, one time in each interval.
	 *
	 * @return true if a fire went in the world
	 */
	public static boolean fireTrail(Mob mob, ServerLevel level, GeneEffect.FireTrail trail) {
		BlockPos pos = mob.blockPosition();
		net.minecraft.world.level.block.state.BlockState fire = net.minecraft.world.level.block.BaseFireBlock.getState(level, pos);
		if (!mob.onGround() || !level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)
				|| !GeneCombat.ready(mob, trail, trail.intervalSeconds()) || !fire.canSurvive(level, pos)
				|| !TemporaryBlocks.place(level, pos, fire, Math.round(trail.seconds() * 20.0F))) {
			return false;
		}
		GeneCombat.startCooldown(mob, trail);
		return true;
	}

	/**
	 * Lets each mob with the guard gold gene in its radius attack the player that broke a gold block or opened a
	 * chest. With {@code mustSee}, only a mob that sees the player attacks.
	 */
	public static void guardGold(ServerLevel level, Player player, boolean mustSee) {
		AABB area = player.getBoundingBox().inflate(64.0);
		for (Mob mob : level.getEntitiesOfClass(Mob.class, area, other -> other.hasAttached(ModAttachments.GENES))) {
			GeneEffects.of(mob).filter(GeneEffect.GuardGold.class::isInstance).map(GeneEffect.GuardGold.class::cast)
					.filter(guard -> mob.distanceTo(player) <= guard.radius()).findFirst().ifPresent(guard -> {
						if ((!mustSee || mob.hasLineOfSight(player)) && mob.canAttack(player)) {
							mob.setTarget(player);
						}
					});
		}
	}

	/**
	 * Lets the mob eat one item of the list that lies on the ground next to it.
	 *
	 * @return true if the mob ate an item
	 */
	public static boolean eatItems(Mob mob, ServerLevel level, GeneEffect.EatItems eat) {
		var item = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				mob.getBoundingBox().inflate(1.0), entity -> entity.getItem().is(eat.items())).stream().findFirst();
		if (item.isEmpty()) {
			return false;
		}
		item.get().getItem().shrink(1);
		if (item.get().getItem().isEmpty()) {
			item.get().discard();
		}
		mob.heal(eat.heal());
		mob.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
		return true;
	}

	/**
	 * Lets a mob with no target attack the nearest mob of the list that it sees in its follow range.
	 *
	 * @return true if the mob found a target
	 */
	public static boolean huntMobs(Mob mob, ServerLevel level, GeneEffect.HuntMobs hunt) {
		double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
		var prey = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(range), other -> other != mob
				&& other.isAlive() && hunt.mobs().contains(other.getType().builtInRegistryHolder())
				&& mob.hasLineOfSight(other) && mob.canAttack(other))
				.stream().min((a, b) -> Double.compare(a.distanceToSqr(mob), b.distanceToSqr(mob)));
		prey.ifPresent(mob::setTarget);
		return prey.isPresent();
	}

	/**
	 * Adds the move of the mob in this tick to its run. A tick with almost no move ends the run.
	 */
	public static void countRun(Mob mob) {
		Vec3 last = mob.getAttached(ModAttachments.RUN_POS);
		Vec3 now = mob.position();
		mob.setAttached(ModAttachments.RUN_POS, now);
		if (last == null) {
			return;
		}
		double moved = Math.sqrt((now.x - last.x) * (now.x - last.x) + (now.z - last.z) * (now.z - last.z));
		if (moved < RUN_STEP) {
			mob.removeAttached(ModAttachments.RUN_BLOCKS);
		} else {
			mob.setAttached(ModAttachments.RUN_BLOCKS, mob.getAttachedOrElse(ModAttachments.RUN_BLOCKS, 0.0) + moved);
		}
	}

	/**
	 * The smallest move in one tick that counts as a run, in blocks.
	 */
	private static final double RUN_STEP = 0.05;

	/**
	 * Lets the mobs of the species of the mob in the radius of its swoop call target its target.
	 */
	public static void swoopCall(Mob mob) {
		LivingEntity target = mob.getTarget();
		if (target == null) {
			return;
		}
		GeneEffects.of(mob).filter(GeneEffect.SwoopCall.class::isInstance).map(GeneEffect.SwoopCall.class::cast)
				.mapToDouble(GeneEffect.SwoopCall::radius).max().ifPresent(radius -> {
					AABB area = mob.getBoundingBox().inflate(radius);
					for (Mob ally : mob.level().getEntitiesOfClass(Mob.class, area,
							other -> other != mob && other.getType() == mob.getType() && other.isAlive())) {
						ally.setTarget(target);
					}
				});
	}

	/**
	 * Sets the players in the radius of the mob on fire.
	 *
	 * @return the number of players that burn
	 */
	public static int fireAura(Mob mob, ServerLevel level, GeneEffect.FireAura aura) {
		List<Player> players = level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(aura.radius()),
				player -> player.isAlive() && !player.isSpectator() && !player.isCreative());
		for (Player player : players) {
			player.igniteForSeconds(aura.seconds());
		}
		if (!players.isEmpty()) {
			level.sendParticles(ParticleTypes.FLAME, mob.getX(), mob.getY(0.5), mob.getZ(), 12, aura.radius() / 2.0, 0.2,
					aura.radius() / 2.0, 0.02);
		}
		return players.size();
	}

	/**
	 * Leaves a cobweb at the place of the mob while it walks, one time in each interval.
	 *
	 * @return true if a cobweb went in the world
	 */
	public static boolean webTrail(Mob mob, ServerLevel level, GeneEffect.WebTrail trail) {
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.WEB_TRAIL_TIME, Long.MIN_VALUE);
		if (mob.getDeltaMovement().horizontalDistanceSqr() < WALK_SPEED_SQR
				|| last != Long.MIN_VALUE && now - last < Math.round(trail.intervalSeconds() * 20.0F)
				|| !GeneCombat.putWeb(level, mob, trail.seconds())) {
			return false;
		}
		mob.setAttached(ModAttachments.WEB_TRAIL_TIME, now);
		return true;
	}

	/**
	 * Spits silk at the target of the mob in the range, one time in each cooldown. The silk puts a cobweb at the
	 * target.
	 *
	 * @return true if the mob spat
	 */
	public static boolean webShot(Mob mob, ServerLevel level, GeneEffect.WebShot shot) {
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.WEB_SHOT_TIME, Long.MIN_VALUE);
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) < shot.minRange()
				|| mob.distanceTo(target) > shot.range() || !mob.hasLineOfSight(target)
				|| last != Long.MIN_VALUE && now - last < Math.round(shot.cooldownSeconds() * 20.0F)) {
			return false;
		}
		Vec3 from = mob.getEyePosition();
		Vec3 step = target.position().subtract(from).scale(0.1);
		for (int i = 0; i <= 10; i++) {
			Vec3 point = from.add(step.scale(i));
			level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COBWEB.defaultBlockState()),
					point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.0);
		}
		GeneCombat.putWeb(level, target, shot.seconds());
		mob.setAttached(ModAttachments.WEB_SHOT_TIME, now);
		return true;
	}

	/**
	 * Gives an arrow that the mob shoots the effects of the arrow genes of the mob: the status effects of the arrow,
	 * and the ghost arrows. The bow of a skeleton and the crossbow of a pillager use it.
	 */
	public static void armArrow(Mob mob, net.minecraft.world.entity.projectile.Projectile projectile) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.GhostArrows ghost) {
				projectile.setAttached(ModAttachments.GHOST_ARROW, ghost.blocks());
			} else if (effect instanceof GeneEffect.ArrowEffect arrowEffect
					&& projectile instanceof net.minecraft.world.entity.projectile.arrow.Arrow tipped) {
				tipped.addEffect(new MobEffectInstance(arrowEffect.effect(), Math.round(arrowEffect.seconds() * 20.0F),
						arrowEffect.amplifier()));
			}
		}
	}

	/**
	 * Returns a firework rocket with one red star. Its blast hurts the entities near it, and it breaks no blocks.
	 */
	public static ItemStack rocket() {
		ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
		rocket.set(net.minecraft.core.component.DataComponents.FIREWORKS, new net.minecraft.world.item.component.Fireworks(1,
				List.of(new net.minecraft.world.item.component.FireworkExplosion(
						net.minecraft.world.item.component.FireworkExplosion.Shape.SMALL_BALL,
						it.unimi.dsi.fastutil.ints.IntList.of(RED_STAR), it.unimi.dsi.fastutil.ints.IntList.of(), false, false))));
		return rocket;
	}

	/**
	 * Shoots a firework rocket at the target of the mob in the range, one time in each cooldown.
	 *
	 * @return true if the mob shot
	 */
	public static boolean rocketShot(Mob mob, ServerLevel level, GeneEffect.RocketShot shot) {
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.ROCKET_TIME, Long.MIN_VALUE);
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) > shot.range() || !mob.hasLineOfSight(target)
				|| last != Long.MIN_VALUE && now - last < Math.round(shot.cooldownSeconds() * 20.0F)) {
			return false;
		}
		net.minecraft.world.entity.projectile.FireworkRocketEntity rocket = new net.minecraft.world.entity.projectile.FireworkRocketEntity(
				level, rocket(), mob, mob.getX(), mob.getEyeY() - 0.15, mob.getZ(), true);
		// The rocket flies as a crossbow shot does: at a third of the height of the target, a little higher when far.
		double dx = target.getX() - mob.getX();
		double dz = target.getZ() - mob.getZ();
		double dy = target.getY(1.0 / 3.0) - rocket.getY() + Math.sqrt(dx * dx + dz * dz) * 0.2;
		rocket.shoot(dx, dy, dz, ROCKET_SPEED, 1.0F);
		level.addFreshEntity(rocket);
		level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), net.minecraft.sounds.SoundEvents.CROSSBOW_SHOOT,
				mob.getSoundSource(), 1.0F, 1.0F);
		mob.setAttached(ModAttachments.ROCKET_TIME, now);
		return true;
	}

	/**
	 * Shoots a small fireball at the target of the mob between the two ranges, one time in each cooldown.
	 *
	 * @return true if the mob shot
	 */
	public static boolean fireballShot(Mob mob, ServerLevel level, GeneEffect.FireballShot shot) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) < shot.minRange()
				|| mob.distanceTo(target) > shot.range() || !mob.hasLineOfSight(target)
				|| !GeneCombat.ready(mob, shot, shot.cooldownSeconds())) {
			return false;
		}
		Vec3 direction = new Vec3(target.getX() - mob.getX(), target.getY(0.5) - mob.getY(0.5), target.getZ() - mob.getZ());
		var fireball = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(level, mob,
				direction.normalize());
		fireball.setPos(fireball.getX(), mob.getY(0.5) + 0.5, fireball.getZ());
		level.addFreshEntity(fireball);
		if (!mob.isSilent()) {
			level.levelEvent(null, net.minecraft.world.level.block.LevelEvent.SOUND_BLAZE_FIREBALL, mob.blockPosition(), 0);
		}
		GeneCombat.startCooldown(mob, shot);
		return true;
	}

	/**
	 * Returns the product of the rocket blast multipliers of the owner of a rocket. A rocket of a player or of a mob
	 * with no genes gives 1.
	 */
	public static float rocketBlastMultiplier(net.minecraft.world.entity.Entity owner) {
		float multiplier = 1.0F;
		if (owner instanceof Mob mob && mob.hasAttached(ModAttachments.GENES)) {
			for (GeneEffect effect : GeneEffects.of(mob).toList()) {
				if (effect instanceof GeneEffect.RocketBlast blast) {
					multiplier *= blast.multiplier();
				}
			}
		}
		return multiplier;
	}

	/**
	 * Returns true if the next crossbow shot of the mob is a firework rocket. Each rocket chance of the genes has its
	 * own roll.
	 */
	public static boolean rollsRocket(Mob mob) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.RocketChance chance && mob.getRandom().nextFloat() < chance.chance()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Puts the item in the sack of the mob, up to 5 stacks, as a pillager puts items in its inventory. The part that
	 * does not fit stays on the ground.
	 */
	public static void putInSack(Mob mob, net.minecraft.world.entity.item.ItemEntity entity) {
		net.minecraft.world.SimpleContainer sack = new net.minecraft.world.SimpleContainer(SACK_SLOTS);
		mob.getAttachedOrElse(ModAttachments.SACK, List.of()).forEach(sack::addItem);
		ItemStack remainder = sack.addItem(entity.getItem().copy());
		int taken = entity.getItem().getCount() - remainder.getCount();
		if (taken <= 0) {
			return;
		}
		mob.onItemPickup(entity);
		mob.take(entity, taken);
		if (remainder.isEmpty()) {
			entity.discard();
		} else {
			entity.getItem().setCount(remainder.getCount());
		}
		mob.setAttached(ModAttachments.SACK, sack.getItems().stream().filter(stack -> !stack.isEmpty()).toList());
	}

	/**
	 * Records the leap of a mob with genes now. The first melee hit after it can do more damage.
	 */
	public static void markLeap(Mob mob) {
		if (mob.hasAttached(ModAttachments.GENES)) {
			mob.setAttached(ModAttachments.LEAP_TIME, mob.level().getGameTime());
		}
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
		if (call.mobs().isPresent()) {
			AABB area = mob.getBoundingBox().inflate(call.radius());
			for (Mob ally : level.getEntitiesOfClass(Mob.class, area,
					other -> other != mob && call.mobs().get().contains(other.getType().builtInRegistryHolder()))) {
				ally.setTarget(player);
			}
		} else {
			callAllies(mob, call.radius(), player);
		}
		mob.setAttached(ModAttachments.STATE, state.withCallTime(now));
	}

	/**
	 * Makes all mobs of the species of the mob in the radius target the player.
	 */
	public static void callAllies(Mob mob, float radius, Player player) {
		AABB area = mob.getBoundingBox().inflate(radius);
		for (Mob ally : mob.level().getEntitiesOfClass(Mob.class, area, other -> other.getType() == mob.getType())) {
			ally.setTarget(player);
		}
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
	 * Lets the witch drink the potion of the drink effect, if the state is true, the cooldown is over, and the witch
	 * does not drink now. The vanilla witch then ends the drink and gets the effects.
	 *
	 * @return true if the witch started to drink
	 */
	public static boolean drink(Witch witch, ServerLevel level, GeneEffect.Drink drink) {
		if (witch.isDrinkingPotion() || !GeneCombat.test(witch, drink.when())) {
			return false;
		}
		long now = level.getGameTime();
		java.util.Map<GeneEffect.Drink, Long> times = witch.getAttachedOrElse(ModAttachments.DRINK_TIMES, java.util.Map.of());
		Long last = times.get(drink);
		if (last != null && now - last < Math.round(drink.cooldownSeconds() * 20.0F)) {
			return false;
		}
		List<MobEffectInstance> effects = new java.util.ArrayList<>();
		for (MobEffectInstance instance : drink.potion().value().getEffects()) {
			int ticks = drink.seconds().map(seconds -> Math.round(seconds * 20.0F)).orElse(instance.getDuration());
			effects.add(new MobEffectInstance(instance.getEffect(), ticks, instance.getAmplifier()));
		}
		// A witch that has each lasting effect of the potion already does not need it.
		if (!effects.isEmpty() && effects.stream().allMatch(instance -> !instance.getEffect().value().isInstantaneous()
				&& witch.hasEffect(instance.getEffect()))) {
			return false;
		}
		ItemStack potion = PotionContents.createItemStack(Items.POTION, drink.potion());
		if (drink.seconds().isPresent()) {
			potion.set(DataComponents.POTION_CONTENTS, new PotionContents(java.util.Optional.empty(),
					java.util.Optional.of(new PotionContents(drink.potion()).getColor()), effects, java.util.Optional.empty()));
		}
		// The same steps as the vanilla drink of the witch.
		witch.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, potion);
		((WitchAccessor) witch).originofspawns$setUsingTime(potion.getUseDuration(witch));
		witch.setUsingItem(true);
		if (!witch.isSilent()) {
			level.playSound(null, witch.getX(), witch.getY(), witch.getZ(), net.minecraft.sounds.SoundEvents.WITCH_DRINK,
					witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
		}
		AttributeInstance speed = witch.getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeModifier slow = WitchAccessor.originofspawns$drinkingModifier();
		speed.removeModifier(slow.id());
		speed.addTransientModifier(slow);
		java.util.Map<GeneEffect.Drink, Long> updated = new java.util.HashMap<>(times);
		updated.put(drink, now);
		witch.setAttached(ModAttachments.DRINK_TIMES, updated);
		return true;
	}

	/**
	 * Returns the life of the mob in ticks, with the lifespan effects of its genes. A life with no end gives the
	 * largest number.
	 *
	 * @param ticks the vanilla life
	 */
	public static int lifespan(Mob mob, int ticks) {
		int life = ticks;
		boolean changed = false;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.Lifespan span) {
				if (span.endless()) {
					return Integer.MAX_VALUE;
				}
				int next = span.seconds().map(seconds -> Math.round(seconds * 20.0F))
						.orElse(Math.round(ticks * span.multiplier()));
				life = changed ? Math.max(life, next) : next;
				changed = true;
			}
		}
		return life;
	}

	/**
	 * Lets the endermites of the age spawn effects of the mob come out, when the mob dies of age. They get genes from
	 * the pool.
	 */
	public static void ageSpawn(Mob mob, ServerLevel level) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.AgeSpawn spawn) {
				spawnBrood(mob, level, spawn.count(), EntitySpawnReason.REINFORCEMENT, true, 0.0F);
			}
		}
	}

	/**
	 * The vanilla chance that a thrown ender pearl makes an endermite.
	 */
	private static final float PEARL_CHANCE = 0.05F;

	/**
	 * Changes the roll of the vanilla endermite of an ender pearl. A roll under the vanilla chance makes the vanilla
	 * endermite, which gets its genes as a new mob. Another roll makes an endermite with genes from the pool here, if
	 * its genes give it a higher chance. The vanilla code then makes no endermite.
	 *
	 * @return the roll for the vanilla code
	 */
	public static float pearlRoll(ServerLevel level, net.minecraft.world.entity.Entity owner, float roll) {
		if (roll < PEARL_CHANCE || !level.isSpawningMonsters()
				|| level.getLevelData().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
			return roll;
		}
		net.minecraft.world.entity.monster.Endermite candidate = EntityTypes.ENDERMITE.create(level, EntitySpawnReason.TRIGGERED);
		if (candidate != null) {
			candidate.snapTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
			Evolution.rollGenes(candidate, level);
			pearlSpawn(level, candidate, level.getRandom().nextFloat());
		}
		return roll;
	}

	/**
	 * Adds the endermite of a pearl to the level, if its genes give it a higher chance than the vanilla chance. The
	 * roll comes after the failed vanilla roll, so the full chance of the endermite is the chance of its gene.
	 *
	 * @return true if the endermite joined the level
	 */
	public static boolean pearlSpawn(ServerLevel level, net.minecraft.world.entity.monster.Endermite candidate, float roll) {
		float chance = (float) GeneEffects.of(candidate).filter(GeneEffect.PearlChance.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.PearlChance) effect).chance()).max().orElse(0.0);
		if (chance <= PEARL_CHANCE || roll >= (chance - PEARL_CHANCE) / (1.0F - PEARL_CHANCE)) {
			return false;
		}
		level.addFreshEntity(candidate);
		// The endermite joins with its genes, so the load does not run its spawn effects. They run here.
		onSpawn(candidate, level, EntitySpawnReason.TRIGGERED);
		return true;
	}

	/**
	 * Returns true if a gene of the mob lets it pick up the block.
	 */
	public static boolean canPickUp(Mob mob, net.minecraft.world.level.block.state.BlockState state) {
		return GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.PickupBlocks pickup
				&& state.is(pickup.blocks()));
	}

	/**
	 * Gives the effect of the stare to each player that stares at the face of the mob now.
	 *
	 * @return the number of players that got the effect
	 */
	public static int stareEffect(Mob mob, ServerLevel level, GeneEffect.StareEffect stare) {
		int stared = 0;
		for (Player player : level.players()) {
			if (player.isAlive() && !player.isSpectator() && GeneCombat.staredBy(mob, player)) {
				player.addEffect(new MobEffectInstance(stare.effect(), Math.round(stare.seconds() * 20.0F),
						stare.amplifier()), mob);
				stared++;
			}
		}
		return stared;
	}

	/**
	 * Throws a splash potion of Healing at the nearest hurt monster in the radius, one time in each cooldown. The
	 * potion of an undead monster is Harming, which heals it. A witch that drinks does not throw.
	 *
	 * @return the monster that the mob threw at, or null
	 */
	public static Mob healThrow(Mob mob, ServerLevel level, GeneEffect.HealThrow heal) {
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.HEAL_THROW_TIME, Long.MIN_VALUE);
		if (mob instanceof Witch witch && witch.isDrinkingPotion()
				|| last != Long.MIN_VALUE && now - last < Math.round(heal.cooldownSeconds() * 20.0F)) {
			return null;
		}
		AABB area = mob.getBoundingBox().inflate(heal.radius());
		Mob hurt = level.getEntitiesOfClass(Mob.class, area, other -> other != mob && other != mob.getTarget()
						&& other instanceof net.minecraft.world.entity.monster.Enemy && other.isAlive()
						&& other.getHealth() < other.getMaxHealth() && mob.distanceTo(other) <= heal.radius()
						&& mob.hasLineOfSight(other))
				.stream().min(java.util.Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
		if (hurt == null) {
			return null;
		}
		ItemStack potion = PotionContents.createItemStack(Items.SPLASH_POTION,
				hurt.isInvertedHealAndHarm() ? net.minecraft.world.item.alchemy.Potions.HARMING
						: net.minecraft.world.item.alchemy.Potions.HEALING);
		// The same aim as the vanilla throw of the witch.
		double x = hurt.getX() + hurt.getDeltaMovement().x - mob.getX();
		double y = hurt.getEyeY() - 1.1F - mob.getY();
		double z = hurt.getZ() + hurt.getDeltaMovement().z - mob.getZ();
		double distance = Math.sqrt(x * x + z * z);
		Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, potion, mob, x, y + distance * 0.2, z,
				distance <= 2.0 ? 0.45F : 0.75F, 0.0F);
		if (!mob.isSilent()) {
			level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), net.minecraft.sounds.SoundEvents.WITCH_THROW,
					mob.getSoundSource(), 1.0F, 0.8F + mob.getRandom().nextFloat() * 0.4F);
		}
		mob.setAttached(ModAttachments.HEAL_THROW_TIME, now);
		return hurt;
	}

	/**
	 * Gives the effects of the potion that the witch drank to the monsters near it, if a gene shares its drinks.
	 */
	public static void shareDrink(Witch witch, PotionContents contents, float durationScale) {
		if (!(witch.level() instanceof ServerLevel level)) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(witch).toList()) {
			if (effect instanceof GeneEffect.SharedDrink shared) {
				AABB area = witch.getBoundingBox().inflate(shared.radius());
				for (Mob ally : level.getEntitiesOfClass(Mob.class, area,
						other -> other != witch && other instanceof net.minecraft.world.entity.monster.Enemy)) {
					contents.forEachEffect(instance -> ally.addEffect(instance, witch), durationScale);
				}
			}
		}
	}

	/**
	 * Throws the potion of the mob at a target, with the changes of its genes. A witch calls this in place of its
	 * vanilla throw, but not when it heals a raider.
	 *
	 * @return the thrown potion
	 */
	public static AbstractThrownPotion throwPotion(Mob mob, ServerLevel level, ItemStack stack, double x, double y,
			double z, float power, float uncertainty) {
		GeneEffect.PotionCount count = GeneEffects.of(mob).filter(GeneEffect.PotionCount.class::isInstance)
				.map(GeneEffect.PotionCount.class::cast)
				.max(java.util.Comparator.comparingInt(GeneEffect.PotionCount::count))
				.orElse(new GeneEffect.PotionCount(1, 0.0F));
		AbstractThrownPotion first = null;
		for (int i = 0; i < count.count(); i++) {
			// The potions fan out to both sides of the aim, by the same angle each.
			double angle = Math.toRadians((i - (count.count() - 1) / 2.0) * count.spreadDegrees());
			double turnedX = x * Math.cos(angle) - z * Math.sin(angle);
			double turnedZ = x * Math.sin(angle) + z * Math.cos(angle);
			ItemStack brewed = brew(mob, stack);
			AbstractThrownPotion potion = brewed.is(Items.LINGERING_POTION)
					? new ThrownLingeringPotion(level, mob, brewed) : new ThrownSplashPotion(level, mob, brewed);
			potion.setAttached(ModAttachments.GENE_POTION, true);
			Projectile.spawnProjectileUsingShoot(potion, level, brewed, turnedX, y, turnedZ, power, uncertainty);
			if (first == null) {
				first = potion;
			}
		}
		return first;
	}

	/**
	 * Returns the product of the splash multipliers of the owner of a potion. A potion of a player or of a mob with no
	 * genes gives 1.
	 */
	public static float splashMultiplier(net.minecraft.world.entity.Entity owner) {
		float multiplier = 1.0F;
		if (owner instanceof Mob mob) {
			for (GeneEffect effect : GeneEffects.of(mob).toList()) {
				if (effect instanceof GeneEffect.PotionSplash splash) {
					multiplier *= splash.multiplier();
				}
			}
		}
		return multiplier;
	}

	/**
	 * Returns a copy of the potion with the changes of the genes of the mob: a lingering potion, the swapped and the
	 * extra effects, the stronger levels, and the longer time.
	 */
	public static ItemStack brew(Mob mob, ItemStack stack) {
		float duration = 1.0F;
		int levels = 0;
		boolean lingering = false;
		List<GeneEffect.PotionSwap> swaps = new java.util.ArrayList<>();
		List<GeneEffect.PotionExtra> extras = new java.util.ArrayList<>();
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.PotionDuration more -> duration *= more.multiplier();
				case GeneEffect.PotionLevel more -> levels += more.levels();
				case GeneEffect.PotionLingering ignored -> lingering = true;
				case GeneEffect.PotionSwap swap -> swaps.add(swap);
				case GeneEffect.PotionExtra extra -> extras.add(extra);
				default -> {
				}
			}
		}
		ItemStack brewed = lingering ? stack.transmuteCopy(Items.LINGERING_POTION) : stack.copy();
		PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		List<MobEffectInstance> effects = new java.util.ArrayList<>();
		boolean changed = false;
		for (MobEffectInstance instance : contents.getAllEffects()) {
			Holder<MobEffect> type = instance.getEffect();
			int ticks = instance.getDuration();
			for (GeneEffect.PotionSwap swap : swaps) {
				if (type.value() == swap.from().value()) {
					type = swap.to();
					ticks = swap.seconds().map(seconds -> Math.round(seconds * 20.0F)).orElse(ticks);
					changed = true;
				}
			}
			int amplifier = instance.getAmplifier();
			if (levels > 0 && !type.value().isInstantaneous()) {
				amplifier += levels;
				changed = true;
			}
			effects.add(new MobEffectInstance(type, ticks, amplifier, instance.isAmbient(), instance.isVisible()));
			for (GeneEffect.PotionExtra extra : extras) {
				if (instance.getEffect().value() == extra.with().value()) {
					effects.add(new MobEffectInstance(extra.effect(), Math.round(extra.seconds() * 20.0F), extra.amplifier()));
					changed = true;
				}
			}
		}
		if (changed) {
			brewed.set(DataComponents.POTION_CONTENTS, new PotionContents(java.util.Optional.empty(),
					java.util.Optional.of(contents.getColor()), effects, java.util.Optional.empty()));
		}
		if (duration != 1.0F) {
			brewed.set(DataComponents.POTION_DURATION_SCALE,
					brewed.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F) * duration);
		}
		return brewed;
	}

	/**
	 * Gives the target of the mob the effect of the hex, one time in each cooldown, if the mob sees the target in the
	 * range. The mob and the target show the spell particles of the evoker, in the color of the effect.
	 *
	 * @return true if the mob cast the hex
	 */
	public static boolean hex(Mob mob, ServerLevel level, GeneEffect.Hex hex) {
		long now = level.getGameTime();
		java.util.Map<GeneEffect.Hex, Long> times = mob.getAttachedOrElse(ModAttachments.HEX_TIMES, java.util.Map.of());
		Long last = times.get(hex);
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) > hex.range() || !mob.hasLineOfSight(target)
				|| last != null && now - last < Math.round(hex.cooldownSeconds() * 20.0F)) {
			return false;
		}
		target.addEffect(new MobEffectInstance(hex.effect(), Math.round(hex.seconds() * 20.0F), hex.amplifier()), mob);
		int color = hex.effect().value().getColor();
		var particle = net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,
				(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
		level.sendParticles(particle, mob.getX(), mob.getY(0.9), mob.getZ(), 12, 0.4, 0.3, 0.4, 1.0);
		level.sendParticles(particle, target.getX(), target.getY(0.5), target.getZ(), 20, 0.3, 0.5, 0.3, 1.0);
		level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
				mob.getSoundSource(), 1.0F, 1.0F);
		java.util.Map<GeneEffect.Hex, Long> updated = new java.util.HashMap<>(times);
		updated.put(hex, now);
		mob.setAttached(ModAttachments.HEX_TIMES, updated);
		return true;
	}

	/**
	 * Summons the vexes of a decoy near the mob, one time in each cooldown. The vexes attack the attacker.
	 *
	 * @return the number of vexes that the mob summoned
	 */
	public static int decoy(Mob mob, ServerLevel level, GeneEffect.Decoy decoy, LivingEntity attacker) {
		long now = level.getGameTime();
		long last = mob.getAttachedOrElse(ModAttachments.DECOY_TIME, Long.MIN_VALUE);
		if (last != Long.MIN_VALUE && now - last < Math.round(decoy.cooldownSeconds() * 20.0F)) {
			return 0;
		}
		mob.setAttached(ModAttachments.DECOY_TIME, now);
		int summoned = 0;
		for (int i = 0; i < decoy.count(); i++) {
			BlockPos pos = mob.blockPosition().offset(-2 + mob.getRandom().nextInt(5), 1, -2 + mob.getRandom().nextInt(5));
			Vex vex = EntityTypes.VEX.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (vex == null) {
				continue;
			}
			// The same steps as the vex spell of the evoker.
			vex.snapTo(pos, 0.0F, 0.0F);
			vex.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
			vex.setOwner(mob);
			vex.setBoundOrigin(pos);
			vex.setLimitedLife(20 * (30 + mob.getRandom().nextInt(90)));
			if (mob.getTeam() != null) {
				level.getScoreboard().addPlayerToTeam(vex.getScoreboardName(), mob.getTeam());
			}
			shapeVex(mob, vex);
			vex.setTarget(attacker);
			level.addFreshEntityWithPassengers(vex);
			summoned++;
		}
		level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY(0.6), mob.getZ(), 10, 0.4, 0.4, 0.4, 0.02);
		return summoned;
	}

	/**
	 * Returns the number of vexes that the vex spell of the mob summons. The genes can change the vanilla number.
	 */
	public static int vexCount(Mob mob, int count) {
		return GeneEffects.of(mob).filter(GeneEffect.VexCount.class::isInstance).mapToInt(effect ->
				((GeneEffect.VexCount) effect).count()).max().orElse(count);
	}

	/**
	 * Gives a vex that the mob summons the health, the damage, and the life of the genes of the mob. A vex has no
	 * genes of its own.
	 */
	public static void shapeVex(Mob mob, Vex vex) {
		double health = 1.0;
		double damage = 1.0;
		double life = 1.0;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.VexHealth more -> health *= more.multiplier();
				case GeneEffect.VexDamage more -> damage *= more.multiplier();
				case GeneEffect.VexLife more -> life *= more.multiplier();
				default -> {
				}
			}
		}
		if (health != 1.0) {
			vex.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(
					OriginOfSpawns.id("vex_health"), health - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			vex.setHealth(vex.getMaxHealth());
		}
		if (damage != 1.0) {
			vex.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(
					OriginOfSpawns.id("vex_damage"), damage - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		VexAccessor accessor = (VexAccessor) vex;
		if (life != 1.0 && accessor.originofspawns$hasLimitedLife()) {
			vex.setLimitedLife(Math.max(1, (int) Math.round(accessor.originofspawns$limitedLifeTicks() * life)));
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
	/**
	 * Changes the attack cooldown of a mob with a brain, such as a piglin brute or a hoglin, by its attack interval
	 * genes. The brain sets a new cooldown after each hit, and this changes each new cooldown one time.
	 */
	public static void scaleBrainCooldown(Mob mob, ServerLevel level) {
		var brain = mob.getBrain();
		var cooling = net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_COOLING_DOWN;
		if (!brain.checkMemory(cooling, net.minecraft.world.entity.ai.memory.MemoryStatus.VALUE_PRESENT)) {
			return;
		}
		float multiplier = attackIntervalMultiplier(mob);
		long now = level.getGameTime();
		long remaining = brain.getTimeUntilExpiry(cooling);
		if (multiplier == 1.0F || mob.getAttachedOrElse(ModAttachments.COOLDOWN_END, -1L) == now + remaining) {
			return;
		}
		long scaled = Math.max(1L, Math.round(remaining * multiplier));
		brain.setMemoryWithExpiry(cooling, true, scaled);
		mob.setAttached(ModAttachments.COOLDOWN_END, now + scaled);
	}

	/**
	 * Returns the product of the flying speed multipliers of the mob. A mob with no such gene gives 1.
	 */
	public static double flyingSpeedMultiplier(LivingEntity mob) {
		double multiplier = GeneEffects.of(mob).filter(GeneEffect.FlyingSpeed.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.FlyingSpeed) effect).multiplier()).reduce(1.0, (a, b) -> a * b);
		return fleeing(mob).map(flee -> multiplier * flee.speed()).orElse(multiplier);
	}

	/**
	 * Starts the flee of the mob, if a flee gene of the mob is under its threshold and its cooldown is over.
	 */
	public static void startFlee(Mob mob) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.Flee flee && mob.getHealth() < mob.getMaxHealth() * flee.threshold()
					&& GeneCombat.ready(mob, flee, Math.max(flee.seconds(), flee.cooldownSeconds()))) {
				GeneCombat.startCooldown(mob, flee);
				return;
			}
		}
	}

	/**
	 * Returns the flee gene of the mob whose flee runs now.
	 */
	public static java.util.Optional<GeneEffect.Flee> fleeing(LivingEntity mob) {
		return GeneEffects.of(mob).filter(GeneEffect.Flee.class::isInstance).map(GeneEffect.Flee.class::cast)
				.filter(flee -> !GeneCombat.ready(mob, flee, flee.seconds())
						&& mob.getAttachedOrElse(ModAttachments.EFFECT_TIMES, java.util.Map.of()).containsKey(flee))
				.findFirst();
	}

	/**
	 * While the mob flees, sets the place that it moves to: 16 blocks away from its target, at the speed of the flee.
	 * The game runs this after the goals of the mob, so that no goal changes the place.
	 */
	public static void fleeMove(Mob mob) {
		LivingEntity target = mob.getTarget();
		var flee = fleeing(mob);
		if (target == null || flee.isEmpty()) {
			return;
		}
		Vec3 away = mob.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
		if (away.lengthSqr() < 1.0E-6) {
			away = new Vec3(1.0, 0.0, 0.0);
		}
		Vec3 place = mob.position().add(away.normalize().scale(16.0));
		mob.getMoveControl().setWantedPosition(place.x, mob.getY(), place.z, flee.get().speed());
	}

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
						result.hurt() || silence.hurt(), result.steps() || silence.steps(), result.death() || silence.death(),
						result.all() || silence.all(), result.warning() || silence.warning());
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
