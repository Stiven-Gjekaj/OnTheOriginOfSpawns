package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.FireworkRocketEntityAccessor;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the pillager genes use: the crossbow, the rockets, the patrol, and the raid. It also tests
 * that the reused effects work on a pillager.
 */
public class PillagerEffectsTest {
	@GameTest
	public void aDeathInARaidCountsForTheRaidPressure(GameTestHelper helper) {
		Pillager raider = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager patroller = pillagerWith(helper, new BlockPos(4, 1, 1));
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.NORMAL);
		raid.joinRaid(helper.getLevel(), 1, raider, null, true);
		var source = helper.getLevel().damageSources().generic();
		raider.hurtServer(helper.getLevel(), source, 100.0F);
		patroller.hurtServer(helper.getLevel(), source, 100.0F);
		// The raid lets the raider go when it dies, so only the record of the death can tell.
		helper.assertTrue(raider.getCurrentRaid() == null, "the raid let the raider go");
		helper.assertTrue(Evolution.facts(raider, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death in a raid");
		helper.assertFalse(Evolution.facts(patroller, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death out of a raid");
		helper.succeed();
	}

	@GameTest
	public void theDrawTimeChangesTheLoadTimeOfTheCrossbow(GameTestHelper helper) {
		Pillager plain = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager quick = pillagerWith(helper, new BlockPos(3, 1, 1), "test_quick_charge");
		ItemStack crossbow = new ItemStack(Items.CROSSBOW);
		helper.assertValueEqual(CrossbowItem.getChargeDuration(crossbow, plain), 25, "the vanilla load time");
		helper.assertValueEqual(CrossbowItem.getChargeDuration(crossbow, quick), 18, "30% faster");
		helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void aFastShooterShootsItsCrossbowSooner(GameTestHelper helper) {
		// The goal sees the target for 5 ticks first. Then the plain pillager loads for 25 ticks and waits 20 to 39
		// ticks, so it shoots at 52 or later. The fast one shoots 4 times as often, so it loads for 6 ticks and waits 5
		// to 10 ticks, and it shoots at 23 or sooner. With the vanilla load or the vanilla wait, it shoots at 32 or later.
		long plain = firstShotTick(helper, new BlockPos(1, 1, 1));
		long fast = firstShotTick(helper, new BlockPos(1, 1, 6), "test_fast_crossbow");
		helper.runAfterDelay(90, () -> {
			helper.assertTrue(SHOTS.containsKey(plain) && SHOTS.get(plain) >= 50, "the plain shot, at " + SHOTS.get(plain));
			helper.assertTrue(SHOTS.containsKey(fast) && SHOTS.get(fast) <= 25, "the fast shot, at " + SHOTS.get(fast));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 120)
	public void theRangeGenesChangeTheRangeOfTheCrossbow(GameTestHelper helper) {
		// The vanilla range is 8 blocks, so a target 6 blocks away is in it. With half the range, it is not.
		long plain = firstShotTick(helper, new BlockPos(1, 1, 1), 6);
		long near = firstShotTick(helper, new BlockPos(1, 1, 6), 6, "test_short_bolt");
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(SHOTS.containsKey(plain), "the plain pillager shoots");
			helper.assertFalse(SHOTS.containsKey(near), "the pillager with half the range does not shoot so far");
			helper.succeed();
		});
	}

	@GameTest
	public void noSpreadShootsEachBoltOnTheSameLine(GameTestHelper helper) {
		helper.assertTrue(directions(helper, new BlockPos(1, 1, 1)).size() > 1, "the vanilla bolts spread");
		helper.assertValueEqual(directions(helper, new BlockPos(1, 1, 6), "test_true_bolt").size(), 1,
				"with no spread, each bolt flies on the same line");
		helper.succeed();
	}

	@GameTest
	public void theArrowGenesActOnTheBolts(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_splinter");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		pillager.setTarget(pig);
		load(pillager);
		pillager.performRangedAttack(pig, 1.0F);
		AbstractArrow bolt = boltsOf(helper, pillager).getFirst();
		PotionContents contents = bolt.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS,
				PotionContents.EMPTY);
		helper.assertTrue(contents.customEffects().stream().anyMatch(effect -> effect.is(MobEffects.SLOWNESS)
				&& effect.getDuration() == 40), "the bolt carries Slowness for 2 seconds: " + contents);
		helper.assertValueEqual(bolt.getAttached(ModAttachments.GHOST_ARROW), 12.0F, "the bolt is a ghost arrow");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void theHitGenesActOnABoltThatHits(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_bolt_hits");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		pillager.setTarget(pig);
		load(pillager);
		pillager.performRangedAttack(pig, 1.0F);
		AbstractArrow bolt = boltsOf(helper, pillager).getFirst();
		// A bolt is a projectile hit, so the bonus for projectile hits acts on it, and a melee hit gets none.
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().arrow(bolt, pillager), 4.0F),
				6.0F, "the bolt damage");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().mobAttack(pillager), 4.0F),
				4.0F, "the melee damage");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void aBoltThatHitsGivesItsEffectsAndItsFire(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_bolt_hits");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		pillager.setTarget(pig);
		load(pillager);
		pillager.performRangedAttack(pig, 1.0F);
		helper.succeedWhen(() -> {
			helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "the bolt gives Slowness");
			helper.assertTrue(pig.getRemainingFireTicks() > 0, "the bolt sets the pig on fire");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 40)
	public void aPiercerBoltGoesThroughAShield(GameTestHelper helper) {
		ServerPlayer plainTarget = CreeperEffectsTest.shieldedPlayer(helper);
		helper.assertFalse(boltHurts(helper, pillagerWith(helper, new BlockPos(1, 1, 1)), plainTarget),
				"the shield stops a plain bolt");
		helper.assertTrue(plainTarget.getOffhandItem().getDamageValue() > 0, "a plain bolt damages the shield");
		ServerPlayer target = CreeperEffectsTest.shieldedPlayer(helper);
		helper.assertTrue(boltHurts(helper, pillagerWith(helper, new BlockPos(1, 1, 3), "test_bolt_hits"), target),
				"the bolt hurts the player behind the shield");
		helper.assertValueEqual(target.getOffhandItem().getDamageValue(), 0, "the shield takes no damage");
		helper.succeed();
	}

	/**
	 * Hits the player with a bolt of the pillager from the west, and returns true if the player lost health.
	 */
	static boolean boltHurts(GameTestHelper helper, Pillager pillager, ServerPlayer player) {
		Arrow bolt = new Arrow(helper.getLevel(), pillager, new ItemStack(Items.ARROW), new ItemStack(Items.CROSSBOW));
		bolt.setPos(helper.absoluteVec(new Vec3(2.0, 1.5, 1.5)));
		float before = player.getHealth();
		player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(bolt, pillager), 4.0F);
		return player.getHealth() < before;
	}

	@GameTest
	public void aRocketChanceShootsARocketInPlaceOfTheBolt(GameTestHelper helper) {
		Pillager plain = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager bomber = pillagerWith(helper, new BlockPos(1, 1, 4), "test_rocket_chance");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 2));
		for (Pillager pillager : List.of(plain, bomber)) {
			pillager.setTarget(pig);
			load(pillager);
			pillager.performRangedAttack(pig, 1.0F);
		}
		helper.assertValueEqual(boltsOf(helper, plain).size(), 1, "the plain pillager shoots a bolt");
		helper.assertTrue(rocketsOf(helper, plain).isEmpty(), "the plain pillager shoots no rocket");
		helper.assertTrue(boltsOf(helper, bomber).isEmpty(), "the bomber shoots no bolt");
		List<FireworkRocketEntity> rockets = rocketsOf(helper, bomber);
		helper.assertValueEqual(rockets.size(), 1, "the bomber shoots a rocket");
		helper.assertValueEqual(rockets.getFirst().getItem().get(DataComponents.FIREWORKS).explosions().size(), 1,
				"the rocket has one star, so its blast hurts");
		helper.succeed();
	}

	@GameTest
	public void aRocketeerShootsARocketOneTimeInEachCooldown(GameTestHelper helper) {
		Pillager rocketeer = pillagerWith(helper, new BlockPos(1, 1, 1), "test_rocketeer");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		var shot = new GeneEffect.RocketShot(10.0F, 16.0F);
		helper.assertFalse(GeneBehaviors.rocketShot(rocketeer, helper.getLevel(), shot), "no target, no rocket");
		rocketeer.setTarget(pig);
		helper.assertTrue(GeneBehaviors.rocketShot(rocketeer, helper.getLevel(), shot), "the first rocket");
		helper.assertValueEqual(rocketsOf(helper, rocketeer).size(), 1, "one rocket flies");
		helper.assertFalse(GeneBehaviors.rocketShot(rocketeer, helper.getLevel(), shot), "no rocket in the cooldown");
		rocketeer.setAttached(ModAttachments.ROCKET_TIME, helper.getLevel().getGameTime() - 300L);
		helper.assertTrue(GeneBehaviors.rocketShot(rocketeer, helper.getLevel(), shot), "a rocket after the cooldown");
		helper.succeed();
	}

	@GameTest
	public void aFlakRocketHasALargerBlast(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 1));
		// The vanilla blast reaches 5 blocks, so the pig 6 blocks away is safe. The Flak blast reaches 7.5 blocks.
		blastRocketOf(helper, pillagerWith(helper, new BlockPos(1, 1, 3)));
		helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth(), "the plain blast does not reach the pig");
		blastRocketOf(helper, pillagerWith(helper, new BlockPos(1, 1, 4), "test_flak"));
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the Flak blast reaches the pig");
		helper.succeed();
	}

	/**
	 * Makes the blast of a rocket of the pillager 6 blocks west of the pig.
	 */
	static void blastRocketOf(GameTestHelper helper, Pillager pillager) {
		Vec3 place = helper.absoluteVec(new Vec3(1.5, 1.5, 1.5));
		FireworkRocketEntity rocket = new FireworkRocketEntity(helper.getLevel(), GeneBehaviors.rocket(), pillager,
				place.x, place.y, place.z, true);
		((FireworkRocketEntityAccessor) rocket).originofspawns$dealExplosionDamage(helper.getLevel());
	}

	@GameTest
	public void aPatrolLeaderBringsOneMorePillagerToAPatrol(GameTestHelper helper) {
		// Each case uses the same place in the test area, so each pillager is in a loaded chunk.
		helper.assertValueEqual(patrolAfter(helper, EntitySpawnReason.PATROL), 2, "a patrol gets one more pillager");
		helper.assertValueEqual(patrolAfter(helper, EntitySpawnReason.EVENT), 1, "a raid does not change");
		helper.assertValueEqual(patrolAfter(helper, EntitySpawnReason.NATURAL), 1, "an outpost does not change");
		helper.succeed();
	}

	/**
	 * Spawns a Patrol Leader for the reason, and returns the number of pillagers with its gene near it. Each extra
	 * pillager must follow the patrol. Then it removes them, so that the next case starts with none.
	 */
	static int patrolAfter(GameTestHelper helper, EntitySpawnReason reason) {
		Pillager leader = pillagerWith(helper, new BlockPos(2, 1, 2), "test_patrol_leader");
		leader.setPatrolTarget(helper.absolutePos(new BlockPos(2, 1, 6)));
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_patrol_leader");
		GeneBehaviors.onSpawn(leader, helper.getLevel(), reason);
		List<Pillager> patrol = helper.getLevel().getEntitiesOfClass(Pillager.class, leader.getBoundingBox().inflate(3.0),
				other -> other.getAttachedOrElse(ModAttachments.GENES, CarriedGenes.NONE).genes().contains(gene));
		for (Pillager member : patrol) {
			helper.assertTrue(member.hasPatrolTarget() && helper.absolutePos(new BlockPos(2, 1, 6)).equals(member.getPatrolTarget()),
					"each pillager follows the patrol");
		}
		patrol.forEach(Pillager::discard);
		return patrol.size();
	}

	@GameTest
	public void aLooterBoltTakesFoodWithItsChance(GameTestHelper helper) {
		Pillager always = pillagerWith(helper, new BlockPos(1, 1, 1), "test_looter");
		Pillager never = pillagerWith(helper, new BlockPos(1, 1, 3), "test_looter_never");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getFoodData().setFoodLevel(20);
		GeneCombat.onHit(always, player);
		helper.assertValueEqual(player.getFoodData().getFoodLevel(), 20, "a melee hit takes no food from a bolt gene");
		GeneCombat.onProjectileHit(always, player);
		helper.assertValueEqual(player.getFoodData().getFoodLevel(), 18, "a bolt takes 2 food points");
		GeneCombat.onProjectileHit(never, player);
		helper.assertValueEqual(player.getFoodData().getFoodLevel(), 18, "a chance of 0 takes no food");
		helper.succeed();
	}

	@GameTest
	public void aGreedBoltTakesXp(GameTestHelper helper) {
		Pillager greedy = pillagerWith(helper, new BlockPos(1, 1, 1), "test_greed");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.giveExperiencePoints(20);
		GeneCombat.onHit(greedy, player);
		helper.assertValueEqual(player.totalExperience, 20, "a melee hit takes no XP from a bolt gene");
		GeneCombat.onProjectileHit(greedy, player);
		helper.assertValueEqual(player.totalExperience, 17, "a bolt takes 3 XP points");
		helper.succeed();
	}

	@GameTest
	public void aPlundererKeepsWhatItPicksUpAndDropsItWhenItDies(GameTestHelper helper) {
		Pillager plain = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager plunderer = pillagerWith(helper, new BlockPos(1, 1, 4), "test_plunder");
		GeneBehaviors.onSpawn(plunderer, helper.getLevel(), EntitySpawnReason.NATURAL);
		ItemEntity plainLoot = lootAt(helper, plain);
		ItemEntity loot = lootAt(helper, plunderer);
		((MobAccessor) plain).originofspawns$pickUpItem(helper.getLevel(), plainLoot);
		((MobAccessor) plunderer).originofspawns$pickUpItem(helper.getLevel(), loot);
		helper.assertFalse(plainLoot.isRemoved(), "a plain pillager does not pick up the bread");
		helper.assertTrue(plunderer.getInventory().hasAnyMatching(stack -> stack.is(Items.BREAD)), "the bread is in the sack");
		helper.assertTrue(loot.isRemoved(), "the bread is gone from the ground");
		plunderer.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 100.0F);
		helper.assertFalse(helper.getLevel().getEntitiesOfClass(ItemEntity.class, plunderer.getBoundingBox().inflate(2.0),
				item -> item.getItem().is(Items.BREAD)).isEmpty(), "the plunderer drops the bread when it dies");
		helper.succeed();
	}

	static ItemEntity lootAt(GameTestHelper helper, Pillager pillager) {
		ItemEntity item = new ItemEntity(helper.getLevel(), pillager.getX(), pillager.getY(), pillager.getZ(),
				new ItemStack(Items.BREAD, 3));
		helper.getLevel().addFreshEntity(item);
		return item;
	}

	static List<FireworkRocketEntity> rocketsOf(GameTestHelper helper, Pillager pillager) {
		return helper.getLevel().getEntitiesOfClass(FireworkRocketEntity.class, pillager.getBoundingBox().inflate(8.0),
				rocket -> rocket.getOwner() == pillager);
	}

	/**
	 * Makes a pillager with the genes shoot 4 loaded bolts at a pig, and returns the directions of the bolts.
	 */
	static java.util.Set<Vec3> directions(GameTestHelper helper, BlockPos pos, String... genes) {
		Pillager pillager = pillagerWith(helper, pos, genes);
		Pig pig = CreeperEffectsTest.pigAt(helper, pos.east(5));
		pillager.setTarget(pig);
		java.util.Set<Vec3> directions = new java.util.HashSet<>();
		for (int i = 0; i < 4; i++) {
			List<AbstractArrow> before = boltsOf(helper, pillager);
			load(pillager);
			pillager.performRangedAttack(pig, 1.0F);
			for (AbstractArrow bolt : boltsOf(helper, pillager)) {
				if (!before.contains(bolt)) {
					directions.add(bolt.getDeltaMovement());
				}
			}
		}
		return directions;
	}

	static List<AbstractArrow> boltsOf(GameTestHelper helper, Pillager pillager) {
		return helper.getLevel().getEntitiesOfClass(AbstractArrow.class, pillager.getBoundingBox().inflate(8.0),
				arrow -> arrow.getOwner() == pillager);
	}

	/**
	 * Loads one arrow in the crossbow of the pillager, as a full load does.
	 */
	static void load(Pillager pillager) {
		pillager.getMainHandItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(
				List.of(new ItemStack(Items.ARROW))));
	}

	/**
	 * The tick of the first shot of each pillager that {@link #firstShotTick} watches, by the id of the watch.
	 */
	static final java.util.Map<Long, Long> SHOTS = new java.util.concurrent.ConcurrentHashMap<>();
	static final java.util.concurrent.atomic.AtomicLong WATCHES = new java.util.concurrent.atomic.AtomicLong();

	/**
	 * Makes a pillager with the genes and a pig 5 blocks from it, or at the distance. Each tick runs its crossbow goal, and the tick of its
	 * first arrow goes in {@link #SHOTS}.
	 *
	 * @return the id of the watch
	 */
	static long firstShotTick(GameTestHelper helper, BlockPos pos, String... genes) {
		return firstShotTick(helper, pos, 5, genes);
	}

	static long firstShotTick(GameTestHelper helper, BlockPos pos, int distance, String... genes) {
		Pillager pillager = pillagerWith(helper, pos, genes);
		Pig pig = CreeperEffectsTest.pigAt(helper, pos.east(distance));
		pillager.setTarget(pig);
		Goal goal = crossbowGoal(pillager);
		long watch = WATCHES.incrementAndGet();
		long start = helper.getLevel().getGameTime();
		helper.onEachTick(() -> {
			if (SHOTS.containsKey(watch)) {
				return;
			}
			goal.tick();
			if (!helper.getLevel().getEntitiesOfClass(AbstractArrow.class, pillager.getBoundingBox().inflate(8.0),
					arrow -> arrow.getOwner() == pillager).isEmpty()) {
				SHOTS.put(watch, helper.getLevel().getGameTime() - start);
			}
		});
		return watch;
	}

	static Goal crossbowGoal(Pillager pillager) {
		return ((MobAccessor) pillager).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(RangedCrossbowAttackGoal.class::isInstance).findFirst().orElseThrow();
	}

	static Pillager pillagerWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Pillager pillager = EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		pillager.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
		pillager.setNoAi(true);
		pillager.setPersistenceRequired();
		pillager.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(pillager);
		return pillager;
	}
}
