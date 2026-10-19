package io.github.stivengjekaj.originofspawns.evolution;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

/**
 * A group of biomes. Each species has one gene pool for each category.
 * The biome tags {@code originofspawns:category/<name>} select the category of a biome.
 */
public enum BiomeCategory implements StringRepresentable {
	HOT,
	COLD,
	WET,
	TEMPERATE,
	NETHER,
	END;

	public static final Codec<BiomeCategory> CODEC = StringRepresentable.fromEnum(BiomeCategory::values);

	/**
	 * The categories in the order of the check. The first match wins. TEMPERATE has no tag.
	 */
	private static final BiomeCategory[] CHECK_ORDER = {NETHER, END, HOT, COLD, WET};

	private final String serializedName = name().toLowerCase(Locale.ROOT);
	private final TagKey<Biome> tag =
			TagKey.create(Registries.BIOME, OriginOfSpawns.id("category/" + serializedName));

	@Override
	public String getSerializedName() {
		return serializedName;
	}

	public TagKey<Biome> tag() {
		return tag;
	}

	public static BiomeCategory of(Holder<Biome> biome) {
		for (BiomeCategory category : CHECK_ORDER) {
			if (biome.is(category.tag)) {
				return category;
			}
		}
		return TEMPERATE;
	}

	public static Optional<BiomeCategory> byName(String name) {
		for (BiomeCategory category : values()) {
			if (category.serializedName.equals(name)) {
				return Optional.of(category);
			}
		}
		return Optional.empty();
	}
}
