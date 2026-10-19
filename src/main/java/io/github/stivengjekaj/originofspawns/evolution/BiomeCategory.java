package io.github.stivengjekaj.originofspawns.evolution;

import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * A group of biomes. Each species has one gene pool for each category.
 */
public enum BiomeCategory implements StringRepresentable {
	HOT,
	COLD,
	WET,
	TEMPERATE,
	NETHER,
	END;

	public static final Codec<BiomeCategory> CODEC = StringRepresentable.fromEnum(BiomeCategory::values);

	private final String serializedName = name().toLowerCase(Locale.ROOT);

	@Override
	public String getSerializedName() {
		return serializedName;
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
