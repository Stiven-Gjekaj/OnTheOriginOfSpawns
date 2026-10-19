package io.github.stivengjekaj.originofspawns.client.render;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Reads the gene models from the resource packs, and makes one model part for each bone of each model.
 */
public class GeneModels extends SimpleJsonResourceReloadListener<GeneModel> {
	/**
	 * A gene model, ready to draw. The glowing parts are apart from the other parts, because they get full light.
	 */
	public record Baked(Optional<Identifier> texture, boolean mobTexture, Map<GeneModel.Bone, ModelPart> parts,
			Map<GeneModel.Bone, ModelPart> glowing) {
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
		Map<GeneModel.Bone, MeshDefinition> glowingMeshes = new EnumMap<>(GeneModel.Bone.class);
		List<GeneModel.Part> parts = model.parts();
		for (int index = 0; index < parts.size(); index++) {
			GeneModel.Part part = parts.get(index);
			List<Float> pivot = part.pivot();
			List<Float> rotation = part.rotation();
			// The box is relative to the pivot, so that the part turns around the pivot.
			CubeListBuilder cube = CubeListBuilder.create()
					.texOffs(part.uv().get(0), part.uv().get(1))
					.mirror(part.mirror())
					.addBox(part.from().get(0) - pivot.get(0), part.from().get(1) - pivot.get(1),
							part.from().get(2) - pivot.get(2),
							part.size().get(0), part.size().get(1), part.size().get(2),
							new CubeDeformation(part.inflate()));
			PartPose pose = PartPose.offsetAndRotation(pivot.get(0), pivot.get(1), pivot.get(2),
					rotation.get(0) * Mth.DEG_TO_RAD, rotation.get(1) * Mth.DEG_TO_RAD, rotation.get(2) * Mth.DEG_TO_RAD);
			(part.glow() ? glowingMeshes : meshes).computeIfAbsent(part.bone(), bone -> new MeshDefinition())
					.getRoot().addOrReplaceChild("part" + index, cube, pose);
		}
		return new Baked(model.texture(), model.mobTexture(), bakeAll(meshes, model), bakeAll(glowingMeshes, model));
	}

	private static Map<GeneModel.Bone, ModelPart> bakeAll(Map<GeneModel.Bone, MeshDefinition> meshes, GeneModel model) {
		Map<GeneModel.Bone, ModelPart> baked = new EnumMap<>(GeneModel.Bone.class);
		meshes.forEach((bone, mesh) ->
				baked.put(bone, LayerDefinition.create(mesh, model.textureWidth(), model.textureHeight()).bakeRoot()));
		return baked;
	}
}
