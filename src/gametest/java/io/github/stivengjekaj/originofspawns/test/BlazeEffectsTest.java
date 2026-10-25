package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.mixin.BlazeAccessor;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.ProjectileAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a blaze: its bursts of fireballs, its flight, and the reused effects.
 */
public class BlazeEffectsTest {
	/**
	 * The ticks from the last fireball of a vanilla burst to the first fireball of the next burst: 6 ticks to the end
	 * of the burst, the rest of 100 ticks, and the charge of 60 ticks.
	 */
	static final long VANILLA_GAP = 166L;

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void aPlainBlazeShootsBurstsOfThree(GameTestHelper helper) {
		Shots shots = shooter(helper);
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(0).size(), 3, "fireballs in a burst");
			helper.assertValueEqual(bursts.get(1).getFirst() - bursts.get(0).getLast(), VANILLA_GAP, "ticks between bursts");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void longBurstShootsMoreFireballsInEachBurst(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_long_burst");
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(0).size(), 5, "fireballs in a burst");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void quickBurstMakesTheRestBetweenBurstsShorter(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_quick_burst");
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(1).getFirst() - bursts.get(0).getLast(), VANILLA_GAP - 50L,
					"half of the rest of 100 ticks");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void aPlainBurstStaysNarrow(GameTestHelper helper) {
		// The vanilla spread turns each fireball at most about 27 degrees at this distance.
		Shots shots = shooter(helper);
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.projectiles.size() >= 3, "the blaze shoots a burst");
			double width = burstWidth(shots);
			helper.assertTrue(width < 60.0, "the plain burst is narrow, not " + width + " degrees");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void fanShotShootsTheBurstInAFan(GameTestHelper helper) {
		// The test fan is 170 degrees wide, so the vanilla spread cannot hide it.
		Shots shots = shooter(helper, "test_fan_burst");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.projectiles.size() >= 3, "the blaze shoots a burst");
			double width = burstWidth(shots);
			helper.assertTrue(width > 100.0, "the burst is a wide fan, not " + width + " degrees");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void swiftShotMakesTheFireballsOfABlazeFaster(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_swift_shot");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.projectiles.size() >= 1, "the blaze shoots");
			var fireball = (net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball) shots.projectiles.get(0);
			helper.assertTrue(Math.abs(fireball.accelerationPower - 0.15) < 1.0E-6,
					"1.5 times the acceleration, not " + fireball.accelerationPower);
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void swiftShotMakesTheFireballsOfAGhastFaster(GameTestHelper helper) {
		GhastEffectsTest.Shots shots = GhastEffectsTest.shooter(helper, "test_swift_shot");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 1, "the ghast shoots");
			var fireball = (net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball) shots.projectiles.get(0).get(0);
			helper.assertTrue(Math.abs(fireball.accelerationPower - 0.15) < 1.0E-6,
					"1.5 times the acceleration, not " + fireball.accelerationPower);
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void deepBurnMakesTheFireOfTheFireballsLonger(GameTestHelper helper) {
		IronGolem plainTarget = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 5));
		hitWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1)), plainTarget);
		helper.assertValueEqual(plainTarget.getRemainingFireTicks(), 100, "a plain fireball burns for 5 seconds");
		plainTarget.discard();
		IronGolem target = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 5));
		hitWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1), "test_deep_burn"), target);
		helper.assertValueEqual(target.getRemainingFireTicks(), 220, "the fireball burns for 6 more seconds");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void blastEmberMakesASmallBlastThatBreaksNoBlocks(GameTestHelper helper) {
		BlockPos stone = new BlockPos(3, 1, 3);
		helper.setBlock(stone, net.minecraft.world.level.block.Blocks.STONE);
		Pig plainPig = CreeperEffectsTest.pigAt(helper, stone.above());
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1)), stone);
		helper.assertValueEqual(plainPig.getHealth(), plainPig.getMaxHealth(), "a plain fireball makes no blast");
		plainPig.discard();
		Pig pig = CreeperEffectsTest.pigAt(helper, stone.above());
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1), "test_blast_ember"), stone);
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the blast hurts the pig next to the hit");
		helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.STONE, stone);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void sootCloudLeavesACloudWhereTheFireballHits(GameTestHelper helper) {
		BlockPos stone = new BlockPos(3, 1, 3);
		helper.setBlock(stone, net.minecraft.world.level.block.Blocks.STONE);
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1)), stone);
		helper.assertTrue(clouds(helper).isEmpty(), "a plain fireball leaves no cloud");
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(1, 1, 1), "test_soot_cloud"), stone);
		var clouds = clouds(helper);
		helper.assertValueEqual(clouds.size(), 1, "clouds");
		var cloud = clouds.getFirst();
		helper.assertValueEqual(cloud.getRadius(), 2.0F, "the radius of the cloud");
		helper.assertValueEqual(cloud.getDuration(), 60, "the cloud stays for 3 seconds");
		helper.succeed();
	}

	static java.util.List<net.minecraft.world.entity.AreaEffectCloud> clouds(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, helper.getBounds());
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void shieldScorchStopsTheShieldThatStopsTheFireball(GameTestHelper helper) {
		ServerPlayer plainTarget = CreeperEffectsTest.shieldedPlayer(helper);
		shieldHit(helper, blazeWith(helper, new BlockPos(1, 1, 4)), plainTarget);
		helper.assertTrue(plainTarget.getHealth() > plainTarget.getMaxHealth() - 1.0F, "the shield stops the fireball");
		helper.assertFalse(plainTarget.getCooldowns().isOnCooldown(plainTarget.getOffhandItem()),
				"a plain fireball does not stop the shield");
		ServerPlayer target = CreeperEffectsTest.shieldedPlayer(helper);
		shieldHit(helper, blazeWith(helper, new BlockPos(1, 1, 4), "test_shield_scorch"), target);
		helper.assertTrue(target.getCooldowns().isOnCooldown(target.getOffhandItem()), "the fireball stops the shield");
		helper.succeed();
	}

	/**
	 * Makes a small fireball of the blaze hit the player from the front. The test player faces west.
	 */
	static void shieldHit(GameTestHelper helper, Blaze blaze, ServerPlayer player) {
		SmallFireball fireball = new SmallFireball(helper.getLevel(), blaze, new Vec3(1.0, 0.0, 0.0));
		fireball.setPos(player.getEyePosition().add(-1.0, 0.0, 0.0));
		helper.getLevel().addFreshEntity(fireball);
		((ProjectileAccessor) fireball).originofspawns$onHit(new EntityHitResult(player));
	}

	@GameTest
	public void fireTrailSetsFireRoundTheHit(GameTestHelper helper) {
		BlockPos stone = new BlockPos(3, 1, 3);
		stoneSquare(helper, stone);
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(6, 1, 6)), stone);
		helper.assertValueEqual(firesOver(helper, stone), 1, "a plain fireball sets fire to the hit block only");
		stoneSquare(helper, stone);
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(6, 1, 6), "test_fire_trail"), stone);
		helper.assertValueEqual(firesOver(helper, stone), 9, "fire on each block in 1 block round the hit");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void fireTrailObeysMobGriefing(GameTestHelper helper) {
		BlockPos stone = new BlockPos(3, 1, 3);
		stoneSquare(helper, stone);
		hitBlockWithFireball(helper, blazeWith(helper, new BlockPos(6, 1, 6), "test_fire_trail"), stone);
		helper.assertValueEqual(firesOver(helper, stone), 0, "no fire with mobGriefing off");
		helper.succeed();
	}

	/**
	 * Puts stone in a square of 3 x 3 with the center at the place, and air over it.
	 */
	static void stoneSquare(GameTestHelper helper, BlockPos center) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
			helper.setBlock(pos, net.minecraft.world.level.block.Blocks.STONE);
			helper.setBlock(pos.above(), net.minecraft.world.level.block.Blocks.AIR);
		}
	}

	static int firesOver(GameTestHelper helper, BlockPos center) {
		int fires = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 1, -1), center.offset(1, 1, 1))) {
			if (helper.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.FIRE)) {
				fires++;
			}
		}
		return fires;
	}

	/**
	 * Makes a small fireball of the blaze hit the top of the block.
	 */
	static void hitBlockWithFireball(GameTestHelper helper, Blaze blaze, BlockPos block) {
		SmallFireball fireball = new SmallFireball(helper.getLevel(), blaze, new Vec3(0.0, -1.0, 0.0));
		Vec3 top = helper.absoluteVec(Vec3.atBottomCenterOf(block.above()));
		fireball.setPos(top);
		helper.getLevel().addFreshEntity(fireball);
		((ProjectileAccessor) fireball).originofspawns$onHit(new net.minecraft.world.phys.BlockHitResult(top,
				net.minecraft.core.Direction.UP, helper.absolutePos(block), false));
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void seekerSparkTurnsTheFireballsAtMostFiveDegreesEachTick(GameTestHelper helper) {
		// The fireball flies east, and the target is to the south, at a right angle.
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 2, 6));
		double plain = turnInOneTick(helper, blazeWith(helper, new BlockPos(6, 1, 6)), pig);
		helper.assertTrue(plain < 0.01, "a plain fireball does not turn, but " + plain + " degrees");
		double seeker = turnInOneTick(helper, blazeWith(helper, new BlockPos(6, 1, 6), "test_seeker_spark"), pig);
		helper.assertTrue(Math.abs(seeker - 5.0) < 0.01, "the fireball turns 5 degrees, not " + seeker);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void sparkRingShootsALevelRingOfFireballs(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(4, 2, 4));
		var ring = new GeneEffect.FireballRing(8, 10.0F);
		helper.assertFalse(GeneBehaviors.fireballRing(blaze, helper.getLevel(), ring), "no ring with no target");
		blaze.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(1, 2, 1)));
		helper.assertTrue(GeneBehaviors.fireballRing(blaze, helper.getLevel(), ring), "the blaze shoots the ring");
		var fireballs = helper.getLevel().getEntitiesOfClass(SmallFireball.class, helper.getBounds().inflate(4.0),
				fireball -> fireball.getOwner() == blaze);
		helper.assertValueEqual(fireballs.size(), 8, "fireballs in the ring");
		helper.assertTrue(fireballs.stream().allMatch(fireball -> Math.abs(fireball.getDeltaMovement().y) < 1.0E-6),
				"the fireballs go out level");
		var directions = fireballs.stream().map(fireball -> Math.round(Math.toDegrees(Math.atan2(
				fireball.getDeltaMovement().z, fireball.getDeltaMovement().x)) + 360.0) % 360L).sorted().toList();
		helper.assertValueEqual(directions, java.util.List.of(0L, 45L, 90L, 135L, 180L, 225L, 270L, 315L), "the directions");
		fireballs.forEach(SmallFireball::discard);
		helper.assertFalse(GeneBehaviors.fireballRing(blaze, helper.getLevel(), ring), "the next ring waits 10 seconds");
		helper.succeed();
	}

	@GameTest
	public void burnPainMakesABurningTargetTakeMoreDamageFromEachSource(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		Blaze blaze = blazeWith(helper, new BlockPos(5, 1, 5), "test_burn_pain");
		blaze.setTarget(pig);
		var other = helper.getLevel().damageSources().generic();
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, other, 5.0F), 5.0F, "no change while the target does not burn");
		pig.igniteForSeconds(5.0F);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, other, 5.0F), 6.0F, "20% more damage from another source");
		blaze.setTarget(null);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, other, 5.0F), 5.0F, "no change when it is not the target");
		helper.succeed();
	}

	@GameTest
	public void fireFeedHealsWhileTheTargetBurnsNear(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(1, 1, 1));
		blaze.setHealth(10.0F);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		blaze.setTarget(pig);
		var feed = new GeneEffect.FireFeed(1.0F, 8.0F);
		helper.assertFalse(GeneBehaviors.fireFeed(blaze, feed), "no heal while the target does not burn");
		pig.igniteForSeconds(5.0F);
		helper.assertTrue(GeneBehaviors.fireFeed(blaze, feed), "a heal while the target burns");
		helper.assertValueEqual(blaze.getHealth(), 11.0F, "the blaze heals 1");
		pig.snapTo(pig.position().add(0.0, 0.0, 6.0));
		helper.assertFalse(GeneBehaviors.fireFeed(blaze, new GeneEffect.FireFeed(1.0F, 5.0F)),
				"no heal when the target is out of the radius");
		helper.succeed();
	}

	@GameTest
	public void quickWingsMakeTheFlightOfABlazeFaster(GameTestHelper helper) {
		// The test gene gives 30% more flying speed.
		double plain = airStep(blazeWith(helper, new BlockPos(2, 3, 2)));
		double quick = airStep(blazeWith(helper, new BlockPos(5, 3, 5), "test_swift_wing"));
		helper.assertTrue(plain > 0.0, "the blaze moves in the air");
		helper.assertTrue(Math.abs(quick / plain - 1.3) < 0.001, "30% more flying speed, not " + quick / plain);
		helper.succeed();
	}

	@GameTest
	public void risingHeatMakesTheBlazeRiseHigherThanItsTarget(GameTestHelper helper) {
		// The eyes of the pig are lower than the eyes of the blaze, so a plain blaze does not rise.
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		helper.assertValueEqual(riseInOneStep(helper, blazeWith(helper, new BlockPos(2, 1, 2)), pig), 0.0,
				"a plain blaze does not rise");
		helper.assertTrue(riseInOneStep(helper, blazeWith(helper, new BlockPos(2, 1, 2), "test_high_float"), pig) > 0.0,
				"the blaze rises to 4 blocks above its target");
		helper.succeed();
	}

	@GameTest
	public void skyGuardMakesTheBlazeRiseOverAGolem(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 5));
		helper.assertValueEqual(riseInOneStep(helper, blazeWith(helper, new BlockPos(2, 1, 2)), pig), 0.0,
				"a plain blaze does not rise");
		helper.assertTrue(riseInOneStep(helper, blazeWith(helper, new BlockPos(2, 1, 2), "test_sky_guard"), pig) > 0.0,
				"the blaze rises over the golem");
		helper.succeed();
	}

	@GameTest
	public void divingFlameDivesAtTheTargetAndHitsHarder(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(2, 4, 2));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		blaze.setTarget(pig);
		var dive = new GeneEffect.Dive(10.0F, 4.0F, 12.0F, 1.0F);
		var hit = helper.getLevel().damageSources().mobAttack(blaze);
		blaze.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_diving_flame"))));
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 6.0F), 6.0F, "no extra damage before a dive");
		helper.assertTrue(GeneBehaviors.dive(blaze, dive), "the blaze dives");
		Vec3 motion = blaze.getDeltaMovement();
		helper.assertTrue(motion.y < 0.0 && motion.x > 0.0 && motion.z > 0.0, "the blaze dives down at the pig");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 6.0F), 10.0F, "4 more damage after the dive");
		helper.assertFalse(GeneBehaviors.dive(blaze, dive), "the next dive waits 10 seconds");
		helper.succeed();
	}

	@GameTest
	public void smokeDashFleesFastUnderThirtyPercentHealth(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(4, 1, 4), "test_smoke_dash");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 4));
		blaze.setTarget(pig);
		var hit = helper.getLevel().damageSources().mobAttack(pig);
		blaze.setHealth(10.0F);
		GeneCombat.onHurt(blaze, hit);
		helper.assertTrue(GeneBehaviors.fleeing(blaze).isEmpty(), "no flee at half health");
		blaze.setHealth(5.0F);
		GeneCombat.onHurt(blaze, hit);
		helper.assertTrue(GeneBehaviors.fleeing(blaze).isPresent(), "the blaze flees under 30% health");
		helper.assertValueEqual(GeneBehaviors.flyingSpeedMultiplier(blaze), 1.5, "50% more flying speed");
		GeneBehaviors.fleeMove(blaze);
		helper.assertTrue(blaze.getMoveControl().getWantedX() > blaze.getX() + 8.0, "the blaze moves away from the pig");
		helper.succeed();
	}

	@GameTest
	public void snowShrugStopsTheDamageOfSnowballs(GameTestHelper helper) {
		Blaze plain = blazeWith(helper, new BlockPos(1, 1, 1));
		hitWithSnowball(helper, plain);
		helper.assertValueEqual(plain.getHealth(), plain.getMaxHealth() - 3.0F, "a snowball does 3 damage to a blaze");
		Blaze shrug = blazeWith(helper, new BlockPos(5, 1, 5), "test_snow_shrug");
		hitWithSnowball(helper, shrug);
		helper.assertValueEqual(shrug.getHealth(), shrug.getMaxHealth(), "a snowball does no damage");
		helper.succeed();
	}

	@GameTest
	public void boilOffTurnsTheWaterNearToSteam(GameTestHelper helper) {
		BlockPos near = new BlockPos(3, 1, 2);
		BlockPos far = new BlockPos(7, 1, 2);
		helper.setBlock(near, net.minecraft.world.level.block.Blocks.WATER);
		helper.setBlock(far, net.minecraft.world.level.block.Blocks.WATER);
		Blaze blaze = blazeWith(helper, new BlockPos(2, 1, 2));
		helper.assertValueEqual(GeneBehaviors.boilOff(blaze, helper.getLevel(), new GeneEffect.BoilOff(2.0F)), 1,
				"blocks that turn to steam");
		helper.assertBlockNotPresent(net.minecraft.world.level.block.Blocks.WATER, near);
		helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.WATER, far);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void boilOffObeysMobGriefing(GameTestHelper helper) {
		BlockPos near = new BlockPos(3, 1, 2);
		helper.setBlock(near, net.minecraft.world.level.block.Blocks.WATER);
		Blaze blaze = blazeWith(helper, new BlockPos(2, 1, 2));
		helper.assertValueEqual(GeneBehaviors.boilOff(blaze, helper.getLevel(), new GeneEffect.BoilOff(2.0F)), 0,
				"no steam with mobGriefing off");
		helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.WATER, near);
		helper.succeed();
	}

	@GameTest
	public void steamBurstPushesTheMobsNearAwayWhenItIsHurt(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(3, 1, 3), "test_steam_burst");
		Pig near = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 3));
		Pig far = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 7));
		GeneCombat.onHurt(blaze, helper.getLevel().damageSources().mobAttack(near));
		helper.assertTrue(near.getDeltaMovement().x > 1.0, "the near pig flies away from the blaze");
		helper.assertTrue(near.getDeltaMovement().y > 0.0, "the push goes up a little");
		helper.assertValueEqual(far.getDeltaMovement().horizontalDistance(), 0.0, "the far pig does not move");
		helper.succeed();
	}

	static void hitWithSnowball(GameTestHelper helper, Blaze blaze) {
		var snowball = new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(helper.getLevel(),
				blaze.getX(), blaze.getY() + 3.0, blaze.getZ(), new net.minecraft.world.item.ItemStack(
						net.minecraft.world.item.Items.SNOWBALL));
		helper.getLevel().addFreshEntity(snowball);
		((ProjectileAccessor) snowball).originofspawns$onHit(new EntityHitResult(blaze));
	}

	/**
	 * Returns the speed up that one server step gives the blaze, with the smallest random offset of the game.
	 */
	static double riseInOneStep(GameTestHelper helper, Blaze blaze, LivingEntity target) {
		blaze.setTarget(target);
		blaze.setDeltaMovement(Vec3.ZERO);
		((BlazeAccessor) blaze).originofspawns$setHeightOffset(0.5F);
		((BlazeAccessor) blaze).originofspawns$setNextHeightOffsetChange(100);
		((BlazeAccessor) blaze).originofspawns$serverStep(helper.getLevel());
		double rise = blaze.getDeltaMovement().y;
		blaze.discard();
		return rise;
	}

	/**
	 * Returns the speed on the ground plane that one step forward in the air gives the blaze.
	 */
	static double airStep(Blaze blaze) {
		blaze.setOnGround(false);
		blaze.setDeltaMovement(Vec3.ZERO);
		blaze.travel(new Vec3(0.0, 0.0, 1.0));
		return blaze.getDeltaMovement().horizontalDistance();
	}

	/**
	 * Returns the degrees that a small fireball of the blaze turns in one tick, while the blaze has the target.
	 */
	static double turnInOneTick(GameTestHelper helper, Blaze blaze, LivingEntity target) {
		blaze.setTarget(target);
		SmallFireball fireball = new SmallFireball(helper.getLevel(), blaze, new Vec3(1.0, 0.0, 0.0));
		fireball.setPos(helper.absoluteVec(new Vec3(2.5, 2.5, 1.5)));
		helper.getLevel().addFreshEntity(fireball);
		Vec3 before = fireball.getDeltaMovement().normalize();
		fireball.tick();
		Vec3 after = fireball.getDeltaMovement().normalize();
		fireball.discard();
		return Math.toDegrees(Math.acos(Math.min(1.0, before.dot(after))));
	}

	/**
	 * Makes a small fireball of the blaze hit the target directly.
	 */
	static SmallFireball hitWithFireball(GameTestHelper helper, Blaze blaze, LivingEntity target) {
		SmallFireball fireball = new SmallFireball(helper.getLevel(), blaze, new Vec3(1.0, 0.0, 0.0));
		fireball.setPos(target.position().add(0.0, 1.0, 0.0));
		helper.getLevel().addFreshEntity(fireball);
		((ProjectileAccessor) fireball).originofspawns$onHit(new EntityHitResult(target));
		return fireball;
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void theNotShootingStateEndsAtAShot(GameTestHelper helper) {
		Shots shots = shooter(helper);
		helper.assertTrue(GeneCombat.test(shots.blaze, GeneEffect.When.NOT_SHOOTING), "the blaze did not shoot yet");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 1, "the blaze shoots");
			helper.assertFalse(GeneCombat.test(shots.blaze, GeneEffect.When.NOT_SHOOTING), "the blaze just shot");
		});
	}

	@GameTest
	public void whiteHeatAndGolemMeltChangeTheDamageOfTheFireballs(GameTestHelper helper) {
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 5));
		Blaze hot = blazeWith(helper, new BlockPos(1, 1, 1), "test_white_heat");
		helper.assertValueEqual(GeneCombat.modifyDamage(golem, fireballHit(helper, hot), 5.0F), 7.0F,
				"40% more damage from a fireball");
		helper.assertValueEqual(GeneCombat.modifyDamage(golem, helper.getLevel().damageSources().mobAttack(hot), 5.0F), 5.0F,
				"no change to the touch");
		Blaze melt = blazeWith(helper, new BlockPos(1, 1, 4), "test_golem_bane");
		helper.assertValueEqual(GeneCombat.modifyDamage(golem, fireballHit(helper, melt), 5.0F), 7.5F,
				"1.5 times the fireball damage to a golem");
		helper.succeed();
	}

	@GameTest
	public void heatHazeHidesTheBlazeOnlyWhenItDoesNotShoot(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(4, 1, 4), "test_heat_haze");
		helper.assertTrue(GeneEffects.translucentOpacity(blaze).isPresent(), "the blaze is near invisible");
		blaze.setAttached(ModAttachments.SHOT_TIME, helper.getLevel().getGameTime());
		helper.assertTrue(GeneEffects.translucentOpacity(blaze).isEmpty(), "the blaze shows after a shot");
		helper.succeed();
	}

	@GameTest
	public void steamShellChangesTheDamageOfWater(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(4, 1, 4), "test_dry_skin");
		helper.assertValueEqual(GeneCombat.modifyDamage(blaze, helper.getLevel().damageSources().drown(), 1.0F), 0.5F,
				"half the damage of water and rain");
		helper.succeed();
	}

	@GameTest
	public void airDodgeStepsAsideInTheAir(GameTestHelper helper) {
		Blaze blaze = blazeWith(helper, new BlockPos(4, 3, 1));
		blaze.setNoGravity(true);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		// The eyes of the player are at the height of the eyes of the blaze, so the bow aims at it.
		player.setPos(blaze.position().add(0.0, blaze.getEyeHeight() - player.getEyeHeight(), 5.0));
		CreeperEffectsTest.face(player, 180.0F);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
		player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
		Vec3 before = blaze.position();
		helper.assertTrue(GeneBehaviors.sidestep(blaze, helper.getLevel(), new GeneEffect.Sidestep(12.0F, 3.0F, 8.0F, true)),
				"the blaze steps aside");
		helper.assertValueEqual(Math.round(Math.abs(blaze.getX() - before.x)), 3L, "the side distance");
		helper.assertValueEqual(blaze.getY(), before.y, "it stays at the same height in the air");
		helper.succeed();
	}

	@GameTest
	public void theReusedEffectsWorkOnABlaze(GameTestHelper helper) {
		ReusedEffects.igniteTarget(helper, BlazeEffectsTest::blazeWith);
		ReusedEffects.healOnce(helper, BlazeEffectsTest::blazeWith);
		ReusedEffects.damageTaken(helper, BlazeEffectsTest::blazeWith);
		ReusedEffects.translucent(helper, BlazeEffectsTest::blazeWith);
		helper.succeed();
	}

	/**
	 * Returns the damage source of a hit of a small fireball of the blaze.
	 */
	static net.minecraft.world.damagesource.DamageSource fireballHit(GameTestHelper helper, Blaze blaze) {
		SmallFireball fireball = new SmallFireball(helper.getLevel(), blaze, new Vec3(1.0, 0.0, 0.0));
		return helper.getLevel().damageSources().fireball(fireball, blaze);
	}

	/**
	 * Returns the angle in degrees on the ground between the first and the third fireball.
	 */
	static double burstWidth(Shots shots) {
		Vec3 first = shots.motions.get(0).multiply(1.0, 0.0, 1.0).normalize();
		Vec3 last = shots.motions.get(2).multiply(1.0, 0.0, 1.0).normalize();
		return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, first.dot(last)))));
	}

	/**
	 * The shots of a test blaze: the game time and the projectiles of each fireball.
	 */
	static final class Shots {
		Blaze blaze;
		final java.util.List<Long> times = new java.util.ArrayList<>();
		final java.util.List<Projectile> projectiles = new java.util.ArrayList<>();
		final java.util.List<Vec3> motions = new java.util.ArrayList<>();

		/**
		 * Returns the times of the shots, in groups. A group ends at a gap of more than 20 ticks.
		 */
		java.util.List<java.util.List<Long>> bursts() {
			java.util.List<java.util.List<Long>> bursts = new java.util.ArrayList<>();
			for (long time : times) {
				if (bursts.isEmpty() || time - bursts.getLast().getLast() > 20L) {
					bursts.add(new java.util.ArrayList<>());
				}
				bursts.getLast().add(time);
			}
			return bursts;
		}
	}

	/**
	 * Makes a blaze with its AI that shoots at a pig. The blaze stays in its place, and has no target goals, so it
	 * shoots only at the pig. Each tick, the test records the new projectiles of the blaze and removes them, so that
	 * no fireball leaves the test area. The blaze can shoot a burst only when the last burst is complete, so the test
	 * waits for the second burst to start before it counts the first one.
	 */
	static Shots shooter(GameTestHelper helper, String... genes) {
		Blaze blaze = blazeWith(helper, new BlockPos(1, 2, 4), genes);
		blaze.setNoAi(false);
		((MobAccessor) blaze).originofspawns$targetSelector().removeAllGoals(goal -> true);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 2, 4));
		blaze.setTarget(pig);
		Vec3 home = blaze.position();
		Shots shots = new Shots();
		shots.blaze = blaze;
		helper.onEachTick(() -> {
			blaze.setPos(home);
			blaze.setDeltaMovement(Vec3.ZERO);
			for (Projectile shot : helper.getLevel().getEntitiesOfClass(Projectile.class, helper.getBounds().inflate(8.0),
					projectile -> projectile.getOwner() == blaze && projectile.isAlive())) {
				shots.times.add(helper.getLevel().getGameTime());
				shots.projectiles.add(shot);
				shots.motions.add(shot.getDeltaMovement());
				shot.discard();
			}
		});
		return shots;
	}

	static Blaze blazeWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Blaze blaze = EntityTypes.BLAZE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		blaze.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		blaze.setNoAi(true);
		blaze.setPersistenceRequired();
		blaze.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(blaze);
		return blaze;
	}
}
