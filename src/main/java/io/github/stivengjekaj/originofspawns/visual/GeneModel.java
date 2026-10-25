package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * Extra cubes that a gene adds to the model of a mob. Each cube moves with one bone of the model.
 * The file {@code assets/<namespace>/gene_models/<name>.json} is the model {@code <namespace>:<name>}.
 *
 * @param texture the texture of the cubes. A model with {@code mobTexture} needs no texture
 * @param mobTexture if true, the cubes use the texture of the mob, with the marks of its genes. A swollen copy of a body
 *        part keeps the marks of the other genes this way
 * @param textureWidth the width of the texture, in pixels
 * @param textureHeight the height of the texture, in pixels
 * @param parts the cubes
 */
public record GeneModel(Optional<Identifier> texture, boolean mobTexture, int textureWidth, int textureHeight,
		List<Part> parts) {
	private static final Codec<GeneModel> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("texture").forGetter(GeneModel::texture),
			Codec.BOOL.optionalFieldOf("mob_texture", false).forGetter(GeneModel::mobTexture),
			Codec.intRange(1, 4096).optionalFieldOf("texture_width", 64).forGetter(GeneModel::textureWidth),
			Codec.intRange(1, 4096).optionalFieldOf("texture_height", 64).forGetter(GeneModel::textureHeight),
			Part.CODEC.listOf().fieldOf("parts").forGetter(GeneModel::parts)
	).apply(instance, GeneModel::new));

	public static final Codec<GeneModel> CODEC = RAW_CODEC.validate(model -> model.texture().isPresent() || model.mobTexture()
			? DataResult.success(model)
			: DataResult.error(() -> "A gene model needs a texture, or mob_texture set to true"));

	/**
	 * A bone of a humanoid model.
	 */
	public enum Bone implements StringRepresentable {
		HEAD,
		BODY,
		RIGHT_ARM,
		LEFT_ARM,
		RIGHT_LEG,
		LEFT_LEG,
		/**
		 * The four legs of a creeper. A spider also has these legs, at the back and at the front.
		 */
		RIGHT_HIND_LEG,
		LEFT_HIND_LEG,
		RIGHT_FRONT_LEG,
		LEFT_FRONT_LEG,
		/**
		 * The four middle legs of a spider.
		 */
		RIGHT_MIDDLE_HIND_LEG,
		LEFT_MIDDLE_HIND_LEG,
		RIGHT_MIDDLE_FRONT_LEG,
		LEFT_MIDDLE_FRONT_LEG,
		/**
		 * The large back part of the body of a spider. The spider model calls it {@code body1}.
		 */
		ABDOMEN,
		/**
		 * The hat of a witch. It is a part of the head, so it moves with the head.
		 */
		HAT,
		/**
		 * The nose of a witch. It is a part of the head, so it moves with the head.
		 */
		NOSE,
		/**
		 * The crossed arms of a witch. They are one part.
		 */
		ARMS,
		/**
		 * The four parts of an endermite, from the front: the head, the body, the tail, and the tip of the tail.
		 */
		SEGMENT0,
		SEGMENT1,
		SEGMENT2,
		SEGMENT3,
		/**
		 * The inner cube of a slime, with its eyes and its mouth. The outer gel has the same place as the inner cube.
		 */
		CUBE,
		RIGHT_EYE,
		LEFT_EYE,
		MOUTH,
		/**
		 * The eight layers of a magma cube, from the top, and its inside cube.
		 */
		CUBE0,
		CUBE1,
		CUBE2,
		CUBE3,
		CUBE4,
		CUBE5,
		CUBE6,
		CUBE7,
		INSIDE_CUBE,
		/**
		 * The ears of a piglin or a hoglin. They are parts of the head, so they move with the head.
		 */
		RIGHT_EAR,
		LEFT_EAR,
		/**
		 * The tusks of a hoglin. They are parts of the head.
		 */
		RIGHT_HORN,
		LEFT_HORN,
		/**
		 * The mane of a hoglin. It is a part of the body.
		 */
		MANE,
		/**
		 * The tail and the wings of a phantom, each in two parts. The tip is a part of the base, and the bases are parts
		 * of the body.
		 */
		TAIL_BASE,
		TAIL_TIP,
		LEFT_WING_BASE,
		LEFT_WING_TIP,
		RIGHT_WING_BASE,
		RIGHT_WING_TIP;

		public static final Codec<Bone> CODEC = StringRepresentable.fromEnum(Bone::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * One cube, in the coordinates of its bone, in pixels.
	 *
	 * @param uv the top left corner of the box UV of the cube in the texture
	 * @param from the corner of the cube with the smallest coordinates
	 * @param size the size of the cube
	 * @param inflate makes the cube larger on each side, without a change to its UV
	 * @param pivot the point that the cube turns around, in the coordinates of its bone
	 * @param rotation the angles of the cube around the X, Y, and Z axes of the pivot, in degrees
	 * @param glow if true, the cube has full light, so that it is bright in the dark
	 */
	public record Part(Bone bone, List<Integer> uv, List<Float> from, List<Float> size, float inflate, boolean mirror,
			List<Float> pivot, List<Float> rotation, boolean glow) {
		private static final List<Float> ZERO = List.of(0.0F, 0.0F, 0.0F);

		public static final Codec<Part> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Bone.CODEC.fieldOf("bone").forGetter(Part::bone),
				Codec.INT.listOf(2, 2).fieldOf("uv").forGetter(Part::uv),
				Codec.FLOAT.listOf(3, 3).fieldOf("from").forGetter(Part::from),
				Codec.FLOAT.listOf(3, 3).fieldOf("size").forGetter(Part::size),
				Codec.FLOAT.optionalFieldOf("inflate", 0.0F).forGetter(Part::inflate),
				Codec.BOOL.optionalFieldOf("mirror", false).forGetter(Part::mirror),
				Codec.FLOAT.listOf(3, 3).optionalFieldOf("pivot", ZERO).forGetter(Part::pivot),
				Codec.FLOAT.listOf(3, 3).optionalFieldOf("rotation", ZERO).forGetter(Part::rotation),
				Codec.BOOL.optionalFieldOf("glow", false).forGetter(Part::glow)
		).apply(instance, Part::new));

		/**
		 * Makes a part with no rotation and no glow.
		 */
		public Part(Bone bone, List<Integer> uv, List<Float> from, List<Float> size, float inflate, boolean mirror) {
			this(bone, uv, from, size, inflate, mirror, ZERO, ZERO, false);
		}
	}
}
