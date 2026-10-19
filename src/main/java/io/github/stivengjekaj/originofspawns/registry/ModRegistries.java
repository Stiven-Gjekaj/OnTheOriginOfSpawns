package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.Perk;
import io.github.stivengjekaj.originofspawns.definition.Pressure;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * The data pack registries of the mod.
 * A data pack puts an entry in {@code data/<namespace>/originofspawns/<registry>/<name>.json}.
 */
public final class ModRegistries {
	public static final ResourceKey<Registry<Pressure>> PRESSURE =
			ResourceKey.createRegistryKey(OriginOfSpawns.id("pressure"));
	public static final ResourceKey<Registry<Gene>> GENE =
			ResourceKey.createRegistryKey(OriginOfSpawns.id("gene"));
	public static final ResourceKey<Registry<Perk>> PERK =
			ResourceKey.createRegistryKey(OriginOfSpawns.id("perk"));

	private ModRegistries() {
	}

	public static void register() {
		DynamicRegistries.register(PRESSURE, Pressure.CODEC);
		// The client needs genes and perks for the notebook and the perk screen.
		DynamicRegistries.registerSynced(GENE, Gene.CODEC);
		DynamicRegistries.registerSynced(PERK, Perk.CODEC);
	}
}
