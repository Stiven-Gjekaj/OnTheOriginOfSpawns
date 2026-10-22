package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.mixin.FireworkRocketEntityAccessor;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
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
import net.minecraft.world.level.block.Blocks;
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

	// The genes of these tests reach only 4 blocks, so they do not reach the pillagers of other tests.
	@GameTest(maxTicks = 80)
	public void aBannerBearerAuraReachesOnlyPillagers(GameTestHelper helper) {
		pillagerWith(helper, new BlockPos(1, 1, 1), "test_banner_bearer");
		Pillager ally = pillagerWith(helper, new BlockPos(4, 1, 1));
		Vindicator vindicator = helper.spawnWithNoFreeWill(EntityTypes.VINDICATOR, new BlockPos(1, 1, 3));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		helper.succeedWhen(() -> {
			helper.assertTrue(ally.hasEffect(MobEffects.RESISTANCE), "the pillager gets Resistance");
			helper.assertFalse(vindicator.hasEffect(MobEffects.RESISTANCE), "the vindicator is not a pillager");
			helper.assertFalse(player.hasEffect(MobEffects.RESISTANCE), "the player is not an ally");
		});
	}

	@GameTest
	public void camouflageHidesAPillagerInGrass(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.GRASS_BLOCK);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.SHORT_GRASS);
		helper.setBlock(new BlockPos(4, 0, 1), Blocks.STONE);
		Pillager inGrass = pillagerWith(helper, new BlockPos(1, 1, 1), "test_camouflage");
		Pillager inOpen = pillagerWith(helper, new BlockPos(4, 1, 1), "test_camouflage");
		helper.assertTrue(GeneEffects.translucentOpacity(inGrass).isPresent(), "short grass hides the pillager");
		helper.assertTrue(GeneEffects.translucentOpacity(inOpen).isEmpty(), "the pillager in the open is solid");
		helper.succeed();
	}

	@GameTest
	public void signalSmokeCallsOnlyThePillagers(GameTestHelper helper) {
		Pillager caller = pillagerWith(helper, new BlockPos(1, 1, 1), "test_signal_smoke");
		Pillager ally = pillagerWith(helper, new BlockPos(4, 1, 1));
		Vindicator vindicator = helper.spawnWithNoFreeWill(EntityTypes.VINDICATOR, new BlockPos(1, 1, 3));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		caller.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(ally.getTarget() == player, "the pillager attacks the attacker");
		helper.assertTrue(vindicator.getTarget() != player, "the vindicator does not answer the call");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void aWarHornCallsThePillagersWhenItSeesAPlayer(GameTestHelper helper) {
		Pillager horn = pillagerWith(helper, new BlockPos(1, 1, 1), "test_war_horn");
		Pillager ally = pillagerWith(helper, new BlockPos(3, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		horn.setTarget(player);
		helper.succeedWhen(() -> helper.assertTrue(ally.getTarget() == player, "the pillager targets the player"));
	}

	@GameTest
	public void rallyCryAddsDamageForEachPillagerNear(GameTestHelper helper) {
		Pillager crier = pillagerWith(helper, new BlockPos(1, 1, 1), "test_rally_cry");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 4));
		var bite = helper.getLevel().damageSources().mobAttack(crier);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 10.0F), 10.0F, "no pillager near");
		pillagerWith(helper, new BlockPos(3, 1, 1));
		pillagerWith(helper, new BlockPos(4, 1, 1));
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pig, bite, 10.0F) - 11.0F) < 0.001F, "two pillagers add 10%");
		helper.succeed();
	}

	@GameTest
	public void grudgeAndPowderKegActWhenThePillagerDies(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_grudge", "test_powder_keg");
		Pillager ally = pillagerWith(helper, new BlockPos(4, 1, 1));
		Vindicator vindicator = helper.spawnWithNoFreeWill(EntityTypes.VINDICATOR, new BlockPos(1, 1, 3));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 1))));
		float before = player.getHealth();
		pillager.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 100.0F);
		helper.assertTrue(ally.hasEffect(MobEffects.STRENGTH), "the pillager gets Strength");
		helper.assertFalse(vindicator.hasEffect(MobEffects.STRENGTH), "the vindicator is not a pillager");
		helper.assertTrue(player.getHealth() < before, "the keg hurts the player near it");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void escapeArtistIsFastOnlyWhenWounded(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_escape_artist");
		double base = pillager.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
		GeneBehaviors.tick(pillager, helper.getLevel());
		helper.assertTrue(pillager.getAttributeValue(Attributes.MOVEMENT_SPEED) < base, "slower while healthy");
		pillager.setHealth(pillager.getMaxHealth() / 4.0F);
		GeneBehaviors.tick(pillager, helper.getLevel());
		helper.assertTrue(pillager.getAttributeValue(Attributes.MOVEMENT_SPEED) > base, "faster while wounded");
		helper.succeed();
	}

	@GameTest
	public void formationIsStrongInAGroupAndWeakAlone(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1), "test_formation");
		var hit = helper.getLevel().damageSources().generic();
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pillager, hit, 10.0F) - 12.0F) < 0.001F, "more damage alone");
		pillagerWith(helper, new BlockPos(4, 1, 1));
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pillager, hit, 10.0F) - 8.0F) < 0.001F, "less damage in a group");
		helper.succeed();
	}

	@GameTest
	public void evasionStepsAsideFromAnAimedBow(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			helper.setBlock(new BlockPos(x, 0, 1), Blocks.STONE);
		}
		Pillager pillager = pillagerWith(helper, new BlockPos(4, 1, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(pillager.position().add(0.0, 0.0, 5.0));
		CreeperEffectsTest.face(player, 180.0F);
		var evasion = new GeneEffect.Sidestep(12.0F, 3.0F, 10.0F, true);
		helper.assertFalse(GeneBehaviors.sidestep(pillager, helper.getLevel(), evasion), "no bow, no step");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		player.startUsingItem(InteractionHand.MAIN_HAND);
		Vec3 before = pillager.position();
		helper.assertTrue(GeneBehaviors.sidestep(pillager, helper.getLevel(), evasion), "the pillager steps aside");
		helper.assertValueEqual(Math.round(Math.abs(pillager.getX() - before.x)), 3L, "the side distance");
		helper.succeed();
	}

	@GameTest
	public void aPatrolGetsItsCaptainFromTheCaptainGene(GameTestHelper helper) {
		// The game makes the first member the captain, before the genes exist.
		Pillager first = patrolMember(helper, new BlockPos(1, 1, 1), true);
		Pillager captain = patrolMember(helper, new BlockPos(3, 1, 1), false, "test_captain");
		Pillager other = patrolMember(helper, new BlockPos(5, 1, 1), false);
		helper.assertFalse(first.isPatrolLeader() || first.isCaptain(), "the first member has no Captain gene");
		helper.assertTrue(first.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "the first member has no banner");
		helper.assertTrue(captain.isCaptain(), "the pillager with the Captain gene is the captain, with the banner");
		helper.assertFalse(other.isPatrolLeader(), "only one captain");
		helper.assertTrue(captain.hasPatrolTarget() && other.hasPatrolTarget(), "each member goes to the place of the patrol");
		helper.succeed();
	}

	@GameTest
	public void aPatrolWithNoCaptainGeneHasNoCaptain(GameTestHelper helper) {
		List<Pillager> patrol = List.of(patrolMember(helper, new BlockPos(1, 1, 1), true),
				patrolMember(helper, new BlockPos(3, 1, 1), false), patrolMember(helper, new BlockPos(5, 1, 1), false));
		for (Pillager member : patrol) {
			helper.assertFalse(member.isPatrolLeader(), "no captain");
			helper.assertTrue(member.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "no banner");
			helper.assertTrue(member.hasPatrolTarget(), "each member goes to the place of the patrol");
		}
		helper.succeed();
	}

	@GameTest
	public void onlyTheDeathOfACaptainGivesBadOmen(GameTestHelper helper) {
		Pillager first = patrolMember(helper, new BlockPos(1, 1, 1), true);
		Pillager captain = patrolMember(helper, new BlockPos(1, 1, 5), false, "test_captain");
		var source = helper.getLevel().damageSources().generic();
		first.hurtServer(helper.getLevel(), source, 100.0F);
		captain.hurtServer(helper.getLevel(), source, 100.0F);
		helper.assertValueEqual(bottlesNear(helper, first), 0, "the first member was not a captain");
		helper.assertValueEqual(bottlesNear(helper, captain), 1, "the captain drops the ominous bottle of Bad Omen");
		helper.succeed();
	}

	@GameTest
	public void aRaidWaveGetsItsLeaderFromTheCaptainGene(GameTestHelper helper) {
		Raid raid = new Raid(helper.absolutePos(new BlockPos(3, 1, 3)), Difficulty.NORMAL);
		// The game makes the first raider of the wave the leader, before the genes exist.
		Vindicator first = raidMember(helper, raid, EntityTypes.VINDICATOR.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(1, 1, 1), true);
		Pillager captain = raidMember(helper, raid, EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(3, 1, 1), false, "test_captain");
		Pillager other = raidMember(helper, raid, EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(5, 1, 1), false);
		helper.assertTrue(raid.getLeader(1) == captain, "the pillager with the Captain gene leads the wave");
		helper.assertTrue(captain.isCaptain(), "the leader wears the banner");
		helper.assertFalse(first.isPatrolLeader(), "the vindicator does not lead");
		helper.assertTrue(first.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "the vindicator has no banner");
		helper.assertFalse(other.isPatrolLeader(), "only one leader");
		Raid plainRaid = new Raid(helper.absolutePos(new BlockPos(3, 1, 5)), Difficulty.NORMAL);
		Vindicator plainFirst = raidMember(helper, plainRaid,
				EntityTypes.VINDICATOR.create(helper.getLevel(), EntitySpawnReason.EVENT), new BlockPos(1, 1, 5), true);
		raidMember(helper, plainRaid, EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(3, 1, 5), false);
		helper.assertTrue(plainRaid.getLeader(1) == null, "a wave with no Captain gene has no leader");
		helper.assertTrue(plainFirst.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "no banner");
		helper.succeed();
	}

	@GameTest
	public void onlyACaptainPicksUpTheOminousBanner(GameTestHelper helper) {
		// In a raid, a raider that picks up the ominous banner leads its wave, if the wave has no leader.
		Raid plainRaid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.NORMAL);
		Pillager plain = raidMember(helper, plainRaid, EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(1, 1, 1), false);
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 4)), Difficulty.NORMAL);
		Pillager captain = raidMember(helper, raid, EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.EVENT),
				new BlockPos(1, 1, 4), false, "test_captain");
		// The captain lost its banner, so its wave has no leader now.
		raid.removeLeader(1);
		captain.setPatrolLeader(false);
		captain.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
		helper.assertTrue(plainRaid.getLeader(1) == null && raid.getLeader(1) == null, "no wave has a leader");
		((MobAccessor) plain).originofspawns$pickUpItem(helper.getLevel(), bannerAt(helper, plain));
		((MobAccessor) captain).originofspawns$pickUpItem(helper.getLevel(), bannerAt(helper, captain));
		helper.assertTrue(plainRaid.getLeader(1) == null && plain.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
				"a pillager with no Captain gene leaves the banner");
		helper.assertTrue(raid.getLeader(1) == captain && captain.isCaptain(), "the pillager with the Captain gene leads");
		helper.succeed();
	}

	static ItemEntity bannerAt(GameTestHelper helper, Pillager pillager) {
		ItemStack banner = Raid.getOminousBannerInstance(helper.getLevel().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN));
		ItemEntity item = new ItemEntity(helper.getLevel(), pillager.getX(), pillager.getY(), pillager.getZ(), banner);
		helper.getLevel().addFreshEntity(item);
		return item;
	}

	/**
	 * Spawns a patrol member as the patrol spawner does: the first member is the captain, and the genes come when the
	 * member joins the world.
	 */
	static Pillager patrolMember(GameTestHelper helper, BlockPos pos, boolean first, String... genes) {
		Pillager pillager = EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.PATROL);
		pillager.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		pillager.setNoAi(true);
		pillager.setPersistenceRequired();
		BlockPos absolute = helper.absolutePos(pos);
		if (first) {
			pillager.setPatrolLeader(true);
			pillager.setPatrolTarget(absolute.east(40));
		}
		pillager.snapTo(Vec3.atBottomCenterOf(absolute), 0.0F, 0.0F);
		pillager.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(absolute), EntitySpawnReason.PATROL,
				null);
		helper.getLevel().addFreshEntity(pillager);
		return pillager;
	}

	/**
	 * Adds a raider to wave 1 of the raid, as the raid does: the first raider is the leader, and the genes come when the
	 * raider joins the world.
	 */
	static <T extends net.minecraft.world.entity.raid.Raider> T raidMember(GameTestHelper helper, Raid raid, T raider,
			BlockPos pos, boolean first, String... genes) {
		raider.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		raider.setNoAi(true);
		if (first) {
			raider.setPatrolLeader(true);
			raid.setLeader(1, raider);
		}
		raid.joinRaid(helper.getLevel(), 1, raider, helper.absolutePos(pos).below(), false);
		return raider;
	}

	static int bottlesNear(GameTestHelper helper, Pillager pillager) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, pillager.getBoundingBox().inflate(2.0),
				item -> item.getItem().is(Items.OMINOUS_BOTTLE)).size();
	}

	@GameTest
	public void aPillagerGetsNoRandomCrossbowEnchantmentButTheRaidOnesStay(GameTestHelper helper) {
		Pillager pillager = pillagerWith(helper, new BlockPos(1, 1, 1));
		// On Hard, in a world that is old, with a full moon, the vanilla crossbow gets an enchantment on 1 spawn in 4.
		DifficultyInstance hard = new DifficultyInstance(Difficulty.HARD, 10_000_000L, 10_000_000L, 1.0F);
		for (int i = 0; i < 40; i++) {
			pillager.finalizeSpawn(helper.getLevel(), hard, EntitySpawnReason.NATURAL, null);
			helper.assertTrue(pillager.getMainHandItem().is(Items.CROSSBOW), "the pillager keeps its crossbow");
			helper.assertFalse(pillager.getMainHandItem().isEnchanted(), "the crossbow has no random enchantment");
		}
		// A raid with the strongest omen enchants the crossbow on 3 raiders in 4, from wave 4.
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.HARD);
		raid.setRaidOmenLevel(5);
		pillager.setCurrentRaid(raid);
		boolean enchanted = false;
		for (int i = 0; i < 20 && !enchanted; i++) {
			pillager.applyRaidBuffs(helper.getLevel(), 6, false);
			enchanted = pillager.getMainHandItem().isEnchanted();
		}
		helper.assertTrue(enchanted, "the raid still enchants the crossbow");
		helper.succeed();
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
