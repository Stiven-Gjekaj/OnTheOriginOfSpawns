package io.github.stivengjekaj.originofspawns.definition;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.Block;

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
			Map.entry("convert_villagers", ConvertVillagers.CODEC),
			Map.entry("silence", Silence.CODEC),
			Map.entry("loud", Loud.CODEC),
			Map.entry("attack_interval", AttackInterval.CODEC),
			Map.entry("leap", Leap.CODEC),
			Map.entry("hunt", Hunt.CODEC),
			Map.entry("frenzied_targeting", FrenziedTargeting.CODEC),
			Map.entry("translucent", Translucent.CODEC),
			Map.entry("burrow", Burrow.CODEC),
			Map.entry("lurk", Lurk.CODEC),
			Map.entry("speed_when", SpeedWhen.CODEC),
			Map.entry("attribute_when", AttributeWhen.CODEC),
			Map.entry("dodge_projectiles", DodgeProjectiles.CODEC),
			Map.entry("undying", Undying.CODEC),
			Map.entry("effect_immunity", EffectImmunity.CODEC),
			Map.entry("share_damage", ShareDamage.CODEC),
			Map.entry("share_healing", ShareHealing.CODEC),
			Map.entry("heal_multiplier", HealMultiplier.CODEC),
			Map.entry("drain_food", DrainFood.CODEC),
			Map.entry("death_effect", DeathEffect.CODEC),
			Map.entry("sidestep", Sidestep.CODEC),
			Map.entry("climb", Climb.CODEC),
			Map.entry("throw_effect", ThrowEffect.CODEC),
			Map.entry("pick_up_items", PickUpItems.CODEC),
			Map.entry("no_conversion", NoConversion.CODEC),
			Map.entry("regenerate", Regenerate.CODEC),
			Map.entry("call_on_sight", CallOnSight.CODEC),
			Map.entry("brood", Brood.CODEC),
			Map.entry("runt", Runt.CODEC),
			Map.entry("siege", Siege.CODEC),
			Map.entry("aura", Aura.CODEC),
			Map.entry("glowing", Glowing.CODEC),
			Map.entry("raise_victim", RaiseVictim.CODEC),
			Map.entry("sunburn", Sunburn.CODEC),
			Map.entry("attract_golems", AttractGolems.CODEC),
			Map.entry("water_pull", WaterPull.CODEC),
			Map.entry("pull_down_on_hit", PullDownOnHit.CODEC),
			Map.entry("drag_under", DragUnder.CODEC),
			Map.entry("swim_vertical", SwimVertical.CODEC),
			Map.entry("trident_chance", TridentChance.CODEC),
			Map.entry("throw_rate", ThrowRate.CODEC),
			Map.entry("throw_range", ThrowRange.CODEC),
			Map.entry("scavenge_tridents", ScavengeTridents.CODEC),
			Map.entry("no_weapon_drop", NoWeaponDrop.CODEC),
			Map.entry("lightning_on_hit", LightningOnHit.CODEC),
			Map.entry("damage_from_behind", DamageFromBehind.CODEC),
			Map.entry("thorns", Thorns.CODEC),
			Map.entry("heal_once", HealOnce.CODEC),
			Map.entry("effect_on_hurt", EffectOnHurt.CODEC),
			Map.entry("harm_when", HarmWhen.CODEC),
			Map.entry("break_boats", BreakBoats.CODEC),
			Map.entry("see_invisible", SeeInvisible.CODEC),
			Map.entry("sneak_blind", SneakBlind.CODEC),
			Map.entry("lure", Lure.CODEC)
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
		HUNTING_WOUNDED,
		/**
		 * The mob is in water.
		 */
		IN_WATER,
		/**
		 * The mob stands on a block in the tag {@code minecraft:sand}.
		 */
		ON_SAND,
		/**
		 * The mob does not stand on a block in the tag {@code minecraft:sand}.
		 */
		OFF_SAND,
		/**
		 * Another mob of the same species is in 8 blocks. This is the opposite of {@code alone}.
		 */
		GROUPED,
		/**
		 * The mob is not in water. This is the opposite of {@code in_water}.
		 */
		ON_LAND,
		/**
		 * The mob is 10 or more blocks under the sea level of the world.
		 */
		DEEP,
		/**
		 * The mob is at the sea level of the world or above it.
		 */
		ABOVE_SEA_LEVEL,
		/**
		 * A thunderstorm is in the world.
		 */
		STORM,
		/**
		 * The mob is out of water and out of rain, in the daylight, under the open sky.
		 */
		SUNLIT;

		public static final Codec<When> CODEC = StringRepresentable.fromEnum(When::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * The hits of the mob that an effect acts on.
	 */
	enum Hits implements StringRepresentable {
		/**
		 * The hits of the hands of the mob.
		 */
		MELEE,
		/**
		 * The hits of the projectiles of the mob, for example a thrown trident.
		 */
		PROJECTILE,
		/**
		 * All hits of the mob.
		 */
		ANY;

		public static final Codec<Hits> CODEC = StringRepresentable.fromEnum(Hits::values);

		/**
		 * Returns true if this kind of hit includes a hit with a projectile or a hit without one.
		 */
		public boolean includes(boolean projectile) {
			return this == ANY || (this == PROJECTILE) == projectile;
		}

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
	record EffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier, float chance, When when, Hits hits)
			implements GeneEffect {
		public static final MapCodec<EffectOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(EffectOnHit::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(EffectOnHit::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectOnHit::amplifier),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(EffectOnHit::chance),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(EffectOnHit::when),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(EffectOnHit::hits)
		).apply(instance, EffectOnHit::new));

		public EffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier, float chance) {
			this(effect, seconds, amplifier, chance, When.ALWAYS, Hits.MELEE);
		}

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
	record CallOnHurt(float radius, boolean smoke, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<CallOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).fieldOf("radius").forGetter(CallOnHurt::radius),
				Codec.BOOL.optionalFieldOf("smoke", false).forGetter(CallOnHurt::smoke),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 0.0F).forGetter(CallOnHurt::cooldownSeconds)
		).apply(instance, CallOnHurt::new));

		public CallOnHurt(float radius) {
			this(radius, false, 0.0F);
		}

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

	/**
	 * Stops some sounds of the mob.
	 */
	record Silence(boolean idle, boolean hurt, boolean steps, boolean death) implements GeneEffect {
		public static final MapCodec<Silence> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("idle", false).forGetter(Silence::idle),
				Codec.BOOL.optionalFieldOf("hurt", false).forGetter(Silence::hurt),
				Codec.BOOL.optionalFieldOf("steps", false).forGetter(Silence::steps),
				Codec.BOOL.optionalFieldOf("death", false).forGetter(Silence::death)
		).apply(instance, Silence::new));

		@Override
		public String type() {
			return "silence";
		}
	}

	/**
	 * Multiplies the volume of the sounds of the mob. A higher volume can be heard from farther away.
	 */
	record Loud(float multiplier) implements GeneEffect {
		public static final MapCodec<Loud> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 16.0F).fieldOf("multiplier").forGetter(Loud::multiplier)
		).apply(instance, Loud::new));

		@Override
		public String type() {
			return "loud";
		}
	}

	/**
	 * Multiplies the time between two melee attacks.
	 */
	record AttackInterval(float multiplier) implements GeneEffect {
		public static final MapCodec<AttackInterval> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(AttackInterval::multiplier)
		).apply(instance, AttackInterval::new));

		@Override
		public String type() {
			return "attack_interval";
		}
	}

	/**
	 * The mob leaps at its target. After a missed leap, it cannot move for a short time.
	 */
	record Leap(float minDistance, float maxDistance, float cooldownSeconds, float missStunSeconds, Medium medium)
			implements GeneEffect {
		public static final MapCodec<Leap> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("min_distance", 3.0F).forGetter(Leap::minDistance),
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("max_distance", 5.0F).forGetter(Leap::maxDistance),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 5.0F).forGetter(Leap::cooldownSeconds),
				Codec.floatRange(0.0F, 60.0F).optionalFieldOf("miss_stun_seconds", 1.0F).forGetter(Leap::missStunSeconds),
				Medium.CODEC.optionalFieldOf("medium", Medium.GROUND).forGetter(Leap::medium)
		).apply(instance, Leap::new));

		public Leap(float minDistance, float maxDistance, float cooldownSeconds, float missStunSeconds) {
			this(minDistance, maxDistance, cooldownSeconds, missStunSeconds, Medium.GROUND);
		}

		/**
		 * Where the leap starts and how it moves.
		 */
		public enum Medium implements StringRepresentable {
			/**
			 * A jump from the ground at the target.
			 */
			GROUND,
			/**
			 * A dash through water at a target in water.
			 */
			WATER,
			/**
			 * A jump out of the water at a target on land, a little above the mob.
			 */
			BREACH;

			public static final Codec<Medium> CODEC = StringRepresentable.fromEnum(Medium::values);

			@Override
			public String getSerializedName() {
				return name().toLowerCase(Locale.ROOT);
			}
		}

		@Override
		public String type() {
			return "leap";
		}
	}

	/**
	 * The mob looks for players that other mobs miss: invisible players near it, and wounded players far away.
	 *
	 * @param invisibleRadius the range in which it finds invisible players. 0 turns it off
	 * @param woundedRangeMultiplier the follow range multiplier for players under half health
	 */
	record Hunt(float invisibleRadius, float woundedRangeMultiplier) implements GeneEffect {
		public static final MapCodec<Hunt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("invisible_radius", 0.0F).forGetter(Hunt::invisibleRadius),
				Codec.floatRange(1.0F, 8.0F).optionalFieldOf("wounded_range_multiplier", 1.0F)
						.forGetter(Hunt::woundedRangeMultiplier)
		).apply(instance, Hunt::new));

		@Override
		public String type() {
			return "hunt";
		}
	}

	/**
	 * Now and then, the mob targets the nearest mob of a different species.
	 */
	record FrenziedTargeting(float chance, float intervalSeconds, float radius) implements GeneEffect {
		public static final MapCodec<FrenziedTargeting> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(FrenziedTargeting::chance),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("interval_seconds", 10.0F)
						.forGetter(FrenziedTargeting::intervalSeconds),
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("radius", 8.0F).forGetter(FrenziedTargeting::radius)
		).apply(instance, FrenziedTargeting::new));

		@Override
		public String type() {
			return "frenzied_targeting";
		}
	}

	/**
	 * The client draws the mob translucent when the light at it is low. Other mobs still see it.
	 * With {@code blocks}, the mob is translucent only when its feet or its eyes are in one of these blocks.
	 */
	record Translucent(int maxLight, float opacity, Optional<TagKey<Block>> blocks) implements GeneEffect {
		public static final MapCodec<Translucent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(0, 15).optionalFieldOf("max_light", 7).forGetter(Translucent::maxLight),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("opacity", 0.1F).forGetter(Translucent::opacity),
				TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("blocks").forGetter(Translucent::blocks)
		).apply(instance, Translucent::new));

		@Override
		public String type() {
			return "translucent";
		}
	}

	/**
	 * When the mob spawns in a natural way at night near a player, it rises out of the ground.
	 * It rises only through the blocks in the tag {@code originofspawns:burrowable}, and breaks none.
	 */
	record Burrow(float playerRadius, float riseSeconds, TagKey<Block> blocks, boolean nightOnly, float stunSeconds,
			boolean nearPlayer) implements GeneEffect {
		public static final TagKey<Block> DEFAULT_BLOCKS =
				TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("originofspawns", "burrowable"));

		public static final MapCodec<Burrow> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 128.0F).optionalFieldOf("player_radius", 24.0F).forGetter(Burrow::playerRadius),
				Codec.floatRange(0.05F, 60.0F).optionalFieldOf("rise_seconds", 2.0F).forGetter(Burrow::riseSeconds),
				TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("blocks", DEFAULT_BLOCKS).forGetter(Burrow::blocks),
				Codec.BOOL.optionalFieldOf("night_only", true).forGetter(Burrow::nightOnly),
				Codec.floatRange(0.0F, 60.0F).optionalFieldOf("stun_seconds", 0.0F).forGetter(Burrow::stunSeconds),
				Codec.BOOL.optionalFieldOf("near_player", false).forGetter(Burrow::nearPlayer)
		).apply(instance, Burrow::new));

		public Burrow(float playerRadius, float riseSeconds) {
			this(playerRadius, riseSeconds, DEFAULT_BLOCKS, true, 0.0F, false);
		}

		@Override
		public String type() {
			return "burrow";
		}
	}

	/**
	 * The mob does not move while its target player faces it, until the player is in the radius.
	 * The camera mode of the player does not change this.
	 */
	record Lurk(float radius) implements GeneEffect {
		public static final MapCodec<Lurk> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("radius", 6.0F).forGetter(Lurk::radius)
		).apply(instance, Lurk::new));

		@Override
		public String type() {
			return "lurk";
		}
	}

	/**
	 * Changes an attribute of the mob while the state is true.
	 */
	record AttributeWhen(When when, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation)
			implements GeneEffect {
		public static final MapCodec<AttributeWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(AttributeWhen::when),
				Attribute.CODEC.fieldOf("attribute").forGetter(AttributeWhen::attribute),
				Codec.DOUBLE.fieldOf("amount").forGetter(AttributeWhen::amount),
				AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeWhen::operation)
		).apply(instance, AttributeWhen::new));

		@Override
		public String type() {
			return "attribute_when";
		}
	}

	/**
	 * Multiplies the speed of the mob when the state is true.
	 */
	record SpeedWhen(When when, float multiplier) implements GeneEffect {
		public static final MapCodec<SpeedWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(SpeedWhen::when),
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(SpeedWhen::multiplier)
		).apply(instance, SpeedWhen::new));

		@Override
		public String type() {
			return "speed_when";
		}
	}

	/**
	 * The mob heals when it was not in combat for a time.
	 */
	record Regenerate(float amount, float intervalSeconds, float idleSeconds, When when) implements GeneEffect {
		public static final MapCodec<Regenerate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1000.0F).fieldOf("amount").forGetter(Regenerate::amount),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("interval_seconds", 3.0F)
						.forGetter(Regenerate::intervalSeconds),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("idle_seconds", 5.0F).forGetter(Regenerate::idleSeconds),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Regenerate::when)
		).apply(instance, Regenerate::new));

		public Regenerate(float amount, float intervalSeconds, float idleSeconds) {
			this(amount, intervalSeconds, idleSeconds, When.ALWAYS);
		}

		@Override
		public String type() {
			return "regenerate";
		}
	}

	/**
	 * When the mob first targets a player, all mobs of its species in the radius target that player.
	 */
	record CallOnSight(float radius, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<CallOnSight> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 128.0F).fieldOf("radius").forGetter(CallOnSight::radius),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 30.0F)
						.forGetter(CallOnSight::cooldownSeconds)
		).apply(instance, CallOnSight::new));

		@Override
		public String type() {
			return "call_on_sight";
		}
	}

	/**
	 * When the mob spawns in a natural way, more mobs with the same genes spawn near it.
	 */
	record Brood(int extra) implements GeneEffect {
		public static final MapCodec<Brood> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).fieldOf("extra").forGetter(Brood::extra)
		).apply(instance, Brood::new));

		@Override
		public String type() {
			return "brood";
		}
	}

	/**
	 * The mob has a chance to spawn as a baby, with less health. A baby can ride a chicken at the vanilla chance.
	 * Babies of the zombie spawn only through this effect.
	 */
	record Runt(float chance, float babyHealthMultiplier) implements GeneEffect {
		public static final MapCodec<Runt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Runt::chance),
				Codec.floatRange(0.05F, 10.0F).optionalFieldOf("baby_health_multiplier", 0.5F)
						.forGetter(Runt::babyHealthMultiplier)
		).apply(instance, Runt::new));

		@Override
		public String type() {
			return "runt";
		}
	}

	/**
	 * The mob breaks doors on each difficulty, and breaks the blocks in the tag {@code originofspawns:siege_breakable}
	 * slowly. It obeys the gamerule that stops mobs from breaking blocks.
	 */
	record Siege(float breakSeconds) implements GeneEffect {
		public static final MapCodec<Siege> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("break_seconds", 10.0F).forGetter(Siege::breakSeconds)
		).apply(instance, Siege::new));

		@Override
		public String type() {
			return "siege";
		}
	}

	/**
	 * Gives a status effect to the players near the mob, renewed after each interval.
	 * With {@code in_water_only}, only the players in water get it.
	 */
	record Aura(Holder<MobEffect> effect, int amplifier, float radius, float intervalSeconds, boolean particles,
			boolean inWaterOnly) implements GeneEffect {
		public static final MapCodec<Aura> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(Aura::effect),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(Aura::amplifier),
				Codec.floatRange(0.5F, 32.0F).fieldOf("radius").forGetter(Aura::radius),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("interval_seconds", 2.0F).forGetter(Aura::intervalSeconds),
				Codec.BOOL.optionalFieldOf("particles", false).forGetter(Aura::particles),
				Codec.BOOL.optionalFieldOf("in_water_only", false).forGetter(Aura::inWaterOnly)
		).apply(instance, Aura::new));

		@Override
		public String type() {
			return "aura";
		}
	}

	/**
	 * The mob glows while the state is true, so that players see it through walls.
	 */
	record Glowing(When when) implements GeneEffect {
		public static final MapCodec<Glowing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Glowing::when)
		).apply(instance, Glowing::new));

		@Override
		public String type() {
			return "glowing";
		}
	}

	/**
	 * A player that the mob kills rises as a mob of its species, with its genes, and wears the head of the player.
	 */
	record RaiseVictim() implements GeneEffect {
		public static final MapCodec<RaiseVictim> CODEC = MapCodec.unit(new RaiseVictim());

		@Override
		public String type() {
			return "raise_victim";
		}
	}

	/**
	 * Multiplies the fire damage that the mob takes while the sun burns it. Other fire does normal damage.
	 */
	record Sunburn(float multiplier) implements GeneEffect {
		public static final MapCodec<Sunburn> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(Sunburn::multiplier)
		).apply(instance, Sunburn::new));

		@Override
		public String type() {
			return "sunburn";
		}
	}

	/**
	 * Iron golems in the radius attack the mob first, before other targets.
	 */
	record AttractGolems(float radius) implements GeneEffect {
		public static final MapCodec<AttractGolems> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("radius", 16.0F).forGetter(AttractGolems::radius)
		).apply(instance, AttractGolems::new));

		@Override
		public String type() {
			return "attract_golems";
		}
	}

	/**
	 * Each projectile has a chance to miss the mob while the state is true. A miss does no damage.
	 */
	record DodgeProjectiles(float chance, When when) implements GeneEffect {
		public static final MapCodec<DodgeProjectiles> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(DodgeProjectiles::chance),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(DodgeProjectiles::when)
		).apply(instance, DodgeProjectiles::new));

		@Override
		public String type() {
			return "dodge_projectiles";
		}
	}

	/**
	 * One time in its life, a hit that kills the mob leaves it with this much health.
	 */
	record Undying(float health) implements GeneEffect {
		public static final MapCodec<Undying> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 1000.0F).optionalFieldOf("health", 4.0F).forGetter(Undying::health)
		).apply(instance, Undying::new));

		@Override
		public String type() {
			return "undying";
		}
	}

	/**
	 * These status effects do not affect the mob.
	 */
	record EffectImmunity(HolderSet<MobEffect> effects) implements GeneEffect {
		public static final MapCodec<EffectImmunity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				RegistryCodecs.holderSet(Registries.MOB_EFFECT).fieldOf("effects").forGetter(EffectImmunity::effects)
		).apply(instance, EffectImmunity::new));

		@Override
		public String type() {
			return "effect_immunity";
		}
	}

	/**
	 * A part of the damage that the mob takes goes to the mobs of its species in the radius, split between them.
	 */
	record ShareDamage(float fraction, float radius) implements GeneEffect {
		public static final MapCodec<ShareDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("fraction").forGetter(ShareDamage::fraction),
				Codec.floatRange(0.5F, 32.0F).optionalFieldOf("radius", 8.0F).forGetter(ShareDamage::radius)
		).apply(instance, ShareDamage::new));

		@Override
		public String type() {
			return "share_damage";
		}
	}

	/**
	 * When the mob heals, the mobs of its species in the radius heal by the same amount.
	 */
	record ShareHealing(float radius) implements GeneEffect {
		public static final MapCodec<ShareHealing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 32.0F).optionalFieldOf("radius", 6.0F).forGetter(ShareHealing::radius)
		).apply(instance, ShareHealing::new));

		@Override
		public String type() {
			return "share_healing";
		}
	}

	/**
	 * Multiplies each heal of the mob.
	 */
	record HealMultiplier(float multiplier) implements GeneEffect {
		public static final MapCodec<HealMultiplier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(HealMultiplier::multiplier)
		).apply(instance, HealMultiplier::new));

		@Override
		public String type() {
			return "heal_multiplier";
		}
	}

	/**
	 * Each hit on a player takes food points, and heals the mob.
	 */
	record DrainFood(int amount, float heal) implements GeneEffect {
		public static final MapCodec<DrainFood> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 20).fieldOf("amount").forGetter(DrainFood::amount),
				Codec.floatRange(0.0F, 100.0F).optionalFieldOf("heal", 0.0F).forGetter(DrainFood::heal)
		).apply(instance, DrainFood::new));

		@Override
		public String type() {
			return "drain_food";
		}
	}

	/**
	 * When the mob dies while the state is true, the players in the radius get a status effect.
	 * With {@code allies}, the mobs of its species in the radius get it, and the players do not.
	 */
	record DeathEffect(Holder<MobEffect> effect, float seconds, int amplifier, float radius, When when, boolean allies)
			implements GeneEffect {
		public static final MapCodec<DeathEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(DeathEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(DeathEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(DeathEffect::amplifier),
				Codec.floatRange(0.5F, 32.0F).fieldOf("radius").forGetter(DeathEffect::radius),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(DeathEffect::when),
				Codec.BOOL.optionalFieldOf("allies", false).forGetter(DeathEffect::allies)
		).apply(instance, DeathEffect::new));

		@Override
		public String type() {
			return "death_effect";
		}
	}

	/**
	 * When a player looks at the mob from farther than the range, the mob moves to the side, after each cooldown.
	 */
	record Sidestep(float range, float distance, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<Sidestep> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 12.0F).forGetter(Sidestep::range),
				Codec.floatRange(1.0F, 16.0F).optionalFieldOf("distance", 4.0F).forGetter(Sidestep::distance),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(Sidestep::cooldownSeconds)
		).apply(instance, Sidestep::new));

		@Override
		public String type() {
			return "sidestep";
		}
	}

	/**
	 * The mob climbs walls, like a spider.
	 */
	record Climb() implements GeneEffect {
		public static final MapCodec<Climb> CODEC = MapCodec.unit(new Climb());

		@Override
		public String type() {
			return "climb";
		}
	}

	/**
	 * The mob throws at its target in the range, which gives a status effect, after each cooldown.
	 * The throw needs a clear line of sight.
	 */
	record ThrowEffect(Holder<MobEffect> effect, float seconds, int amplifier, float range, float cooldownSeconds)
			implements GeneEffect {
		public static final MapCodec<ThrowEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(ThrowEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(ThrowEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(ThrowEffect::amplifier),
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("range", 8.0F).forGetter(ThrowEffect::range),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 8.0F).forGetter(ThrowEffect::cooldownSeconds)
		).apply(instance, ThrowEffect::new));

		@Override
		public String type() {
			return "throw_effect";
		}
	}

	/**
	 * The mob picks up items. A picked up item drops when the mob dies, as in vanilla.
	 */
	record PickUpItems() implements GeneEffect {
		public static final MapCodec<PickUpItems> CODEC = MapCodec.unit(new PickUpItems());

		@Override
		public String type() {
			return "pick_up_items";
		}
	}

	/**
	 * The mob does not convert to a different species in water, for example a husk to a zombie.
	 */
	record NoConversion() implements GeneEffect {
		public static final MapCodec<NoConversion> CODEC = MapCodec.unit(new NoConversion());

		@Override
		public String type() {
			return "no_conversion";
		}
	}

	/**
	 * Pulls the players in water in the radius slowly toward the mob, like a song.
	 *
	 * @param strength the speed that the pull adds each tick, in blocks for each tick
	 */
	record WaterPull(float radius, float strength) implements GeneEffect {
		public static final MapCodec<WaterPull> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 64.0F).fieldOf("radius").forGetter(WaterPull::radius),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("strength", 0.01F).forGetter(WaterPull::strength)
		).apply(instance, WaterPull::new));

		@Override
		public String type() {
			return "water_pull";
		}
	}

	/**
	 * Each hit on a player in water pulls the player down, up to the number of blocks. A block stops the pull.
	 */
	record PullDownOnHit(float blocks) implements GeneEffect {
		public static final MapCodec<PullDownOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 16.0F).fieldOf("blocks").forGetter(PullDownOnHit::blocks)
		).apply(instance, PullDownOnHit::new));

		@Override
		public String type() {
			return "pull_down_on_hit";
		}
	}

	/**
	 * When the mob hits a player in water, it holds on, and both sink for some seconds.
	 * The player breaks free with a hit on the mob, or when the player leaves the water.
	 */
	record DragUnder(float seconds, float sinkSpeed) implements GeneEffect {
		public static final MapCodec<DragUnder> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 60.0F).fieldOf("seconds").forGetter(DragUnder::seconds),
				Codec.floatRange(0.0F, 2.0F).optionalFieldOf("sink_speed", 0.12F).forGetter(DragUnder::sinkSpeed)
		).apply(instance, DragUnder::new));

		@Override
		public String type() {
			return "drag_under";
		}
	}

	/**
	 * Multiplies the vertical speed of the mob in water: {@code down} when it sinks, {@code up} when it swims up.
	 */
	record SwimVertical(float down, float up) implements GeneEffect {
		public static final MapCodec<SwimVertical> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).optionalFieldOf("down", 1.0F).forGetter(SwimVertical::down),
				Codec.floatRange(0.0F, 10.0F).optionalFieldOf("up", 1.0F).forGetter(SwimVertical::up)
		).apply(instance, SwimVertical::new));

		@Override
		public String type() {
			return "swim_vertical";
		}
	}

	/**
	 * Gives the mob a chance to spawn with a trident in its hand. This adds to the vanilla chance.
	 */
	record TridentChance(float chance) implements GeneEffect {
		public static final MapCodec<TridentChance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(TridentChance::chance)
		).apply(instance, TridentChance::new));

		@Override
		public String type() {
			return "trident_chance";
		}
	}

	/**
	 * Multiplies how often the mob throws or shoots. The multiplier 2 throws two times as often.
	 */
	record ThrowRate(float multiplier) implements GeneEffect {
		public static final MapCodec<ThrowRate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(ThrowRate::multiplier)
		).apply(instance, ThrowRate::new));

		@Override
		public String type() {
			return "throw_rate";
		}
	}

	/**
	 * Multiplies the throw range of the mob at a target: a wet target is in water or in a boat, and a dry target
	 * is not.
	 */
	record ThrowRange(float multiplier, Target target) implements GeneEffect {
		public static final MapCodec<ThrowRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 10.0F).fieldOf("multiplier").forGetter(ThrowRange::multiplier),
				Target.CODEC.fieldOf("target").forGetter(ThrowRange::target)
		).apply(instance, ThrowRange::new));

		@Override
		public String type() {
			return "throw_range";
		}

		/**
		 * The targets of a throw range.
		 */
		public enum Target implements StringRepresentable {
			WET,
			DRY;

			public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);

			@Override
			public String getSerializedName() {
				return name().toLowerCase(Locale.ROOT);
			}
		}
	}

	/**
	 * The mob picks up the tridents that players throw near it, when its hand is empty, and keeps them.
	 */
	record ScavengeTridents(float radius) implements GeneEffect {
		public static final MapCodec<ScavengeTridents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 8.0F).optionalFieldOf("radius", 2.0F).forGetter(ScavengeTridents::radius)
		).apply(instance, ScavengeTridents::new));

		@Override
		public String type() {
			return "scavenge_tridents";
		}
	}

	/**
	 * The item in the hand of the mob does not drop when the mob dies.
	 */
	record NoWeaponDrop() implements GeneEffect {
		public static final MapCodec<NoWeaponDrop> CODEC = MapCodec.unit(new NoWeaponDrop());

		@Override
		public String type() {
			return "no_weapon_drop";
		}
	}

	/**
	 * A projectile hit of the mob calls lightning on the target, if the target is under the open sky.
	 * With {@code storm_only}, this works only in a thunderstorm.
	 */
	record LightningOnHit(boolean stormOnly) implements GeneEffect {
		public static final MapCodec<LightningOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("storm_only", true).forGetter(LightningOnHit::stormOnly)
		).apply(instance, LightningOnHit::new));

		@Override
		public String type() {
			return "lightning_on_hit";
		}
	}

	/**
	 * Multiplies the damage that the mob takes from an attacker behind it.
	 */
	record DamageFromBehind(float multiplier) implements GeneEffect {
		public static final MapCodec<DamageFromBehind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(DamageFromBehind::multiplier)
		).apply(instance, DamageFromBehind::new));

		@Override
		public String type() {
			return "damage_from_behind";
		}
	}

	/**
	 * An attacker that hits the mob in melee takes damage.
	 */
	record Thorns(float damage) implements GeneEffect {
		public static final MapCodec<Thorns> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("damage").forGetter(Thorns::damage)
		).apply(instance, Thorns::new));

		@Override
		public String type() {
			return "thorns";
		}
	}

	/**
	 * One time in its life, the mob heals to full health when its health drops under the part of its maximum.
	 */
	record HealOnce(float threshold) implements GeneEffect {
		public static final MapCodec<HealOnce> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("threshold", 0.5F).forGetter(HealOnce::threshold)
		).apply(instance, HealOnce::new));

		@Override
		public String type() {
			return "heal_once";
		}
	}

	/**
	 * When an attacker hurts the mob, the attacker gets a status effect. The effect waits for its cooldown.
	 * With {@code ink}, the mob sprays a cloud of ink.
	 */
	record EffectOnHurt(Holder<MobEffect> effect, float seconds, int amplifier, float cooldownSeconds, boolean ink)
			implements GeneEffect {
		public static final MapCodec<EffectOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(EffectOnHurt::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(EffectOnHurt::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectOnHurt::amplifier),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 0.0F).forGetter(EffectOnHurt::cooldownSeconds),
				Codec.BOOL.optionalFieldOf("ink", false).forGetter(EffectOnHurt::ink)
		).apply(instance, EffectOnHurt::new));

		@Override
		public String type() {
			return "effect_on_hurt";
		}
	}

	/**
	 * Hurts the mob after each interval while the state is true. The damage is the damage of drying out, so fire
	 * resistance does not stop it.
	 */
	record HarmWhen(When when, float amount, float intervalSeconds) implements GeneEffect {
		public static final MapCodec<HarmWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(HarmWhen::when),
				Codec.floatRange(0.0F, 1000.0F).fieldOf("amount").forGetter(HarmWhen::amount),
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("interval_seconds", 4.0F).forGetter(HarmWhen::intervalSeconds)
		).apply(instance, HarmWhen::new));

		@Override
		public String type() {
			return "harm_when";
		}
	}

	/**
	 * The hits of the mob on a boat or on a rider of a boat break the boat after the number of hits.
	 */
	record BreakBoats(int hits) implements GeneEffect {
		public static final MapCodec<BreakBoats> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 100).optionalFieldOf("hits", 3).forGetter(BreakBoats::hits)
		).apply(instance, BreakBoats::new));

		@Override
		public String type() {
			return "break_boats";
		}
	}

	/**
	 * The mob sees invisible entities in the radius as if they were visible.
	 */
	record SeeInvisible(float radius) implements GeneEffect {
		public static final MapCodec<SeeInvisible> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).fieldOf("radius").forGetter(SeeInvisible::radius)
		).apply(instance, SeeInvisible::new));

		@Override
		public String type() {
			return "see_invisible";
		}
	}

	/**
	 * The mob does not see a sneaking entity farther than the radius.
	 */
	record SneakBlind(float radius) implements GeneEffect {
		public static final MapCodec<SneakBlind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).fieldOf("radius").forGetter(SneakBlind::radius)
		).apply(instance, SneakBlind::new));

		@Override
		public String type() {
			return "sneak_blind";
		}
	}

	/**
	 * A light hangs in front of the mob. When a player comes into the radius, the light goes out, and the next hit
	 * of the mob does more damage. The light shows again after some seconds with no player in the radius.
	 */
	record Lure(float radius, float resetSeconds, float multiplier) implements GeneEffect {
		public static final MapCodec<Lure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 64.0F).optionalFieldOf("radius", 6.0F).forGetter(Lure::radius),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("reset_seconds", 20.0F).forGetter(Lure::resetSeconds),
				Codec.floatRange(0.0F, 10.0F).optionalFieldOf("multiplier", 2.0F).forGetter(Lure::multiplier)
		).apply(instance, Lure::new));

		@Override
		public String type() {
			return "lure";
		}
	}
}
