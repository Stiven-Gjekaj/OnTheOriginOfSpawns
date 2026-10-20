package io.github.stivengjekaj.originofspawns.notebook;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The families that a player knows and the families that a player only saw, by the translation keys of their names.
 * A pure family and its major family alone have the same key, so one find shows both.
 *
 * @param keys the known families
 * @param seen the families that the player only saw. A known family is not in this set
 */
public record DiscoveredFamilies(Set<String> keys, Set<String> seen) {
	public static final DiscoveredFamilies NONE = new DiscoveredFamilies(Set.of(), Set.of());

	/**
	 * Reads the old format too: a plain list of known families, from version 0.2.0-alpha.
	 */
	public static final Codec<DiscoveredFamilies> CODEC = Codec.withAlternative(
			RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.listOf().fieldOf("known").forGetter(found -> sorted(found.keys())),
					Codec.STRING.listOf().optionalFieldOf("seen", List.of()).forGetter(found -> sorted(found.seen()))
			).apply(instance, (known, seen) -> new DiscoveredFamilies(Set.copyOf(known), Set.copyOf(seen)))),
			Codec.STRING.listOf().xmap(list -> new DiscoveredFamilies(Set.copyOf(list)), found -> sorted(found.keys())));

	public static final StreamCodec<RegistryFriendlyByteBuf, DiscoveredFamilies> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).cast(), found -> sorted(found.keys()),
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).cast(), found -> sorted(found.seen()),
			(known, seen) -> new DiscoveredFamilies(Set.copyOf(known), Set.copyOf(seen)));

	public DiscoveredFamilies(Set<String> keys) {
		this(keys, Set.of());
	}

	public DiscoveredFamilies {
		keys = Set.copyOf(keys);
		Set<String> onlySeen = new HashSet<>(seen);
		onlySeen.removeAll(keys);
		seen = Set.copyOf(onlySeen);
	}

	/**
	 * Tells if the player knows the family.
	 */
	public boolean contains(String key) {
		return keys.contains(key);
	}

	public Knowledge knowledge(String key) {
		return keys.contains(key) ? Knowledge.KNOWN : seen.contains(key) ? Knowledge.SEEN : Knowledge.NONE;
	}

	public DiscoveredFamilies with(Set<String> more) {
		Set<String> all = new HashSet<>(keys);
		all.addAll(more);
		return new DiscoveredFamilies(all, seen);
	}

	public DiscoveredFamilies withSeen(String key) {
		Set<String> all = new HashSet<>(seen);
		all.add(key);
		return new DiscoveredFamilies(keys, all);
	}

	private static List<String> sorted(Set<String> keys) {
		return keys.stream().sorted().toList();
	}
}
