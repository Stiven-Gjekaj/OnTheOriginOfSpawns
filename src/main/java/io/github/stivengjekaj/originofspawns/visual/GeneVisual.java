package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.resources.Identifier;

/**
 * The visual recipe of a gene: the changes that the gene makes to the texture of the mob.
 *
 * @param regionMap the texture that labels the pixels of the mob texture
 * @param layers the changes, in order
 * @param model a model change, if the gene has one
 */
public record GeneVisual(Identifier regionMap, List<VisualLayer> layers, Optional<Identifier> model) {
	public static final Identifier HUMANOID = OriginOfSpawns.id("textures/region/humanoid.png");

	public static final Codec<GeneVisual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("region_map", HUMANOID).forGetter(GeneVisual::regionMap),
			VisualLayer.CODEC.listOf().fieldOf("layers").forGetter(GeneVisual::layers),
			Identifier.CODEC.optionalFieldOf("model").forGetter(GeneVisual::model)
	).apply(instance, GeneVisual::new));
}
