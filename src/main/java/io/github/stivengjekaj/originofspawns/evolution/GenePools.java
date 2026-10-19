package io.github.stivengjekaj.originofspawns.evolution;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * All gene pools of a world. The world saves them in {@code data/originofspawns/gene_pools.dat}.
 */
public final class GenePools extends SavedData {
	public static final Codec<GenePools> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Identifier.CODEC, Codec.unboundedMap(BiomeCategory.CODEC, GenePool.CODEC))
					.fieldOf("species")
					.forGetter(pools -> pools.species)
	).apply(instance, GenePools::new));

	public static final SavedDataType<GenePools> TYPE =
			new SavedDataType<>(OriginOfSpawns.id("gene_pools"), GenePools::new, CODEC, null);

	private final Map<Identifier, Map<BiomeCategory, GenePool>> species = new HashMap<>();

	public GenePools() {
	}

	private GenePools(Map<Identifier, Map<BiomeCategory, GenePool>> species) {
		species.forEach((id, pools) -> this.species.put(id, new EnumMap<>(pools)));
	}

	public static GenePools get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	/**
	 * Returns the pool of a species in a category. The result is empty if the pool does not exist.
	 */
	public Map<Identifier, Float> pool(Identifier speciesId, BiomeCategory category) {
		Map<BiomeCategory, GenePool> pools = species.get(speciesId);
		if (pools == null || !pools.containsKey(category)) {
			return Map.of();
		}
		return pools.get(category).frequencies();
	}

	public float frequency(Identifier speciesId, BiomeCategory category, Identifier gene) {
		return pool(speciesId, category).getOrDefault(gene, 0.0F);
	}

	public void setFrequency(Identifier speciesId, BiomeCategory category, Identifier gene, float frequency) {
		species.computeIfAbsent(speciesId, id -> new EnumMap<>(BiomeCategory.class))
				.computeIfAbsent(category, c -> new GenePool())
				.setFrequency(gene, frequency);
		setDirty();
	}
}
