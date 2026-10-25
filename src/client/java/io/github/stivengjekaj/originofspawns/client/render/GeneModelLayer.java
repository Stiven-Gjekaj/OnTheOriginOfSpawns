package io.github.stivengjekaj.originofspawns.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.visual.BabyLayout;
import io.github.stivengjekaj.originofspawns.visual.GeneModel;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

/**
 * Draws the gene models of a mob, such as a zombie, a skeleton, or a creeper. Each part moves with its bone.
 */
public class GeneModelLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
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
		List<ModelPart> path = bonePath(bone);
		// A bone that the pose hides, such as the arm of an illager with crossed arms, hides its gene models too.
		if (path.isEmpty() || path.stream().anyMatch(each -> !each.visible)) {
			return;
		}
		poseStack.pushPose();
		// The root of some models has its own pose. The wither skeleton has a root that is 1.2 times larger.
		getParentModel().root().translateAndRotate(poseStack);
		for (ModelPart each : path) {
			each.translateAndRotate(poseStack);
		}
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

	/**
	 * Returns the parts from the root to the bone, or an empty list if the model has no such part. Most bones are a
	 * child of the root. The hat and the nose of the witch and the ears of a piglin are children of the head, so their
	 * path goes through the head. The mane of a hoglin is a child of the body.
	 */
	private List<ModelPart> bonePath(GeneModel.Bone bone) {
		ModelPart part = bone(bone);
		if (part != null) {
			return List.of(part);
		}
		ModelPart root = getParentModel().root();
		String name = bone.getSerializedName();
		// A bone such as the ear of a piglin is a child of the head, and the mane of a hoglin is a child of the body.
		for (String parent : List.of("head", "body")) {
			if (root.hasChild(parent) && root.getChild(parent).hasChild(name)) {
				ModelPart owner = root.getChild(parent);
				return List.of(owner, owner.getChild(name));
			}
		}
		return List.of();
	}

	/**
	 * Returns the part of the mob model for the bone, or null if the model has no such part. A humanoid model gives
	 * its own fields. Another model, such as the creeper or the spider, gives the child of its root with the name of
	 * the bone. A model with no part of that name, such as the spider, uses {@code body0} for the body and
	 * {@code body1} for the abdomen.
	 */
	private ModelPart bone(GeneModel.Bone bone) {
		M model = getParentModel();
		if (model instanceof HumanoidModel<?> humanoid) {
			return switch (bone) {
				case HEAD -> humanoid.head;
				case BODY -> humanoid.body;
				case RIGHT_ARM -> humanoid.rightArm;
				case LEFT_ARM -> humanoid.leftArm;
				case RIGHT_LEG -> humanoid.rightLeg;
				case LEFT_LEG -> humanoid.leftLeg;
				default -> null;
			};
		}
		ModelPart root = model.root();
		String name = bone.getSerializedName();
		if (!root.hasChild(name)) {
			name = switch (bone) {
				case BODY -> "body0";
				case ABDOMEN -> "body1";
				default -> name;
			};
		}
		return root.hasChild(name) ? root.getChild(name) : null;
	}
}
