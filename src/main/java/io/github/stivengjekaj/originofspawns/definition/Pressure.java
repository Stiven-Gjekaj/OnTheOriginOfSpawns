package io.github.stivengjekaj.originofspawns.definition;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * A cause of death. A death counts for this pressure when it matches all the parts that the pressure has.
 *
 * @param damageTypes the damage types that count, if the pressure checks the damage
 * @param conditions the conditions of the death that must all be true
 */
public record Pressure(Optional<TagKey<DamageType>> damageTypes, List<DeathCondition> conditions) {
	public static final Codec<Pressure> CODEC = RecordCodecBuilder.<Pressure>create(instance -> instance.group(
			TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_types").forGetter(Pressure::damageTypes),
			DeathCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(Pressure::conditions)
	).apply(instance, Pressure::new)).validate(pressure -> pressure.damageTypes.isEmpty() && pressure.conditions.isEmpty()
			? DataResult.error(() -> "A pressure needs damage_types, conditions, or both")
			: DataResult.success(pressure));

	public boolean matches(DeathContext context) {
		if (damageTypes.isPresent() && !context.source().is(damageTypes.get())) {
			return false;
		}
		for (DeathCondition condition : conditions) {
			if (!condition.test(context)) {
				return false;
			}
		}
		return true;
	}
}
