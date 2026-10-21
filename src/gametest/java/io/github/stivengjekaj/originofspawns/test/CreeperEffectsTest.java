package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.CreeperAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the creeper genes use: the blast, the fuse, the charge, and the states of a creeper.
 */
public class CreeperEffectsTest {
	@GameTest
	public void aChargedCreeperIsInTheChargedState(GameTestHelper helper) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1));
		helper.assertFalse(GeneCombat.test(creeper, When.CHARGED), "a plain creeper is not charged");
		charge(creeper);
		helper.assertTrue(GeneCombat.test(creeper, When.CHARGED), "a charged creeper");
		var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 1));
		helper.assertFalse(GeneCombat.test(zombie, When.CHARGED), "a zombie is never charged");
		helper.succeed();
	}

	static void charge(Creeper creeper) {
		creeper.getEntityData().set(CreeperAccessor.originofspawns$poweredData(), true);
	}

	static Creeper creeperWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Creeper creeper = EntityTypes.CREEPER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		creeper.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		creeper.setNoAi(true);
		creeper.setPersistenceRequired();
		creeper.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(creeper);
		return creeper;
	}
}
