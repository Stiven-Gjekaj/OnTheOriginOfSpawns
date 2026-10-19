package io.github.stivengjekaj.originofspawns.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Draws bone lower legs on a zombie with a gene that has the model {@code originofspawns:bone_legs}.
 * The visual recipe of the gene erases the lower legs of the texture, so that the bones show in their place.
 */
public class BoneLegsLayer extends RenderLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>> {
	public static final Identifier MODEL = OriginOfSpawns.id("bone_legs");
	private static final Identifier TEXTURE = OriginOfSpawns.id("textures/entity/bone_leg.png");

	private final ModelPart bone;

	public BoneLegsLayer(RenderLayerParent<ZombieRenderState, HumanoidModel<ZombieRenderState>> parent) {
		super(parent);
		MeshDefinition mesh = new MeshDefinition();
		// A 3x6x3 bone in the lower half of a 4x12x4 leg, in leg coordinates.
		mesh.getRoot().addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 6.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.ZERO);
		this.bone = LayerDefinition.create(mesh, 16, 16).bakeRoot();
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ZombieRenderState state,
			float yRot, float xRot) {
		List<Identifier> genes = state.getData(GeneTextures.GENES);
		if (state.isInvisible || !GeneTextures.hasModel(genes, MODEL)) {
			return;
		}
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
		for (ModelPart leg : List.of(getParentModel().rightLeg, getParentModel().leftLeg)) {
			poseStack.pushPose();
			leg.translateAndRotate(poseStack);
			collector.submitModelPart(bone, poseStack, RenderTypes.entityCutout(TEXTURE), light, overlay, null);
			poseStack.popPose();
		}
	}
}
