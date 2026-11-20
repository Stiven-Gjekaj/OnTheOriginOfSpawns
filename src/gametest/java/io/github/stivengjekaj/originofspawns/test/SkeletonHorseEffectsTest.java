package io.github.stivengjekaj.originofspawns.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a skeleton horse: its trap, its swim, and the reused effects.
 */
public class SkeletonHorseEffectsTest {
	/**
	 * Fills the test area from y 1 to y 3 with water.
	 */
	static void flood(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				for (int y = 1; y <= 3; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
				}
			}
		}
	}

	@GameTest
	public void aSwimSpeedGeneMakesTheSkeletonHorseSwimFaster(GameTestHelper helper) {
		flood(helper);
		SkeletonHorse plain = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(1, 1, 1));
		SkeletonHorse swimmer = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(6, 1, 1), "test_horse_swim");
		for (SkeletonHorse horse : java.util.List.of(plain, swimmer)) {
			horse.setNoGravity(true);
			horse.baseTick();
			helper.assertTrue(horse.isInWater(), "the horse is in water");
			horse.setDeltaMovement(Vec3.ZERO);
			horse.setSpeed(0.2F);
			horse.travel(new Vec3(0.0, 0.0, 1.0));
		}
		double plainSpeed = Math.hypot(plain.getDeltaMovement().x, plain.getDeltaMovement().z);
		double swimSpeed = Math.hypot(swimmer.getDeltaMovement().x, swimmer.getDeltaMovement().z);
		helper.assertTrue(plainSpeed > 0.0, "the plain horse swims");
		helper.assertTrue(Math.abs(swimSpeed - plainSpeed * 1.3) < 0.0001, "30% more push in water, not " + swimSpeed / plainSpeed);
		helper.succeed();
	}

	@GameTest
	public void coldCurrentSlowsAMeleeAttackerOnlyInWater(GameTestHelper helper) {
		var level = helper.getLevel();
		helper.setBlock(new BlockPos(6, 1, 6), Blocks.WATER);
		SkeletonHorse dry = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(1, 1, 1), "test_horse_cold_current");
		SkeletonHorse wet = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(6, 1, 6), "test_horse_cold_current");
		wet.baseTick();
		helper.assertTrue(wet.isInWater(), "one horse is in water");
		var dryPig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 3));
		var wetPig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		dry.hurtServer(level, level.damageSources().mobAttack(dryPig), 1.0F);
		wet.hurtServer(level, level.damageSources().mobAttack(wetPig), 1.0F);
		helper.assertFalse(dryPig.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "no Slowness on land");
		helper.assertTrue(wetPig.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "Slowness in water");
		helper.succeed();
	}

	/**
	 * Makes a skeleton horse trap with the test genes and returns its trap goal.
	 */
	static net.minecraft.world.entity.animal.equine.SkeletonTrapGoal trap(GameTestHelper helper, SkeletonHorse horse) {
		horse.setTrap(true);
		return (net.minecraft.world.entity.animal.equine.SkeletonTrapGoal) ((io.github.stivengjekaj.originofspawns.mixin.MobAccessor) horse)
				.originofspawns$goalSelector().getAvailableGoals().stream()
				.map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
				.filter(net.minecraft.world.entity.animal.equine.SkeletonTrapGoal.class::isInstance).findFirst().orElseThrow();
	}

	@GameTest
	public void wideTrapOpensWhenAPlayerComesInTwentyBlocks(GameTestHelper helper) {
		SkeletonHorse plain = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(1, 1, 1));
		SkeletonHorse wide = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(1, 1, 2), "test_horse_wide_trap");
		var plainGoal = trap(helper, plain);
		var wideGoal = trap(helper, wide);
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(plain.position().add(15.0, 0.0, 0.0));
		helper.assertFalse(plainGoal.canUse(), "a plain trap does not open at 15 blocks");
		helper.assertTrue(wideGoal.canUse(), "a wide trap opens at 15 blocks");
		helper.succeed();
	}

	/**
	 * Makes a skeleton horse trap that can go away: it is not persistent, as a natural trap.
	 */
	static SkeletonHorse naturalTrap(GameTestHelper helper, BlockPos pos, String... genes) {
		SkeletonHorse horse = EntityTypes.SKELETON_HORSE.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		horse.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.GENES,
				new io.github.stivengjekaj.originofspawns.evolution.CarriedGenes(java.util.Arrays.stream(genes)
						.map(gene -> net.minecraft.resources.Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		horse.setNoAi(true);
		horse.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(horse);
		horse.setTrap(true);
		return horse;
	}

	@GameTest
	public void longWaitKeepsTheTrapTwoTimesAsLong(GameTestHelper helper) {
		SkeletonHorse plain = naturalTrap(helper, new BlockPos(1, 1, 1));
		SkeletonHorse patient = naturalTrap(helper, new BlockPos(5, 1, 5), "test_horse_long_wait");
		for (SkeletonHorse horse : java.util.List.of(plain, patient)) {
			// 20 minutes: more than the vanilla 15 minutes, less than 30 minutes.
			((io.github.stivengjekaj.originofspawns.mixin.SkeletonHorseAccessor) horse).originofspawns$setTrapTime(24000);
			horse.aiStep();
		}
		helper.assertTrue(plain.isRemoved(), "a plain trap goes away after 15 minutes");
		helper.assertFalse(patient.isRemoved(), "a long wait trap stays for 30 minutes");
		patient.discard();
		helper.succeed();
	}

	/**
	 * Opens the trap of the horse with a player near it, and returns the horses of the trap.
	 */
	static java.util.List<SkeletonHorse> openTrap(GameTestHelper helper, SkeletonHorse horse) {
		var goal = trap(helper, horse);
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(horse.position().add(0.0, 0.0, 5.0));
		helper.assertTrue(goal.canUse(), "a player opens the trap");
		goal.tick();
		java.util.UUID group = horse.getUUID();
		return helper.getLevel().getEntitiesOfClass(SkeletonHorse.class, horse.getBoundingBox().inflate(4.0),
				other -> group.equals(other.getAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.TRAP_GROUP)));
	}

	@GameTest
	public void aPlainTrapMakesFourHorsemenOfOneGroup(GameTestHelper helper) {
		SkeletonHorse horse = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(3, 1, 1));
		var horses = openTrap(helper, horse);
		helper.assertValueEqual(horses.size(), 4, "the trap horse and three more");
		horses.forEach(other -> helper.assertTrue(other.getFirstPassenger() instanceof net.minecraft.world.entity.monster.skeleton.Skeleton,
				"each horse has a skeleton rider"));
		helper.succeed();
	}

	@GameTest
	public void thunderhoovesMakeMoreHorsemen(GameTestHelper helper) {
		SkeletonHorse horse = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(3, 1, 1), "test_horse_thunder");
		var horses = openTrap(helper, horse);
		helper.assertValueEqual(horses.size(), 6, "two more horsemen");
		horses.forEach(other -> helper.assertTrue(other.getFirstPassenger() instanceof net.minecraft.world.entity.monster.skeleton.Skeleton,
				"each horse has a skeleton rider"));
		helper.succeed();
	}

	@GameTest
	public void chargedRidersGiveSpeedToEachHorseOfTheTrap(GameTestHelper helper) {
		SkeletonHorse horse = MountEffectsTest.mob(helper, EntityTypes.SKELETON_HORSE, new BlockPos(3, 1, 1), "test_horse_charged");
		var horses = openTrap(helper, horse);
		helper.assertValueEqual(horses.size(), 5, "the trap has five horses");
		horses.forEach(other -> helper.assertTrue(other.hasEffect(net.minecraft.world.effect.MobEffects.SPEED),
				"each horse of the trap has Speed, also the extra one"));
		horses.forEach(other -> helper.assertFalse(((net.minecraft.world.entity.LivingEntity) other.getFirstPassenger())
				.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the riders get no Speed"));
		helper.succeed();
	}
}
