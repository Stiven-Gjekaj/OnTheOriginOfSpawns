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
}
