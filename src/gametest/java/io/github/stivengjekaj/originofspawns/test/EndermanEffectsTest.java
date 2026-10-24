package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the enderman genes use: the teleports, the stare, the thrown block, and the water. It also
 * tests that the reused effects work on an enderman. Each teleport goes to a place inside the test area.
 */
public class EndermanEffectsTest {
	@GameTest
	public void teleportToTargetGoesNextToTheTargetOneTimeInEachCooldown(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		enderman.setTarget(pig);
		var teleport = new GeneEffect.TeleportToTarget(6.0F, GeneEffect.When.ALWAYS, 3.0F);
		helper.assertTrue(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "the enderman teleports");
		helper.assertTrue(enderman.distanceTo(pig) < 3.0F, "the enderman is next to the pig, at " + enderman.distanceTo(pig));
		helper.assertTrue(enderman.hasAttached(ModAttachments.TELEPORT_TIME), "the teleport is marked");
		enderman.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))), 0.0F, 0.0F);
		helper.assertFalse(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "the teleport waits for its cooldown");
		helper.succeed();
	}

	@GameTest
	public void aTeleportWithNoFreePlaceDoesNotStartTheCooldown(GameTestHelper helper) {
		// A ring of stone fills each place next to the pig, also the places at the edge of the test area.
		for (int x = 1; x <= 7; x++) {
			for (int z = 1; z <= 7; z++) {
				if (x != 4 || z != 4) {
					for (int y = 1; y <= 3; y++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
					}
				}
			}
		}
		Enderman enderman = endermanWith(helper, new BlockPos(0, 1, 0));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		enderman.setTarget(pig);
		var teleport = new GeneEffect.TeleportToTarget(6.0F, GeneEffect.When.ALWAYS, 3.0F);
		helper.assertFalse(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "no free place, no teleport");
		// A gap of two blocks to the west makes one free place, 1.5 blocks from the pig.
		for (int y = 1; y <= 3; y++) {
			helper.setBlock(new BlockPos(2, y, 4), Blocks.AIR);
			helper.setBlock(new BlockPos(3, y, 4), Blocks.AIR);
		}
		helper.assertTrue(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport),
				"the cooldown did not start, so the next try teleports");
		helper.assertTrue(enderman.getX() < pig.getX() && enderman.distanceTo(pig) < 2.0F, "the enderman is in the gap");
		helper.succeed();
	}

	@GameTest
	public void farStepTeleportsOnlyWhenTheTargetIsOutOfReach(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1), "test_far_step");
		enderman.setOnGround(true);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		enderman.setTarget(pig);
		helper.assertFalse(GeneCombat.test(enderman, GeneEffect.When.TARGET_UNREACHABLE), "the enderman can walk to the pig");
		// A wall of stone around the enderman stops each path.
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				if (x != 1 || z != 1) {
					for (int y = 1; y <= 4; y++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
					}
				}
			}
		}
		helper.assertTrue(GeneCombat.test(enderman, GeneEffect.When.TARGET_UNREACHABLE), "the wall stops the enderman");
		helper.succeed();
	}

	static Enderman endermanWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Enderman enderman = EntityTypes.ENDERMAN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		enderman.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		enderman.setNoAi(true);
		enderman.setPersistenceRequired();
		enderman.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(enderman);
		return enderman;
	}
}
