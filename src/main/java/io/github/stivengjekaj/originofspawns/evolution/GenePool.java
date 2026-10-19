package io.github.stivengjekaj.originofspawns.evolution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The gene frequencies of one species in one biome category.
 * Each frequency is from 0 to 1. A gene that is not in the pool has the frequency 0.
 */
public final class GenePool {
	public static final Codec<GenePool> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.floatRange(0.0F, 1.0F))
			.xmap(GenePool::new, pool -> pool.frequencies);

	private final Map<Identifier, Float> frequencies;

	public GenePool() {
		this(Map.of());
	}

	private GenePool(Map<Identifier, Float> frequencies) {
		this.frequencies = new HashMap<>(frequencies);
	}

	public float frequency(Identifier gene) {
		return frequencies.getOrDefault(gene, 0.0F);
	}

	/**
	 * Sets the frequency of a gene. The value is clamped to the range from 0 to 1.
	 */
	public void setFrequency(Identifier gene, float frequency) {
		frequencies.put(gene, Mth.clamp(frequency, 0.0F, 1.0F));
	}

	public Map<Identifier, Float> frequencies() {
		return Collections.unmodifiableMap(frequencies);
	}
}
