package io.github.stivengjekaj.originofspawns.client.screen;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.MobInfo;
import io.github.stivengjekaj.originofspawns.notebook.Knowledge;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The Beagle Notebook. It has a tab for each species. Each species has a page of genes and a page of families.
 */
public class NotebookScreen extends Screen {
	private static final int PAPER = 0xFFE9DFC4;
	private static final int INK = 0xFF3B2F22;
	private static final int FADED_INK = 0xFF8A7A62;
	private static final int ROW_HEIGHT = 11;
	private static final int PADDING = 8;
	private static final int COLUMN_GAP = 6;
	private static final int TAB_HEIGHT = 16;
	private static final int PAGE_WIDTH = 360;
	private static final int PAGE_HEIGHT = 210;
	private static final int FAMILY_COLUMNS = 4;
	private static final Component UNKNOWN = Component.literal("???");

	/**
	 * The open tabs. The notebook opens on the same tabs the next time.
	 */
	private static int lastSpecies;
	private static Section lastSection = Section.GENES;

	/**
	 * The two pages of a species.
	 */
	public enum Section {
		GENES,
		FAMILIES
	}

	private final List<NotebookPayload.Page> pages;
	private int left;
	private int top;
	private int scroll;
	private int maxScroll;

	public NotebookScreen(NotebookPayload payload) {
		super(Component.translatable("item.originofspawns.beagle_notebook"));
		this.pages = payload.pages();
		if (lastSpecies >= pages.size()) {
			lastSpecies = 0;
		}
	}

