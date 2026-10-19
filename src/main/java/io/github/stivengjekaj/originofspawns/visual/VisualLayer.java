package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * One step of a visual recipe: an effect on the pixels of some regions and materials.
 * An empty list means all regions, or all materials.
 */
public record VisualLayer(List<BodyRegion> regions, List<BodyMaterial> materials, VisualEffect effect) {
	public static final Codec<VisualLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BodyRegion.CODEC.listOf().optionalFieldOf("regions", List.of()).forGetter(VisualLayer::regions),
			BodyMaterial.CODEC.listOf().optionalFieldOf("materials", List.of()).forGetter(VisualLayer::materials),
			VisualEffect.CODEC.fieldOf("effect").forGetter(VisualLayer::effect)
	).apply(instance, VisualLayer::new));

	public boolean covers(int regionId, int materialId) {
		boolean region = regions.isEmpty() || regions.stream().anyMatch(r -> r.id() == regionId);
		boolean material = materials.isEmpty() || materials.stream().anyMatch(m -> m.id() == materialId);
		return region && material;
	}
}
