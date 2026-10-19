package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPages;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

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

		NotebookPayload page = NotebookPages.page(helper.getLevel().getServer(), EntityTypes.ZOMBIE);

		helper.assertValueEqual(page.species(), Identifier.withDefaultNamespace("zombie"), "species");
		helper.assertTrue(page.entries().contains(new NotebookPayload.Entry(BiomeCategory.END, HEAT_SKIN, 0.3F)),
				"the page has the frequency of the gene");
		long categories = page.entries().stream().filter(entry -> entry.gene().equals(HEAT_SKIN)).count();
		helper.assertValueEqual(categories, (long) BiomeCategory.values().length, "one entry for each category");
		helper.assertFalse(page.entries().stream().anyMatch(entry -> entry.gene().equals(SWIFT)),
				"the page has a gene of a different species");
		helper.succeed();
	}
}
