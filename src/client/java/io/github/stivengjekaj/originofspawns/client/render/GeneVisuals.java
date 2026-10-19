package io.github.stivengjekaj.originofspawns.client.render;

import java.util.Map;

import io.github.stivengjekaj.originofspawns.visual.GeneVisual;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Reads the visual recipes of genes from the resource packs.
 * The file {@code assets/<namespace>/gene_visuals/<name>.json} is the visual of the gene {@code <namespace>:<name>}.
 */
public class GeneVisuals extends SimpleJsonResourceReloadListener<GeneVisual> {
	private static Map<Identifier, GeneVisual> visuals = Map.of();

	public GeneVisuals() {
		super(GeneVisual.CODEC, FileToIdConverter.json("gene_visuals"));
	}

	@Override
	protected void apply(Map<Identifier, GeneVisual> loaded, ResourceManager manager, ProfilerFiller profiler) {
		visuals = Map.copyOf(loaded);
		GeneTextures.clear();
	}

	/**
	 * Returns the visual of a gene, or null if the gene does not change how the mob looks.
	 */
	public static GeneVisual get(Identifier gene) {
		return visuals.get(gene);
	}
}
