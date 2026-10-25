package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the piglin brute: the axe, the heavy body, the rage, the gate, and the broken guard.
 */
public class PiglinBruteEffectsTest {
	@GameTest
	public void theHomeOfABruteIsThePlaceWhereItSpawned(GameTestHelper helper) {
		PiglinBrute brute = bruteWith(helper, new BlockPos(4, 1, 4));
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "no home before the spawn");
		GeneBehaviors.onSpawn(brute, helper.getLevel(), EntitySpawnReason.NATURAL);
		BlockPos home = brute.getAttached(ModAttachments.HOME);
		helper.assertTrue(home.equals(brute.blockPosition()), "the home is the spawn place");
		helper.assertTrue(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "near home at the spawn");
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "not far from home at the spawn");
		// The test moves the home, not the brute, so the brute stays in the test area.
		brute.setAttached(ModAttachments.HOME, brute.blockPosition().east(20));
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "20 blocks is not near");
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "20 blocks is not far");
		brute.setAttached(ModAttachments.HOME, brute.blockPosition().east(40));
		helper.assertTrue(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "40 blocks is far");
		GeneBehaviors.onSpawn(brute, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(brute.getAttached(ModAttachments.HOME).equals(brute.blockPosition().east(40)),
				"a later spawn call keeps the old home");
		helper.succeed();
	}

	/**
	 * Adds a piglin brute with the test genes, a gold axe, and no AI. The genes go on before it enters the level, so
	 * it gets no genes from the pool.
	 */
	static PiglinBrute bruteWith(GameTestHelper helper, BlockPos pos, String... genes) {
		PiglinBrute brute = EntityTypes.PIGLIN_BRUTE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		brute.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		brute.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
		brute.setNoAi(true);
		brute.setPersistenceRequired();
		brute.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(brute);
		return brute;
	}
}
