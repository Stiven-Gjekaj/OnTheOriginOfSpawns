package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Connects the evolution of the mod to the game.
 */
public final class Evolution {
	private Evolution() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register(Evolution::onLoad);
	}

	private static void onLoad(Entity entity, ServerLevel level) {
		if (entity instanceof Mob mob && !mob.hasAttached(ModAttachments.GENES)) {
			rollGenes(mob, level);
		}
	}

	/**
	 * Gives a new mob its genes from the pool of its species in the category of its biome.
	 * A mob of a species that no gene can go to gets no genes.
	 */
	public static void rollGenes(Mob mob, ServerLevel level) {
		Registry<Gene> registry = level.registryAccess().lookupOrThrow(ModRegistries.GENE);
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		GenePools pools = GenePools.get(level.getServer());

		List<GeneRoller.Candidate> candidates = new ArrayList<>();
		for (Map.Entry<ResourceKey<Gene>, Gene> entry : registry.entrySet()) {
			if (entry.getValue().carriedBy(mob.getType())) {
				Identifier id = entry.getKey().identifier();
				candidates.add(new GeneRoller.Candidate(id, pools.frequency(species, category, id),
						entry.getValue().mutationRate()));
			}
		}
		if (candidates.isEmpty()) {
			return;
		}

		List<Identifier> rolled = GeneRoller.roll(candidates, mob.getRandom());
		Map<Identifier, Gene> genes = new LinkedHashMap<>();
		for (Identifier id : rolled) {
			genes.put(id, registry.getValue(id));
		}
		mob.setAttached(ModAttachments.GENES, new CarriedGenes(rolled));
		GeneEffects.applyAttributes(mob, genes);
	}
}
