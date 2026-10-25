package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
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
