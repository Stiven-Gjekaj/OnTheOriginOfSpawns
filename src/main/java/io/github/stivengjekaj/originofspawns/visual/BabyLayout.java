package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;

/**
 * Fits the gene visuals of an adult humanoid to the baby zombie of Minecraft 26.3.
 * The baby model has smaller cubes at other places of the 64 x 64 texture. Thus, this class moves the pixels of a mark
 * face by face from the adult layout to the baby layout, and gives the change from an adult bone to a baby bone for
 * the cubes of gene models. This class has no client code, so that tests can run it on the server.
 */
public final class BabyLayout {
	/**
	 * A cube of the model: its box UV corner and its size in the texture, and its corner in the bone, in pixels.
	 */
	public record Cube(int u, int v, int width, int height, int depth, float x, float y, float z) {
		/**
		 * Returns the six faces of the box UV, in this order: top, bottom, side 1, front, side 2, back.
		 * Each face is {x, y, width, height} in the texture.
		 */
		int[][] faces() {
			return new int[][] {
					{u + depth, v, width, depth},
					{u + depth + width, v, width, depth},
					{u, v + depth, depth, height},
					{u + depth, v + depth, width, height},
					{u + depth + width, v + depth, depth, height},
					{u + depth * 2 + width, v + depth, width, height}
			};
		}
	}

	/**
	 * One cube of the adult model and the same cube of the baby model.
	 *
	 * @param bone the bone of the cube, or null for the hat layer
	 * @param mirror true if the adult cube mirrors its texture and the baby cube does not
	 */
	public record Pair(GeneModel.Bone bone, Cube adult, Cube baby, boolean mirror) {
	}

	/**
	 * The cubes of the vanilla humanoid model and the baby zombie model. The adult left arm and left leg mirror the
	 * texture of the right arm and right leg. The baby has its own texture for each limb.
	 */
	public static final List<Pair> PAIRS = List.of(
			new Pair(GeneModel.Bone.HEAD, new Cube(0, 0, 8, 8, 8, -4, -8, -4), new Cube(3, 3, 6, 6, 6, -3, -6.25F, -3), false),
			new Pair(null, new Cube(32, 0, 8, 8, 8, -4, -8, -4), new Cube(35, 3, 6, 6, 6, -3, -6.15F, -3), false),
			new Pair(GeneModel.Bone.BODY, new Cube(16, 16, 8, 12, 4, -4, 0, -2), new Cube(16, 16, 4, 5, 2, -2, -2.5F, -1), false),
			new Pair(GeneModel.Bone.RIGHT_ARM, new Cube(40, 16, 4, 12, 4, -3, -2, -2), new Cube(36, 16, 2, 5, 2, -1, -0.5F, -1), false),
			new Pair(GeneModel.Bone.LEFT_ARM, new Cube(40, 16, 4, 12, 4, -1, -2, -2), new Cube(28, 16, 2, 5, 2, -1, -0.5F, -1), true),
			new Pair(GeneModel.Bone.RIGHT_LEG, new Cube(0, 16, 4, 12, 4, -2, 0, -2), new Cube(8, 16, 2, 4, 2, -1, 0, -1), false),
			new Pair(GeneModel.Bone.LEFT_LEG, new Cube(0, 16, 4, 12, 4, -2, 0, -2), new Cube(0, 16, 2, 4, 2, -1, 0, -1), true));

	/**
	 * The faces that change places in a mirrored cube: side 1 and side 2.
	 */
	private static final int[] MIRRORED_FACE = {0, 1, 4, 3, 2, 5};

	private BabyLayout() {
	}

	/**
	 * Moves an image in the adult layout to the baby layout. The image can be 64 x 64 or a multiple of it.
	 * Pixels that no baby face covers become transparent.
	 */
	public static TextureBaker.Image toBaby(TextureBaker.Image adult) {
		int scale = Math.max(adult.width() / 64, 1);
		int[] pixels = new int[adult.width() * adult.height()];
		for (Pair pair : PAIRS) {
			int[][] babyFaces = pair.baby().faces();
			int[][] adultFaces = pair.adult().faces();
			for (int face = 0; face < babyFaces.length; face++) {
				int[] to = babyFaces[face];
				int[] from = adultFaces[pair.mirror() ? MIRRORED_FACE[face] : face];
				copyFace(adult, from, pixels, adult.width(), to, scale, pair.mirror());
			}
		}
		return new TextureBaker.Image(adult.width(), adult.height(), pixels);
	}

