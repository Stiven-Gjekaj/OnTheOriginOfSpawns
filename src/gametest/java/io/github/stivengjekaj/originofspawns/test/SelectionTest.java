package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.GameType;

public class SelectionTest {
	private static final Identifier SPECIES = Identifier.fromNamespaceAndPath("originofspawns_test", "species");
	private static final Identifier GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "gene");
	private static final Identifier ZOMBIE = Identifier.withDefaultNamespace("zombie");
	private static final Identifier HEAT_SKIN = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");
	private static final Identifier CHASER = Identifier.fromNamespaceAndPath("originofspawns_test", "test_chaser");

	@GameTest
	public void eachDeathRaisesThePartOfTheGap(GameTestHelper helper) {
		GenePools pools = new GenePools();
		pools.select(SPECIES, BiomeCategory.HOT, GENE, 0.02F);
		helper.assertValueEqual(pools.frequency(SPECIES, BiomeCategory.HOT, GENE), 0.02F, "after one death");
		for (int i = 1; i < 50; i++) {
			pools.select(SPECIES, BiomeCategory.HOT, GENE, 0.02F);
		}
		float expected = (float) (1.0 - Math.pow(0.98, 50));
		helper.assertTrue(Math.abs(pools.frequency(SPECIES, BiomeCategory.HOT, GENE) - expected) < 0.0001F,
				"after 50 deaths, expected about " + expected);
		helper.succeed();
	}

	@GameTest
	public void decayLowersEachDay(GameTestHelper helper) {
		GenePools pools = new GenePools();
		pools.setFrequency(SPECIES, BiomeCategory.COLD, GENE, 0.5F);
		pools.decayTo(10L, id -> 0.01);
		helper.assertValueEqual(pools.frequency(SPECIES, BiomeCategory.COLD, GENE), 0.5F, "the first call only records");
		pools.decayTo(13L, id -> 0.01);
		float expected = (float) (0.5 * Math.pow(0.99, 3));
		helper.assertTrue(Math.abs(pools.frequency(SPECIES, BiomeCategory.COLD, GENE) - expected) < 0.0001F,
				"after 3 days, expected about " + expected);
		pools.decayTo(13L, id -> 0.01);
		helper.assertTrue(Math.abs(pools.frequency(SPECIES, BiomeCategory.COLD, GENE) - expected) < 0.0001F,
				"the same day does not decay again");
		helper.succeed();
	}

	@GameTest
	public void fireDeathRaisesTheFireGene(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Zombie zombie = plainZombie(helper, pos);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
		float heatBefore = pools.frequency(ZOMBIE, category, HEAT_SKIN);
		float chaserBefore = pools.frequency(ZOMBIE, category, CHASER);

		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);

		helper.assertTrue(zombie.isDeadOrDying(), "the zombie died");
		helper.assertValueEqual(pools.frequency(ZOMBIE, category, HEAT_SKIN), heatBefore + 0.02F * (1.0F - heatBefore),
				"fire gene");
		helper.assertValueEqual(pools.frequency(ZOMBIE, category, CHASER), chaserBefore, "unreached gene");
		helper.succeed();
	}

	@GameTest
	public void unreachedDeathRaisesTheChaseGene(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Zombie zombie = plainZombie(helper, pos);
		zombie.setTarget(helper.makeMockPlayer(GameType.SURVIVAL));
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
		float chaserBefore = pools.frequency(ZOMBIE, category, CHASER);

		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);

		helper.assertTrue(zombie.isDeadOrDying(), "the zombie died");
		helper.assertValueEqual(pools.frequency(ZOMBIE, category, CHASER), chaserBefore + 0.02F * (1.0F - chaserBefore),
				"unreached gene");
		helper.succeed();
	}

	private static Zombie plainZombie(GameTestHelper helper, BlockPos pos) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of()));
		return zombie;
	}
}
