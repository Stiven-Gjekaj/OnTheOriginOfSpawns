package io.github.stivengjekaj.originofspawns.visual;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * What a pixel of a mob texture shows. The green channel of a region map pixel holds the id.
 */
public enum BodyMaterial implements StringRepresentable {
	SKIN(1),
	CLOTH(2),
	EYES(3),
	BONE(4);

	public static final Codec<BodyMaterial> CODEC = StringRepresentable.fromEnum(BodyMaterial::values);

	private final int id;

	BodyMaterial(int id) {
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
