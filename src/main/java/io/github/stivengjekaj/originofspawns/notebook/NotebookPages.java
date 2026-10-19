package io.github.stivengjekaj.originofspawns.notebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;

/**
 * Makes notebook pages from the gene pools of a world.
 */
public final class NotebookPages {
	private NotebookPages() {
	}

	/**
	 * Returns the page of a species: each gene that the species can carry, with its frequency in each category.
	 */
	public static NotebookPayload page(MinecraftServer server, EntityType<?> type) {
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		GenePools pools = GenePools.get(server);
		List<NotebookPayload.Entry> entries = new ArrayList<>();
		for (Map.Entry<ResourceKey<Gene>, Gene> gene : server.registryAccess().lookupOrThrow(ModRegistries.GENE).entrySet()) {
			if (!gene.getValue().carriedBy(type)) {
				continue;
			}
			Identifier id = gene.getKey().identifier();
			for (BiomeCategory category : BiomeCategory.values()) {
				entries.add(new NotebookPayload.Entry(category, id, pools.frequency(species, category, id)));
			}
		}
		entries.sort((a, b) -> a.gene().compareTo(b.gene()));
		return new NotebookPayload(species, entries);
	}
}
