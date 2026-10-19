package io.github.stivengjekaj.originofspawns.definition;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.entity.EntityType;

/**
 * A major family of a species. It has one big buff and one big debuff, at 3 tiers.
 *
 * @param species the species that the family is for
 * @param tiers the effects of tier 1, tier 2, and tier 3, in this order
 */
public record Family(HolderSet<EntityType<?>> species, List<Tier> tiers) {
	public static final int TIERS = 3;

	public static final Codec<Family> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("species").forGetter(Family::species),
			Tier.CODEC.listOf(TIERS, TIERS).fieldOf("tiers").forGetter(Family::tiers)
	).apply(instance, Family::new));

	/**
	 * Returns the tier with this number, from 1 to 3.
	 */
	public Tier tier(int number) {
		return tiers.get(number - 1);
	}

	/**
	 * The effects of one tier: the buff and the debuff.
	 */
	public record Tier(List<GeneEffect> buffs, List<GeneEffect> debuffs) {
		public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				GeneEffect.CODEC.listOf().optionalFieldOf("buffs", List.of()).forGetter(Tier::buffs),
				GeneEffect.CODEC.listOf().optionalFieldOf("debuffs", List.of()).forGetter(Tier::debuffs)
		).apply(instance, Tier::new));

		public Stream<GeneEffect> effects() {
			return Stream.concat(buffs.stream(), debuffs.stream());
		}
	}
}
