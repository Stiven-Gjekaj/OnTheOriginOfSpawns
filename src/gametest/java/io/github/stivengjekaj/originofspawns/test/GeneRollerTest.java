package io.github.stivengjekaj.originofspawns.test;

import java.util.ArrayList;
import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.GeneRoller;
import io.github.stivengjekaj.originofspawns.evolution.GeneRoller.Candidate;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class GeneRollerTest {
	@GameTest
	public void rollKeepsTheFiveMostFrequentGenes(GameTestHelper helper) {
		List<Candidate> candidates = new ArrayList<>();
		for (int i = 1; i <= 7; i++) {
			candidates.add(new Candidate(gene("g" + i), i / 10.0F, 1.0F));
		}
		List<Identifier> rolled = GeneRoller.roll(candidates, RandomSource.create(1));
		helper.assertValueEqual(rolled, List.of(gene("g7"), gene("g6"), gene("g5"), gene("g4"), gene("g3")),
				"rolled genes");
		helper.succeed();
	}

	@GameTest
	public void frequencyAndMutationSetTheChance(GameTestHelper helper) {
		Candidate never = new Candidate(gene("never"), 0.0F, 0.0F);
		Candidate always = new Candidate(gene("always"), 1.0F, 0.0F);
		Candidate mutant = new Candidate(gene("mutant"), 0.0F, 1.0F);
		RandomSource random = RandomSource.create(2);
		for (int i = 0; i < 100; i++) {
			helper.assertValueEqual(GeneRoller.roll(List.of(never, always, mutant), random),
					List.of(gene("always"), gene("mutant")), "rolled genes");
		}
		helper.succeed();
	}

	@GameTest
	public void halfFrequencyGivesTheGeneToAboutHalf(GameTestHelper helper) {
		Candidate half = new Candidate(gene("half"), 0.5F, 0.0F);
		RandomSource random = RandomSource.create(3);
		int count = 0;
		for (int i = 0; i < 1000; i++) {
			count += GeneRoller.roll(List.of(half), random).size();
		}
		helper.assertTrue(count > 400 && count < 600, "count was " + count + " of 1000");
		helper.succeed();
	}

	private static Identifier gene(String path) {
		return Identifier.fromNamespaceAndPath("originofspawns_test", path);
	}
}
