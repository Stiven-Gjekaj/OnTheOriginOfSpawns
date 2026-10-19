package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.TaggedGene;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Stray;

public class FamilyTest {
	private static final Identifier A = id("a");
	private static final Identifier B = id("b");
	private static final Identifier C = id("c");

	@GameTest
	public void noGenesIsNeutral(GameTestHelper helper) {
		helper.assertValueEqual(FamilyResult.of(List.of()), FamilyResult.NEUTRAL, "result");
		helper.succeed();
	}

	@GameTest
	public void oneGeneIsNeutral(GameTestHelper helper) {
		helper.assertValueEqual(FamilyResult.of(List.of(gene(A, 0.5F))), FamilyResult.NEUTRAL, "result");
		helper.succeed();
	}

	@GameTest
	public void twoGenesOfOneFamilyAreAMajorAlone(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(A, 0.5F), gene(A, 0.2F)));
		helper.assertValueEqual(result, new FamilyResult(Kind.MAJOR, Optional.of(A), Optional.empty()), "result");
		helper.succeed();
	}

	@GameTest
	public void threeGenesOfOneFamilyArePure(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(A, 0.5F), gene(A, 0.2F), gene(A, 0.1F)));
		helper.assertValueEqual(result, new FamilyResult(Kind.PURE, Optional.of(A), Optional.empty()), "result");
		helper.succeed();
	}

	@GameTest
	public void threeDifferentFamiliesAreNeutral(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(A, 0.5F), gene(B, 0.2F), gene(C, 0.1F)));
		helper.assertValueEqual(result, FamilyResult.NEUTRAL, "result");
		helper.succeed();
	}

	@GameTest
	public void moreGenesMakeTheMajorFamily(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(B, 0.9F), gene(A, 0.1F), gene(A, 0.1F)));
		helper.assertValueEqual(result, new FamilyResult(Kind.MINOR, Optional.of(A), Optional.of(B)), "result");
		helper.succeed();
	}

	@GameTest
	public void samePointsMakeAHybridInFrequencyOrder(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(A, 0.2F), gene(A, 0.1F), gene(B, 0.7F), gene(B, 0.1F)));
		helper.assertValueEqual(result, new FamilyResult(Kind.HYBRID, Optional.of(B), Optional.of(A)), "result");
		helper.succeed();
	}

	@GameTest
	public void subTagGivesOnePoint(GameTestHelper helper) {
		FamilyResult oneSubTag = FamilyResult.of(List.of(new TaggedGene(A, Optional.of(B), 0.5F), gene(A, 0.4F)));
		helper.assertValueEqual(oneSubTag, new FamilyResult(Kind.MAJOR, Optional.of(A), Optional.empty()),
				"one sub tag is not enough for a sub family");
		FamilyResult twoSubTags = FamilyResult.of(List.of(new TaggedGene(A, Optional.of(B), 0.5F),
				new TaggedGene(A, Optional.of(B), 0.4F)));
		helper.assertValueEqual(twoSubTags, new FamilyResult(Kind.MINOR, Optional.of(A), Optional.of(B)),
				"two sub tags make a sub family");
		helper.succeed();
	}

	@GameTest
	public void frequencyBreaksATieForTheSubFamily(GameTestHelper helper) {
		FamilyResult result = FamilyResult.of(List.of(gene(A, 0.1F), gene(A, 0.1F), gene(B, 0.3F), gene(C, 0.6F)));
		helper.assertValueEqual(result, new FamilyResult(Kind.MINOR, Optional.of(A), Optional.of(C)), "result");
		helper.succeed();
	}

	@GameTest
	public void oldSaveFormatStillLoads(GameTestHelper helper) {
		CarriedGenes carried = CarriedGenes.CODEC.parse(JsonOps.INSTANCE,
				JsonParser.parseString("[\"originofspawns_test:a\"]")).getOrThrow();
		helper.assertValueEqual(carried, new CarriedGenes(List.of(A), FamilyResult.NEUTRAL), "genes");
		helper.succeed();
	}

	@GameTest
	public void pureFamilyGivesTierThreeEffects(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		for (String gene : List.of("test_swift", "test_swift_2", "test_swift_3")) {
			pools.setFrequency(Identifier.withDefaultNamespace("stray"), category, id(gene), 1.0F);
		}

		Stray stray = helper.spawnWithNoFreeWill(EntityTypes.STRAY, pos);

		CarriedGenes carried = stray.getAttached(ModAttachments.GENES);
		helper.assertValueEqual(carried.family(),
				new FamilyResult(Kind.PURE, Optional.of(id("predator")), Optional.empty()), "family");
		helper.assertValueEqual(stray.getAttributeValue(Attributes.ARMOR), 3.0, "armor from tier 3");
		helper.succeed();
	}

	private static TaggedGene gene(Identifier family, float frequency) {
		return new TaggedGene(family, Optional.empty(), frequency);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("originofspawns_test", path);
	}

	@GameTest
	public void resultsHaveTranslationKeys(GameTestHelper helper) {
		Identifier predator = Identifier.fromNamespaceAndPath("originofspawns", "zombie/predator");
		Identifier stalker = Identifier.fromNamespaceAndPath("originofspawns", "zombie/stalker");
		helper.assertValueEqual(FamilyResult.NEUTRAL.translationKey(), "family.originofspawns.neutral", "neutral");
		helper.assertValueEqual(new FamilyResult(Kind.PURE, Optional.of(predator), Optional.empty()).translationKey(),
				"family.originofspawns.zombie.predator", "pure");
		helper.assertValueEqual(new FamilyResult(Kind.MINOR, Optional.of(stalker), Optional.of(predator)).translationKey(),
				"family.originofspawns.zombie.stalker.predator", "minor");
		helper.assertValueEqual(new FamilyResult(Kind.HYBRID, Optional.of(stalker), Optional.of(predator)).translationKey(),
				"family.originofspawns.zombie.predator.and.stalker", "hybrid in either order");
		helper.succeed();
	}
}
