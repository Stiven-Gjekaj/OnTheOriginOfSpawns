package io.github.stivengjekaj.originofspawns.notebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Makes notebook pages from the gene pools of a world and the families that a player found.
 */
public final class NotebookPages {
	private NotebookPages() {
	}

	/**
	 * Returns a page for each species that has genes or families, in the order of their ids.
	 */
	public static NotebookPayload pages(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		DiscoveredFamilies found = player.getAttachedOrElse(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		List<NotebookPayload.Page> pages = new ArrayList<>();
		for (EntityType<?> type : species(server)) {
			pages.add(page(server, type, found));
		}
		return new NotebookPayload(pages);
	}

	/**
	 * Returns the species that have genes or families, in the order of their ids.
	 */
	public static List<EntityType<?>> species(MinecraftServer server) {
		var genes = server.registryAccess().lookupOrThrow(ModRegistries.GENE);
		var families = server.registryAccess().lookupOrThrow(ModRegistries.FAMILY);
		List<EntityType<?>> types = new ArrayList<>();
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			boolean hasGenes = genes.stream().anyMatch(gene -> gene.carriedBy(type));
			boolean hasFamilies = families.stream()
					.anyMatch(family -> family.species().stream().anyMatch(holder -> holder.value() == type));
			if (hasGenes || hasFamilies) {
				types.add(type);
			}
		}
		types.sort((a, b) -> BuiltInRegistries.ENTITY_TYPE.getKey(a).compareTo(BuiltInRegistries.ENTITY_TYPE.getKey(b)));
		return types;
	}

	/**
	 * Returns the page of a species: each gene that the species can carry, with its frequency in each category,
	 * and each family name of the species.
	 */
	public static NotebookPayload.Page page(MinecraftServer server, EntityType<?> type, DiscoveredFamilies found) {
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
		List<NotebookPayload.FamilyEntry> families = FamilyCatalog.of(server.registryAccess(), type).stream()
				.map(entry -> new NotebookPayload.FamilyEntry(entry.key(), entry.kind(), found.contains(entry.key())))
				.toList();
		return new NotebookPayload.Page(species, entries, families);
	}
}
