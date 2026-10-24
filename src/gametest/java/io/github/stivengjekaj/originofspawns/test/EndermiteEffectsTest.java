package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the endermite genes use: the swarm, the ender pearl, the life, and the call to the
 * endermen. It also tests that the teleports and the reused effects work on an endermite.
 */
public class EndermiteEffectsTest {
	@GameTest
	public void aSwarmSpawnsMoreEndermitesWithGenesFromThePool(GameTestHelper helper) {
		// The gene of the test is an enderman gene, so the pool of the endermite never gives it to the new endermites.
		Endermite mite = endermiteWith(helper, new BlockPos(4, 1, 4), "test_swarm_pool");
		GeneBehaviors.onSpawn(mite, helper.getLevel(), EntitySpawnReason.TRIGGERED);
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_swarm_pool");
		List<Endermite> extras = helper.getLevel().getEntitiesOfClass(Endermite.class, mite.getBoundingBox().inflate(3.0),
				other -> other != mite);
		helper.assertValueEqual(extras.size(), 2, "2 more endermites spawn");
		for (Endermite extra : extras) {
			CarriedGenes genes = extra.getAttached(ModAttachments.GENES);
			helper.assertFalse(genes != null && genes.genes().contains(gene), "a new endermite has genes from the pool");
		}
		helper.succeed();
	}

	@GameTest
	public void pearlBroodRaisesTheChanceOfAnEndermiteFromAPearl(GameTestHelper helper) {
		// Each endermite of the test is not in the level yet, as an endermite that a pearl may make.
		Endermite brood = loose(helper, new BlockPos(2, 1, 2), "test_pearl_brood");
		Endermite plain = loose(helper, new BlockPos(5, 1, 5));
		helper.assertFalse(GeneBehaviors.pearlSpawn(helper.getLevel(), plain, 0.0F), "a plain endermite keeps the vanilla chance");
		helper.assertTrue(GeneBehaviors.pearlSpawn(helper.getLevel(), brood, 0.99F), "a chance of 1 always makes the endermite");
		helper.assertTrue(helper.getLevel().getEntity(brood.getId()) == brood, "the endermite joins the level");
		helper.assertValueEqual(GeneBehaviors.pearlRoll(helper.getLevel(), plain, 0.01F), 0.01F,
				"a roll under 5% makes the vanilla endermite");
		helper.succeed();
	}

	/**
	 * Makes an endermite with the genes at the place, but does not add it to the level.
	 */
	static Endermite loose(GameTestHelper helper, BlockPos pos, String... genes) {
		Endermite mite = EntityTypes.ENDERMITE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		mite.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		mite.setNoAi(true);
		mite.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		return mite;
	}

	static Endermite endermiteWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Endermite mite = EntityTypes.ENDERMITE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		mite.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		mite.setNoAi(true);
		mite.setPersistenceRequired();
		mite.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(mite);
		return mite;
	}
}