	/**
	 * Returns the tab of a species, or 0 if the notebook has no page for it.
	 */
	public int speciesIndex(Identifier species) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i).species().equals(species)) {
				return i;
			}
		}
		return 0;
	}

	public void show(int species, Section section) {
		lastSpecies = Math.clamp(species, 0, Math.max(pages.size() - 1, 0));
		lastSection = section;
		scroll = 0;
		rebuildWidgets();
	}

	@Override
	protected void init() {
		left = (width - PAGE_WIDTH) / 2;
		top = (height - PAGE_HEIGHT) / 2;
		int x = left;
		for (int i = 0; i < pages.size(); i++) {
			Component name = speciesName(pages.get(i).species());
			int tabWidth = font.width(name) + 12;
			int index = i;
			Button tab = Button.builder(name, button -> show(index, lastSection))
					.bounds(x, top - TAB_HEIGHT, tabWidth, TAB_HEIGHT).build();
			tab.active = i != lastSpecies;
			addRenderableWidget(tab);
			x += tabWidth + 2;
		}
		if (pages.isEmpty()) {
			return;
		}
		int sectionX = left + PAGE_WIDTH - PADDING;
		for (Section section : List.of(Section.FAMILIES, Section.GENES)) {
			Component name = sectionName(section);
			int tabWidth = font.width(name) + 12;
			sectionX -= tabWidth;
			Button tab = Button.builder(name, button -> show(lastSpecies, section))
					.bounds(sectionX, top + PADDING - 4, tabWidth, TAB_HEIGHT).build();
			tab.active = section != lastSection;
			addRenderableWidget(tab);
			sectionX -= 2;
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.fill(left, top, left + PAGE_WIDTH, top + PAGE_HEIGHT, PAPER);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		if (pages.isEmpty()) {
			graphics.text(font, Component.translatable("screen.originofspawns.notebook.no_species"),
					left + PADDING, top + PADDING, FADED_INK, false);
			return;
		}
		NotebookPayload.Page page = pages.get(lastSpecies);
		graphics.text(font, speciesName(page.species()), left + PADDING, top + PADDING, INK, false);
		int y = top + PADDING + TAB_HEIGHT + 4;
		if (lastSection == Section.GENES) {
			drawGenes(graphics, page, y);
		} else {
			drawFamilies(graphics, page, y);
		}
	}

	private void drawGenes(GuiGraphicsExtractor graphics, NotebookPayload.Page page, int y) {
		Map<Identifier, Map<BiomeCategory, Float>> rows = new LinkedHashMap<>();
		for (NotebookPayload.Entry entry : page.entries()) {
			rows.computeIfAbsent(entry.gene(), gene -> new EnumMap<>(BiomeCategory.class))
					.put(entry.category(), entry.frequency());
		}
		if (rows.isEmpty()) {
			graphics.text(font, Component.translatable("screen.originofspawns.notebook.no_genes"),
					left + PADDING, y, FADED_INK, false);
			return;
		}
		List<Component> names = rows.keySet().stream().map(MobInfo::geneName).toList();
		int nameWidth = names.stream().mapToInt(font::width).max().orElse(0) + COLUMN_GAP;
		int columnWidth = (PAGE_WIDTH - PADDING * 2 - nameWidth) / BiomeCategory.values().length;
		for (BiomeCategory category : BiomeCategory.values()) {
			int x = left + PADDING + nameWidth + category.ordinal() * columnWidth;
			graphics.text(font, categoryName(category), x, y, FADED_INK, false);
		}
		y += ROW_HEIGHT + 2;
		// The rows scroll with the mouse wheel when the species has more genes than the page holds.
		int bottom = top + PAGE_HEIGHT - PADDING;
		int visibleRows = (bottom - y) / ROW_HEIGHT;
		maxScroll = Math.max(rows.size() - visibleRows, 0);
		scroll = Math.clamp(scroll, 0, maxScroll);
		int row = 0;
		for (Map<BiomeCategory, Float> frequencies : rows.values()) {
			if (row < scroll || row >= scroll + visibleRows) {
				row++;
				continue;
			}
			graphics.text(font, names.get(row++), left + PADDING, y, INK, false);
			for (BiomeCategory category : BiomeCategory.values()) {
				int percent = Math.round(frequencies.getOrDefault(category, 0.0F) * 100.0F);
				int x = left + PADDING + nameWidth + category.ordinal() * columnWidth;
				graphics.text(font, percent + "%", x, y, percent > 0 ? INK : FADED_INK, false);
			}
			y += ROW_HEIGHT;
		}
		if (maxScroll > 0) {
			int trackTop = top + PADDING + TAB_HEIGHT + 4 + ROW_HEIGHT + 2;
			int track = bottom - trackTop;
			int thumb = Math.max(track * visibleRows / rows.size(), 8);
			int thumbTop = trackTop + (track - thumb) * scroll / maxScroll;
			graphics.fill(left + PAGE_WIDTH - 4, thumbTop, left + PAGE_WIDTH - 2, thumbTop + thumb, FADED_INK);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (lastSection == Section.GENES && maxScroll > 0) {
			scroll = Math.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	/**
	 * Scrolls the gene rows. A test uses it.
	 */
	public void scrollTo(int row) {
		scroll = row;
	}

	private void drawFamilies(GuiGraphicsExtractor graphics, NotebookPayload.Page page, int y) {
		long found = page.families().stream().filter(NotebookPayload.FamilyEntry::found).count();
		long seen = page.families().stream().filter(entry -> entry.knowledge() == Knowledge.SEEN).count();
		graphics.text(font, Component.translatable("screen.originofspawns.notebook.found", found, seen, page.families().size()),
				left + PADDING, y, FADED_INK, false);
		y += ROW_HEIGHT + 2;
		int columnWidth = (PAGE_WIDTH - PADDING * 2) / FAMILY_COLUMNS;
		for (FamilyResult.Kind kind : List.of(FamilyResult.Kind.PURE, FamilyResult.Kind.MINOR, FamilyResult.Kind.HYBRID,
				FamilyResult.Kind.NEUTRAL)) {
			List<NotebookPayload.FamilyEntry> entries = page.families().stream()
					.filter(entry -> entry.kind() == kind).toList();
			if (entries.isEmpty()) {
				continue;
			}
			graphics.text(font, Component.translatable("screen.originofspawns.notebook.kind." + kind.getSerializedName()),
					left + PADDING, y, INK, false);
			y += ROW_HEIGHT;
			for (int i = 0; i < entries.size(); i++) {
				NotebookPayload.FamilyEntry entry = entries.get(i);
				int x = left + PADDING + (i % FAMILY_COLUMNS) * columnWidth;
				// A known family is in ink. A seen family is in faded ink. An unknown family is "???".
				Component name = entry.knowledge() == Knowledge.NONE ? UNKNOWN : Component.translatable(entry.key());
				graphics.text(font, name, x + 4, y, entry.found() ? INK : FADED_INK, false);
				if (i % FAMILY_COLUMNS == FAMILY_COLUMNS - 1 || i == entries.size() - 1) {
					y += ROW_HEIGHT;
				}
			}
			y += 2;
		}
	}

	private static Component speciesName(Identifier species) {
		return Component.translatable("entity." + species.getNamespace() + "." + species.getPath());
	}

	private static Component sectionName(Section section) {
		return Component.translatable("screen.originofspawns.notebook.section." + section.name().toLowerCase(java.util.Locale.ROOT));
	}

	private static Component categoryName(BiomeCategory category) {
		return Component.translatable("biome_category.originofspawns." + category.getSerializedName() + ".short");
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
