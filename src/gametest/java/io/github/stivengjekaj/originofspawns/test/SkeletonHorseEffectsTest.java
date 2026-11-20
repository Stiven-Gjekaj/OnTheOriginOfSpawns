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
}
