package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;
import io.github.stivengjekaj.originofspawns.notebook.FamilyCatalog;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPages;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

import java.util.List;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class NotebookPagesTest {
	private static final Identifier HEAT_SKIN = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");
	private static final Identifier SWIFT = Identifier.fromNamespaceAndPath("originofspawns_test", "test_swift");

	@GameTest
	public void pageListsTheGenesOfTheSpecies(GameTestHelper helper) {
		GenePools.get(helper.getLevel().getServer())
				.setFrequency(Identifier.withDefaultNamespace("zombie"), BiomeCategory.END, HEAT_SKIN, 0.3F);

		NotebookPayload.Page page = NotebookPages.page(helper.getLevel().getServer(), EntityTypes.ZOMBIE,
				DiscoveredFamilies.NONE);

		helper.assertValueEqual(page.species(), Identifier.withDefaultNamespace("zombie"), "species");
		helper.assertTrue(page.entries().contains(new NotebookPayload.Entry(BiomeCategory.END, HEAT_SKIN, 0.3F)),
				"the page has the frequency of the gene");
		long categories = page.entries().stream().filter(entry -> entry.gene().equals(HEAT_SKIN)).count();
		helper.assertValueEqual(categories, (long) BiomeCategory.values().length, "one entry for each category");
		helper.assertFalse(page.entries().stream().anyMatch(entry -> entry.gene().equals(SWIFT)),
				"the page has a gene of a different species");
		helper.succeed();
	}

	@GameTest
	public void catalogListsEachFamilyName(GameTestHelper helper) {
		List<FamilyCatalog.Entry> catalog = FamilyCatalog.of(helper.getLevel().registryAccess(), EntityTypes.GIANT);
		helper.assertValueEqual(catalog, List.of(
				new FamilyCatalog.Entry("family.originofspawns_test.predator", Kind.PURE),
				new FamilyCatalog.Entry("family.originofspawns_test.prey", Kind.PURE),
				new FamilyCatalog.Entry("family.originofspawns_test.predator.prey", Kind.MINOR),
				new FamilyCatalog.Entry("family.originofspawns_test.prey.predator", Kind.MINOR),
				new FamilyCatalog.Entry("family.originofspawns_test.predator.and.prey", Kind.HYBRID),
				new FamilyCatalog.Entry("family.originofspawns.neutral", Kind.NEUTRAL)), "catalog");
		helper.succeed();
	}

	@GameTest
	public void pageMarksTheFoundFamilies(GameTestHelper helper) {
		NotebookPayload.Page page = NotebookPages.page(helper.getLevel().getServer(), EntityTypes.GIANT,
				new DiscoveredFamilies(Set.of("family.originofspawns_test.prey.predator")));
		List<String> found = page.families().stream().filter(NotebookPayload.FamilyEntry::found)
				.map(NotebookPayload.FamilyEntry::key).toList();
		helper.assertValueEqual(found, List.of("family.originofspawns_test.prey.predator"), "found families");
		helper.succeed();
	}

	@GameTest
	public void speciesWithGenesOrFamiliesHavePages(GameTestHelper helper) {
		var species = NotebookPages.species(helper.getLevel().getServer());
		helper.assertTrue(species.contains(EntityTypes.GIANT), "a species with families only");
		helper.assertTrue(species.contains(EntityTypes.HUSK), "a species with genes only");
		helper.assertFalse(species.contains(EntityTypes.PIG), "a species with nothing");
		helper.succeed();
	}
}
