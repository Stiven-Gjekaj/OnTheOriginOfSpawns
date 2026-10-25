package io.github.stivengjekaj.originofspawns.definition;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
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
			Map.entry("call_on_hit", CallOnHit.CODEC),
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
			Map.entry("lure", Lure.CODEC),
			Map.entry("keep_distance", KeepDistance.CODEC),
			Map.entry("arrow_spread", ArrowSpread.CODEC),
			Map.entry("pierce_shields", PierceShields.CODEC),
			Map.entry("damage_from_front", DamageFromFront.CODEC),
			Map.entry("damage_from_enchantment", DamageFromEnchantment.CODEC),
			Map.entry("ignored_by", IgnoredBy.CODEC),
			Map.entry("reassemble", Reassemble.CODEC),
			Map.entry("barrage", Barrage.CODEC),
			Map.entry("jockey", Jockey.CODEC),
			Map.entry("draw_time", DrawTime.CODEC),
			Map.entry("freeze_on_hit", FreezeOnHit.CODEC),
			Map.entry("freeze_attacker", FreezeAttacker.CODEC),
			Map.entry("freeze_aura", FreezeAura.CODEC),
			Map.entry("fan_shot", FanShot.CODEC),
			Map.entry("arrow_knockback", ArrowKnockback.CODEC),
			Map.entry("walk_on_powder_snow", WalkOnPowderSnow.CODEC),
			Map.entry("arrow_effect", ArrowEffect.CODEC),
			Map.entry("death_block", DeathBlock.CODEC),
			Map.entry("armor_break", ArmorBreak.CODEC),
			Map.entry("gaze_effect", GazeEffect.CODEC),
			Map.entry("call_on_death", CallOnDeath.CODEC),
			Map.entry("heal_on_ally_death", HealOnAllyDeath.CODEC),
			Map.entry("hidden_from_afar", HiddenFromAfar.CODEC),
			Map.entry("ghost_arrows", GhostArrows.CODEC),
			Map.entry("heal_on_hit", HealOnHit.CODEC),
			Map.entry("regrow", Regrow.CODEC),
			Map.entry("parry", Parry.CODEC),
			Map.entry("cleave", Cleave.CODEC),
			Map.entry("skull_shot", SkullShot.CODEC),
			Map.entry("death_spawn", DeathSpawn.CODEC),
			Map.entry("soul_hunger", SoulHunger.CODEC),
			Map.entry("damage_to", DamageTo.CODEC),
			Map.entry("snowball_damage", SnowballDamage.CODEC),
			Map.entry("water_sensitive", WaterSensitive.CODEC),
			Map.entry("disarm", Disarm.CODEC),
			Map.entry("blast_size", BlastSize.CODEC),
			Map.entry("fuse_time", FuseTime.CODEC),
			Map.entry("blast_cloud", BlastCloud.CODEC),
			Map.entry("blast_effect", BlastEffect.CODEC),
			Map.entry("chain_reaction", ChainReaction.CODEC),
			Map.entry("aftershock", Aftershock.CODEC),
			Map.entry("silent_fuse", SilentFuse.CODEC),
			Map.entry("charged_spawn", ChargedSpawn.CODEC),
			Map.entry("charge_when", ChargeWhen.CODEC),
			Map.entry("blast_lightning", BlastLightning.CODEC),
			Map.entry("lightning_immunity", LightningImmunity.CODEC),
			Map.entry("web_on_hit", WebOnHit.CODEC),
			Map.entry("web_trail", WebTrail.CODEC),
			Map.entry("web_shot", WebShot.CODEC),
			Map.entry("leap_strike", LeapStrike.CODEC),
			Map.entry("first_strike", FirstStrike.CODEC),
			Map.entry("hunt_in_light", HuntInLight.CODEC),
			Map.entry("bite_poison", BitePoison.CODEC),
			Map.entry("damage_from_item", DamageFromItem.CODEC),
			Map.entry("rocket_chance", RocketChance.CODEC),
			Map.entry("rocket_shot", RocketShot.CODEC),
			Map.entry("rocket_blast", RocketBlast.CODEC),
			Map.entry("patrol_size", PatrolSize.CODEC),
			Map.entry("knockback", Knockback.CODEC),
			Map.entry("fang_damage", FangDamage.CODEC),
			Map.entry("fang_effect", FangEffect.CODEC),
			Map.entry("fang_launch", FangLaunch.CODEC),
			Map.entry("fang_heal", FangHeal.CODEC),
			Map.entry("spell_interval", SpellInterval.CODEC),
			Map.entry("fang_line", FangLine.CODEC),
			Map.entry("fang_ring", FangRing.CODEC),
			Map.entry("vex_count", VexCount.CODEC),
			Map.entry("vex_health", VexHealth.CODEC),
			Map.entry("vex_damage", VexDamage.CODEC),
			Map.entry("vex_life", VexLife.CODEC),
			Map.entry("vex_effect", VexEffect.CODEC),
			Map.entry("vex_heal", VexHeal.CODEC),
			Map.entry("hex", Hex.CODEC),
			Map.entry("decoy", Decoy.CODEC),
			Map.entry("potion_duration", PotionDuration.CODEC),
			Map.entry("potion_level", PotionLevel.CODEC),
			Map.entry("potion_swap", PotionSwap.CODEC),
			Map.entry("potion_extra", PotionExtra.CODEC),
			Map.entry("potion_lingering", PotionLingering.CODEC),
			Map.entry("potion_count", PotionCount.CODEC),
			Map.entry("potion_splash", PotionSplash.CODEC),
			Map.entry("potion_damage", PotionDamage.CODEC),
			Map.entry("potion_shards", PotionShards.CODEC),
			Map.entry("potion_heal", PotionHeal.CODEC),
			Map.entry("drink", Drink.CODEC),
			Map.entry("shared_drink", SharedDrink.CODEC),
			Map.entry("heal_throw", HealThrow.CODEC),
			Map.entry("teleport_to_target", TeleportToTarget.CODEC),
			Map.entry("teleport_behind", TeleportBehind.CODEC),
			Map.entry("escape_teleport", EscapeTeleport.CODEC),
			Map.entry("blast_teleport", BlastTeleport.CODEC),
			Map.entry("aimed_teleport", AimedTeleport.CODEC),
			Map.entry("teleport_strike", TeleportStrike.CODEC),
			Map.entry("stare_effect", StareEffect.CODEC),
			Map.entry("stare_range", StareRange.CODEC),
			Map.entry("stare_through_pumpkin", StareThroughPumpkin.CODEC),
			Map.entry("block_throw", BlockThrow.CODEC),
			Map.entry("thrown_block_damage", ThrownBlockDamage.CODEC),
			Map.entry("thrown_block_effect", ThrownBlockEffect.CODEC),
			Map.entry("thrown_block_shield", ThrownBlockShield.CODEC),
			Map.entry("pickup_blocks", PickupBlocks.CODEC),
			Map.entry("teleport_target_on_hit", TeleportTargetOnHit.CODEC),
			Map.entry("water_damage_taken", WaterDamageTaken.CODEC),
			Map.entry("pearl_chance", PearlChance.CODEC),
			Map.entry("lifespan", Lifespan.CODEC),
			Map.entry("life_on_hit", LifeOnHit.CODEC),
			Map.entry("age_spawn", AgeSpawn.CODEC),
			Map.entry("projectile_teleport", ProjectileTeleport.CODEC),
			Map.entry("call_others", CallOthers.CODEC),
			Map.entry("spawn_size", SpawnSize.CODEC),
			Map.entry("hop", Hop.CODEC),
			Map.entry("landing_damage", LandingDamage.CODEC),
			Map.entry("landing_effect", LandingEffect.CODEC),
			Map.entry("landing_armor_damage", LandingArmorDamage.CODEC),
			Map.entry("landing_shield_stop", LandingShieldStop.CODEC),
			Map.entry("split_count", SplitCount.CODEC),
			Map.entry("split_effect", SplitEffect.CODEC),
			Map.entry("split_jump", SplitJump.CODEC),
			Map.entry("split_area_effect", SplitAreaEffect.CODEC),
			Map.entry("tiny_damage", TinyDamage.CODEC),
			Map.entry("grow", Grow.CODEC),
			Map.entry("bud", Bud.CODEC),
			Map.entry("touch_reach", TouchReach.CODEC),
			Map.entry("shield_break", ShieldBreak.CODEC),
			Map.entry("hop_back", HopBack.CODEC),
			Map.entry("fire_on_hurt", FireOnHurt.CODEC),
			Map.entry("lava_exit_fire", LavaExitFire.CODEC),
			Map.entry("fire_aura", FireAura.CODEC),
			Map.entry("fireball_shot", FireballShot.CODEC),
			Map.entry("blast_heal", BlastHeal.CODEC),
			Map.entry("fire_trail", FireTrail.CODEC),
			Map.entry("lava_pool", LavaPool.CODEC),
			Map.entry("anger_range", AngerRange.CODEC),
			Map.entry("anger_time", AngerTime.CODEC),
			Map.entry("anger_after_death", AngerAfterDeath.CODEC),
			Map.entry("self_effect_on_hurt", SelfEffectOnHurt.CODEC),
			Map.entry("self_effect_on_hit", SelfEffectOnHit.CODEC),
			Map.entry("spawn_enchantment", SpawnEnchantment.CODEC),
			Map.entry("conversion_time", ConversionTime.CODEC),
			Map.entry("gold_armor_attack", GoldArmorAttack.CODEC),
			Map.entry("no_fear", NoFear.CODEC),
			Map.entry("guard_gold", GuardGold.CODEC),
			Map.entry("shatter_armor", ShatterArmor.CODEC)
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
	record IgniteTarget(float chance, float seconds, Hits hits) implements GeneEffect {
		public static final MapCodec<IgniteTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(IgniteTarget::chance),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("seconds").forGetter(IgniteTarget::seconds),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(IgniteTarget::hits)
		).apply(instance, IgniteTarget::new));

		public IgniteTarget(float chance, float seconds) {
			this(chance, seconds, Hits.MELEE);
		}

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
		SUNLIT,
		/**
		 * The target of the mob is 2 or more blocks lower than the mob.
		 */
		TARGET_BELOW,
		/**
		 * The target of the mob is 2 or more blocks higher than the mob.
		 */
		TARGET_ABOVE,
		/**
		 * The health of the mob is under half of its maximum.
		 */
		WOUNDED,
		/**
		 * It snows on the mob: a storm in a biome where the storm is snow, with open sky above the mob.
		 */
		SNOWING,
		/**
		 * It does not snow on the mob. This is the opposite of {@code snowing}.
		 */
		NOT_SNOWING,
		/**
		 * The mob stands on snow or ice, or in a snow layer or powder snow.
		 */
		ON_SNOW,
		/**
		 * The mob does not stand on snow or ice. This is the opposite of {@code on_snow}.
		 */
		OFF_SNOW,
		/**
		 * The mob stands on ice.
		 */
		ON_ICE,
		/**
		 * The mob is in a biome that is cold enough for snow.
		 */
		COLD_BIOME,
		/**
		 * It is dark outside, between dusk and dawn.
		 */
		NIGHT,
		/**
		 * The target of the mob freezes, as in powder snow.
		 */
		TARGET_FROZEN,
		/**
		 * The mob is in water, or stands on a block in the tag {@code originofspawns:mud}.
		 */
		WET_GROUND,
		/**
		 * The mob is not in water and does not stand on mud. This is the opposite of {@code wet_ground}.
		 */
		DRY_GROUND,
		/**
		 * The mob stands on a block in the tag {@code originofspawns:rot_ground}: mud, moss, or mycelium.
		 */
		ON_ROT_GROUND,
		/**
		 * The target of the mob is in water.
		 */
		TARGET_IN_WATER,
		/**
		 * The feet of the mob are in lava.
		 */
		IN_LAVA,
		/**
		 * The feet of the mob are not in lava. This is the opposite of {@code in_lava}.
		 */
		OUT_OF_LAVA,
		/**
		 * The feet of the mob are in fire, soul fire, or lava.
		 */
		IN_FIRE,
		/**
		 * The mob stands on a block in the tag {@code minecraft:soul_speed_blocks}.
		 */
		ON_SOUL_GROUND,
		/**
		 * The mob does not stand on soul sand or soul soil. This is the opposite of {@code on_soul_ground}.
		 */
		OFF_SOUL_GROUND,
		/**
		 * It is bright outside. This is the opposite of {@code night}.
		 */
		DAY,
		/**
		 * The health of the mob is half of its maximum or more. This is the opposite of {@code wounded}.
		 */
		HEALTHY,
		/**
		 * The mob targets a player with half health or more. This is the opposite of {@code hunting_wounded}.
		 */
		HUNTING_HEALTHY,
		/**
		 * The target of the mob has the Weakness effect.
		 */
		TARGET_WEAKENED,
		/**
		 * The mob is a charged creeper.
		 */
		CHARGED,
		/**
		 * A player did not see the mob: the mob is behind the player, or the player did not look at it in the last
		 * 3 seconds. In {@code damage_dealt_when}, the state asks about the entity that takes the damage. In other
		 * effects, it asks about the target of the mob.
		 */
		UNSEEN,
		/**
		 * A monster spawner is in 16 blocks of the mob.
		 */
		NEAR_SPAWNER,
		/**
		 * No monster spawner is in 16 blocks of the mob. This is the opposite of {@code near_spawner}.
		 */
		FAR_FROM_SPAWNER,
		/**
		 * The mob burns.
		 */
		ON_FIRE,
		/**
		 * The target of the mob is farther than 11 blocks.
		 */
		TARGET_FAR,
		/**
		 * The mob falls: it is in the air, out of water, and it fell more than 2 blocks.
		 */
		FALLING,
		/**
		 * The target of the mob is a player, and the mob sees the player.
		 */
		SEES_PLAYER,
		/**
		 * Rain falls on the mob: the world has rain, the biome has rain and not snow, and the mob has open sky above
		 * it.
		 */
		IN_RAIN,
		/**
		 * The mob has a target, and it finds no path that reaches the target.
		 */
		TARGET_UNREACHABLE,
		/**
		 * The mob is in water, or rain falls on it.
		 */
		WET,
		/**
		 * The mob is an enderman that carries a block.
		 */
		CARRYING_BLOCK,
		/**
		 * The target of the mob is a player.
		 */
		TARGET_PLAYER,
		/**
		 * The target of the mob is a player with at least one piece of gold armor.
		 */
		TARGET_GOLD_ARMOR,
		/**
		 * The mob is in 16 blocks of the place where it spawned.
		 */
		/**
		 * The target of the mob is a player that does not hold a shield up.
		 */
		TARGET_NO_SHIELD,
		NEAR_HOME,
		/**
		 * The mob is farther than 32 blocks from the place where it spawned.
		 */
		FAR_FROM_HOME,
		/**
		 * The mob is in the air: it does not stand on the ground, and it is not in water or in lava.
		 */
		IN_AIR;

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
	record DamageDealtWhen(When when, float multiplier, boolean playersOnly, Hits hits) implements GeneEffect {
		public static final MapCodec<DamageDealtWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(DamageDealtWhen::when),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageDealtWhen::multiplier),
				Codec.BOOL.optionalFieldOf("players_only", false).forGetter(DamageDealtWhen::playersOnly),
				Hits.CODEC.optionalFieldOf("hits", Hits.ANY).forGetter(DamageDealtWhen::hits)
		).apply(instance, DamageDealtWhen::new));

		@Override
		public String type() {
			return "damage_dealt_when";
		}
	}

	/**
	 * Multiplies the damage that the mob takes when the state is true.
	 */
	record DamageTakenWhen(When when, float multiplier, float radius, Optional<TagKey<DamageType>> damageTypes)
			implements GeneEffect {
		public static final MapCodec<DamageTakenWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.fieldOf("when").forGetter(DamageTakenWhen::when),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("multiplier").forGetter(DamageTakenWhen::multiplier),
				Codec.floatRange(0.5F, 64.0F).optionalFieldOf("radius", 8.0F).forGetter(DamageTakenWhen::radius),
				TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_types")
						.forGetter(DamageTakenWhen::damageTypes)
		).apply(instance, DamageTakenWhen::new));

		public DamageTakenWhen(When when, float multiplier, float radius) {
			this(when, multiplier, radius, Optional.empty());
		}

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
	 * Adds damage for each mob of the same species near the mob, up to a maximum. With {@code angry_only}, only the
	 * angry mobs count, for example the zombified piglins that are angry at a player.
	 */
	record Rally(float perAlly, float max, float radius, boolean angryOnly) implements GeneEffect {
		public static final MapCodec<Rally> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("per_ally").forGetter(Rally::perAlly),
				Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("max").forGetter(Rally::max),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("radius", 6.0F).forGetter(Rally::radius),
				Codec.BOOL.optionalFieldOf("angry_only", false).forGetter(Rally::angryOnly)
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
	 * Gives the target a status effect when the mob hits it. With {@code targets}, only the mobs of the tag get it.
	 * Rust Axe: {@code minecraft:iron_golem}.
	 */
	record EffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier, float chance, When when, Hits hits,
			Optional<HolderSet<EntityType<?>>> targets) implements GeneEffect {
		public static final MapCodec<EffectOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(EffectOnHit::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(EffectOnHit::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectOnHit::amplifier),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(EffectOnHit::chance),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(EffectOnHit::when),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(EffectOnHit::hits),
				RegistryCodecs.holderSet(Registries.ENTITY_TYPE).optionalFieldOf("targets").forGetter(EffectOnHit::targets)
		).apply(instance, EffectOnHit::new));

		public EffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier, float chance) {
			this(effect, seconds, amplifier, chance, When.ALWAYS, Hits.MELEE, Optional.empty());
		}

		/**
		 * Returns true if the target gets the effect.
		 */
		public boolean reaches(net.minecraft.world.entity.Entity target) {
			return targets.map(set -> set.contains(target.getType().builtInRegistryHolder())).orElse(true);
		}

		@Override
		public String type() {
			return "effect_on_hit";
		}
	}

	/**
	 * Each hit of the kind in {@code hits} takes XP points from a player. The mob drops the XP that it took when it
	 * dies, and no other XP.
	 */
	record LeechXp(int amount, Hits hits) implements GeneEffect {
		public static final MapCodec<LeechXp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 10000).fieldOf("amount").forGetter(LeechXp::amount),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(LeechXp::hits)
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
	 * When the mob dies, it damages the players in the radius. With {@code fire_seconds}, the players there also burn
	 * for this many seconds. The burst breaks no blocks.
	 */
	record DeathBurst(float damage, float radius, float fireSeconds) implements GeneEffect {
		public static final MapCodec<DeathBurst> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1000.0F).fieldOf("damage").forGetter(DeathBurst::damage),
				Codec.floatRange(0.0F, 32.0F).fieldOf("radius").forGetter(DeathBurst::radius),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("fire_seconds", 0.0F).forGetter(DeathBurst::fireSeconds)
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
	record Leap(float minDistance, float maxDistance, float cooldownSeconds, float missStunSeconds, Medium medium,
			When when) implements GeneEffect {
		public static final MapCodec<Leap> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("min_distance", 3.0F).forGetter(Leap::minDistance),
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("max_distance", 5.0F).forGetter(Leap::maxDistance),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 5.0F).forGetter(Leap::cooldownSeconds),
				Codec.floatRange(0.0F, 60.0F).optionalFieldOf("miss_stun_seconds", 1.0F).forGetter(Leap::missStunSeconds),
				Medium.CODEC.optionalFieldOf("medium", Medium.GROUND).forGetter(Leap::medium),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Leap::when)
		).apply(instance, Leap::new));

		public Leap(float minDistance, float maxDistance, float cooldownSeconds, float missStunSeconds) {
			this(minDistance, maxDistance, cooldownSeconds, missStunSeconds, Medium.GROUND);
		}

		public Leap(float minDistance, float maxDistance, float cooldownSeconds, float missStunSeconds, Medium medium) {
			this(minDistance, maxDistance, cooldownSeconds, missStunSeconds, medium, When.ALWAYS);
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
	 * With {@code night_only}, the mob is translucent only while it is dark outside.
	 */
	record Translucent(int maxLight, float opacity, Optional<TagKey<Block>> blocks, boolean nightOnly, When when,
			When also) implements GeneEffect {
		public static final MapCodec<Translucent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(0, 15).optionalFieldOf("max_light", 7).forGetter(Translucent::maxLight),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("opacity", 0.1F).forGetter(Translucent::opacity),
				TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("blocks").forGetter(Translucent::blocks),
				Codec.BOOL.optionalFieldOf("night_only", false).forGetter(Translucent::nightOnly),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Translucent::when),
				When.CODEC.optionalFieldOf("also", When.ALWAYS).forGetter(Translucent::also)
		).apply(instance, Translucent::new));

		@Override
		public String type() {
			return "translucent";
		}
	}

	/**
	 * When the mob spawns in a natural way or from an event, such as an endermite from an ender pearl, at night near a
	 * player, it rises out of the ground.
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
	 * With {@code ignite}, a creeper lights its fuse when its target player comes in the radius and it sees the player.
	 */
	record Lurk(float radius, boolean ignite) implements GeneEffect {
		public static final MapCodec<Lurk> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("radius", 6.0F).forGetter(Lurk::radius),
				Codec.BOOL.optionalFieldOf("ignite", false).forGetter(Lurk::ignite)
		).apply(instance, Lurk::new));

		public Lurk(float radius) {
			this(radius, false);
		}

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
	 * When the mob first targets a player, all mobs of its species in the radius target that player. With
	 * {@code mobs}, the mobs of the list or tag answer the call in place of its species. Alarm Horn:
	 * {@code ["minecraft:piglin", "minecraft:piglin_brute"]}.
	 */
	record CallOnSight(float radius, float cooldownSeconds, Optional<HolderSet<EntityType<?>>> mobs) implements GeneEffect {
		public static final MapCodec<CallOnSight> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 128.0F).fieldOf("radius").forGetter(CallOnSight::radius),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 30.0F)
						.forGetter(CallOnSight::cooldownSeconds),
				RegistryCodecs.holderSet(Registries.ENTITY_TYPE).optionalFieldOf("mobs").forGetter(CallOnSight::mobs)
		).apply(instance, CallOnSight::new));

		@Override
		public String type() {
			return "call_on_sight";
		}
	}

	/**
	 * When the mob hits a player, all mobs of its species in the radius target that player.
	 */
	record CallOnHit(float radius, Hits hits) implements GeneEffect {
		public static final MapCodec<CallOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 128.0F).fieldOf("radius").forGetter(CallOnHit::radius),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(CallOnHit::hits)
		).apply(instance, CallOnHit::new));

		@Override
		public String type() {
			return "call_on_hit";
		}
	}

	/**
	 * When the mob spawns in a natural way, from a spawner, or from an event such as an ender pearl, more mobs spawn
	 * near it. They have the same genes, or with {@code pool_genes} they get new genes from the pool, as new mobs.
	 */
	record Brood(int extra, boolean poolGenes) implements GeneEffect {
		public static final MapCodec<Brood> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).fieldOf("extra").forGetter(Brood::extra),
				Codec.BOOL.optionalFieldOf("pool_genes", false).forGetter(Brood::poolGenes)
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
	 * slowly. With {@code also_breaks}, it also breaks the blocks of that tag, for example fences. A door takes
	 * {@code door_seconds}, 12 seconds as in vanilla. It obeys the gamerule that stops mobs from breaking blocks.
	 */
	record Siege(float breakSeconds, Optional<TagKey<Block>> alsoBreaks, float doorSeconds) implements GeneEffect {
		public static final MapCodec<Siege> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("break_seconds", 10.0F).forGetter(Siege::breakSeconds),
				TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("also_breaks").forGetter(Siege::alsoBreaks),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("door_seconds", 12.0F).forGetter(Siege::doorSeconds)
		).apply(instance, Siege::new));

		public Siege(float breakSeconds) {
			this(breakSeconds, Optional.empty(), 12.0F);
		}

		public Siege(float breakSeconds, Optional<TagKey<Block>> alsoBreaks) {
			this(breakSeconds, alsoBreaks, 12.0F);
		}

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
			boolean inWaterOnly, Optional<Float> seconds, boolean allies, When when) implements GeneEffect {
		public static final MapCodec<Aura> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(Aura::effect),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(Aura::amplifier),
				Codec.floatRange(0.5F, 32.0F).fieldOf("radius").forGetter(Aura::radius),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("interval_seconds", 2.0F).forGetter(Aura::intervalSeconds),
				Codec.BOOL.optionalFieldOf("particles", false).forGetter(Aura::particles),
				Codec.BOOL.optionalFieldOf("in_water_only", false).forGetter(Aura::inWaterOnly),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds").forGetter(Aura::seconds),
				Codec.BOOL.optionalFieldOf("allies", false).forGetter(Aura::allies),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Aura::when)
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
	 * Each hit on a player of the kind in {@code hits} has the chance to take food points, and to heal the mob.
	 */
	record DrainFood(int amount, float heal, float chance, Hits hits) implements GeneEffect {
		public static final MapCodec<DrainFood> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 20).fieldOf("amount").forGetter(DrainFood::amount),
				Codec.floatRange(0.0F, 100.0F).optionalFieldOf("heal", 0.0F).forGetter(DrainFood::heal),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(DrainFood::chance),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(DrainFood::hits)
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
	record Sidestep(float range, float distance, float cooldownSeconds, boolean aimed) implements GeneEffect {
		public static final MapCodec<Sidestep> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 12.0F).forGetter(Sidestep::range),
				Codec.floatRange(1.0F, 16.0F).optionalFieldOf("distance", 4.0F).forGetter(Sidestep::distance),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(Sidestep::cooldownSeconds),
				Codec.BOOL.optionalFieldOf("aimed", false).forGetter(Sidestep::aimed)
		).apply(instance, Sidestep::new));

		public Sidestep(float range, float distance, float cooldownSeconds) {
			this(range, distance, cooldownSeconds, false);
		}

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
	 * The throw needs a clear line of sight. A target nearer than the shortest range gets no throw.
	 */
	record ThrowEffect(Holder<MobEffect> effect, float seconds, int amplifier, float range, float cooldownSeconds,
			float minRange) implements GeneEffect {
		public static final MapCodec<ThrowEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(ThrowEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(ThrowEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(ThrowEffect::amplifier),
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("range", 8.0F).forGetter(ThrowEffect::range),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 8.0F).forGetter(ThrowEffect::cooldownSeconds),
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("min_range", 0.0F).forGetter(ThrowEffect::minRange)
		).apply(instance, ThrowEffect::new));

		public ThrowEffect(Holder<MobEffect> effect, float seconds, int amplifier, float range, float cooldownSeconds) {
			this(effect, seconds, amplifier, range, cooldownSeconds, 0.0F);
		}

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
	record ThrowRate(float multiplier, When when) implements GeneEffect {
		public static final MapCodec<ThrowRate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(ThrowRate::multiplier),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(ThrowRate::when)
		).apply(instance, ThrowRate::new));

		public ThrowRate(float multiplier) {
			this(multiplier, When.ALWAYS);
		}

		@Override
		public String type() {
			return "throw_rate";
		}
	}

	/**
	 * Multiplies the throw range of the mob at a target: a wet target is in water or in a boat, a dry target is not,
	 * and any target is both.
	 */
	record ThrowRange(float multiplier, Target target, When when) implements GeneEffect {
		public static final MapCodec<ThrowRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 10.0F).fieldOf("multiplier").forGetter(ThrowRange::multiplier),
				Target.CODEC.fieldOf("target").forGetter(ThrowRange::target),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(ThrowRange::when)
		).apply(instance, ThrowRange::new));

		public ThrowRange(float multiplier, Target target) {
			this(multiplier, target, When.ALWAYS);
		}

		@Override
		public String type() {
			return "throw_range";
		}

		/**
		 * The targets of a throw range.
		 */
		public enum Target implements StringRepresentable {
			WET,
			DRY,
			ANY;

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
	 * With {@code ink}, the mob sprays a cloud of ink. With an {@code ally_radius} above 0, the mobs of its species
	 * in the radius get the effect, and the attacker does not.
	 */
	record EffectOnHurt(Holder<MobEffect> effect, float seconds, int amplifier, float cooldownSeconds, boolean ink,
			float allyRadius, float playerRadius, Hits hits) implements GeneEffect {
		public static final MapCodec<EffectOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(EffectOnHurt::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(EffectOnHurt::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectOnHurt::amplifier),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 0.0F).forGetter(EffectOnHurt::cooldownSeconds),
				Codec.BOOL.optionalFieldOf("ink", false).forGetter(EffectOnHurt::ink),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("ally_radius", 0.0F).forGetter(EffectOnHurt::allyRadius),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("player_radius", 0.0F).forGetter(EffectOnHurt::playerRadius),
				Hits.CODEC.optionalFieldOf("hits", Hits.ANY).forGetter(EffectOnHurt::hits)
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

	/**
	 * While the mob shoots with a bow, it walks back when its target is nearer than the number of blocks.
	 */
	record KeepDistance(float blocks) implements GeneEffect {
		public static final MapCodec<KeepDistance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 32.0F).fieldOf("blocks").forGetter(KeepDistance::blocks)
		).apply(instance, KeepDistance::new));

		@Override
		public String type() {
			return "keep_distance";
		}
	}

	/**
	 * Multiplies the spread of the arrows of the mob. The multiplier 0 makes each arrow fly straight.
	 */
	record ArrowSpread(float multiplier) implements GeneEffect {
		public static final MapCodec<ArrowSpread> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(ArrowSpread::multiplier)
		).apply(instance, ArrowSpread::new));

		@Override
		public String type() {
			return "arrow_spread";
		}
	}

	/**
	 * The projectiles of the mob go through shields, and do their full damage. A shield takes no damage from them.
	 * With {@code blasts}, the blast of the mob also goes through shields in the same way.
	 */
	record PierceShields(boolean blasts) implements GeneEffect {
		public static final MapCodec<PierceShields> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("blasts", false).forGetter(PierceShields::blasts)
		).apply(instance, PierceShields::new));

		public PierceShields() {
			this(false);
		}

		@Override
		public String type() {
			return "pierce_shields";
		}
	}

	/**
	 * Multiplies the damage that the mob takes from an attacker in front of it.
	 */
	record DamageFromFront(float multiplier, Hits hits) implements GeneEffect {
		public static final MapCodec<DamageFromFront> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(DamageFromFront::multiplier),
				Hits.CODEC.optionalFieldOf("hits", Hits.ANY).forGetter(DamageFromFront::hits)
		).apply(instance, DamageFromFront::new));

		@Override
		public String type() {
			return "damage_from_front";
		}
	}

	/**
	 * Multiplies the damage that the mob takes from a weapon with the enchantment, for example Smite.
	 */
	record DamageFromEnchantment(ResourceKey<Enchantment> enchantment, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageFromEnchantment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(DamageFromEnchantment::enchantment),
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(DamageFromEnchantment::multiplier)
		).apply(instance, DamageFromEnchantment::new));

		@Override
		public String type() {
			return "damage_from_enchantment";
		}
	}

	/**
	 * Mobs of these types do not target the mob.
	 */
	record IgnoredBy(HolderSet<EntityType<?>> mobs) implements GeneEffect {
		public static final MapCodec<IgnoredBy> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("mobs").forGetter(IgnoredBy::mobs)
		).apply(instance, IgnoredBy::new));

		@Override
		public String type() {
			return "ignored_by";
		}
	}

	/**
	 * One time in its life, the mob falls apart when it dies, and stands up again after some seconds with a part of
	 * its health. While it is down, it takes no damage, except from a weapon with Smite, which stops the return.
	 */
	record Reassemble(float seconds, float health) implements GeneEffect {
		public static final MapCodec<Reassemble> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 60.0F).optionalFieldOf("seconds", 3.0F).forGetter(Reassemble::seconds),
				Codec.floatRange(0.01F, 1.0F).optionalFieldOf("health", 0.5F).forGetter(Reassemble::health)
		).apply(instance, Reassemble::new));

		@Override
		public String type() {
			return "reassemble";
		}
	}

	/**
	 * When the mob shoots, and enough mobs of its species in the radius target the same entity with a clear line of
	 * sight, they all shoot at the same time.
	 */
	record Barrage(int group, float radius) implements GeneEffect {
		public static final MapCodec<Barrage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(2, 32).optionalFieldOf("group", 3).forGetter(Barrage::group),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("radius", 16.0F).forGetter(Barrage::radius)
		).apply(instance, Barrage::new));

		@Override
		public String type() {
			return "barrage";
		}
	}

	/**
	 * When the mob spawns in a natural way, it has a chance to spawn on the back of a mob of the mount type.
	 */
	record Jockey(float chance, EntityType<?> mount) implements GeneEffect {
		public static final MapCodec<Jockey> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Jockey::chance),
				EntityType.CODEC.fieldOf("mount").forGetter(Jockey::mount)
		).apply(instance, Jockey::new));

		@Override
		public String type() {
			return "jockey";
		}
	}

	/**
	 * Multiplies the time that the mob draws its bow before it shoots. The multiplier 1.25 draws 25% slower.
	 */
	record DrawTime(float multiplier) implements GeneEffect {
		public static final MapCodec<DrawTime> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 10.0F).fieldOf("multiplier").forGetter(DrawTime::multiplier)
		).apply(instance, DrawTime::new));

		@Override
		public String type() {
			return "draw_time";
		}
	}

	/**
	 * Each hit freezes the target as powder snow does, for the seconds, after the freeze is full. Leather armor stops
	 * it.
	 */
	record FreezeOnHit(float seconds, float chance, Hits hits) implements GeneEffect {
		public static final MapCodec<FreezeOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 600.0F).fieldOf("seconds").forGetter(FreezeOnHit::seconds),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(FreezeOnHit::chance),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(FreezeOnHit::hits)
		).apply(instance, FreezeOnHit::new));

		@Override
		public String type() {
			return "freeze_on_hit";
		}
	}

	/**
	 * An entity that hits the mob in melee freezes for the seconds.
	 */
	record FreezeAttacker(float seconds) implements GeneEffect {
		public static final MapCodec<FreezeAttacker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 600.0F).fieldOf("seconds").forGetter(FreezeAttacker::seconds)
		).apply(instance, FreezeAttacker::new));

		@Override
		public String type() {
			return "freeze_attacker";
		}
	}

	/**
	 * Players in the radius start to freeze while the state is true. The freeze grows each second by the rate, in
	 * ticks of freeze. The game takes 2 ticks of freeze away each tick, so a rate above 40 makes the freeze grow.
	 */
	record FreezeAura(float radius, int rate, When when) implements GeneEffect {
		public static final MapCodec<FreezeAura> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 32.0F).fieldOf("radius").forGetter(FreezeAura::radius),
				Codec.intRange(1, 1000).optionalFieldOf("rate", 60).forGetter(FreezeAura::rate),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(FreezeAura::when)
		).apply(instance, FreezeAura::new));

		@Override
		public String type() {
			return "freeze_aura";
		}
	}

	/**
	 * When the mob shoots its bow, it also shoots more arrows in a fan, one time in each cooldown, while the state is
	 * true. The arrows include the normal shot.
	 */
	record FanShot(int arrows, float angle, float cooldownSeconds, When when) implements GeneEffect {
		public static final MapCodec<FanShot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(2, 9).optionalFieldOf("arrows", 3).forGetter(FanShot::arrows),
				Codec.floatRange(1.0F, 90.0F).optionalFieldOf("angle", 15.0F).forGetter(FanShot::angle),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(FanShot::cooldownSeconds),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(FanShot::when)
		).apply(instance, FanShot::new));

		@Override
		public String type() {
			return "fan_shot";
		}
	}

	/**
	 * The projectiles of the mob push the target back more. A multiplier of 2 pushes two times as far.
	 */
	record ArrowKnockback(float multiplier) implements GeneEffect {
		public static final MapCodec<ArrowKnockback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 10.0F).fieldOf("multiplier").forGetter(ArrowKnockback::multiplier)
		).apply(instance, ArrowKnockback::new));

		@Override
		public String type() {
			return "arrow_knockback";
		}
	}

	/**
	 * The mob walks on powder snow and does not sink, as with leather boots.
	 */
	record WalkOnPowderSnow() implements GeneEffect {
		public static final MapCodec<WalkOnPowderSnow> CODEC = MapCodec.unit(new WalkOnPowderSnow());

		@Override
		public String type() {
			return "walk_on_powder_snow";
		}
	}

	/**
	 * The arrows of the mob carry a status effect, as a tipped arrow does. A stronger effect of the same kind replaces
	 * a weaker one on the target.
	 */
	record ArrowEffect(Holder<MobEffect> effect, float seconds, int amplifier) implements GeneEffect {
		public static final MapCodec<ArrowEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(ArrowEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(ArrowEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(ArrowEffect::amplifier)
		).apply(instance, ArrowEffect::new));

		@Override
		public String type() {
			return "arrow_effect";
		}
	}

	/**
	 * When the mob dies, the block goes where it died. It goes only in air, and not where a player stands.
	 */
	record DeathBlock(Block block) implements GeneEffect {
		public static final MapCodec<DeathBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(DeathBlock::block)
		).apply(instance, DeathBlock::new));

		@Override
		public String type() {
			return "death_block";
		}
	}

	/**
	 * The hits of the mob damage the armor of a player more. A multiplier of 2 damages it two times as much. With
	 * frozen_only, it acts only on a player that freezes.
	 */
	record ArmorBreak(float multiplier, boolean frozenOnly, Hits hits) implements GeneEffect {
		public static final MapCodec<ArmorBreak> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 10.0F).fieldOf("multiplier").forGetter(ArmorBreak::multiplier),
				Codec.BOOL.optionalFieldOf("frozen_only", false).forGetter(ArmorBreak::frozenOnly),
				Hits.CODEC.optionalFieldOf("hits", Hits.PROJECTILE).forGetter(ArmorBreak::hits)
		).apply(instance, ArmorBreak::new));

		@Override
		public String type() {
			return "armor_break";
		}
	}

	/**
	 * A player in the range that looks at the mob gets the status effect. The look check is the one of the enderman,
	 * with a clear line of sight. It checks each interval.
	 */
	record GazeEffect(Holder<MobEffect> effect, float seconds, int amplifier, float range, float intervalSeconds)
			implements GeneEffect {
		public static final MapCodec<GazeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(GazeEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(GazeEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(GazeEffect::amplifier),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(GazeEffect::range),
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("interval_seconds", 2.0F).forGetter(GazeEffect::intervalSeconds)
		).apply(instance, GazeEffect::new));

		@Override
		public String type() {
			return "gaze_effect";
		}
	}

	/**
	 * When the mob dies, the mobs of its species in the radius target the player that killed it.
	 */
	record CallOnDeath(float radius) implements GeneEffect {
		public static final MapCodec<CallOnDeath> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 128.0F).fieldOf("radius").forGetter(CallOnDeath::radius)
		).apply(instance, CallOnDeath::new));

		@Override
		public String type() {
			return "call_on_death";
		}
	}

	/**
	 * The mob heals by the amount when another mob of its species dies in the radius.
	 */
	record HealOnAllyDeath(float amount, float radius) implements GeneEffect {
		public static final MapCodec<HealOnAllyDeath> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1000.0F).fieldOf("amount").forGetter(HealOnAllyDeath::amount),
				Codec.floatRange(0.0F, 32.0F).fieldOf("radius").forGetter(HealOnAllyDeath::radius)
		).apply(instance, HealOnAllyDeath::new));

		@Override
		public String type() {
			return "heal_on_ally_death";
		}
	}

	/**
	 * Players farther than the blocks do not see the mob while the state is true. It is not the invisibility effect.
	 */
	record HiddenFromAfar(float blocks, When when) implements GeneEffect {
		public static final MapCodec<HiddenFromAfar> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 128.0F).fieldOf("blocks").forGetter(HiddenFromAfar::blocks),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(HiddenFromAfar::when)
		).apply(instance, HiddenFromAfar::new));

		@Override
		public String type() {
			return "hidden_from_afar";
		}
	}

	/**
	 * Players farther than the blocks do not see the arrows of the mob while they fly.
	 */
	record GhostArrows(float blocks) implements GeneEffect {
		public static final MapCodec<GhostArrows> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 128.0F).fieldOf("blocks").forGetter(GhostArrows::blocks)
		).apply(instance, GhostArrows::new));

		@Override
		public String type() {
			return "ghost_arrows";
		}
	}

	/**
	 * Each hit of the mob heals it by the amount, while the state is true.
	 */
	record HealOnHit(float amount, Hits hits, When when) implements GeneEffect {
		public static final MapCodec<HealOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1000.0F).fieldOf("amount").forGetter(HealOnHit::amount),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(HealOnHit::hits),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(HealOnHit::when)
		).apply(instance, HealOnHit::new));

		@Override
		public String type() {
			return "heal_on_hit";
		}
	}

	/**
	 * After a shear, the mushrooms of a bogged grow back after the seconds, and the bogged heals by the amount.
	 */
	record Regrow(float seconds, float heal) implements GeneEffect {
		public static final MapCodec<Regrow> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(Regrow::seconds),
				Codec.floatRange(0.0F, 1000.0F).optionalFieldOf("heal", 0.0F).forGetter(Regrow::heal)
		).apply(instance, Regrow::new));

		@Override
		public String type() {
			return "regrow";
		}
	}

	/**
	 * The mob takes no damage from one melee hit, one time in each cooldown.
	 */
	record Parry(float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<Parry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 6.0F).forGetter(Parry::cooldownSeconds)
		).apply(instance, Parry::new));

		@Override
		public String type() {
			return "parry";
		}
	}

	/**
	 * Each melee hit of the mob also hurts the other players in the radius of the target, with a part of the attack
	 * damage of the mob.
	 */
	record Cleave(float part, float radius) implements GeneEffect {
		public static final MapCodec<Cleave> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("part", 0.5F).forGetter(Cleave::part),
				Codec.floatRange(0.5F, 8.0F).optionalFieldOf("radius", 2.0F).forGetter(Cleave::radius)
		).apply(instance, Cleave::new));

		@Override
		public String type() {
			return "cleave";
		}
	}

	/**
	 * One time in each cooldown, the mob shoots a wither skull at its target player in the range, if it can see it.
	 */
	record SkullShot(float cooldownSeconds, float minRange, float maxRange) implements GeneEffect {
		public static final MapCodec<SkullShot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(SkullShot::cooldownSeconds),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_range", 6.0F).forGetter(SkullShot::minRange),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("max_range", 16.0F).forGetter(SkullShot::maxRange)
		).apply(instance, SkullShot::new));

		@Override
		public String type() {
			return "skull_shot";
		}
	}

	/**
	 * When the mob dies, the count of mobs of the type come out and attack the killer. A vex lives for the seconds.
	 * The new mobs get their genes from the pool of their own species.
	 */
	record DeathSpawn(EntityType<?> entity, float lifeSeconds, int count) implements GeneEffect {
		public static final MapCodec<DeathSpawn> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(DeathSpawn::entity),
				Codec.floatRange(1.0F, 3600.0F).optionalFieldOf("life_seconds", 30.0F).forGetter(DeathSpawn::lifeSeconds),
				Codec.intRange(1, 16).optionalFieldOf("count", 1).forGetter(DeathSpawn::count)
		).apply(instance, DeathSpawn::new));

		public DeathSpawn(EntityType<?> entity, float lifeSeconds) {
			this(entity, lifeSeconds, 1);
		}

		@Override
		public String type() {
			return "death_spawn";
		}
	}

	/**
	 * The mob does more melee damage for each player that it hit in melee in the last seconds, up to the most.
	 */
	record SoulHunger(float perPlayer, float max, float seconds) implements GeneEffect {
		public static final MapCodec<SoulHunger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("per_player").forGetter(SoulHunger::perPlayer),
				Codec.floatRange(0.0F, 10.0F).fieldOf("max").forGetter(SoulHunger::max),
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("seconds", 10.0F).forGetter(SoulHunger::seconds)
		).apply(instance, SoulHunger::new));

		@Override
		public String type() {
			return "soul_hunger";
		}
	}

	/**
	 * The hits of the mob do more damage to entities of the tag, and set them on fire for the seconds.
	 */
	record DamageTo(TagKey<EntityType<?>> targets, float multiplier, float fireSeconds) implements GeneEffect {
		public static final MapCodec<DamageTo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("targets").forGetter(DamageTo::targets),
				Codec.floatRange(0.0F, 100.0F).optionalFieldOf("multiplier", 1.0F).forGetter(DamageTo::multiplier),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("fire_seconds", 0.0F).forGetter(DamageTo::fireSeconds)
		).apply(instance, DamageTo::new));

		@Override
		public String type() {
			return "damage_to";
		}
	}

	/**
	 * Snowballs do this damage to the mob, as they do to a blaze.
	 */
	record SnowballDamage(float damage) implements GeneEffect {
		public static final MapCodec<SnowballDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("damage").forGetter(SnowballDamage::damage)
		).apply(instance, SnowballDamage::new));

		@Override
		public String type() {
			return "snowball_damage";
		}
	}

	/**
	 * Water hurts the mob, as it hurts a blaze or an enderman: splash water, rain, and water.
	 */
	record WaterSensitive() implements GeneEffect {
		public static final MapCodec<WaterSensitive> CODEC = MapCodec.unit(new WaterSensitive());

		@Override
		public String type() {
			return "water_sensitive";
		}
	}

	/**
	 * Each hit of the mob has a chance to make a player drop the item in the main hand. A player loses an item one
	 * time in each cooldown at most. An item with Curse of Binding does not drop.
	 */
	record Disarm(float chance, float cooldownSeconds, Hits hits) implements GeneEffect {
		public static final MapCodec<Disarm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Disarm::chance),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(Disarm::cooldownSeconds),
				Hits.CODEC.optionalFieldOf("hits", Hits.PROJECTILE).forGetter(Disarm::hits)
		).apply(instance, Disarm::new));

		@Override
		public String type() {
			return "disarm";
		}
	}

	/**
	 * Multiplies the radius of the blast of the mob while the state is true. It also multiplies the larger blast of a
	 * charged creeper.
	 */
	record BlastSize(float multiplier, When when) implements GeneEffect {
		public static final MapCodec<BlastSize> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 10.0F).fieldOf("multiplier").forGetter(BlastSize::multiplier),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(BlastSize::when)
		).apply(instance, BlastSize::new));

		@Override
		public String type() {
			return "blast_size";
		}
	}

	/**
	 * Multiplies the time that the fuse of the mob burns before its blast. The fuse of a creeper burns 1.5 seconds in
	 * vanilla.
	 */
	record FuseTime(float multiplier) implements GeneEffect {
		public static final MapCodec<FuseTime> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 10.0F).fieldOf("multiplier").forGetter(FuseTime::multiplier)
		).apply(instance, FuseTime::new));

		@Override
		public String type() {
			return "fuse_time";
		}
	}

	/**
	 * The blast of the mob leaves a cloud that gives a status effect. The cloud has the radius of the blast, and it
	 * shrinks to nothing in the seconds. It comes in addition to the cloud that vanilla leaves.
	 *
	 * @param seconds how long the cloud stays
	 * @param effectSeconds the length of the status effect. With no value, it is the same as the seconds
	 */
	record BlastCloud(Holder<MobEffect> effect, float seconds, int amplifier, Optional<Float> effectSeconds)
			implements GeneEffect {
		public static final MapCodec<BlastCloud> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(BlastCloud::effect),
				Codec.floatRange(0.5F, 600.0F).fieldOf("seconds").forGetter(BlastCloud::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(BlastCloud::amplifier),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("effect_seconds").forGetter(BlastCloud::effectSeconds)
		).apply(instance, BlastCloud::new));

		@Override
		public String type() {
			return "blast_cloud";
		}
	}

	/**
	 * Gives a status effect to each living entity that the blast of the mob damages. A shield that stops the blast
	 * stops the effect.
	 */
	record BlastEffect(Holder<MobEffect> effect, float seconds, int amplifier) implements GeneEffect {
		public static final MapCodec<BlastEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(BlastEffect::effect),
				Codec.floatRange(0.05F, 3600.0F).fieldOf("seconds").forGetter(BlastEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(BlastEffect::amplifier)
		).apply(instance, BlastEffect::new));

		@Override
		public String type() {
			return "blast_effect";
		}
	}

	/**
	 * When the creeper explodes, it lights the fuses of the creepers in the radius whose fuses do not burn yet.
	 * A creeper that a chain reaction lit does not light more creepers.
	 */
	record ChainReaction(float radius) implements GeneEffect {
		public static final MapCodec<ChainReaction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 32.0F).optionalFieldOf("radius", 5.0F).forGetter(ChainReaction::radius)
		).apply(instance, ChainReaction::new));

		@Override
		public String type() {
			return "chain_reaction";
		}
	}

	/**
	 * After the blast of the mob, a second blast goes off at the same place. The second blast does not cause more
	 * aftershocks, a chain reaction, a blast cloud, or blast lightning. The damage effects of the genes and
	 * {@code blast_effect} still act on it, because the mob is still its cause.
	 *
	 * @param seconds the time between the two blasts
	 * @param fraction the radius of the second blast, as a part of the radius of the first blast
	 */
	record Aftershock(float seconds, float fraction) implements GeneEffect {
		public static final MapCodec<Aftershock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 60.0F).optionalFieldOf("seconds", 2.0F).forGetter(Aftershock::seconds),
				Codec.floatRange(0.05F, 4.0F).optionalFieldOf("fraction", 0.5F).forGetter(Aftershock::fraction)
		).apply(instance, Aftershock::new));

		@Override
		public String type() {
			return "aftershock";
		}
	}

	/**
	 * The fuse of the creeper makes no sound: no hiss when it starts, and no sound of the flint and steel that lights
	 * it.
	 */
	record SilentFuse() implements GeneEffect {
		public static final MapCodec<SilentFuse> CODEC = MapCodec.unit(new SilentFuse());

		@Override
		public String type() {
			return "silent_fuse";
		}
	}

	/**
	 * The creeper has a chance to spawn charged, as if lightning hit it.
	 */
	record ChargedSpawn(float chance) implements GeneEffect {
		public static final MapCodec<ChargedSpawn> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(ChargedSpawn::chance)
		).apply(instance, ChargedSpawn::new));

		@Override
		public String type() {
			return "charged_spawn";
		}
	}

	/**
	 * The creeper becomes charged when the state is true, for example in a thunderstorm. It stays charged after.
	 */
	record ChargeWhen(When when) implements GeneEffect {
		public static final MapCodec<ChargeWhen> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.optionalFieldOf("when", When.STORM).forGetter(ChargeWhen::when)
		).apply(instance, ChargeWhen::new));

		@Override
		public String type() {
			return "charge_when";
		}
	}

	/**
	 * When the mob explodes, lightning strikes the place of the blast. This lightning does not charge creepers.
	 */
	record BlastLightning() implements GeneEffect {
		public static final MapCodec<BlastLightning> CODEC = MapCodec.unit(new BlastLightning());

		@Override
		public String type() {
			return "blast_lightning";
		}
	}

	/**
	 * A lightning strike does not hurt the mob and does not set it on fire. A creeper still becomes charged. A fire
	 * block that the lightning makes still burns the mob, as other fire does.
	 */
	record LightningImmunity() implements GeneEffect {
		public static final MapCodec<LightningImmunity> CODEC = MapCodec.unit(new LightningImmunity());

		@Override
		public String type() {
			return "lightning_immunity";
		}
	}

	/**
	 * Each hit of the mob has a chance to put a cobweb at the target. The cobweb goes only in air, and it goes away
	 * after the seconds with no drops.
	 */
	record WebOnHit(float chance, float seconds, Hits hits) implements GeneEffect {
		public static final MapCodec<WebOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(WebOnHit::chance),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds", 30.0F).forGetter(WebOnHit::seconds),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(WebOnHit::hits)
		).apply(instance, WebOnHit::new));

		@Override
		public String type() {
			return "web_on_hit";
		}
	}

	/**
	 * While the mob walks, it leaves a cobweb at its place one time in each interval. The cobweb goes only in air,
	 * and it goes away after the seconds with no drops.
	 */
	record WebTrail(float intervalSeconds, float seconds) implements GeneEffect {
		public static final MapCodec<WebTrail> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("interval_seconds", 10.0F).forGetter(WebTrail::intervalSeconds),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds", 30.0F).forGetter(WebTrail::seconds)
		).apply(instance, WebTrail::new));

		@Override
		public String type() {
			return "web_trail";
		}
	}

	/**
	 * One time in each cooldown, the mob spits silk at its target in the range, if it sees the target. The silk puts
	 * a cobweb at the target. The cobweb goes only in air, and it goes away after the seconds with no drops.
	 */
	record WebShot(float minRange, float range, float cooldownSeconds, float seconds) implements GeneEffect {
		public static final MapCodec<WebShot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 32.0F).optionalFieldOf("min_range", 4.0F).forGetter(WebShot::minRange),
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("range", 12.0F).forGetter(WebShot::range),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 8.0F).forGetter(WebShot::cooldownSeconds),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds", 30.0F).forGetter(WebShot::seconds)
		).apply(instance, WebShot::new));

		@Override
		public String type() {
			return "web_shot";
		}
	}

	/**
	 * The first melee hit of the mob in the seconds after a leap does more damage. A leap is the vanilla leap of a
	 * spider or a wolf, or the leap of a gene.
	 */
	record LeapStrike(float multiplier, float seconds) implements GeneEffect {
		public static final MapCodec<LeapStrike> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(LeapStrike::multiplier),
				Codec.floatRange(0.05F, 60.0F).optionalFieldOf("seconds", 2.0F).forGetter(LeapStrike::seconds)
		).apply(instance, LeapStrike::new));

		@Override
		public String type() {
			return "leap_strike";
		}
	}

	/**
	 * The first hit of the mob on a player does more damage. A hit counts as the first again when the mob did not hit
	 * that player for the reset seconds.
	 */
	record FirstStrike(float multiplier, float resetSeconds) implements GeneEffect {
		public static final MapCodec<FirstStrike> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(FirstStrike::multiplier),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("reset_seconds", 10.0F).forGetter(FirstStrike::resetSeconds)
		).apply(instance, FirstStrike::new));

		@Override
		public String type() {
			return "first_strike";
		}
	}

	/**
	 * The spider attacks players in bright light too. In vanilla, a spider in bright light does not look for a
	 * target, and it stops its attack now and then.
	 */
	record HuntInLight() implements GeneEffect {
		public static final MapCodec<HuntInLight> CODEC = MapCodec.unit(new HuntInLight());

		@Override
		public String type() {
			return "hunt_in_light";
		}
	}

	/**
	 * Changes the Poison that the bite of a cave spider gives in vanilla. The bite gives no Poison on Easy, so the
	 * change does nothing there.
	 *
	 * @param durationMultiplier multiplies the time of the Poison
	 * @param extraLevels adds levels to the Poison. {@code 1} makes Poison I into Poison II
	 */
	record BitePoison(float durationMultiplier, int extraLevels) implements GeneEffect {
		public static final MapCodec<BitePoison> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).optionalFieldOf("duration_multiplier", 1.0F)
						.forGetter(BitePoison::durationMultiplier),
				Codec.intRange(0, 10).optionalFieldOf("extra_levels", 0).forGetter(BitePoison::extraLevels)
		).apply(instance, BitePoison::new));

		@Override
		public String type() {
			return "bite_poison";
		}
	}

	/**
	 * Multiplies the damage of a melee hit from an attacker that holds an item of the tag in its main hand, for
	 * example a pickaxe.
	 */
	record DamageFromItem(TagKey<net.minecraft.world.item.Item> items, float multiplier) implements GeneEffect {
		public static final MapCodec<DamageFromItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TagKey.hashedCodec(Registries.ITEM).fieldOf("items").forGetter(DamageFromItem::items),
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(DamageFromItem::multiplier)
		).apply(instance, DamageFromItem::new));

		@Override
		public String type() {
			return "damage_from_item";
		}
	}

	/**
	 * Each crossbow shot of the mob has this chance to be a firework rocket with one red star, in place of an arrow.
	 * The rocket explodes at the target, as a rocket from the crossbow of a player does. It breaks no blocks.
	 */
	record RocketChance(float chance) implements GeneEffect {
		public static final MapCodec<RocketChance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(RocketChance::chance)
		).apply(instance, RocketChance::new));

		@Override
		public String type() {
			return "rocket_chance";
		}
	}

	/**
	 * One time in each cooldown, the mob shoots a firework rocket at its target in the range, if it sees the target.
	 * It is the rocket of {@code rocket_chance}, and it comes in addition to the normal shots.
	 */
	record RocketShot(float cooldownSeconds, float range) implements GeneEffect {
		public static final MapCodec<RocketShot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(RocketShot::cooldownSeconds),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(RocketShot::range)
		).apply(instance, RocketShot::new));

		@Override
		public String type() {
			return "rocket_shot";
		}
	}

	/**
	 * Multiplies the radius of the blast of the firework rockets of the mob. The radius is 5 blocks in vanilla.
	 */
	record RocketBlast(float multiplier) implements GeneEffect {
		public static final MapCodec<RocketBlast> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 10.0F).fieldOf("multiplier").forGetter(RocketBlast::multiplier)
		).apply(instance, RocketBlast::new));

		@Override
		public String type() {
			return "rocket_blast";
		}
	}

	/**
	 * When the mob spawns in a patrol, this many more mobs with the same genes join the patrol. A raid does not
	 * change.
	 */
	record PatrolSize(int extra) implements GeneEffect {
		public static final MapCodec<PatrolSize> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).optionalFieldOf("extra", 1).forGetter(PatrolSize::extra)
		).apply(instance, PatrolSize::new));

		@Override
		public String type() {
			return "patrol_size";
		}
	}

	/**
	 * The hits of the mob of the kind in {@code hits} push the target back more. {@code 2} pushes two times as far.
	 */
	record Knockback(float multiplier, Hits hits) implements GeneEffect {
		public static final MapCodec<Knockback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(Knockback::multiplier),
				Hits.CODEC.optionalFieldOf("hits", Hits.MELEE).forGetter(Knockback::hits)
		).apply(instance, Knockback::new));

		@Override
		public String type() {
			return "knockback";
		}
	}

	/**
	 * Multiplies the damage of the evoker fangs of the mob. The fangs do 6 damage in vanilla. With {@code targets},
	 * it multiplies only the damage to the mobs of the tag.
	 */
	record FangDamage(float multiplier, Optional<TagKey<EntityType<?>>> targets) implements GeneEffect {
		public static final MapCodec<FangDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(FangDamage::multiplier),
				TagKey.hashedCodec(Registries.ENTITY_TYPE).optionalFieldOf("targets").forGetter(FangDamage::targets)
		).apply(instance, FangDamage::new));

		@Override
		public String type() {
			return "fang_damage";
		}
	}

	/**
	 * Gives a status effect to the target of each hit of the evoker fangs of the mob.
	 */
	record FangEffect(Holder<MobEffect> effect, float seconds, int amplifier, float chance) implements GeneEffect {
		public static final MapCodec<FangEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(FangEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(FangEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(FangEffect::amplifier),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(FangEffect::chance)
		).apply(instance, FangEffect::new));

		@Override
		public String type() {
			return "fang_effect";
		}
	}

	/**
	 * Each hit of the evoker fangs of the mob throws the target up this many blocks.
	 */
	record FangLaunch(float blocks) implements GeneEffect {
		public static final MapCodec<FangLaunch> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 10.0F).optionalFieldOf("blocks", 1.0F).forGetter(FangLaunch::blocks)
		).apply(instance, FangLaunch::new));

		@Override
		public String type() {
			return "fang_launch";
		}
	}

	/**
	 * Each hit of the evoker fangs of the mob heals the mob this much health.
	 */
	record FangHeal(float amount) implements GeneEffect {
		public static final MapCodec<FangHeal> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("amount").forGetter(FangHeal::amount)
		).apply(instance, FangHeal::new));

		@Override
		public String type() {
			return "fang_heal";
		}
	}

	/**
	 * Multiplies the time between the spells of an evoker: the cast interval of the fang spell and of the vex spell.
	 * The fang spell waits 5 seconds and the vex spell 17 seconds in vanilla. {@code 0.7} makes the time 30% less.
	 */
	record SpellInterval(float multiplier) implements GeneEffect {
		public static final MapCodec<SpellInterval> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(SpellInterval::multiplier)
		).apply(instance, SpellInterval::new));

		@Override
		public String type() {
			return "spell_interval";
		}
	}

	/**
	 * Sets the number of fangs in the line of the fang spell of an evoker. The line has 16 fangs in vanilla, one each
	 * 1.25 blocks. With more than one effect, the largest number counts.
	 */
	record FangLine(int fangs) implements GeneEffect {
		public static final MapCodec<FangLine> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 64).fieldOf("fangs").forGetter(FangLine::fangs)
		).apply(instance, FangLine::new));

		@Override
		public String type() {
			return "fang_line";
		}
	}

	/**
	 * Sets the range in blocks in which an evoker casts the ring of fangs in place of the line. The range is 3 blocks
	 * in vanilla. With more than one effect, the largest range counts.
	 */
	record FangRing(float range) implements GeneEffect {
		public static final MapCodec<FangRing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 16.0F).fieldOf("range").forGetter(FangRing::range)
		).apply(instance, FangRing::new));

		@Override
		public String type() {
			return "fang_ring";
		}
	}

	/**
	 * Sets the number of vexes that the vex spell of an evoker summons. The spell summons 3 vexes in vanilla. With more
	 * than one effect, the largest number counts.
	 */
	record VexCount(int count) implements GeneEffect {
		public static final MapCodec<VexCount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(0, 16).fieldOf("count").forGetter(VexCount::count)
		).apply(instance, VexCount::new));

		@Override
		public String type() {
			return "vex_count";
		}
	}

	/**
	 * Multiplies the maximum health of the vexes that the mob summons. A vex has 14 health in vanilla.
	 */
	record VexHealth(float multiplier) implements GeneEffect {
		public static final MapCodec<VexHealth> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(VexHealth::multiplier)
		).apply(instance, VexHealth::new));

		@Override
		public String type() {
			return "vex_health";
		}
	}

	/**
	 * Multiplies the attack damage of the vexes that the mob summons.
	 */
	record VexDamage(float multiplier) implements GeneEffect {
		public static final MapCodec<VexDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 20.0F).fieldOf("multiplier").forGetter(VexDamage::multiplier)
		).apply(instance, VexDamage::new));

		@Override
		public String type() {
			return "vex_damage";
		}
	}

	/**
	 * Multiplies the life of the vexes that the mob summons. A vex lives 30 to 119 seconds in vanilla.
	 */
	record VexLife(float multiplier) implements GeneEffect {
		public static final MapCodec<VexLife> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(VexLife::multiplier)
		).apply(instance, VexLife::new));

		@Override
		public String type() {
			return "vex_life";
		}
	}

	/**
	 * Gives a status effect to the target of each hit of a vex that the mob summoned.
	 */
	record VexEffect(Holder<MobEffect> effect, float seconds, int amplifier, float chance) implements GeneEffect {
		public static final MapCodec<VexEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(VexEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(VexEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(VexEffect::amplifier),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(VexEffect::chance)
		).apply(instance, VexEffect::new));

		@Override
		public String type() {
			return "vex_effect";
		}
	}

	/**
	 * Each hit of a vex that the mob summoned heals the mob this much health.
	 */
	record VexHeal(float amount) implements GeneEffect {
		public static final MapCodec<VexHeal> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("amount").forGetter(VexHeal::amount)
		).apply(instance, VexHeal::new));

		@Override
		public String type() {
			return "vex_heal";
		}
	}

	/**
	 * One time in each cooldown, the mob gives its target a status effect, if the target is in the range and the mob
	 * sees it. It is a spell: it throws no item, and it shows the spell particles of the evoker.
	 */
	record Hex(Holder<MobEffect> effect, float seconds, int amplifier, float range, float cooldownSeconds)
			implements GeneEffect {
		public static final MapCodec<Hex> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(Hex::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(Hex::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(Hex::amplifier),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(Hex::range),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 15.0F).forGetter(Hex::cooldownSeconds)
		).apply(instance, Hex::new));

		@Override
		public String type() {
			return "hex";
		}
	}

	/**
	 * When an attacker hurts the mob, the mob summons vexes that attack the attacker, one time in each cooldown. The
	 * vexes live as long as the vexes of the vex spell, and the vex effects of the mob change them.
	 */
	record Decoy(int count, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<Decoy> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).optionalFieldOf("count", 1).forGetter(Decoy::count),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 20.0F).forGetter(Decoy::cooldownSeconds)
		).apply(instance, Decoy::new));

		@Override
		public String type() {
			return "decoy";
		}
	}

	/**
	 * Multiplies the time of the effects of the potions that the mob throws at a target. An effect with no time, such
	 * as Harming or Healing, does not change. More than one effect multiplies together.
	 */
	record PotionDuration(float multiplier) implements GeneEffect {
		public static final MapCodec<PotionDuration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).fieldOf("multiplier").forGetter(PotionDuration::multiplier)
		).apply(instance, PotionDuration::new));

		@Override
		public String type() {
			return "potion_duration";
		}
	}

	/**
	 * Makes the effects of the potions that the mob throws at a target this many levels stronger. An effect with no
	 * time, such as Harming, does not change.
	 */
	record PotionLevel(int levels) implements GeneEffect {
		public static final MapCodec<PotionLevel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 10).optionalFieldOf("levels", 1).forGetter(PotionLevel::levels)
		).apply(instance, PotionLevel::new));

		@Override
		public String type() {
			return "potion_level";
		}
	}

	/**
	 * In the potions that the mob throws at a target, the effect {@code to} takes the place of the effect
	 * {@code from}. The new effect keeps the time and the level of the old one, or it lasts {@code seconds}.
	 */
	record PotionSwap(Holder<MobEffect> from, Holder<MobEffect> to, Optional<Float> seconds) implements GeneEffect {
		public static final MapCodec<PotionSwap> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("from").forGetter(PotionSwap::from),
				MobEffect.CODEC.fieldOf("to").forGetter(PotionSwap::to),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds").forGetter(PotionSwap::seconds)
		).apply(instance, PotionSwap::new));

		@Override
		public String type() {
			return "potion_swap";
		}
	}

	/**
	 * A potion that the mob throws at a target and that has the effect {@code with} also gives the effect
	 * {@code effect}, for example Wither with Harming.
	 */
	record PotionExtra(Holder<MobEffect> with, Holder<MobEffect> effect, float seconds, int amplifier)
			implements GeneEffect {
		public static final MapCodec<PotionExtra> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("with").forGetter(PotionExtra::with),
				MobEffect.CODEC.fieldOf("effect").forGetter(PotionExtra::effect),
				Codec.floatRange(0.05F, 3600.0F).fieldOf("seconds").forGetter(PotionExtra::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(PotionExtra::amplifier)
		).apply(instance, PotionExtra::new));

		@Override
		public String type() {
			return "potion_extra";
		}
	}

	/**
	 * The potions that the mob throws at a target are lingering potions. They leave a cloud, as the lingering potion
	 * of a player does.
	 */
	record PotionLingering() implements GeneEffect {
		public static final MapCodec<PotionLingering> CODEC = MapCodec.unit(new PotionLingering());

		@Override
		public String type() {
			return "potion_lingering";
		}
	}

	/**
	 * The mob throws this many potions at a target in one throw. The potions fly a little apart, {@code spread_degrees}
	 * from each other. With more than one effect, the largest number counts.
	 */
	record PotionCount(int count, float spreadDegrees) implements GeneEffect {
		public static final MapCodec<PotionCount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).optionalFieldOf("count", 2).forGetter(PotionCount::count),
				Codec.floatRange(0.0F, 45.0F).optionalFieldOf("spread_degrees", 8.0F).forGetter(PotionCount::spreadDegrees)
		).apply(instance, PotionCount::new));

		@Override
		public String type() {
			return "potion_count";
		}
	}

	/**
	 * Multiplies the radius of the splash of the potions that the mob throws, and the radius of the cloud of its
	 * lingering potions. The splash reaches 4 blocks and the cloud starts at 3 blocks in vanilla.
	 */
	record PotionSplash(float multiplier) implements GeneEffect {
		public static final MapCodec<PotionSplash> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 5.0F).fieldOf("multiplier").forGetter(PotionSplash::multiplier)
		).apply(instance, PotionSplash::new));

		@Override
		public String type() {
			return "potion_splash";
		}
	}

	/**
	 * Multiplies the damage of the potions that the mob throws, for example the damage of Harming. With
	 * {@code targets}, it multiplies only the damage to the mobs of the tag.
	 */
	record PotionDamage(float multiplier, Optional<TagKey<EntityType<?>>> targets) implements GeneEffect {
		public static final MapCodec<PotionDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(PotionDamage::multiplier),
				TagKey.hashedCodec(Registries.ENTITY_TYPE).optionalFieldOf("targets").forGetter(PotionDamage::targets)
		).apply(instance, PotionDamage::new));

		@Override
		public String type() {
			return "potion_damage";
		}
	}

	/**
	 * A potion that the mob throws at a target and that breaks on a living entity does this much damage to it.
	 */
	record PotionShards(float damage) implements GeneEffect {
		public static final MapCodec<PotionShards> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("damage").forGetter(PotionShards::damage)
		).apply(instance, PotionShards::new));

		@Override
		public String type() {
			return "potion_shards";
		}
	}

	/**
	 * A potion that the mob throws at a target and that breaks on a living entity heals the mob this much health.
	 */
	record PotionHeal(float amount) implements GeneEffect {
		public static final MapCodec<PotionHeal> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("amount").forGetter(PotionHeal::amount)
		).apply(instance, PotionHeal::new));

		@Override
		public String type() {
			return "potion_heal";
		}
	}

	/**
	 * A witch drinks the potion when the state is true, one time in each cooldown. It does not drink when it already
	 * has each effect of the potion. A drink takes the vanilla drink time, and the witch is slow while it drinks. With
	 * {@code seconds}, the effects of the potion last this long.
	 */
	record Drink(Holder<net.minecraft.world.item.alchemy.Potion> potion, When when, float cooldownSeconds,
			Optional<Float> seconds) implements GeneEffect {
		public static final MapCodec<Drink> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				net.minecraft.world.item.alchemy.Potion.CODEC.fieldOf("potion").forGetter(Drink::potion),
				When.CODEC.fieldOf("when").forGetter(Drink::when),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("cooldown_seconds", 0.0F).forGetter(Drink::cooldownSeconds),
				Codec.floatRange(0.05F, 3600.0F).optionalFieldOf("seconds").forGetter(Drink::seconds)
		).apply(instance, Drink::new));

		@Override
		public String type() {
			return "drink";
		}
	}

	/**
	 * When a witch drinks a potion, the monsters in the radius get the same effects. Each drink counts, also a vanilla
	 * drink.
	 */
	record SharedDrink(float radius) implements GeneEffect {
		public static final MapCodec<SharedDrink> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 32.0F).optionalFieldOf("radius", 4.0F).forGetter(SharedDrink::radius)
		).apply(instance, SharedDrink::new));

		@Override
		public String type() {
			return "shared_drink";
		}
	}

	/**
	 * One time in each cooldown, the mob throws a splash potion of Healing at the nearest hurt monster in the radius
	 * that it sees. It does not throw at its own target. An undead monster gets a potion of Harming, which heals it.
	 */
	record HealThrow(float radius, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<HealThrow> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 32.0F).optionalFieldOf("radius", 8.0F).forGetter(HealThrow::radius),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 15.0F).forGetter(HealThrow::cooldownSeconds)
		).apply(instance, HealThrow::new));

		@Override
		public String type() {
			return "heal_throw";
		}
	}

	/**
	 * One time in each cooldown, while the state is true, the mob teleports to a free place next to its target. It
	 * does this only when the target is farther than {@code min_distance}. If it finds no free place, it does not
	 * teleport, and the cooldown does not start.
	 */
	record TeleportToTarget(float cooldownSeconds, When when, float minDistance) implements GeneEffect {
		public static final MapCodec<TeleportToTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 6.0F)
						.forGetter(TeleportToTarget::cooldownSeconds),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(TeleportToTarget::when),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_distance", 4.0F).forGetter(TeleportToTarget::minDistance)
		).apply(instance, TeleportToTarget::new));

		@Override
		public String type() {
			return "teleport_to_target";
		}
	}

	/**
	 * After a melee hit, the mob teleports to a free place behind its target, one time in each cooldown. If it finds
	 * no free place, it does not teleport, and the cooldown does not start.
	 */
	record TeleportBehind(float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<TeleportBehind> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F)
						.forGetter(TeleportBehind::cooldownSeconds)
		).apply(instance, TeleportBehind::new));

		@Override
		public String type() {
			return "teleport_behind";
		}
	}

	/**
	 * One time in the life of the mob, when its health falls under the threshold, it teleports to a random place in
	 * the range and heals this much health.
	 *
	 * @param threshold a part of the maximum health, for example {@code 0.25}
	 */
	record EscapeTeleport(float threshold, float heal, float range) implements GeneEffect {
		public static final MapCodec<EscapeTeleport> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("threshold", 0.25F).forGetter(EscapeTeleport::threshold),
				Codec.floatRange(0.0F, 1024.0F).optionalFieldOf("heal", 10.0F).forGetter(EscapeTeleport::heal),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(EscapeTeleport::range)
		).apply(instance, EscapeTeleport::new));

		@Override
		public String type() {
			return "escape_teleport";
		}
	}

	/**
	 * When an explosion hurts the mob, the mob teleports to a random place in the range and takes no damage, one time
	 * in each cooldown. If it finds no free place, it takes the damage, and the cooldown does not start.
	 */
	record BlastTeleport(float cooldownSeconds, float range) implements GeneEffect {
		public static final MapCodec<BlastTeleport> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 20.0F).forGetter(BlastTeleport::cooldownSeconds),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(BlastTeleport::range)
		).apply(instance, BlastTeleport::new));

		@Override
		public String type() {
			return "blast_teleport";
		}
	}

	/**
	 * While the state is true, each random teleport of an enderman goes to a free place next to its target. The
	 * random teleports are the teleports away from water, from a projectile, and from the daylight. With no target or
	 * no free place, the enderman teleports to a random place, as in vanilla.
	 */
	record AimedTeleport(When when) implements GeneEffect {
		public static final MapCodec<AimedTeleport> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(AimedTeleport::when)
		).apply(instance, AimedTeleport::new));

		@Override
		public String type() {
			return "aimed_teleport";
		}
	}

	/**
	 * The first melee hit of the mob in {@code seconds} after a teleport does more damage. Each teleport counts: the
	 * vanilla teleports of an enderman and the teleports of the genes.
	 */
	record TeleportStrike(float multiplier, float seconds) implements GeneEffect {
		public static final MapCodec<TeleportStrike> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(TeleportStrike::multiplier),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("seconds", 5.0F).forGetter(TeleportStrike::seconds)
		).apply(instance, TeleportStrike::new));

		@Override
		public String type() {
			return "teleport_strike";
		}
	}

	/**
	 * A player that stares at the face of the mob gets the status effect. The stare is the vanilla stare test of the
	 * enderman, in the follow range of the mob. A carved pumpkin on the head of the player stops it.
	 */
	record StareEffect(Holder<MobEffect> effect, float seconds, int amplifier) implements GeneEffect {
		public static final MapCodec<StareEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(StareEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(StareEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(StareEffect::amplifier)
		).apply(instance, StareEffect::new));

		@Override
		public String type() {
			return "stare_effect";
		}
	}

	/**
	 * Multiplies the distance in which the mob sees a stare. The distance is the follow range of the mob, 64 blocks
	 * for an enderman. It changes the anger of an enderman and the effects of {@code stare_effect}.
	 */
	record StareRange(float multiplier) implements GeneEffect {
		public static final MapCodec<StareRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 10.0F).fieldOf("multiplier").forGetter(StareRange::multiplier)
		).apply(instance, StareRange::new));

		@Override
		public String type() {
			return "stare_range";
		}
	}

	/**
	 * A carved pumpkin on the head of a player does not stop the stare at the mob. The anger of an enderman and the
	 * effects of {@code stare_effect} work as with no pumpkin.
	 */
	record StareThroughPumpkin() implements GeneEffect {
		public static final MapCodec<StareThroughPumpkin> CODEC = MapCodec.unit(new StareThroughPumpkin());

		@Override
		public String type() {
			return "stare_through_pumpkin";
		}
	}

	/**
	 * One time in each cooldown, an enderman throws the block that it carries at its target, if the target is in the
	 * range and the enderman sees it. The thrown block is a falling block. It does this damage to the entity that it
	 * hits. Where it lands, it puts the block down, if the {@code mobGriefing} gamerule is on. Else it drops nothing.
	 * The throw rate of the genes changes the cooldown.
	 */
	record BlockThrow(float damage, float minRange, float range, float cooldownSeconds) implements GeneEffect {
		public static final MapCodec<BlockThrow> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).optionalFieldOf("damage", 4.0F).forGetter(BlockThrow::damage),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_range", 4.0F).forGetter(BlockThrow::minRange),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 12.0F).forGetter(BlockThrow::range),
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 8.0F).forGetter(BlockThrow::cooldownSeconds)
		).apply(instance, BlockThrow::new));

		@Override
		public String type() {
			return "block_throw";
		}
	}

	/**
	 * Multiplies the damage of the blocks that the mob throws with {@code block_throw}. Alone it does nothing.
	 */
	record ThrownBlockDamage(float multiplier) implements GeneEffect {
		public static final MapCodec<ThrownBlockDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(ThrownBlockDamage::multiplier)
		).apply(instance, ThrownBlockDamage::new));

		@Override
		public String type() {
			return "thrown_block_damage";
		}
	}

	/**
	 * A block that the mob throws with {@code block_throw} gives a status effect to the entity that it hits, if the
	 * state of the mob is true at the hit. Alone it does nothing.
	 */
	record ThrownBlockEffect(Holder<MobEffect> effect, float seconds, int amplifier, When when) implements GeneEffect {
		public static final MapCodec<ThrownBlockEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(ThrownBlockEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(ThrownBlockEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(ThrownBlockEffect::amplifier),
				When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(ThrownBlockEffect::when)
		).apply(instance, ThrownBlockEffect::new));

		@Override
		public String type() {
			return "thrown_block_effect";
		}
	}

	/**
	 * A block that the mob throws with {@code block_throw} and that a shield stops makes the shield useless for this
	 * many seconds. Alone it does nothing.
	 */
	record ThrownBlockShield(float seconds) implements GeneEffect {
		public static final MapCodec<ThrownBlockShield> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).fieldOf("seconds").forGetter(ThrownBlockShield::seconds)
		).apply(instance, ThrownBlockShield::new));

		@Override
		public String type() {
			return "thrown_block_shield";
		}
	}

	/**
	 * An enderman can also pick up the blocks of the tag, as it picks up the vanilla blocks. It obeys the
	 * {@code mobGriefing} gamerule, as in vanilla.
	 */
	record PickupBlocks(TagKey<Block> blocks) implements GeneEffect {
		public static final MapCodec<PickupBlocks> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TagKey.hashedCodec(Registries.BLOCK).fieldOf("blocks").forGetter(PickupBlocks::blocks)
		).apply(instance, PickupBlocks::new));

		@Override
		public String type() {
			return "pickup_blocks";
		}
	}

	/**
	 * Each melee hit of the mob has this chance to teleport the target to a random place in the range, as a chorus
	 * fruit does.
	 */
	record TeleportTargetOnHit(float chance, float range) implements GeneEffect {
		public static final MapCodec<TeleportTargetOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(TeleportTargetOnHit::chance),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 8.0F).forGetter(TeleportTargetOnHit::range)
		).apply(instance, TeleportTargetOnHit::new));

		@Override
		public String type() {
			return "teleport_target_on_hit";
		}
	}

	/**
	 * Multiplies the damage that water does to a mob that water hurts, such as the enderman: the damage of water and
	 * rain, and the damage of a splash potion of water. With {@code splash_only}, it changes only the damage of the
	 * splash potion.
	 */
	record WaterDamageTaken(float multiplier, boolean splashOnly) implements GeneEffect {
		public static final MapCodec<WaterDamageTaken> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(WaterDamageTaken::multiplier),
				Codec.BOOL.optionalFieldOf("splash_only", false).forGetter(WaterDamageTaken::splashOnly)
		).apply(instance, WaterDamageTaken::new));

		@Override
		public String type() {
			return "water_damage_taken";
		}
	}

	/**
	 * A thrown ender pearl makes an endermite with this gene with this chance, not with the vanilla 5%. The game
	 * first rolls the genes of the endermite, and then the chance of the endermite.
	 */
	record PearlChance(float chance) implements GeneEffect {
		public static final MapCodec<PearlChance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(PearlChance::chance)
		).apply(instance, PearlChance::new));

		@Override
		public String type() {
			return "pearl_chance";
		}
	}

	/**
	 * Sets the life of an endermite. Vanilla gives it 2 minutes, and an endermite with a name or a persistent one does
	 * not die of age. With {@code seconds}, it lives this long. Else the multiplier changes the vanilla life. With
	 * {@code endless}, it does not die of age. With more than one effect, the longest life counts.
	 */
	record Lifespan(Optional<Float> seconds, float multiplier, boolean endless) implements GeneEffect {
		public static final MapCodec<Lifespan> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 86400.0F).optionalFieldOf("seconds").forGetter(Lifespan::seconds),
				Codec.floatRange(0.05F, 100.0F).optionalFieldOf("multiplier", 1.0F).forGetter(Lifespan::multiplier),
				Codec.BOOL.optionalFieldOf("endless", false).forGetter(Lifespan::endless)
		).apply(instance, Lifespan::new));

		@Override
		public String type() {
			return "lifespan";
		}
	}

	/**
	 * Each melee hit of an endermite adds this many seconds to its life.
	 */
	record LifeOnHit(float seconds) implements GeneEffect {
		public static final MapCodec<LifeOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 3600.0F).fieldOf("seconds").forGetter(LifeOnHit::seconds)
		).apply(instance, LifeOnHit::new));

		@Override
		public String type() {
			return "life_on_hit";
		}
	}

	/**
	 * When an endermite dies of age, this many endermites come out. They get genes from the pool, as new mobs.
	 */
	record AgeSpawn(int count) implements GeneEffect {
		public static final MapCodec<AgeSpawn> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 8).optionalFieldOf("count", 2).forGetter(AgeSpawn::count)
		).apply(instance, AgeSpawn::new));

		@Override
		public String type() {
			return "age_spawn";
		}
	}

	/**
	 * When a projectile hurts the mob, the mob teleports to a random place in the range, one time in each cooldown.
	 * If it finds no free place, it does not teleport, and the cooldown does not start.
	 */
	record ProjectileTeleport(float cooldownSeconds, float range) implements GeneEffect {
		public static final MapCodec<ProjectileTeleport> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 8.0F)
						.forGetter(ProjectileTeleport::cooldownSeconds),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(ProjectileTeleport::range)
		).apply(instance, ProjectileTeleport::new));

		@Override
		public String type() {
			return "projectile_teleport";
		}
	}

	/**
	 * When the mob hits a player, the mobs of the list in the radius that have no target attack that player. A mob
	 * that already attacks another target does not change its target.
	 */
	record CallOthers(HolderSet<EntityType<?>> mobs, float radius) implements GeneEffect {
		public static final MapCodec<CallOthers> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("mobs").forGetter(CallOthers::mobs),
				Codec.floatRange(0.5F, 64.0F).optionalFieldOf("radius", 16.0F).forGetter(CallOthers::radius)
		).apply(instance, CallOthers::new));

		@Override
		public String type() {
			return "call_others";
		}
	}

	/**
	 * When a cube spawns in a natural way or from a spawner, it has this size with this chance. Vanilla gives the
	 * sizes 1, 2, and 4 the same chance. The other natural sizes share the rest of the chance.
	 */
	record SpawnSize(int size, float chance) implements GeneEffect {
		public static final MapCodec<SpawnSize> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 4).optionalFieldOf("size", 4).forGetter(SpawnSize::size),
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(SpawnSize::chance)
		).apply(instance, SpawnSize::new));

		@Override
		public String type() {
			return "spawn_size";
		}
	}

	/**
	 * Changes the hop of a cube. The wait multiplier changes the wait between two hops: {@code 0.77} hops 30% more
	 * often. The distance multiplier changes the speed in the air, so the hop goes farther. The height multiplier
	 * changes the speed up at the start of the hop. More than one effect multiplies together.
	 */
	record Hop(float waitMultiplier, float distanceMultiplier, float heightMultiplier) implements GeneEffect {
		public static final MapCodec<Hop> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 20.0F).optionalFieldOf("wait_multiplier", 1.0F).forGetter(Hop::waitMultiplier),
				Codec.floatRange(0.05F, 10.0F).optionalFieldOf("distance_multiplier", 1.0F).forGetter(Hop::distanceMultiplier),
				Codec.floatRange(0.05F, 10.0F).optionalFieldOf("height_multiplier", 1.0F).forGetter(Hop::heightMultiplier)
		).apply(instance, Hop::new));

		@Override
		public String type() {
			return "hop";
		}
	}

	/**
	 * When a cube lands from a hop, it does this damage to the players in the radius.
	 */
	record LandingDamage(float damage, float radius) implements GeneEffect {
		public static final MapCodec<LandingDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 100.0F).fieldOf("damage").forGetter(LandingDamage::damage),
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("radius", 2.0F).forGetter(LandingDamage::radius)
		).apply(instance, LandingDamage::new));

		@Override
		public String type() {
			return "landing_damage";
		}
	}

	/**
	 * When a cube lands from a hop, the players in the radius get the status effect.
	 */
	record LandingEffect(Holder<MobEffect> effect, float seconds, int amplifier, float radius) implements GeneEffect {
		public static final MapCodec<LandingEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(LandingEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(LandingEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(LandingEffect::amplifier),
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("radius", 2.0F).forGetter(LandingEffect::radius)
		).apply(instance, LandingEffect::new));

		@Override
		public String type() {
			return "landing_effect";
		}
	}

	/**
	 * When a cube lands from a hop, it damages each armor piece of the players in the radius by this amount.
	 */
	record LandingArmorDamage(int amount, float radius) implements GeneEffect {
		public static final MapCodec<LandingArmorDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 1000).fieldOf("amount").forGetter(LandingArmorDamage::amount),
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("radius", 2.0F).forGetter(LandingArmorDamage::radius)
		).apply(instance, LandingArmorDamage::new));

		@Override
		public String type() {
			return "landing_armor_damage";
		}
	}

	/**
	 * When a cube lands on a player that holds up a shield, the shield is useless for this many seconds. The cube
	 * lands on a player when the player touches the cube at the landing.
	 */
	record LandingShieldStop(float seconds) implements GeneEffect {
		public static final MapCodec<LandingShieldStop> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).fieldOf("seconds").forGetter(LandingShieldStop::seconds)
		).apply(instance, LandingShieldStop::new));

		@Override
		public String type() {
			return "landing_shield_stop";
		}
	}

	/**
	 * Changes the number of pieces when a cube splits. Vanilla makes 2 to 4 pieces. With {@code count}, the split
	 * always makes this many. Else the split makes {@code extra} more than vanilla. With more than one effect, the
	 * fixed count counts first, and the extras add up.
	 */
	record SplitCount(Optional<Integer> count, int extra) implements GeneEffect {
		public static final MapCodec<SplitCount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.intRange(1, 16).optionalFieldOf("count").forGetter(SplitCount::count),
				Codec.intRange(0, 16).optionalFieldOf("extra", 0).forGetter(SplitCount::extra)
		).apply(instance, SplitCount::new));

		@Override
		public String type() {
			return "split_count";
		}
	}

	/**
	 * When a cube splits, its pieces get the status effect.
	 */
	record SplitEffect(Holder<MobEffect> effect, float seconds, int amplifier) implements GeneEffect {
		public static final MapCodec<SplitEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(SplitEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).fieldOf("seconds").forGetter(SplitEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(SplitEffect::amplifier)
		).apply(instance, SplitEffect::new));

		@Override
		public String type() {
			return "split_effect";
		}
	}

	/**
	 * When a cube splits, its pieces jump about this many blocks: away from the place of the cube, or with
	 * {@code toward_killer}, toward the entity that killed it.
	 */
	record SplitJump(float blocks, boolean towardKiller) implements GeneEffect {
		public static final MapCodec<SplitJump> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("blocks", 3.0F).forGetter(SplitJump::blocks),
				Codec.BOOL.optionalFieldOf("toward_killer", false).forGetter(SplitJump::towardKiller)
		).apply(instance, SplitJump::new));

		@Override
		public String type() {
			return "split_jump";
		}
	}

	/**
	 * When a cube splits, the players in the radius get the status effect, or burn for {@code fire_seconds}, or both.
	 */
	record SplitAreaEffect(Optional<Holder<MobEffect>> effect, float seconds, int amplifier, float fireSeconds,
			float radius) implements GeneEffect {
		public static final MapCodec<SplitAreaEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.optionalFieldOf("effect").forGetter(SplitAreaEffect::effect),
				Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("seconds", 0.0F).forGetter(SplitAreaEffect::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(SplitAreaEffect::amplifier),
				Codec.floatRange(0.0F, 600.0F).optionalFieldOf("fire_seconds", 0.0F).forGetter(SplitAreaEffect::fireSeconds),
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("radius", 3.0F).forGetter(SplitAreaEffect::radius)
		).apply(instance, SplitAreaEffect::new));

		@Override
		public String type() {
			return "split_area_effect";
		}
	}

	/**
	 * A cube of size 1 does damage with its touch attack. A slime of size 1 does no damage in vanilla. The damage is
	 * the attack damage of the cube, 1 at size 1.
	 */
	record TinyDamage() implements GeneEffect {
		public static final MapCodec<TinyDamage> CODEC = MapCodec.unit(new TinyDamage());

		@Override
		public String type() {
			return "tiny_damage";
		}
	}

	/**
	 * After this many seconds out of combat, a cube grows one size: 1 to 2, then 2 to 4, up to {@code max_size}. It
	 * heals to the health of its new size.
	 */
	record Grow(float seconds, int maxSize) implements GeneEffect {
		public static final MapCodec<Grow> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 3600.0F).optionalFieldOf("seconds", 60.0F).forGetter(Grow::seconds),
				Codec.intRange(2, 8).optionalFieldOf("max_size", 4).forGetter(Grow::maxSize)
		).apply(instance, Grow::new));

		@Override
		public String type() {
			return "grow";
		}
	}

	/**
	 * Each this many seconds out of combat, a new cube of size 1 buds off the cube. The bud has the genes of the cube,
	 * but it makes no buds of its own.
	 */
	record Bud(float seconds) implements GeneEffect {
		public static final MapCodec<Bud> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 3600.0F).optionalFieldOf("seconds", 30.0F).forGetter(Bud::seconds)
		).apply(instance, Bud::new));

		@Override
		public String type() {
			return "bud";
		}
	}

	/**
	 * The touch attack of a cube reaches this many blocks farther. A cube hits its target in the reach, also when the
	 * target does not touch it. More than one effect adds up.
	 */
	record TouchReach(float blocks) implements GeneEffect {
		public static final MapCodec<TouchReach> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 8.0F).optionalFieldOf("blocks", 1.0F).forGetter(TouchReach::blocks)
		).apply(instance, TouchReach::new));

		@Override
		public String type() {
			return "touch_reach";
		}
	}

	/**
	 * A touch hit of a cube on a player that holds up a shield makes the shield useless for this many seconds. The
	 * shield stops the hit, as in vanilla.
	 */
	record ShieldBreak(float seconds) implements GeneEffect {
		public static final MapCodec<ShieldBreak> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 5.0F).forGetter(ShieldBreak::seconds)
		).apply(instance, ShieldBreak::new));

		@Override
		public String type() {
			return "shield_break";
		}
	}

	/**
	 * After a melee hit, the mob hops about this many blocks back, away from its target. The largest number counts.
	 */
	record HopBack(float blocks) implements GeneEffect {
		public static final MapCodec<HopBack> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 8.0F).optionalFieldOf("blocks", 3.0F).forGetter(HopBack::blocks)
		).apply(instance, HopBack::new));

		@Override
		public String type() {
			return "hop_back";
		}
	}

	/**
	 * An attacker that hits the mob in melee burns for this many seconds.
	 */
	record FireOnHurt(float seconds) implements GeneEffect {
		public static final MapCodec<FireOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 3.0F).forGetter(FireOnHurt::seconds)
		).apply(instance, FireOnHurt::new));

		@Override
		public String type() {
			return "fire_on_hurt";
		}
	}

	/**
	 * For {@code seconds} after the mob leaves lava, its melee hits set the target on fire for {@code fire_seconds}.
	 */
	record LavaExitFire(float fireSeconds, float seconds) implements GeneEffect {
		public static final MapCodec<LavaExitFire> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("fire_seconds", 8.0F).forGetter(LavaExitFire::fireSeconds),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 10.0F).forGetter(LavaExitFire::seconds)
		).apply(instance, LavaExitFire::new));

		@Override
		public String type() {
			return "lava_exit_fire";
		}
	}

	/**
	 * One time in each interval, the players in the radius burn for the seconds.
	 */
	record FireAura(float seconds, float radius, float intervalSeconds) implements GeneEffect {
		public static final MapCodec<FireAura> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 2.0F).forGetter(FireAura::seconds),
				Codec.floatRange(0.5F, 16.0F).optionalFieldOf("radius", 2.0F).forGetter(FireAura::radius),
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("interval_seconds", 5.0F).forGetter(FireAura::intervalSeconds)
		).apply(instance, FireAura::new));

		@Override
		public String type() {
			return "fire_aura";
		}
	}

	/**
	 * One time in each cooldown, the mob shoots a small fireball at its target, if the target is between the two
	 * ranges and the mob sees it. It is the fireball of a blaze.
	 */
	record FireballShot(float cooldownSeconds, float minRange, float range) implements GeneEffect {
		public static final MapCodec<FireballShot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.5F, 600.0F).optionalFieldOf("cooldown_seconds", 10.0F).forGetter(FireballShot::cooldownSeconds),
				Codec.floatRange(0.0F, 64.0F).optionalFieldOf("min_range", 6.0F).forGetter(FireballShot::minRange),
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("range", 16.0F).forGetter(FireballShot::range)
		).apply(instance, FireballShot::new));

		@Override
		public String type() {
			return "fireball_shot";
		}
	}

	/**
	 * An explosion does no damage to the mob. It heals the mob for this part of the damage.
	 */
	record BlastHeal(float fraction) implements GeneEffect {
		public static final MapCodec<BlastHeal> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 10.0F).optionalFieldOf("fraction", 0.5F).forGetter(BlastHeal::fraction)
		).apply(instance, BlastHeal::new));

		@Override
		public String type() {
			return "blast_heal";
		}
	}

	/**
	 * While the mob stands on the ground, it leaves fire at its place one time in each interval. The fire goes only
	 * in air, it does not spread, and it goes away after the seconds. It obeys the {@code mobGriefing} gamerule.
	 */
	record FireTrail(float intervalSeconds, float seconds) implements GeneEffect {
		public static final MapCodec<FireTrail> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 600.0F).optionalFieldOf("interval_seconds", 3.0F).forGetter(FireTrail::intervalSeconds),
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 4.0F).forGetter(FireTrail::seconds)
		).apply(instance, FireTrail::new));

		@Override
		public String type() {
			return "fire_trail";
		}
	}

	/**
	 * When the mob dies, it leaves a lava source at its place for the seconds. The lava goes only in air, it does not
	 * flow, and it starts no fires. It obeys the {@code mobGriefing} gamerule.
	 */
	record LavaPool(float seconds) implements GeneEffect {
		public static final MapCodec<LavaPool> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 600.0F).optionalFieldOf("seconds", 3.0F).forGetter(LavaPool::seconds)
		).apply(instance, LavaPool::new));

		@Override
		public String type() {
			return "lava_pool";
		}
	}

	/**
	 * When something hurts the mob, its call for help reaches this many times as far. It changes the vanilla call of
	 * a mob that calls its kin, such as the zombified piglin. More than one effect multiplies together.
	 */
	record AngerRange(float multiplier) implements GeneEffect {
		public static final MapCodec<AngerRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 8.0F).fieldOf("multiplier").forGetter(AngerRange::multiplier)
		).apply(instance, AngerRange::new));

		@Override
		public String type() {
			return "anger_range";
		}
	}

	/**
	 * Multiplies the time that a zombified piglin stays angry. Vanilla is 20 to 39 seconds. More than one effect
	 * multiplies together.
	 */
	record AngerTime(float multiplier) implements GeneEffect {
		public static final MapCodec<AngerTime> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.1F, 100.0F).fieldOf("multiplier").forGetter(AngerTime::multiplier)
		).apply(instance, AngerTime::new));

		@Override
		public String type() {
			return "anger_time";
		}
	}

	/**
	 * When a player dies, a zombified piglin that is angry at the player stays angry at the player for this many
	 * seconds. Vanilla forgives a dead player.
	 */
	record AngerAfterDeath(float seconds) implements GeneEffect {
		public static final MapCodec<AngerAfterDeath> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 3600.0F).optionalFieldOf("seconds", 300.0F).forGetter(AngerAfterDeath::seconds)
		).apply(instance, AngerAfterDeath::new));

		@Override
		public String type() {
			return "anger_after_death";
		}
	}

	/**
	 * When something hurts the mob, the mob itself gets the status effect. With {@code damage_types}, only that
	 * damage counts. Avenge: a projectile gives Strength.
	 */
	record SelfEffectOnHurt(Holder<MobEffect> effect, float seconds, int amplifier, Optional<TagKey<DamageType>> damageTypes)
			implements GeneEffect {
		public static final MapCodec<SelfEffectOnHurt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(SelfEffectOnHurt::effect),
				Codec.floatRange(0.05F, 3600.0F).fieldOf("seconds").forGetter(SelfEffectOnHurt::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(SelfEffectOnHurt::amplifier),
				TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_types").forGetter(SelfEffectOnHurt::damageTypes)
		).apply(instance, SelfEffectOnHurt::new));

		@Override
		public String type() {
			return "self_effect_on_hurt";
		}
	}

	/**
	 * After each melee hit, the mob itself gets the status effect. Rot Sprint: Speed.
	 */
	record SelfEffectOnHit(Holder<MobEffect> effect, float seconds, int amplifier) implements GeneEffect {
		public static final MapCodec<SelfEffectOnHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MobEffect.CODEC.fieldOf("effect").forGetter(SelfEffectOnHit::effect),
				Codec.floatRange(0.05F, 3600.0F).fieldOf("seconds").forGetter(SelfEffectOnHit::seconds),
				Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(SelfEffectOnHit::amplifier)
		).apply(instance, SelfEffectOnHit::new));

		@Override
		public String type() {
			return "self_effect_on_hit";
		}
	}

	/**
	 * When the mob spawns, the item in its main hand gets the enchantment at the level. The item drops as in vanilla.
	 * Glint: Sharpness I on the gold sword.
	 */
	record SpawnEnchantment(ResourceKey<Enchantment> enchantment, int level) implements GeneEffect {
		public static final MapCodec<SpawnEnchantment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(SpawnEnchantment::enchantment),
				Codec.intRange(1, 255).optionalFieldOf("level", 1).forGetter(SpawnEnchantment::level)
		).apply(instance, SpawnEnchantment::new));

		@Override
		public String type() {
			return "spawn_enchantment";
		}
	}

	/**
	 * Sets the time in the Overworld after which the mob turns into its Nether kin: a piglin or a piglin brute into a
	 * zombified piglin, a hoglin into a zoglin. Vanilla is 15 seconds. The longest time counts.
	 */
	record ConversionTime(float seconds) implements GeneEffect {
		public static final MapCodec<ConversionTime> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.05F, 36000.0F).fieldOf("seconds").forGetter(ConversionTime::seconds)
		).apply(instance, ConversionTime::new));

		@Override
		public String type() {
			return "conversion_time";
		}
	}

	/**
	 * A piglin attacks a player in gold armor with this chance. Vanilla piglins leave a player in gold armor alone.
	 * The piglin rolls the chance one time for each player that it sees.
	 */
	record GoldArmorAttack(float chance) implements GeneEffect {
		public static final MapCodec<GoldArmorAttack> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(GoldArmorAttack::chance)
		).apply(instance, GoldArmorAttack::new));

		@Override
		public String type() {
			return "gold_armor_attack";
		}
	}

	/**
	 * Stops vanilla fears of the mob. A piglin fears soul fire and zombified piglins. A hoglin fears warped fungus,
	 * nether portals, respawn anchors, and piglins in a group.
	 */
	record NoFear(List<Fear> fears) implements GeneEffect {
		public static final MapCodec<NoFear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Fear.CODEC.listOf().fieldOf("fears").forGetter(NoFear::fears)
		).apply(instance, NoFear::new));

		/**
		 * A vanilla fear of a piglin or a hoglin.
		 */
		public enum Fear implements StringRepresentable {
			/**
			 * Soul fire, soul torches, soul lanterns, and soul campfires. They are the piglin repellents.
			 */
			SOUL_FIRE,
			ZOMBIFIED_PIGLIN,
			WARPED_FUNGUS,
			NETHER_PORTAL,
			RESPAWN_ANCHOR,
			/**
			 * A hoglin runs from a group of piglins that is larger than its own group.
			 */
			PIGLIN;

			public static final Codec<Fear> CODEC = StringRepresentable.fromEnum(Fear::values);

			@Override
			public String getSerializedName() {
				return name().toLowerCase(Locale.ROOT);
			}
		}

		@Override
		public String type() {
			return "no_fear";
		}
	}

	/**
	 * When a player breaks a gold block or opens a chest in the radius, the mob attacks the player. It works as the
	 * vanilla anger of a piglin, for example on a piglin brute.
	 */
	record GuardGold(float radius) implements GeneEffect {
		public static final MapCodec<GuardGold> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(1.0F, 64.0F).optionalFieldOf("radius", 16.0F).forGetter(GuardGold::radius)
		).apply(instance, GuardGold::new));

		@Override
		public String type() {
			return "guard_gold";
		}
	}

	/**
	 * Each melee hit breaks each worn armor piece of the target that has less than this part of its durability left.
	 * Shatter Blow: {@code 0.25}.
	 */
	record ShatterArmor(float threshold) implements GeneEffect {
		public static final MapCodec<ShatterArmor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("threshold", 0.25F).forGetter(ShatterArmor::threshold)
		).apply(instance, ShatterArmor::new));

		@Override
		public String type() {
			return "shatter_armor";
		}
	}
}
