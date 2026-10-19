package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public class GenePoolsTest {
	private static final Identifier ZOMBIE = Identifier.fromNamespaceAndPath("originofspawns_test", "zombie");
	private static final Identifier SPIDER = Identifier.fromNamespaceAndPath("originofspawns_test", "spider");
	private static final Identifier FIRE = Identifier.fromNamespaceAndPath("originofspawns_test", "fire");
	private static final Identifier WATER = Identifier.fromNamespaceAndPath("originofspawns_test", "water");

	@GameTest
	public void poolsSurviveSaveAndLoad(GameTestHelper helper) {
		GenePools pools = new GenePools();
		pools.setFrequency(ZOMBIE, BiomeCategory.HOT, FIRE, 0.75F);
		pools.setFrequency(ZOMBIE, BiomeCategory.WET, WATER, 0.25F);
		pools.setFrequency(SPIDER, BiomeCategory.NETHER, FIRE, 1.0F);

		Tag tag = GenePools.CODEC.encodeStart(NbtOps.INSTANCE, pools).getOrThrow();
		GenePools loaded = GenePools.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();

		helper.assertValueEqual(loaded.frequency(ZOMBIE, BiomeCategory.HOT, FIRE), 0.75F, "zombie hot fire");
		helper.assertValueEqual(loaded.frequency(ZOMBIE, BiomeCategory.WET, WATER), 0.25F, "zombie wet water");
		helper.assertValueEqual(loaded.frequency(SPIDER, BiomeCategory.NETHER, FIRE), 1.0F, "spider nether fire");
		helper.assertValueEqual(loaded.frequency(ZOMBIE, BiomeCategory.HOT, WATER), 0.0F, "missing gene");
		helper.assertValueEqual(loaded.pool(SPIDER, BiomeCategory.HOT).size(), 0, "missing pool size");
		helper.succeed();
	}

	@GameTest
	public void frequencyStaysInRange(GameTestHelper helper) {
		GenePools pools = new GenePools();
		pools.setFrequency(ZOMBIE, BiomeCategory.COLD, FIRE, 1.5F);
		pools.setFrequency(ZOMBIE, BiomeCategory.COLD, WATER, -0.5F);
		helper.assertValueEqual(pools.frequency(ZOMBIE, BiomeCategory.COLD, FIRE), 1.0F, "high value");
		helper.assertValueEqual(pools.frequency(ZOMBIE, BiomeCategory.COLD, WATER), 0.0F, "low value");
		helper.succeed();
	}

	@GameTest
	public void serverKeepsOneInstance(GameTestHelper helper) {
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		pools.setDirty(false);
		pools.setFrequency(SPIDER, BiomeCategory.END, WATER, 0.5F);
		helper.assertTrue(pools.isDirty(), "a change marks the pools to save");
		GenePools again = GenePools.get(helper.getLevel().getServer());
		helper.assertTrue(pools == again, "the server returns the same pools");
		helper.assertValueEqual(again.frequency(SPIDER, BiomeCategory.END, WATER), 0.5F, "stored value");
		helper.succeed();
	}
}
