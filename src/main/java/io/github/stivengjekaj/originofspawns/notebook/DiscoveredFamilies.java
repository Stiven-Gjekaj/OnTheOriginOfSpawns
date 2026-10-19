package io.github.stivengjekaj.originofspawns.notebook;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;

/**
 * The families that a player found, by the translation keys of their names.
 * A pure family and its major family alone have the same key, so one find shows both.
 */
public record DiscoveredFamilies(Set<String> keys) {
	public static final DiscoveredFamilies NONE = new DiscoveredFamilies(Set.of());

	public static final Codec<DiscoveredFamilies> CODEC = Codec.STRING.listOf()
			.xmap(list -> new DiscoveredFamilies(Set.copyOf(list)), found -> found.keys().stream().sorted().toList());

	public DiscoveredFamilies {
		keys = Set.copyOf(keys);
	}

	public boolean contains(String key) {
		return keys.contains(key);
	}

	public DiscoveredFamilies with(Set<String> more) {
		Set<String> all = new HashSet<>(keys);
		all.addAll(more);
		return new DiscoveredFamilies(all);
	}
}
