package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.StringRepresentable;

/**
 * One step of a visual recipe: an effect on the pixels of some regions and materials.
 * An empty list means all regions, or all materials.
 * The order puts the layers of all the genes of a mob in sequence. A layer with a lower order goes on first.
 * Layers with the same order keep the order of the genes, and the order in their recipe.
 * The target selects the texture: the body texture, or the outer layer of a mob, such as the cloth of the stray.
 * A layer on the outer layer covers all of its pixels, because the outer layer has no region map.
 */
public record VisualLayer(List<BodyRegion> regions, List<BodyMaterial> materials, VisualEffect effect, int order,
		Target target) {
	public static final Codec<VisualLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BodyRegion.CODEC.listOf().optionalFieldOf("regions", List.of()).forGetter(VisualLayer::regions),
			BodyMaterial.CODEC.listOf().optionalFieldOf("materials", List.of()).forGetter(VisualLayer::materials),
			VisualEffect.CODEC.fieldOf("effect").forGetter(VisualLayer::effect),
			Codec.INT.optionalFieldOf("order", 0).forGetter(VisualLayer::order),
			Target.CODEC.optionalFieldOf("target", Target.BODY).forGetter(VisualLayer::target)
	).apply(instance, VisualLayer::new));

	public VisualLayer(List<BodyRegion> regions, List<BodyMaterial> materials, VisualEffect effect) {
		this(regions, materials, effect, 0, Target.BODY);
	}

	public VisualLayer(List<BodyRegion> regions, List<BodyMaterial> materials, VisualEffect effect, int order) {
		this(regions, materials, effect, order, Target.BODY);
	}

	/**
	 * The texture that a layer changes.
	 */
	public enum Target implements StringRepresentable {
		BODY("body"),
		OUTER("outer");

		public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);
		private final String name;

		Target(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public boolean covers(int regionId, int materialId) {
		boolean region = regions.isEmpty() || regions.stream().anyMatch(r -> r.id() == regionId);
		boolean material = materials.isEmpty() || materials.stream().anyMatch(m -> m.id() == materialId);
		return region && material;
	}
}