	/**
	 * The left arm and the left leg of a model with split limbs, such as the drowned. They have their own texture
	 * areas, where the zombie mirrors its right limbs.
	 */
	private static final List<Pair> SPLIT_LIMBS = List.of(
			new Pair(GeneModel.Bone.LEFT_ARM, new Cube(40, 16, 4, 12, 4, -1, -2, -2), new Cube(32, 48, 4, 12, 4, -1, -2, -2), true),
			new Pair(GeneModel.Bone.LEFT_LEG, new Cube(0, 16, 4, 12, 4, -2, 0, -2), new Cube(16, 48, 4, 12, 4, -2, 0, -2), true));

	/**
	 * Copies the right arm and the right leg of an image in the zombie layout, mirrored, to the left arm and the left
	 * leg of a model with split limbs. The other pixels stay as they are.
	 */
	public static TextureBaker.Image toSplitLimbs(TextureBaker.Image image) {
		int scale = Math.max(image.width() / 64, 1);
		int[] pixels = image.pixels().clone();
		for (Pair pair : SPLIT_LIMBS) {
			int[][] from = pair.adult().faces();
			int[][] to = pair.baby().faces();
			for (int face = 0; face < to.length; face++) {
				copyFace(image, from[MIRRORED_FACE[face]], pixels, image.width(), to[face], scale, true);
			}
		}
		return new TextureBaker.Image(image.width(), image.height(), pixels);
	}

	/**
	 * Moves an image in the baby layout to the adult layout, so that a gene model with adult UVs can use the texture of
	 * a baby. Pixels that no adult face covers become transparent.
	 */
	public static TextureBaker.Image toAdult(TextureBaker.Image baby) {
		int scale = Math.max(baby.width() / 64, 1);
		int[] pixels = new int[baby.width() * baby.height()];
		for (Pair pair : PAIRS) {
			if (pair.mirror()) {
				// The adult mirrored cube uses the same pixels as its partner, so the partner fills them.
				continue;
			}
			int[][] babyFaces = pair.baby().faces();
			int[][] adultFaces = pair.adult().faces();
			for (int face = 0; face < adultFaces.length; face++) {
				copyFace(baby, babyFaces[face], pixels, baby.width(), adultFaces[face], scale, false);
			}
		}
		return new TextureBaker.Image(baby.width(), baby.height(), pixels);
	}

	/**
	 * Copies one face to another face of a different size, with the nearest pixel. A region label must not blend.
	 */
	private static void copyFace(TextureBaker.Image source, int[] from, int[] target, int targetWidth, int[] to, int scale,
			boolean flip) {
		int toWidth = to[2] * scale;
		int toHeight = to[3] * scale;
		int fromWidth = from[2] * scale;
		int fromHeight = from[3] * scale;
		for (int y = 0; y < toHeight; y++) {
			for (int x = 0; x < toWidth; x++) {
				int fx = flip ? toWidth - 1 - x : x;
				int sx = from[0] * scale + (int) ((fx + 0.5F) * fromWidth / toWidth);
				int sy = from[1] * scale + (int) ((y + 0.5F) * fromHeight / toHeight);
				int tx = to[0] * scale + x;
				int ty = to[1] * scale + y;
				if (sx < source.width() && sy < source.height() && tx < targetWidth && ty * targetWidth + tx < target.length) {
					target[tx + ty * targetWidth] = source.get(sx, sy);
				}
			}
		}
	}

	/**
	 * Returns the change from the adult bone to the baby bone, as {move x, move y, move z, scale x, scale y, scale z,
	 * adult x, adult y, adult z}, in pixels. A point p of the adult bone goes to move + (p - adult) * scale.
	 */
	public static float[] boneChange(GeneModel.Bone bone) {
		for (Pair pair : PAIRS) {
			if (pair.bone() == bone) {
				Cube adult = pair.adult();
				Cube baby = pair.baby();
				return new float[] {
						baby.x(), baby.y(), baby.z(),
						(float) baby.width() / adult.width(), (float) baby.height() / adult.height(),
						(float) baby.depth() / adult.depth(),
						adult.x(), adult.y(), adult.z()
				};
			}
		}
		throw new IllegalArgumentException("No baby cube for the bone " + bone);
	}
}
