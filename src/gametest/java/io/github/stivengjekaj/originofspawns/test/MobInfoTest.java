package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;
import io.github.stivengjekaj.originofspawns.evolution.MobInfo;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

/**
 * Makes the text of the mob info panel from genes and a family.
 */
public class MobInfoTest {
	private static final Identifier STALKER_GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");
	private static final Identifier TEST_GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "test_hunt");
	private static final Identifier TEST = Identifier.fromNamespaceAndPath("originofspawns_test", "test");
	private static final Identifier PREDATOR = Identifier.fromNamespaceAndPath("originofspawns_test", "predator");

	@GameTest
	public void dominantGenesComeFirst(GameTestHelper helper) {
		MobInfo info = info(helper, new FamilyResult(Kind.MAJOR, Optional.of(TEST), Optional.empty()));
		helper.assertValueEqual(info.genes().getFirst().gene(), TEST_GENE, "the first gene");
		helper.assertTrue(info.genes().getFirst().dominant(), "the gene of the family is dominant");
		helper.assertFalse(info.genes().get(1).dominant(), "the other gene is not dominant");
		helper.assertTrue(info.kind().isPresent(), "a major family has a kind");
		helper.succeed();
	}

	@GameTest
	public void aSubTagMakesAGeneDominant(GameTestHelper helper) {
		MobInfo info = info(helper, new FamilyResult(Kind.MAJOR, Optional.of(PREDATOR), Optional.empty()));
		helper.assertValueEqual(info.genes().getFirst().gene(), STALKER_GENE, "the first gene");
		helper.assertTrue(info.genes().getFirst().dominant(), "the sub tag counts");
		helper.succeed();
	}

	@GameTest
	public void aNeutralMobHasNoKind(GameTestHelper helper) {
		MobInfo info = info(helper, FamilyResult.NEUTRAL);
		helper.assertTrue(info.kind().isEmpty(), "no kind");
		helper.assertFalse(info.genes().stream().anyMatch(MobInfo.Line::dominant), "no dominant genes");
		helper.assertTrue(info.family().getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals("family.originofspawns.neutral"), "the neutral name");
		helper.succeed();
	}

	@GameTest
	public void anUnknownGeneIsNotDominant(GameTestHelper helper) {
		Identifier unknown = Identifier.fromNamespaceAndPath("originofspawns_test", "no_such_gene");
		MobInfo info = MobInfo.of(new CarriedGenes(List.of(unknown),
				new FamilyResult(Kind.MAJOR, Optional.of(TEST), Optional.empty())), id -> Optional.empty());
		helper.assertFalse(info.genes().getFirst().dominant(), "not dominant");
		helper.succeed();
	}

	private static MobInfo info(GameTestHelper helper, FamilyResult family) {
		var genes = helper.getLevel().registryAccess().lookupOrThrow(ModRegistries.GENE);
		return MobInfo.of(new CarriedGenes(List.of(STALKER_GENE, TEST_GENE), family), genes::getOptional);
	}
}
