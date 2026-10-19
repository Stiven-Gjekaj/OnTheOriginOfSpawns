package io.github.stivengjekaj.originofspawns.definition;

import java.util.Locale;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * One part of what a gene does. A gene lists its gains and its downsides as effects.
 * The field {@code type} selects the kind of effect.
 */
public sealed interface GeneEffect {
	Map<String, MapCodec<? extends GeneEffect>> TYPES = Map.ofEntries(
			Map.entry("attribute", AttributeChange.CODEC),
			Map.entry("damage_taken", DamageTaken.CODEC),
			Map.entry("ignite_target", IgniteTarget.CODEC),
			Map.entry("water_damage", WaterDamage.CODEC),
			Map.entry("air_loss", AirLoss.CODEC),
			Map.entry("damage_dealt_when", DamageDealtWhen.CODEC),
			Map.entry("damage_taken_when", DamageTakenWhen.CODEC),
			Map.entry("damage_from", DamageFrom.CODEC),
			Map.entry("rally", Rally.CODEC),
			Map.entry("critical_immunity", CriticalImmunity.CODEC),
			Map.entry("effect_on_hit", EffectOnHit.CODEC),
			Map.entry("leech_xp", LeechXp.CODEC),
			Map.entry("call_on_hurt", CallOnHurt.CODEC),
			Map.entry("grave_call", GraveCall.CODEC),
			Map.entry("death_burst", DeathBurst.CODEC),
			Map.entry("death_cloud", DeathCloud.CODEC),
			Map.entry("convert_villagers", ConvertVillagers.CODEC)
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

	/**
	 * A state of the mob that some effects need.
	 */
	enum When implements StringRepresentable {
		/**
		 * Always true.
		 */
		ALWAYS,
		/**
		 * No other mob of the same species is in 8 blocks.
		 */
		ALONE,
		/**
		 * The mob was not in combat for 10 seconds.
		 */
		AMBUSH,
		/**
		 * The light level at the mob is 6 or less.
		 */
		DARK,
		/**
		 * The light level at the mob is 12 or more.
		 */
		BRIGHT,
		/**
		 * The mob targets a player under half health.
		 */
		HUNTING_WOUNDED;

		public static final Codec<When> CODEC = StringRepresentable.fromEnum(When::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * Multiplies the damage that the mob deals when the state is true.
	 */
	record DamageDealtWhen(When when, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageDealtWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(DamageDealtWhen::when),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageDealtWhen::multiplier)
		).apply(instance, DamageDealtWhen::new));

		@Override
		public String type() {
			return "damage_dealt_when";
		}
	}

	/**
	 * Multiplies the damage that the mob takes when the state is true.
	 */
	record DamageTakenWhen(When when, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageTakenWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(DamageTakenWhen::when),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageTakenWhen::multiplier)
		).apply(instance, DamageTakenWhen::new));

		@Override
		public String type() {
			return "damage_taken_when";
		}
	}

	/**
	 * Multiplies the damage that the mob takes from some kinds of attackers.
	 */
	record DamageFrom(HolderSet<EntityType<?>> attackers, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageFrom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("attackers").forGetter(DamageFrom::attackers),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageFrom::multiplier)
		).apply(instance, DamageFrom::new));

		@Override
		public String type() {
			return "damage_from";
		}
	}

	/**
	 * Adds damage for each mob of the same species near the mob, up to a maximum.
	 */
	record Rally(float perAlly, float max, float radius) implements GeneEffect {
		public static final MapCodec<Rally> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("per_ally").forGetter(Rally::perAlly),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("max").forGetter(Rally::max),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("radius", 6.0F).forGetter(Rally::radius)
		).apply(instance, Rally::new));

		@Override
		public String type() {
			return "rally";
		}
	}

	/**
	 * A critical hit of a player does no extra damage to the mob.
	 */
	record CriticalImmunity() implements GeneEffect {
		public static final MapCodec<CriticalImmunity> CODEC = MapCodec.unit(new CriticalImmunity());

		@Override
		public String type() {
			return "critical_immunity";
		}
	}

	/**
	 * Gives the target a status effect when the mob hits it.
	 */
	record EffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier, float chance) implements GeneEffect {
		public static final MapCodec<EffectOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(EffectOnHit::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(EffectOnHit::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectOnHit::amplifier),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(EffectOnHit::chance)
		).apply(instance, EffectOnHit::new));

		@Override
		public String type() {
			return "effect_on_hit";
		}
	}

	/**
	 * Each hit takes XP points from a player. The mob drops the XP that it took when it dies, and no other XP.
	 */
	record LeechXp(int amount) implements GeneEffect {
		public static final MapCodec<LeechXp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 10000).fieldOf("amount").forGetter(LeechXp::amount)
		).apply(instance, LeechXp::new));

		@Override
		public String type() {
			return "leech_xp";
		}
	}

	/**
	 * When the mob is hurt, all mobs of the same species in the radius attack the attacker.
	 */
	record CallOnHurt(float radius) implements GeneEffect {
		public static final MapCodec<CallOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).fieldOf("radius").forGetter(CallOnHurt::radius)
		).apply(instance, CallOnHurt::new));

		@Override
		public String type() {
			return "call_on_hurt";
		}
	}

	/**
	 * When the mob is hurt, it has a chance to call a new mob of its species, with no genes.
	 */
	record GraveCall(float chance, int maxCalls, float radius) implements GeneEffect {
		public static final MapCodec<GraveCall> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(GraveCall::chance),
				Codec.intRange(1, 64).optionalFieldOf("max_calls", 2).forGetter(GraveCall::maxCalls),
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("radius", 8.0F).forGetter(GraveCall::radius)
		).apply(instance, GraveCall::new));

		@Override
		public String type() {
			return "grave_call";
		}
	}

	/**
	 * When the mob dies, it damages the players in the radius.
	 */
	record DeathBurst(float damage, float radius) implements GeneEffect {
		public static final MapCodec<DeathBurst> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1000.0F).fieldOf("damage").forGetter(DeathBurst::damage),
				Codec.floatRange(0.0F, 32.0F).fieldOf("radius").forGetter(DeathBurst::radius)
		).apply(instance, DeathBurst::new));

		@Override
		public String type() {
			return "death_burst";
		}
	}

	/**
	 * When the mob dies, it leaves a cloud that gives a status effect.
	 *
	 * @param seconds the length of the status effect
	 * @param cloudSeconds how long the cloud stays
	 */
	record DeathCloud(Holder<MobEffect> effect, float seconds, int amplifier, float radius, float cloudSeconds)
			implements GeneEffect {
		public static final MapCodec<DeathCloud> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(DeathCloud::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(DeathCloud::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(DeathCloud::amplifier),
				Codec.floatRange(0.5F, 16.0F).fieldOf("radius").forGetter(DeathCloud::radius),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cloud_seconds", 5.0F).forGetter(DeathCloud::cloudSeconds)
		).apply(instance, DeathCloud::new));

		@Override
		public String type() {
			return "death_cloud";
		}
	}

	/**
	 * A villager that the mob kills always becomes a zombie villager, on each difficulty.
	 */
	record ConvertVillagers() implements GeneEffect {
		public static final MapCodec<ConvertVillagers> CODEC = MapCodec.unit(new ConvertVillagers());

		@Override
		public String type() {
			return "convert_villagers";
		}
	}
}
