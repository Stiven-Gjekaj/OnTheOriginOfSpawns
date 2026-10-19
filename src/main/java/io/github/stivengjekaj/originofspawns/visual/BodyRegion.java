package io.github.stivengjekaj.originofspawns.visual;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * A part of the body in a region map. The red channel of a region map pixel holds the id.
 */
public enum BodyRegion implements StringRepresentable {
	FACE(1),
	SCALP(2),
	TORSO(3),
	ARMS(4),
	HANDS(5),
	LEGS(6),
	LOWER_LEGS(7),
	FEET(8);

	public static final Codec<BodyRegion> CODEC = StringRepresentable.fromEnum(BodyRegion::values);

	private final int id;

	BodyRegion(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
