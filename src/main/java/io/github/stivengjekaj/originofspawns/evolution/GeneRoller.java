package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * Selects the genes of a new mob from the frequencies of its pool.
 */
public final class GeneRoller {
	/**
	 * The maximum number of genes on one mob.
	 */
	public static final int MAX_GENES = 5;

	private GeneRoller() {
	}

	/**
	 * A gene that a mob can get.
	 *
	 * @param frequency the frequency of the gene in the pool
	 * @param mutationRate the smallest chance to get the gene
	 */
	public record Candidate(Identifier gene, float frequency, float mutationRate) {
		float chance() {
			return Math.max(frequency, mutationRate);
		}
	}

	/**
	 * Each candidate goes to the mob with a chance equal to its frequency, or its mutation rate if that is higher.
	 * If more than {@link #MAX_GENES} genes pass, the genes with the highest frequency stay.
	 */
	public static List<Identifier> roll(List<Candidate> candidates, RandomSource random) {
		List<Candidate> passed = new ArrayList<>();
		for (Candidate candidate : candidates) {
			if (random.nextFloat() < candidate.chance()) {
				passed.add(candidate);
			}
		}
		return passed.stream()
				.sorted(Comparator.comparing(Candidate::frequency).reversed())
				.limit(MAX_GENES)
				.map(Candidate::gene)
				.toList();
	}
}
