package io.github.stivengjekaj.originofspawns.definition;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.resources.ResourceKey;

/**
 * A player perk.
 *
 * @param xpCost the XP levels that the player pays to take the perk
 * @param incompatibleWith the perks that the player cannot have at the same time
 */
public record Perk(int xpCost, List<ResourceKey<Perk>> incompatibleWith) {
	public static final Codec<Perk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("xp_cost").forGetter(Perk::xpCost),
			ResourceKey.codec(ModRegistries.PERK).listOf().optionalFieldOf("incompatible_with", List.of())
					.forGetter(Perk::incompatibleWith)
	).apply(instance, Perk::new));
}
