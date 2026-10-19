package io.github.stivengjekaj.originofspawns.evolution;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

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
					.forGetter(pools -> pools.species),
			Codec.LONG.optionalFieldOf("last_decay_day", -1L).forGetter(pools -> pools.lastDecayDay)
	).apply(instance, GenePools::new));

	public static final SavedDataType<GenePools> TYPE =
			new SavedDataType<>(OriginOfSpawns.id("gene_pools"), GenePools::new, CODEC, null);

	private final Map<Identifier, Map<BiomeCategory, GenePool>> species = new HashMap<>();
	private long lastDecayDay = -1L;

	public GenePools() {
	}

	private GenePools(Map<Identifier, Map<BiomeCategory, GenePool>> species, long lastDecayDay) {
		species.forEach((id, pools) -> this.species.put(id, new EnumMap<>(pools)));
		this.lastDecayDay = lastDecayDay;
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

	/**
	 * Raises a gene after a death that counts for its pressure. The rise is a part of the distance to 100%.
	 */
	public void select(Identifier speciesId, BiomeCategory category, Identifier gene, float rate) {
		float frequency = frequency(speciesId, category, gene);
		setFrequency(speciesId, category, gene, frequency + rate * (1.0F - frequency));
	}

	/**
	 * Lowers each frequency for each day from the last decay to {@code day}.
	 * The first call only records the day.
	 *
	 * @param decayRate the decay rate of each gene
	 */
	public void decayTo(long day, ToDoubleFunction<Identifier> decayRate) {
		if (lastDecayDay < 0L) {
			lastDecayDay = day;
			setDirty();
			return;
		}
		long days = day - lastDecayDay;
		if (days <= 0L) {
			return;
		}
		for (Map<BiomeCategory, GenePool> pools : species.values()) {
			for (GenePool pool : pools.values()) {
				for (Map.Entry<Identifier, Float> entry : Map.copyOf(pool.frequencies()).entrySet()) {
					double kept = Math.pow(1.0 - decayRate.applyAsDouble(entry.getKey()), days);
					pool.setFrequency(entry.getKey(), (float) (entry.getValue() * kept));
				}
			}
		}
		lastDecayDay = day;
		setDirty();
	}

	public long lastDecayDay() {
		return lastDecayDay;
	}
}
