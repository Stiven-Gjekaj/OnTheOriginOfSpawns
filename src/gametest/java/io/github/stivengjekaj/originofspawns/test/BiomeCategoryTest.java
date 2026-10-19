package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class BiomeCategoryTest {
	@GameTest
	public void biomesGetTheirCategory(GameTestHelper helper) {
		check(helper, Biomes.DESERT, BiomeCategory.HOT);
		check(helper, Biomes.JUNGLE, BiomeCategory.HOT);
		check(helper, Biomes.SNOWY_PLAINS, BiomeCategory.COLD);
		check(helper, Biomes.SWAMP, BiomeCategory.WET);
		check(helper, Biomes.PLAINS, BiomeCategory.TEMPERATE);
		check(helper, Biomes.FOREST, BiomeCategory.TEMPERATE);
		check(helper, Biomes.NETHER_WASTES, BiomeCategory.NETHER);
		check(helper, Biomes.THE_END, BiomeCategory.END);
		helper.succeed();
	}

	private static void check(GameTestHelper helper, ResourceKey<Biome> key, BiomeCategory expected) {
		Holder<Biome> biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key);
		helper.assertValueEqual(BiomeCategory.of(biome), expected, key.identifier().toString());
	}
}
