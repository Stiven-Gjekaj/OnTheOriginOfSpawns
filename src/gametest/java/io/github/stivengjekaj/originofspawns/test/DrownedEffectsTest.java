package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the drowned genes use: water, tridents, and the deep.
 */
public class DrownedEffectsTest {
	@GameTest(maxTicks = 40)
	public void landFollowsTheWater(GameTestHelper helper) {
		BlockPos wet = new BlockPos(1, 1, 1);
		helper.setBlock(wet, Blocks.WATER);
		helper.setBlock(wet.above(), Blocks.WATER);
		Drowned swimmer = drownedWith(helper, wet);
		Drowned walker = drownedWith(helper, new BlockPos(4, 1, 4));
		helper.runAfterDelay(5, () -> {
			helper.assertFalse(GeneCombat.test(swimmer, When.ON_LAND), "a drowned in water");
			helper.assertTrue(GeneCombat.test(walker, When.ON_LAND), "a drowned on land");
			helper.succeed();
		});
	}

	@GameTest
	public void seaLevelStatesFollowTheHeight(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1));
		int sea = helper.getLevel().getSeaLevel();
		drowned.setPos(drowned.getX(), sea - 10, drowned.getZ());
		helper.assertTrue(GeneCombat.test(drowned, When.DEEP), "10 blocks under the sea level");
		helper.assertFalse(GeneCombat.test(drowned, When.ABOVE_SEA_LEVEL), "under the sea level");
		drowned.setPos(drowned.getX(), sea - 9, drowned.getZ());
		helper.assertFalse(GeneCombat.test(drowned, When.DEEP), "9 blocks under the sea level");
		drowned.setPos(drowned.getX(), sea, drowned.getZ());
		helper.assertTrue(GeneCombat.test(drowned, When.ABOVE_SEA_LEVEL), "at the sea level");
		helper.succeed();
	}

	@GameTest
	public void stormFollowsTheWeather(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1));
		var level = helper.getLevel();
		float rain = level.getRainLevel(1.0F);
		float thunder = level.getThunderLevel(1.0F);
		level.setRainLevel(1.0F);
		level.setThunderLevel(1.0F);
		boolean storm = GeneCombat.test(drowned, When.STORM);
		level.setRainLevel(0.0F);
		level.setThunderLevel(0.0F);
		boolean clear = GeneCombat.test(drowned, When.STORM);
		level.setRainLevel(rain);
		level.setThunderLevel(thunder);
		helper.assertTrue(storm, "in a storm");
		helper.assertFalse(clear, "in clear weather");
		helper.succeed();
	}

	/**
	 * Makes a drowned with test genes and no AI. The genes go on before the drowned enters the level, so it gets
	 * nothing from the pool.
	 */
	static Drowned drownedWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Drowned drowned = EntityTypes.DROWNED.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		drowned.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		drowned.setNoAi(true);
		drowned.setPersistenceRequired();
		drowned.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(drowned);
		return drowned;
	}
}
