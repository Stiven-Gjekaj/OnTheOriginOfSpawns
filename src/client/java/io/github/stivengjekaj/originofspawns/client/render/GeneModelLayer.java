package io.github.stivengjekaj.originofspawns.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.visual.GeneModel;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

/**
 * Draws the gene models of a zombie. Each part moves with its bone.
 */
public class GeneModelLayer extends RenderLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>> {
	private final RenderLayerParent<ZombieRenderState, HumanoidModel<ZombieRenderState>> parent;

	public GeneModelLayer(RenderLayerParent<ZombieRenderState, HumanoidModel<ZombieRenderState>> parent) {
		super(parent);
		this.parent = parent;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ZombieRenderState state,
			float yRot, float xRot) {
		List<Identifier> genes = state.getData(GeneTextures.GENES);
		if (genes == null || genes.isEmpty() || state.isInvisible) {
			return;
		}
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
		for (Identifier gene : genes) {
			GeneVisual visual = GeneVisuals.get(gene);
			if (visual == null || visual.model().isEmpty()) {
				continue;
			}
			GeneModels.Baked model = GeneModels.get(visual.model().get());
			if (model == null) {
				continue;
			}
			Identifier texture = model.mobTexture() ? mobTexture(state) : model.texture().orElse(null);
			if (texture == null) {
				continue;
			}
			model.parts().forEach((bone, part) -> submit(poseStack, collector, bone, part, texture, light, overlay));
			model.glowing().forEach((bone, part) ->
					submit(poseStack, collector, bone, part, texture, LightCoordsUtil.FULL_BRIGHT, overlay));
		}
	}

	private void submit(PoseStack poseStack, SubmitNodeCollector collector, GeneModel.Bone bone, ModelPart part,
			Identifier texture, int light, int overlay) {
		poseStack.pushPose();
		bone(bone).translateAndRotate(poseStack);
		collector.submitModelPart(part, poseStack, RenderTypes.entityCutout(texture), light, overlay, null);
		poseStack.popPose();
	}

	/**
	 * Returns the texture that the mob uses now, with the marks of its genes.
	 */
	@SuppressWarnings("unchecked")
	private Identifier mobTexture(ZombieRenderState state) {
		return parent instanceof LivingEntityRenderer<?, ?, ?> renderer
				? ((LivingEntityRenderer<?, ZombieRenderState, ?>) renderer).getTextureLocation(state)
				: null;
	}

	private ModelPart bone(GeneModel.Bone bone) {
		HumanoidModel<ZombieRenderState> model = getParentModel();
		return switch (bone) {
			case HEAD -> model.head;
			case BODY -> model.body;
			case RIGHT_ARM -> model.rightArm;
			case LEFT_ARM -> model.leftArm;
			case RIGHT_LEG -> model.rightLeg;
			case LEFT_LEG -> model.leftLeg;
		};
	}
}
