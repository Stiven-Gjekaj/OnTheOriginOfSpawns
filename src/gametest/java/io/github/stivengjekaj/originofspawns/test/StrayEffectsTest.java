package io.github.stivengjekaj.originofspawns.test;

import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the stray genes use: snow, ice, cold, and frost.
 */
public class StrayEffectsTest {
	@GameTest
	public void snowAndIceUnderTheMobAreStates(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.SNOW_BLOCK);
		helper.setBlock(new BlockPos(3, 0, 1), Blocks.PACKED_ICE);
		helper.setBlock(new BlockPos(5, 0, 1), Blocks.STONE);
		Stray onSnow = strayWith(helper, new BlockPos(1, 1, 1));
		Stray onIce = strayWith(helper, new BlockPos(3, 1, 1));
		Stray onStone = strayWith(helper, new BlockPos(5, 1, 1));
		helper.assertTrue(GeneCombat.test(onSnow, When.ON_SNOW), "a snow block counts as snow");
		helper.assertFalse(GeneCombat.test(onSnow, When.ON_ICE), "a snow block is not ice");
		helper.assertTrue(GeneCombat.test(onIce, When.ON_SNOW), "ice counts as snow and ice");
		helper.assertTrue(GeneCombat.test(onIce, When.ON_ICE), "packed ice is ice");
		helper.assertTrue(GeneCombat.test(onStone, When.OFF_SNOW), "stone is not snow");
		helper.succeed();
	}

	@GameTest
	public void aSnowLayerAtTheFeetCounts(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.SNOW);
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1));
		helper.assertTrue(GeneCombat.test(stray, When.ON_SNOW), "a snow layer at the feet");
		helper.succeed();
	}

	@GameTest
	public void snowAndColdFollowTheBiome(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(2, 1, 2));
		biome(helper, "minecraft:plains");
		BlockPos head = helper.absolutePos(new BlockPos(2, 2, 2));
		helper.assertFalse(GeneCombat.snowsAt(helper.getLevel(), head), "a storm in the plains is rain");
		helper.assertFalse(GeneCombat.test(stray, When.COLD_BIOME), "the plains are not cold");
		Set<DeathCondition> warm = Evolution.facts(stray, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(warm.contains(DeathCondition.WARM), "a death in the plains is warm");

		biome(helper, "minecraft:snowy_plains");
		helper.assertTrue(GeneCombat.snowsAt(helper.getLevel(), head), "a storm in the snowy plains is snow");
		helper.assertTrue(GeneCombat.test(stray, When.COLD_BIOME), "the snowy plains are cold");
		Set<DeathCondition> cold = Evolution.facts(stray, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(cold.contains(DeathCondition.COLD), "a death in the snowy plains is cold");
		helper.assertFalse(cold.contains(DeathCondition.WARM), "a death in the snowy plains is not warm");
		helper.assertValueEqual(cold.contains(DeathCondition.SNOW), helper.getLevel().isRaining(), "snow needs a storm");
		helper.assertValueEqual(GeneCombat.test(stray, When.NOT_SNOWING), !helper.getLevel().isRaining(),
				"no storm is no snow");
		helper.succeed();
	}

	@GameTest
	public void aFreezingTargetIsAState(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		stray.setTarget(pig);
		helper.assertFalse(GeneCombat.test(stray, When.TARGET_FROZEN), "a warm target");
		pig.setTicksFrozen(40);
		helper.assertTrue(GeneCombat.test(stray, When.TARGET_FROZEN), "a target that freezes");
		helper.succeed();
	}

	private static void biome(GameTestHelper helper, String biome) {
		BlockPos from = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos to = helper.absolutePos(new BlockPos(5, 4, 5));
		helper.getLevel().getServer().getCommands().performPrefixedCommand(
				helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(),
				"fillbiome " + from.getX() + " " + from.getY() + " " + from.getZ() + " "
						+ to.getX() + " " + to.getY() + " " + to.getZ() + " " + biome);
	}

	static Stray strayWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Stray stray = EntityTypes.STRAY.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		stray.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		stray.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		stray.setNoAi(true);
		stray.setPersistenceRequired();
		stray.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(stray);
		return stray;
	}
}
