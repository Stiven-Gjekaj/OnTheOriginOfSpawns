package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.visual.BabyLayout;
import io.github.stivengjekaj.originofspawns.visual.GeneModel;
import io.github.stivengjekaj.originofspawns.visual.TextureBaker.Image;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Moves gene marks from the adult layout to the baby layout, and adult bones to baby bones.
 */
public class BabyLayoutTest {
	private static final int HEAD_FRONT = 0xFF110000;
	private static final int ARM_SIDE_ONE = 0xFF002200;
	private static final int ARM_SIDE_TWO = 0xFF000033;

	/**
	 * Paints three faces of an adult texture: the front of the head, and the two sides of the right arm.
	 */
	private static Image adult() {
		Image image = new Image(64, 64, new int[64 * 64]);
		fill(image, 8, 8, 8, 8, HEAD_FRONT);
		fill(image, 40, 20, 4, 12, ARM_SIDE_ONE);
		fill(image, 48, 20, 4, 12, ARM_SIDE_TWO);
		return image;
	}

	@GameTest
	public void marksMoveToTheBabyFaces(GameTestHelper helper) {
		Image baby = BabyLayout.toBaby(adult());
		// The baby head is 6 x 6 x 6 at UV (3, 3), so its front face is at (9, 9).
		helper.assertValueEqual(baby.get(9, 9), HEAD_FRONT, "the top left of the baby face");
		helper.assertValueEqual(baby.get(14, 14), HEAD_FRONT, "the bottom right of the baby face");
		// The baby right arm is 2 x 5 x 2 at UV (36, 16). Its side 1 is at (36, 18).
		helper.assertValueEqual(baby.get(36, 18), ARM_SIDE_ONE, "the right arm keeps its sides");
		// The baby left arm at UV (28, 16) mirrors the adult right arm, so its side 1 gets the adult side 2.
		helper.assertValueEqual(baby.get(28, 18), ARM_SIDE_TWO, "the left arm swaps its sides");
		helper.assertValueEqual(baby.get(0, 0), 0, "a pixel that no baby face covers");
		helper.succeed();
	}

	@GameTest
	public void babyPixelsMoveBackToTheAdultFaces(GameTestHelper helper) {
		Image back = BabyLayout.toAdult(BabyLayout.toBaby(adult()));
		helper.assertValueEqual(back.get(8, 8), HEAD_FRONT, "the top left of the adult face");
		helper.assertValueEqual(back.get(15, 15), HEAD_FRONT, "the bottom right of the adult face");
		helper.assertValueEqual(back.get(43, 31), ARM_SIDE_ONE, "the adult arm side");
		helper.succeed();
	}

	@GameTest
	public void adultBonesFitTheBabyBones(GameTestHelper helper) {
		float[] head = BabyLayout.boneChange(GeneModel.Bone.HEAD);
		helper.assertValueEqual(move(head, 0, -4.0F), -3.0F, "the head corner x");
		helper.assertValueEqual(move(head, 1, -8.0F), -6.25F, "the head top");
		helper.assertValueEqual(move(head, 1, 0.0F), -0.25F, "the head bottom");
		float[] body = BabyLayout.boneChange(GeneModel.Bone.BODY);
		helper.assertValueEqual(move(body, 1, 12.0F), 2.5F, "the bottom of the body");
		helper.succeed();
	}

	private static float move(float[] change, int axis, float value) {
		return change[axis] + (value - change[6 + axis]) * change[3 + axis];
	}

	private static void fill(Image image, int x0, int y0, int width, int height, int color) {
		for (int y = y0; y < y0 + height; y++) {
			for (int x = x0; x < x0 + width; x++) {
				image.pixels()[x + y * image.width()] = color;
			}
		}
	}
}
