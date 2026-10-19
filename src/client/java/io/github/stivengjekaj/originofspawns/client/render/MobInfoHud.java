package io.github.stivengjekaj.originofspawns.client.render;

import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.MobInfo;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

/**
 * Shows a small panel at the top of the screen for the mob under the crosshair: its family and its genes.
 */
public class MobInfoHud implements HudElement {
	private static final int BACKGROUND = 0xC0100810;
	private static final int BORDER = 0xFF4A3A5A;
	private static final int TITLE = 0xFFFFFFFF;
	private static final int FADED = 0xFFA0A0A0;
	private static final int DOMINANT = 0xFFE8D27A;
	private static final int PADDING = 4;
	private static final int LINE = 10;
	private static final int TOP = 4;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || !(minecraft.crosshairPickEntity instanceof Mob mob)
				|| !MobInfo.shows(mob, minecraft.player)) {
			return;
		}
		CarriedGenes carried = mob.getAttached(ModAttachments.GENES);
		if (carried == null) {
			return;
		}
		var genes = minecraft.level.registryAccess().lookup(ModRegistries.GENE);
		MobInfo info = MobInfo.of(carried, id -> genes.flatMap(registry -> registry.getOptional(id)));
		draw(graphics, minecraft.font, info);
	}

	private static void draw(GuiGraphicsExtractor graphics, Font font, MobInfo info) {
		Component title = info.kind()
				.<Component>map(kind -> Component.empty().append(info.family()).append(" (").append(kind).append(")"))
				.orElse(info.family());
		int width = font.width(title);
		for (MobInfo.Line line : info.genes()) {
			width = Math.max(width, font.width(line.name()));
		}
		Optional<Component> empty = info.genes().isEmpty()
				? Optional.of(Component.translatable("mob_info.originofspawns.no_genes"))
				: Optional.empty();
		width = Math.max(width, empty.map(font::width).orElse(0));
		int lines = 1 + Math.max(info.genes().size(), 1);
		int boxWidth = width + PADDING * 2;
		int boxHeight = lines * LINE + PADDING * 2 - 1;
		int left = (graphics.guiWidth() - boxWidth) / 2;
		graphics.fill(left - 1, TOP - 1, left + boxWidth + 1, TOP + boxHeight + 1, BORDER);
		graphics.fill(left, TOP, left + boxWidth, TOP + boxHeight, BACKGROUND);
		int y = TOP + PADDING;
		graphics.text(font, title, left + PADDING, y, TITLE, true);
		y += LINE;
		if (empty.isPresent()) {
			graphics.text(font, empty.get(), left + PADDING, y, FADED, true);
			return;
		}
		for (MobInfo.Line line : info.genes()) {
			graphics.text(font, line.name(), left + PADDING, y, line.dominant() ? DOMINANT : FADED, true);
			y += LINE;
		}
	}
}
