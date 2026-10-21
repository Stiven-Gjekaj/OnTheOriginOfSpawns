package io.github.stivengjekaj.originofspawns.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.visual.BabyLayout;
import io.github.stivengjekaj.originofspawns.visual.GeneModel;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

/**
 * Draws the gene models of a mob with a humanoid body, such as a zombie or a skeleton. Each part moves with its bone.
 */
public class GeneModelLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private final RenderLayerParent<S, M> parent;

	public GeneModelLayer(RenderLayerParent<S, M> parent) {
		super(parent);
		this.parent = parent;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state,
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
			if (texture != null && model.mobTexture() && state.isBaby) {
				texture = GeneTextures.adultLayoutOf(texture);
			}
			if (texture == null) {
				continue;
			}
			Identifier drawn = texture;
			model.parts().forEach((bone, part) -> submit(poseStack, collector, bone, part, drawn, light, overlay, state.isBaby));
			model.glowing().forEach((bone, part) ->
					submit(poseStack, collector, bone, part, drawn, LightCoordsUtil.FULL_BRIGHT, overlay, state.isBaby));
		}
	}

	private void submit(PoseStack poseStack, SubmitNodeCollector collector, GeneModel.Bone bone, ModelPart part,
			Identifier texture, int light, int overlay, boolean baby) {
		poseStack.pushPose();
		bone(bone).translateAndRotate(poseStack);
		if (baby) {
			// The parts are in adult bone coordinates. This moves and scales them onto the smaller baby bone.
			float[] change = BabyLayout.boneChange(bone);
			poseStack.translate(change[0] / 16.0F, change[1] / 16.0F, change[2] / 16.0F);
			poseStack.scale(change[3], change[4], change[5]);
			poseStack.translate(-change[6] / 16.0F, -change[7] / 16.0F, -change[8] / 16.0F);
		}
		collector.submitModelPart(part, poseStack, RenderTypes.entityCutout(texture), light, overlay, null);
		poseStack.popPose();
	}

	/**
	 * Returns the texture that the mob uses now, with the marks of its genes.
	 */
	@SuppressWarnings("unchecked")
	private Identifier mobTexture(S state) {
		return parent instanceof LivingEntityRenderer<?, ?, ?> renderer
				? ((LivingEntityRenderer<?, S, ?>) renderer).getTextureLocation(state)
				: null;
	}

	private ModelPart bone(GeneModel.Bone bone) {
		M model = getParentModel();
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
