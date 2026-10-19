package io.github.stivengjekaj.originofspawns.notebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.definition.Family;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/**
 * Lists each family name that a species can have: the pure families, the minor families, the hybrids, and neutral.
 */
public final class FamilyCatalog {
	private FamilyCatalog() {
	}

	/**
	 * One family name of a species.
	 *
	 * @param key the translation key of the name
	 * @param kind the kind of the family. A pure family stands for its major family alone too
	 */
	public record Entry(String key, Kind kind) {
	}

	public static List<Entry> of(RegistryAccess registries, EntityType<?> type) {
		List<Identifier> families = new ArrayList<>();
		registries.lookupOrThrow(ModRegistries.FAMILY).entrySet().forEach(entry -> {
			Family family = entry.getValue();
			if (family.species().stream().anyMatch(holder -> holder.value() == type)) {
				families.add(entry.getKey().identifier());
			}
		});
		families.sort(null);
		List<Entry> entries = new ArrayList<>();
		for (Identifier family : families) {
			entries.add(entry(new FamilyResult(Kind.PURE, Optional.of(family), Optional.empty())));
		}
		for (Identifier major : families) {
			for (Identifier sub : families) {
				if (!major.equals(sub)) {
					entries.add(entry(new FamilyResult(Kind.MINOR, Optional.of(major), Optional.of(sub))));
				}
			}
		}
		for (int i = 0; i < families.size(); i++) {
			for (int j = i + 1; j < families.size(); j++) {
				entries.add(entry(new FamilyResult(Kind.HYBRID, Optional.of(families.get(i)), Optional.of(families.get(j)))));
			}
		}
		entries.add(entry(FamilyResult.NEUTRAL));
		return entries;
	}

	private static Entry entry(FamilyResult result) {
		return new Entry(result.translationKey(), result.kind());
	}
}
