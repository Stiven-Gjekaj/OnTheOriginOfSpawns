package io.github.stivengjekaj.originofspawns.definition;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * One part of what a gene does. A gene lists its gains and its downsides as effects.
 * The field {@code type} selects the kind of effect.
 */
public sealed interface GeneEffect {
	Map<String, MapCodec<? extends GeneEffect>> TYPES = Map.of(
			"attribute", AttributeChange.CODEC,
			"damage_taken", DamageTaken.CODEC,
			"ignite_target", IgniteTarget.CODEC,
			"water_damage", WaterDamage.CODEC,
			"air_loss", AirLoss.CODEC
	);

	Codec<GeneEffect> CODEC = Codec.STRING.partialDispatch("type",
			effect -> DataResult.success(effect.type()),
			type -> TYPES.containsKey(type)
					? DataResult.success(TYPES.get(type))
					: DataResult.error(() -> "Unknown gene effect type: " + type));

	String type();

	/**
	 * Changes an attribute of the mob, for example its speed or its maximum health.
	 */
	record AttributeChange(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation)
			implements GeneEffect {
		public static final MapCodec<AttributeChange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Attribute.CODEC.fieldOf("attribute").forGetter(AttributeChange::attribute),
				Codec.DOUBLE.fieldOf("amount").forGetter(AttributeChange::amount),
				AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeChange::operation)
		).apply(instance, AttributeChange::new));

		@Override
		public String type() {
			return "attribute";
		}
	}

	/**
	 * Multiplies the damage that the mob takes from some damage types. The multiplier 0 makes the mob immune.
	 */
	record DamageTaken(TagKey<DamageType> damageTypes, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageTaken> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("damage_types").forGetter(DamageTaken::damageTypes),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageTaken::multiplier)
		).apply(instance, DamageTaken::new));

		@Override
		public String type() {
			return "damage_taken";
		}
	}

	/**
	 * Gives each hit of the mob a chance to set the target on fire.
	 */
	record IgniteTarget(float chance, float seconds) implements GeneEffect {
		public static final MapCodec<IgniteTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(IgniteTarget::chance),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("seconds").forGetter(IgniteTarget::seconds)
		).apply(instance, IgniteTarget::new));

		@Override
		public String type() {
			return "ignite_target";
		}
	}

	/**
	 * Damages the mob while it is in water or in rain.
	 *
	 * @param amount the damage of each time
	 * @param interval the ticks between two times
	 */
	record WaterDamage(float amount, int interval) implements GeneEffect {
		public static final MapCodec<WaterDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("amount").forGetter(WaterDamage::amount),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("interval", 20).forGetter(WaterDamage::interval)
		).apply(instance, WaterDamage::new));

		@Override
		public String type() {
			return "water_damage";
		}
	}

	/**
	 * Makes the mob lose more air for each tick under water. Thus, it drowns faster.
	 */
	record AirLoss(int extra) implements GeneEffect {
		public static final MapCodec<AirLoss> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("extra").forGetter(AirLoss::extra)
		).apply(instance, AirLoss::new));

		@Override
		public String type() {
			return "air_loss";
		}
	}
}
