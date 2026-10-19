package io.github.stivengjekaj.originofspawns.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

/**
 * A gene that a species can carry.
 *
 * @param species the mobs that can carry the gene
 * @param counters the pressure that makes the gene stronger
 * @param cost the gene points that the gene uses
 * @param mutationRate the random change in each generation, from 0 to 1
 * @param decayRate the loss in each generation when the pressure stops, from 0 to 1
 */
public record Gene(
		HolderSet<EntityType<?>> species,
		ResourceKey<Pressure> counters,
		int cost,
		float mutationRate,
		float decayRate
) {
	public static final Codec<Gene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("species").forGetter(Gene::species),
			ResourceKey.codec(ModRegistries.PRESSURE).fieldOf("counters").forGetter(Gene::counters),
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("cost").forGetter(Gene::cost),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("mutation_rate", 0.01F).forGetter(Gene::mutationRate),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("decay_rate", 0.001F).forGetter(Gene::decayRate)
	).apply(instance, Gene::new));
}
