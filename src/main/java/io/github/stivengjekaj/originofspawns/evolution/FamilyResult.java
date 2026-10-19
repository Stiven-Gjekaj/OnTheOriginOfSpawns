package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * The family of one mob, from the family tags of its genes.
 *
 * @param kind what kind of result it is
 * @param first the major family, or the first family of a hybrid
 * @param second the sub family, or the second family of a hybrid
 */
public record FamilyResult(Kind kind, Optional<Identifier> first, Optional<Identifier> second) {
	public static final FamilyResult NEUTRAL = new FamilyResult(Kind.NEUTRAL, Optional.empty(), Optional.empty());

	/**
	 * The points of a main family tag.
	 */
	public static final int MAIN_POINTS = 2;

	/**
	 * The points of a sub family tag.
	 */
	public static final int SUB_POINTS = 1;

	public static final Codec<FamilyResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Kind.CODEC.fieldOf("kind").forGetter(FamilyResult::kind),
			Identifier.CODEC.optionalFieldOf("first").forGetter(FamilyResult::first),
			Identifier.CODEC.optionalFieldOf("second").forGetter(FamilyResult::second)
	).apply(instance, FamilyResult::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FamilyResult> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT.map(index -> Kind.values()[index], Kind::ordinal).cast(), FamilyResult::kind,
			ByteBufCodecs.optional(Identifier.STREAM_CODEC).cast(), FamilyResult::first,
			ByteBufCodecs.optional(Identifier.STREAM_CODEC).cast(), FamilyResult::second,
			FamilyResult::new);

	/**
	 * The kinds of results, and the tiers of effects that they give.
	 */
	public enum Kind implements StringRepresentable {
		/**
		 * No family points. No family effects.
		 */
		NEUTRAL(0, 0),
		/**
		 * All points in one family. Tier 3 effects of that family.
		 */
		PURE(3, 0),
		/**
		 * A major family and a sub family. Tier 2 effects of the major, and tier 1 effects of the sub.
		 */
		MINOR(2, 1),
		/**
		 * Two families with the same points. Tier 2 effects of each.
		 */
		HYBRID(2, 2);

		public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

		private final int firstTier;
		private final int secondTier;

		Kind(int firstTier, int secondTier) {
			this.firstTier = firstTier;
			this.secondTier = secondTier;
		}

		public int firstTier() {
			return firstTier;
		}

		public int secondTier() {
			return secondTier;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * A gene of a mob, with its family tags and its frequency in the pool.
	 */
	public record TaggedGene(Identifier family, Optional<Identifier> subFamily, float frequency) {
	}

	/**
	 * Finds the family of a mob from its genes.
	 * The family with the most points is first. If two families have the same points, the family of the gene with
	 * the highest frequency comes first.
	 */
	public static FamilyResult of(List<TaggedGene> genes) {
		Map<Identifier, Integer> points = new HashMap<>();
		Map<Identifier, Float> bestFrequency = new HashMap<>();
		for (TaggedGene gene : genes) {
			add(points, bestFrequency, gene.family(), MAIN_POINTS, gene.frequency());
			gene.subFamily().ifPresent(sub -> add(points, bestFrequency, sub, SUB_POINTS, gene.frequency()));
		}
		if (points.isEmpty()) {
			return NEUTRAL;
		}
		List<Identifier> order = new ArrayList<>(points.keySet());
		order.sort(Comparator.<Identifier>comparingInt(points::get).reversed()
				.thenComparing(Comparator.<Identifier, Float>comparing(bestFrequency::get).reversed())
				.thenComparing(Comparator.naturalOrder()));
		Identifier first = order.getFirst();
		if (order.size() == 1) {
			return new FamilyResult(Kind.PURE, Optional.of(first), Optional.empty());
		}
		Identifier second = order.get(1);
		Kind kind = points.get(first).equals(points.get(second)) ? Kind.HYBRID : Kind.MINOR;
		return new FamilyResult(kind, Optional.of(first), Optional.of(second));
	}

	private static void add(Map<Identifier, Integer> points, Map<Identifier, Float> bestFrequency, Identifier family,
			int amount, float frequency) {
		points.merge(family, amount, Integer::sum);
		bestFrequency.merge(family, frequency, Math::max);
	}
}
