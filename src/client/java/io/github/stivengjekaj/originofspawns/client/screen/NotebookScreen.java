package io.github.stivengjekaj.originofspawns.client.screen;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.MobInfo;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The Beagle Notebook page of one species: a table of gene frequencies in each biome category.
 */
public class NotebookScreen extends Screen {
	private static final int PAPER = 0xFFE9DFC4;
	private static final int INK = 0xFF3B2F22;
	private static final int FADED_INK = 0xFF8A7A62;
	private static final int ROW_HEIGHT = 12;
	private static final int PADDING = 8;
	private static final int COLUMN_GAP = 6;

	private final NotebookPayload page;
	private final Map<Identifier, Map<BiomeCategory, Float>> rows = new LinkedHashMap<>();

	public NotebookScreen(NotebookPayload page) {
		super(Component.translatable("item.originofspawns.beagle_notebook"));
		this.page = page;
		for (NotebookPayload.Entry entry : page.entries()) {
			rows.computeIfAbsent(entry.gene(), gene -> new EnumMap<>(BiomeCategory.class))
					.put(entry.category(), entry.frequency());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		List<Component> names = rows.keySet().stream().map(NotebookScreen::geneName).toList();
		int nameWidth = names.stream().mapToInt(font::width).max().orElse(0) + COLUMN_GAP;
		int columnWidth = 0;
		for (BiomeCategory category : BiomeCategory.values()) {
			columnWidth = Math.max(columnWidth, font.width(categoryName(category)) + COLUMN_GAP);
		}
		int pageWidth = Math.max(PADDING * 2 + nameWidth + columnWidth * BiomeCategory.values().length,
				font.width(Component.translatable("screen.originofspawns.notebook.no_genes")) + PADDING * 2);
		int pageHeight = 44 + Math.max(rows.size(), 1) * ROW_HEIGHT;
		int left = (width - pageWidth) / 2;
		int top = (height - pageHeight) / 2;
		graphics.fill(left, top, left + pageWidth, top + pageHeight, PAPER);

		Component species = Component.translatable("entity." + page.species().getNamespace() + "." + page.species().getPath());
		graphics.text(font, species, left + PADDING, top + PADDING, INK, false);

		int y = top + 24;
		for (BiomeCategory category : BiomeCategory.values()) {
			int x = left + PADDING + nameWidth + category.ordinal() * columnWidth;
			graphics.text(font, categoryName(category), x, y, FADED_INK, false);
		}
		y += ROW_HEIGHT + 2;
		if (rows.isEmpty()) {
			graphics.text(font, Component.translatable("screen.originofspawns.notebook.no_genes"),
					left + PADDING, y, FADED_INK, false);
			return;
		}
		int row = 0;
		for (Map<BiomeCategory, Float> frequencies : rows.values()) {
			graphics.text(font, names.get(row++), left + PADDING, y, INK, false);
			for (BiomeCategory category : BiomeCategory.values()) {
				int percent = Math.round(frequencies.getOrDefault(category, 0.0F) * 100.0F);
				int x = left + PADDING + nameWidth + category.ordinal() * columnWidth;
				graphics.text(font, percent + "%", x, y, percent > 0 ? INK : FADED_INK, false);
			}
			y += ROW_HEIGHT;
		}
	}

	private static Component categoryName(BiomeCategory category) {
		return Component.translatable("biome_category.originofspawns." + category.getSerializedName() + ".short");
	}

	/**
	 * Returns the name of a gene. A gene with no translation gets its path, for example "test_gene".
	 */
	private static Component geneName(Identifier gene) {
		return MobInfo.geneName(gene);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
