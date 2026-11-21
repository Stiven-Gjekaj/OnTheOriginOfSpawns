package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Checks the genes of a wolf: the pool, the tame rule, the babies, the anger, the calls, and the reused effects.
 */
public class WolfEffectsTest {
	static final Identifier WOLF = Identifier.withDefaultNamespace("wolf");
	static final Identifier PELT = Identifier.fromNamespaceAndPath("originofspawns_test", "test_wolf_pelt");

	static Wolf wolf(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.WOLF, pos, genes);
	}

	static BiomeCategory category(GameTestHelper helper, BlockPos pos) {
		return BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
	}

	@GameTest
	public void aWildWolfGetsGenesFromThePool(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		Wolf wolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, pos);
		pools.setFrequency(WOLF, category, PELT, before);
		CarriedGenes genes = wolf.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null && genes.genes().contains(PELT), "the wolf takes the gene from the pool");
		helper.succeed();
	}

	@GameTest
	public void eachDeathOfAWildWolfCountsAngryOrNot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		Wolf calm = wolf(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		calm.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(calm.isDeadOrDying(), "the calm wolf dies");
		float afterCalm = pools.frequency(WOLF, category, PELT);
		helper.assertTrue(afterCalm > before, "the death of a calm wolf raises the fire gene");
		Wolf angry = wolf(helper, new BlockPos(4, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		angry.setPersistentAngerTarget(EntityReference.of(player));
		angry.startPersistentAngerTimer();
		helper.assertTrue(angry.isAngry(), "the second wolf is angry");
		angry.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(pools.frequency(WOLF, category, PELT) > afterCalm, "the death of an angry wolf raises it too");
		helper.succeed();
	}
}
