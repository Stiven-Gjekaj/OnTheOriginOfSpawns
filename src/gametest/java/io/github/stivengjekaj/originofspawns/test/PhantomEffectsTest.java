package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.mixin.PhantomAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the phantom: the dive, the flight, the sun, the cry, and the flock. Each phantom stays inside
 * the test area, under the roof.
 */
public class PhantomEffectsTest {
	@GameTest
	public void swiftWingMakesTheFlightFaster(GameTestHelper helper) {
		Phantom plain = phantomWith(helper, new BlockPos(1, 3, 1));
		Phantom swift = phantomWith(helper, new BlockPos(1, 3, 6), "test_swift_wing");
		double[] speeds = new double[2];
		Phantom[] phantoms = {plain, swift};
		for (int i = 0; i < 2; i++) {
			Phantom phantom = phantoms[i];
			phantom.setYRot(-90.0F);
			((PhantomAccessor) phantom).originofspawns$setMoveTargetPoint(phantom.position().add(5.0, 0.0, 0.0));
			phantom.setDeltaMovement(Vec3.ZERO);
			phantom.getMoveControl().tick();
			speeds[i] = phantom.getDeltaMovement().length();
		}
		helper.assertTrue(speeds[0] > 0.0, "the phantom flies");
		helper.assertTrue(Math.abs(speeds[1] / speeds[0] - 1.3) < 0.001, "30% more flying speed, not " + speeds[1] / speeds[0]);
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aMobUnderARoofDoesNotCountAsBurningInTheSun(GameTestHelper helper) {
		// A stone block over the phantom stops the sun, at each time of day. The barrier roof lets the sky light in.
		helper.setBlock(new BlockPos(4, 4, 4), Blocks.STONE);
		// The light under the stone changes some ticks later.
		helper.runAfterDelay(10, () -> {
			Phantom phantom = phantomWith(helper, new BlockPos(4, 2, 4));
			for (int i = 0; i < 40; i++) {
				phantom.aiStep();
			}
			helper.assertValueEqual(phantom.getRemainingFireTicks(), 0, "the sun does not burn the phantom");
			helper.assertFalse(Evolution.isSunBurning(phantom, helper.getLevel().getGameTime()),
					"a phantom that the sun does not burn is not burning in the sun");
			helper.succeed();
		});
	}

	/**
	 * Adds a phantom with the test genes and no AI. The genes go on before it enters the level, so it gets no genes
	 * from the pool.
	 */
	static Phantom phantomWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Phantom phantom = EntityTypes.PHANTOM.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		phantom.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		phantom.setNoAi(true);
		phantom.setPersistenceRequired();
		phantom.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(phantom);
		return phantom;
	}
}
