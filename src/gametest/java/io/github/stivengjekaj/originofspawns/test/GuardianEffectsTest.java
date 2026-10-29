package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a guardian: its laser, its spikes, its swim, and the reused effects. Each guardian of a test is
 * in water: the tests build a pool of glass with water in it.
 */
public class GuardianEffectsTest {
	/**
	 * The places in the pool of a laser test: the guardian at the west end, the target 4 blocks east of it. A
	 * vanilla guardian does not attack a target in 3 blocks.
	 */
	static final BlockPos SHOOTER = new BlockPos(2, 1, 4);
	static final BlockPos TARGET = new BlockPos(6, 1, 4);

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void aPlainLaserDoesSixDamage(GameTestHelper helper) {
		pool(helper);
		IronGolem plain = target(helper);
		shooter(helper, plain);
		helper.succeedWhen(() -> {
			helper.assertTrue(plain.getHealth() < plain.getMaxHealth(), "the plain laser fires");
			float plainDamage = plain.getMaxHealth() - plain.getHealth();
			// The magic damage of 1 comes first. The attack of 6 comes in the hurt time of the target, so it adds only 5.
			helper.assertValueEqual(plainDamage, 6.0F, "the plain laser does 6 damage in all");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void deepBeamDoublesTheDamageOfTheLaser(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		shooter(helper, golem, "test_deep_beam");
		helper.succeedWhen(() -> {
			helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires");
			helper.assertValueEqual(golem.getMaxHealth() - golem.getHealth(), 12.0F, "two times the damage of the laser");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void quickFocusMakesTheLaserChargeFaster(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = shooter(helper, golem, "test_quick_focus");
		helper.assertValueEqual(guardian.getAttackDuration(), 40, "half of the 80 ticks of the charge");
		// The goal waits 10 ticks before the charge, so the laser fires about 50 ticks after the start.
		helper.succeedWhen(() -> {
			helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires");
			helper.assertTrue(helper.getTick() < 70, "the laser fires before a vanilla charge could end");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void killFocusChargesTheNextLaserAtOnceAfterAKill(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = shooter(helper, golem, "test_kill_focus");
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		io.github.stivengjekaj.originofspawns.evolution.Evolution.onPlayerKilled(helper.getLevel(), guardian,
				helper.getLevel().damageSources().mobAttack(guardian));
		player.discard();
		// The goal waits 10 ticks before the lock, and the laser fires one tick after it.
		helper.succeedWhen(() -> {
			helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires");
			helper.assertTrue(helper.getTick() < 30, "the laser fires with no charge");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void splitBeamAlsoHitsASecondTargetWithHalfTheDamage(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		var squid = helper.spawnWithNoFreeWill(EntityTypes.SQUID, TARGET.north());
		shooter(helper, golem, "test_split_beam");
		helper.succeedWhen(() -> {
			helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires");
			helper.assertValueEqual(squid.getMaxHealth() - squid.getHealth(), 3.0F, "half of the 6 damage of the laser");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void aPlainLaserBreaksWhenTheTargetGoesOutOfSight(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = shooter(helper, golem);
		blockSightForHalfASecond(helper);
		helper.runAtTickTime(150, () -> {
			helper.assertTrue(guardian.getTarget() == null, "the guardian lost its target");
			helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "the laser does not fire");
			helper.succeed();
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void steadyLockKeepsTheLaserWhenTheTargetIsOutOfSightForAShortTime(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		shooter(helper, golem, "test_steady_lock");
		blockSightForHalfASecond(helper);
		helper.succeedWhen(() -> helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires"));
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void pulseBeamHurtsTheTargetWhileTheLaserCharges(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = shooter(helper, golem, "test_pulse_beam");
		// The charge of 80 ticks gives a pulse at the ticks 20, 40, and 60 of the charge. The goal waits 10 ticks first.
		helper.runAtTickTime(75, () -> helper.assertValueEqual(golem.getMaxHealth() - golem.getHealth(), 3.0F,
				"three pulses of 1 damage before the laser fires"));
		helper.succeedWhen(() -> helper.assertTrue(guardian.getTarget() == null && golem.getHealth() < golem.getMaxHealth() - 3.0F,
				"the laser fires after the pulses"));
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 100)
	public void guideBeamCallsTheGuardiansNearToTheLockedTarget(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian other = guardianWith(helper, SHOOTER.north());
		shooter(helper, golem, "test_guide_beam");
		helper.succeedWhen(() -> helper.assertTrue(other.getTarget() == golem, "the other guardian targets the golem"));
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void longSpikesHurtTheAttackerMore(GameTestHelper helper) {
		pool(helper);
		helper.assertValueEqual(spikeHitOn(helper, guardianWith(helper, SHOOTER)), 2.0F, "the vanilla spikes do 2 damage");
		helper.assertValueEqual(spikeHitOn(helper, guardianWith(helper, TARGET, "test_long_spikes")), 4.0F, "2 more damage");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void venomSpikesPoisonTheAttacker(GameTestHelper helper) {
		pool(helper);
		Guardian guardian = guardianWith(helper, SHOOTER, "test_venom_spikes");
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 3, 4));
		guardian.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 1.0F);
		helper.assertValueEqual(pig.getEffect(net.minecraft.world.effect.MobEffects.POISON).getDuration(), 60,
				"Poison for 3 seconds");
		helper.succeed();
	}

	@GameTest
	public void shoreHopHopsToTheNearestWater(GameTestHelper helper) {
		// This test needs a guardian on land, next to the pool. The pool is east of it.
		pool(helper);
		Guardian guardian = guardianWith(helper, new BlockPos(1, 4, 0));
		helper.setBlock(new BlockPos(1, 3, 0), Blocks.STONE);
		guardian.setOnGround(true);
		var hop = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.ShoreHop(8.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.shoreHop(guardian, helper.getLevel(), hop),
				"the guardian hops");
		Vec3 motion = guardian.getDeltaMovement();
		helper.assertTrue(motion.y > 0.0 && motion.z > 0.0, "it hops up and south, to the water, not " + motion);
		helper.succeed();
	}

	@GameTest
	public void ramFinDartsAtTheTargetAndHitsIt(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = guardianWith(helper, SHOOTER);
		guardian.setTarget(golem);
		var ram = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.Ram(4.0F, 5.0F, 8.0F, 0.8F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.ram(guardian, helper.getLevel(), ram),
				"the ram starts");
		helper.assertTrue(guardian.getDeltaMovement().x > 0.7, "the guardian darts east, at the golem");
		helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "no hit before the touch");
		guardian.snapTo(golem.position().add(-1.0, 0.0, 0.0), 0.0F, 0.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.ram(guardian, helper.getLevel(), ram),
				"the ram hits at the touch");
		helper.assertValueEqual(golem.getMaxHealth() - golem.getHealth(), 4.0F, "the ram does 4 damage");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.ram(guardian, helper.getLevel(), ram),
				"the next ram waits 5 seconds");
		helper.succeed();
	}

	@GameTest
	public void deepFlightSwimsAwayFromTheTarget(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = guardianWith(helper, new BlockPos(4, 1, 4), "test_smoke_dash");
		guardian.setTarget(golem);
		// The base tick finds the water around the guardian. The water navigation works only in water.
		guardian.baseTick();
		helper.assertTrue(guardian.isInWater(), "the guardian is in water");
		guardian.setHealth(guardian.getMaxHealth() / 5.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(guardian,
				helper.getLevel().damageSources().mobAttack(golem));
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.fleeing(guardian).isPresent(),
				"the guardian flees under 30% health");
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.fleeMove(guardian);
		// A guardian swims only while its navigation has a path.
		helper.assertFalse(guardian.getNavigation().isDone(), "the guardian has a path away from the golem");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void tiringGazeGivesTheEffectWithTheLaser(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		shooter(helper, golem, "test_tiring_gaze");
		helper.succeedWhen(() -> {
			helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the laser fires");
			var fatigue = golem.getEffect(net.minecraft.world.effect.MobEffects.MINING_FATIGUE);
			helper.assertTrue(fatigue != null && fatigue.getAmplifier() == 1 && fatigue.getDuration() == 120,
					"Mining Fatigue II for 6 seconds");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void hullCutterBreaksTheBoatOfThePlayerThatTheLaserHits(GameTestHelper helper) {
		pool(helper);
		var boat = EntityTypes.OAK_BOAT.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		// The boat floats on the top of the water. Vanilla makes the rider of a boat under water leave it.
		boat.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(TARGET.above(2))), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(boat);
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(boat.position());
		player.startRiding(boat);
		shooter(helper, player, "test_hull_cutter");
		helper.succeedWhen(() -> {
			helper.assertTrue(player.getHealth() < player.getMaxHealth(), "the laser hits the player");
			helper.assertFalse(boat.isAlive(), "the boat breaks");
		});
	}

	@GameTest
	public void packGazeAndShoalWardActWithTwoGuardiansNear(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = target(helper);
		Guardian guardian = guardianWith(helper, SHOOTER, "test_pack_gaze");
		var hit = helper.getLevel().damageSources().indirectMagic(guardian, guardian);
		boolean[] checked = new boolean[1];
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.asLaser(() -> {
			helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(golem, hit, 5.0F),
					5.0F, "no change with no guardians near");
			return checked[0] = true;
		});
		guardianWith(helper, SHOOTER.north());
		guardianWith(helper, SHOOTER.south());
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.asLaser(() -> {
			helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(golem, hit, 5.0F),
					6.0F, "20% more laser damage with two guardians near");
			return true;
		});
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(guardian,
				helper.getLevel().damageSources().generic(), 5.0F), 4.0F, "20% less damage taken with two guardians near");
		helper.succeed();
	}

	@GameTest
	public void spikeBurstHurtsThePlayersNearButNotTheGuardians(GameTestHelper helper) {
		pool(helper);
		Guardian guardian = guardianWith(helper, new BlockPos(3, 1, 4), "test_spike_burst");
		Guardian other = guardianWith(helper, new BlockPos(5, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 3, 4))));
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onDeath(guardian, helper.getLevel(),
				helper.getLevel().damageSources().generic());
		helper.assertValueEqual(player.getMaxHealth() - player.getHealth(), 4.0F, "the spikes do 4 damage to the player");
		helper.assertValueEqual(other.getHealth(), other.getMaxHealth(), "the other guardian takes no damage");
		helper.succeed();
	}

	@GameTest
	public void dodgeFinAndSlickHideStepAsideInWater(GameTestHelper helper) {
		pool(helper);
		for (var item : java.util.List.of(net.minecraft.world.item.Items.BOW, net.minecraft.world.item.Items.TRIDENT)) {
			Guardian guardian = guardianWith(helper, SHOOTER);
			guardian.baseTick();
			var player = CreeperEffectsTest.survivalPlayer(helper).player();
			player.setNoGravity(true);
			// The player is in the pool at the east end, and looks west at the guardian. The pool is 3 blocks wide, so the
			// step is 1 block.
			player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(TARGET)));
			player.setYRot(90.0F);
			player.setYHeadRot(90.0F);
			Vec3 toGuardian = guardian.getEyePosition().subtract(player.getEyePosition());
			player.setXRot((float) Math.toDegrees(Math.atan2(-toGuardian.y, toGuardian.horizontalDistance())));
			player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(item));
			player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
			Vec3 before = guardian.position();
			helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.sidestep(guardian, helper.getLevel(),
					new io.github.stivengjekaj.originofspawns.definition.GeneEffect.Sidestep(12.0F, 1.0F, 8.0F, true)),
					"the guardian steps aside from the " + item);
			helper.assertValueEqual(Math.round(Math.abs(guardian.getZ() - before.z)), 1L, "the side distance");
			helper.assertTrue(guardian.isInWater() || helper.getLevel().getFluidState(guardian.blockPosition()).isSource(),
					"it stays in the water");
			guardian.discard();
			player.discard();
		}
		helper.succeed();
	}

	@GameTest
	public void theReusedEffectsWorkOnAGuardian(GameTestHelper helper) {
		pool(helper);
		ReusedEffects.callOnHurt(helper, GuardianEffectsTest::guardianWith);
		ReusedEffects.damageTaken(helper, GuardianEffectsTest::guardianWith);
		helper.succeed();
	}

	@GameTest
	public void kinGrievingHealsWhenAnotherGuardianDies(GameTestHelper helper) {
		pool(helper);
		Guardian mourner = guardianWith(helper, SHOOTER, "test_mourner");
		Guardian dead = guardianWith(helper, TARGET);
		mourner.setHealth(10.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onDeath(dead, helper.getLevel(),
				helper.getLevel().damageSources().generic());
		helper.assertValueEqual(mourner.getHealth(), 14.0F, "the guardian heals 4");
		helper.succeed();
	}

	/**
	 * Makes a pig hit the guardian in melee while its spikes are out, and returns the damage that the pig takes.
	 */
	static float spikeHitOn(GameTestHelper helper, Guardian guardian) {
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 3, 4));
		guardian.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 1.0F);
		float damage = pig.getMaxHealth() - pig.getHealth();
		pig.discard();
		return damage;
	}

	/**
	 * Puts a glass wall between the guardian and its target in the middle of the charge, and takes it away 10 ticks
	 * later.
	 */
	static void blockSightForHalfASecond(GameTestHelper helper) {
		helper.runAtTickTime(40, () -> {
			helper.setBlock(new BlockPos(4, 1, 4), Blocks.GLASS);
			helper.setBlock(new BlockPos(4, 2, 4), Blocks.GLASS);
		});
		helper.runAtTickTime(50, () -> {
			helper.setBlock(new BlockPos(4, 1, 4), Blocks.WATER);
			helper.setBlock(new BlockPos(4, 2, 4), Blocks.WATER);
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void aPlainGuardianStopsItsLaserOnANearTarget(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, SHOOTER.east(2));
		Guardian guardian = shooter(helper, golem);
		helper.runAtTickTime(150, () -> {
			helper.assertTrue(guardian.getTarget() == null, "the guardian drops a target in 3 blocks");
			helper.assertValueEqual(golem.getHealth(), golem.getMaxHealth(), "the laser does not fire");
			helper.succeed();
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 200)
	public void closeSnapFiresAtOnceOnANearTarget(GameTestHelper helper) {
		pool(helper);
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, SHOOTER.east(2));
		shooter(helper, golem, "test_close_snap");
		// The goal waits 10 ticks before the lock, and the laser fires one tick after it.
		helper.succeedWhen(() -> {
			helper.assertValueEqual(golem.getMaxHealth() - golem.getHealth(), 6.0F, "the laser fires with its full damage");
			helper.assertTrue(helper.getTick() < 30, "the laser fires with no charge");
		});
	}

	/**
	 * Builds a pool of glass, 6 blocks long and 3 blocks wide inside, with water 2 blocks deep, from x 1 to 6 and z 3
	 * to 5. The guardian and its target are in the pool.
	 */
	static void pool(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 2; z <= 6; z++) {
				for (int y = 0; y <= 3; y++) {
					boolean wall = x == 0 || x == 7 || z == 2 || z == 6 || y == 0;
					helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.GLASS : y <= 2 ? Blocks.WATER : Blocks.AIR);
				}
			}
		}
	}

	static IronGolem target(GameTestHelper helper) {
		return helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, TARGET);
	}

	/**
	 * Makes a guardian with its AI at the west end of the pool, with the target. It has no target goals, so it
	 * attacks only the target.
	 */
	static Guardian shooter(GameTestHelper helper, LivingEntity target, String... genes) {
		Guardian guardian = guardianWith(helper, SHOOTER, genes);
		guardian.setNoAi(false);
		((MobAccessor) guardian).originofspawns$targetSelector().removeAllGoals(goal -> true);
		guardian.setTarget(target);
		return guardian;
	}

	static Guardian guardianWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Guardian guardian = EntityTypes.GUARDIAN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		guardian.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		guardian.setNoAi(true);
		guardian.setPersistenceRequired();
		guardian.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(guardian);
		return guardian;
	}
}
