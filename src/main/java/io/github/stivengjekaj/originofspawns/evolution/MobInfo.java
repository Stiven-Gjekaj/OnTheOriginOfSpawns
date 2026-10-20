package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.notebook.Knowledge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * The text of the info panel of one mob: its family, the kind of its family, and its genes.
 *
 * @param family the name of the family
 * @param kind the kind of the family, for example "Minor family", or empty for a neutral mob
 * @param genes the genes, with the dominant genes first
 */
public record MobInfo(Component family, Optional<Component> kind, List<Line> genes) {
	private static final Component UNKNOWN = Component.literal("???");

	/**
	 * One gene of the mob. A dominant gene has a family tag of the family of the mob.
	 */
	public record Line(Identifier gene, Component name, boolean dominant) {
	}

	/**
	 * A player sees the panel of a translucent mob only in this distance.
	 */
	public static final double TRANSLUCENT_RANGE = 2.0;

	/**
	 * Tells if the viewer can see the panel of the mob. A mob that is translucent now hides its panel from afar.
	 */
	public static boolean shows(Entity mob, Entity viewer) {
		return GeneEffects.translucentOpacity(mob).isEmpty() || mob.distanceTo(viewer) <= TRANSLUCENT_RANGE;
	}

	/**
	 * Makes the info of a mob from its genes.
	 *
	 * @param lookup finds the definition of a gene, or nothing for an unknown gene
	 */
	public static MobInfo of(CarriedGenes carried, Function<Identifier, Optional<Gene>> lookup) {
		return of(carried, lookup, Knowledge.KNOWN);
	}

	/**
	 * Makes the info of a mob, with only what the player knows. An unknown family shows "???". A family that the
	 * player only saw shows its name, but its genes show "???".
	 */
	public static MobInfo of(CarriedGenes carried, Function<Identifier, Optional<Gene>> lookup, Knowledge knowledge) {
		if (knowledge != Knowledge.KNOWN) {
			MobInfo full = of(carried, lookup, Knowledge.KNOWN);
			List<Line> hidden = full.genes().stream().map(line -> new Line(line.gene(), UNKNOWN, false)).toList();
			return knowledge == Knowledge.SEEN
					? new MobInfo(full.family(), full.kind(), hidden)
					: new MobInfo(UNKNOWN, Optional.empty(), hidden);
		}
		FamilyResult result = carried.family();
		Component family = Component.translatableWithFallback(result.translationKey(),
				result.first().map(Identifier::getPath).orElse("Neutral"));
		Optional<Component> kind = result.kind() == FamilyResult.Kind.NEUTRAL
				? Optional.empty()
				: Optional.of(Component.translatable("mob_info.originofspawns.kind." + result.kind().getSerializedName()));
		List<Line> lines = new ArrayList<>();
		for (Identifier id : carried.genes()) {
			boolean dominant = lookup.apply(id).map(gene -> isDominant(gene, result)).orElse(false);
			lines.add(new Line(id, geneName(id), dominant));
		}
		lines.sort(Comparator.comparing((Line line) -> !line.dominant()));
		return new MobInfo(family, kind, List.copyOf(lines));
	}

	private static boolean isDominant(Gene gene, FamilyResult result) {
		return inFamily(gene.family(), result) || gene.subFamily().map(sub -> inFamily(sub, result)).orElse(false);
	}

	private static boolean inFamily(Identifier family, FamilyResult result) {
		return result.first().filter(family::equals).isPresent() || result.second().filter(family::equals).isPresent();
	}

	/**
	 * Returns the name of a gene. A gene with no translation gets its path.
	 */
	public static Component geneName(Identifier gene) {
		return Component.translatableWithFallback("gene." + gene.getNamespace() + "." + gene.getPath(), gene.getPath());
	}
}
