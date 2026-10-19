package io.github.stivengjekaj.originofspawns.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * A cause of death. A death counts for this pressure when its damage type is in the tag.
 */
public record Pressure(TagKey<DamageType> damageTypes) {
	public static final Codec<Pressure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("damage_types").forGetter(Pressure::damageTypes)
	).apply(instance, Pressure::new));
}
