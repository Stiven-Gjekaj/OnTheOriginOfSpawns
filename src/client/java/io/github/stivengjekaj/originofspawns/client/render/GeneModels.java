package io.github.stivengjekaj.originofspawns.client.render;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.stivengjekaj.originofspawns.visual.GeneModel;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Reads the gene models from the resource packs, and makes one model part for each bone of each model.
 */
public class GeneModels extends SimpleJsonResourceReloadListener<GeneModel> {
	/**
	 * A gene model, ready to draw.
	 */
	public record Baked(Identifier texture, Map<GeneModel.Bone, ModelPart> parts) {
	}

	private static Map<Identifier, Baked> models = Map.of();

	public GeneModels() {
		super(GeneModel.CODEC, FileToIdConverter.json("gene_models"));
	}

	@Override
	protected void apply(Map<Identifier, GeneModel> loaded, ResourceManager manager, ProfilerFiller profiler) {
		Map<Identifier, Baked> baked = new HashMap<>();
		loaded.forEach((id, model) -> baked.put(id, bake(model)));
		models = Map.copyOf(baked);
	}

	public static Baked get(Identifier model) {
		return models.get(model);
	}

	private static Baked bake(GeneModel model) {
		Map<GeneModel.Bone, MeshDefinition> meshes = new EnumMap<>(GeneModel.Bone.class);
		List<GeneModel.Part> parts = model.parts();
		for (int index = 0; index < parts.size(); index++) {
			GeneModel.Part part = parts.get(index);
			CubeListBuilder cube = CubeListBuilder.create()
					.texOffs(part.uv().get(0), part.uv().get(1))
					.mirror(part.mirror())
					.addBox(part.from().get(0), part.from().get(1), part.from().get(2),
							part.size().get(0), part.size().get(1), part.size().get(2),
							new CubeDeformation(part.inflate()));
			meshes.computeIfAbsent(part.bone(), bone -> new MeshDefinition())
					.getRoot().addOrReplaceChild("part" + index, cube, PartPose.ZERO);
		}
		Map<GeneModel.Bone, ModelPart> baked = new EnumMap<>(GeneModel.Bone.class);
		meshes.forEach((bone, mesh) ->
				baked.put(bone, LayerDefinition.create(mesh, model.textureWidth(), model.textureHeight()).bakeRoot()));
		return new Baked(model.texture(), baked);
	}
}
