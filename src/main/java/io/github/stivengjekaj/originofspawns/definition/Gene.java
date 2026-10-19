package io.github.stivengjekaj.originofspawns.definition;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.registry.ModRegistries;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

/**
 * A gene that a species can carry. A gene works like a player perk: it has gains and downsides.
 *
 * @param species the mobs that can carry the gene
 * @param counters the pressure that makes the gene stronger
 * @param family the main family tag. It gives 2 points to the family
 * @param subFamily the sub family tag, if the gene has one. It gives 1 point to the family
 * @param gains the effects that help the mob
 * @param downsides the effects that hurt the mob
 * @param selectionRate the rise for each death, as a part of the distance to 100%, from 0 to 1
 * @param mutationRate the smallest chance that a new mob gets the gene, from 0 to 1
 * @param decayRate the loss for each Minecraft day, as a part of the frequency, from 0 to 1
 * @param visual the visual recipe, if the gene changes how the mob looks
 */
public record Gene(
		HolderSet<EntityType<?>> species,
		ResourceKey<Pressure> counters,
		Identifier family,
		Optional<Identifier> subFamily,
		List<GeneEffect> gains,
		List<GeneEffect> downsides,
		float selectionRate,
		float mutationRate,
		float decayRate,
		Optional<GeneVisual> visual
) {
	public static final Codec<Gene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("species").forGetter(Gene::species),
			ResourceKey.codec(ModRegistries.PRESSURE).fieldOf("counters").forGetter(Gene::counters),
			Identifier.CODEC.fieldOf("family").forGetter(Gene::family),
			Identifier.CODEC.optionalFieldOf("sub_family").forGetter(Gene::subFamily),
			GeneEffect.CODEC.listOf().optionalFieldOf("gains", List.of()).forGetter(Gene::gains),
			GeneEffect.CODEC.listOf().optionalFieldOf("downsides", List.of()).forGetter(Gene::downsides),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("selection_rate", 0.02F).forGetter(Gene::selectionRate),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("mutation_rate", 0.01F).forGetter(Gene::mutationRate),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("decay_rate", 0.01F).forGetter(Gene::decayRate),
			GeneVisual.CODEC.optionalFieldOf("visual").forGetter(Gene::visual)
	).apply(instance, Gene::new));

	/**
	 * Returns the gains and then the downsides.
	 */
	public Stream<GeneEffect> effects() {
		return Stream.concat(gains.stream(), downsides.stream());
	}

	public boolean carriedBy(EntityType<?> type) {
		return species.stream().anyMatch(holder -> holder.value() == type);
	}
}
