package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.util.RandomSource;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

/**
 * The effects of genes and families that act in combat: on damage, on a hit, on a hurt, and on a death.
 */
public final class GeneCombat {
	private static final long AMBUSH_TICKS = 200L;
	private static final int DARK_LIGHT = 6;
	private static final int BRIGHT_LIGHT = 12;
	private static final double ALONE_RADIUS = 8.0;
	private static final double HEAL_ON_DEATH_REACH = 32.0;
	private static final double KNOCKBACK = 0.4;
	private static final int DEEP_BLOCKS = 10;
	private static final double HEIGHT_DIFFERENCE = 2.0;
	/**
	 * The damage multiplier of a critical hit in vanilla.
	 */
	private static final float CRITICAL_MULTIPLIER = 1.5F;

	private GeneCombat() {
	}

	/**
	 * Returns true if the state is true for the entity now.
	 */
	private static boolean onSand(LivingEntity entity) {
		return entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.SAND);
	}

	public static boolean test(LivingEntity entity, When when) {
		return test(entity, when, ALONE_RADIUS);
	}

	/**
	 * Returns true if the state is true for the entity now. The states {@code alone} and {@code grouped} use the
	 * radius.
	 */
	public static boolean test(LivingEntity entity, When when, double groupRadius) {
		return switch (when) {
			case ALWAYS -> true;
			case ALONE -> sameSpeciesNear(entity, groupRadius) == 0;
			case AMBUSH -> {
				long combat = entity.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).combatTime();
				yield combat < 0L || entity.level().getGameTime() - combat >= AMBUSH_TICKS;
			}
			case DARK -> entity.level().getMaxLocalRawBrightness(entity.blockPosition()) <= DARK_LIGHT;
			case BRIGHT -> entity.level().getMaxLocalRawBrightness(entity.blockPosition()) >= BRIGHT_LIGHT;
			case HUNTING_WOUNDED -> entity instanceof Mob mob && mob.getTarget() instanceof Player player
					&& player.getHealth() < player.getMaxHealth() / 2.0F;
			case IN_WATER -> entity.isInWater();
			case ON_SAND -> onSand(entity);
			case OFF_SAND -> !onSand(entity);
			case GROUPED -> sameSpeciesNear(entity, groupRadius) > 0;
			case ON_LAND -> !entity.isInWater();
			case DEEP -> entity.getY() <= entity.level().getSeaLevel() - DEEP_BLOCKS;
			case ABOVE_SEA_LEVEL -> entity.getY() >= entity.level().getSeaLevel();
			case STORM -> entity.level().isThundering();
			case TARGET_BELOW -> entity instanceof Mob mob && mob.getTarget() != null
					&& mob.getTarget().getY() <= entity.getY() - HEIGHT_DIFFERENCE;
			case TARGET_ABOVE -> entity instanceof Mob mob && mob.getTarget() != null
					&& mob.getTarget().getY() >= entity.getY() + HEIGHT_DIFFERENCE;
			case WOUNDED -> entity.getHealth() < entity.getMaxHealth() / 2.0F;
			case SUNLIT -> entity.level().isBrightOutside() && !entity.isInWaterOrRain()
					&& entity.level().canSeeSky(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
			case SNOWING -> snowingOn(entity);
			case NOT_SNOWING -> !snowingOn(entity);
			case ON_SNOW -> onSnow(entity);
			case OFF_SNOW -> !onSnow(entity);
			case ON_ICE -> entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.ICE);
			case COLD_BIOME -> entity.level().getBiome(entity.blockPosition()).value()
					.coldEnoughToSnow(entity.blockPosition(), entity.level().getSeaLevel());
			case NIGHT -> entity.level().isDarkOutside();
			case TARGET_FROZEN -> entity instanceof Mob mob && mob.getTarget() != null && mob.getTarget().getTicksFrozen() > 0;
		};
	}

	/**
	 * Heals the mobs of the species of the dead mob that heal when an ally dies near them.
	 */
	private static void healAllies(Mob dead, ServerLevel level) {
		AABB area = dead.getBoundingBox().inflate(HEAL_ON_DEATH_REACH);
		for (Mob ally : level.getEntitiesOfClass(Mob.class, area, other -> other != dead && other.isAlive()
				&& other.getType() == dead.getType())) {
			for (GeneEffect effect : GeneEffects.of(ally).toList()) {
				if (effect instanceof GeneEffect.HealOnAllyDeath heal && ally.distanceTo(dead) <= heal.radius()) {
					ally.heal(heal.amount());
				}
			}
		}
	}

	/**
	 * Damages the armor of a player more after a projectile hit of a mob with an armor break effect.
	 */
	public static void breakArmor(Mob mob, LivingEntity target, float damage) {
		if (!(target instanceof Player player)) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ArmorBreak armorBreak
					&& (!armorBreak.frozenOnly() || player.getTicksFrozen() > 0)) {
				// The game damages each armor piece by a quarter of the damage, and by 1 or more.
				int extra = Math.round(Math.max(1.0F, damage / 4.0F) * (armorBreak.multiplier() - 1.0F));
				for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
					ItemStack stack = player.getItemBySlot(slot);
					if (!stack.isEmpty() && stack.isDamageableItem() && extra > 0) {
						stack.hurtAndBreak(extra, player, slot);
					}
				}
			}
		}
	}

	/**
	 * Freezes the entity as powder snow does, so that the freeze stays full for the seconds. An entity that cannot
	 * freeze, for example a player in leather armor, does not freeze.
	 */
	public static void freeze(LivingEntity entity, float seconds) {
		if (!entity.canFreeze()) {
			return;
		}
		// The game takes 2 ticks of freeze away each tick out of powder snow.
		int ticks = entity.getTicksRequiredToFreeze() + Math.round(seconds * 40.0F);
		entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), ticks));
	}

	/**
	 * Returns true if it snows on the entity: a storm, open sky above it, and a biome where the storm is snow.
	 */
	public static boolean snowingOn(Entity entity) {
		BlockPos head = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
		return entity.level().isRaining() && entity.level().canSeeSky(head) && snowsAt(entity.level(), head);
	}

	/**
	 * Returns true if a storm at the position is snow, from its biome and its height.
	 */
	public static boolean snowsAt(net.minecraft.world.level.Level level, BlockPos pos) {
		return level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()) == Biome.Precipitation.SNOW;
	}

	/**
	 * Returns true if the entity stands on snow or ice, or in a snow layer or in powder snow.
	 */
	public static boolean onSnow(Entity entity) {
		var below = entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement());
		var feet = entity.level().getBlockState(entity.blockPosition());
		return below.is(BlockTags.SNOW) || below.is(BlockTags.ICE) || feet.is(BlockTags.SNOW);
	}

	public static int sameSpeciesNear(LivingEntity entity, double radius) {
		AABB area = entity.getBoundingBox().inflate(radius);
		return entity.level().getEntities(entity.getType(), area, other -> other != entity && other.isAlive()).size();
	}

	/**
	 * Returns the damage after the effects of the victim and of the attacker.
	 * Then it records the combat time of both.
	 */
	public static float modifyDamage(LivingEntity victim, DamageSource source, float amount) {
		float result = amount;
		List<GeneEffect> victimEffects = GeneEffects.of(victim).toList();
		for (GeneEffect effect : victimEffects) {
			switch (effect) {
				case GeneEffect.ShareDamage share when !SHARING.get() && victim.level() instanceof ServerLevel level ->
						result -= shareDamage(victim, level, share, result);
				case GeneEffect.DamageTaken taken when source.is(taken.damageTypes()) -> result *= taken.multiplier();
				case GeneEffect.Sunburn sunburn when source.is(DamageTypeTags.IS_FIRE)
						&& Evolution.isSunBurning(victim, victim.level().getGameTime()) -> result *= sunburn.multiplier();
				case GeneEffect.DamageTakenWhen taken when test(victim, taken.when(), taken.radius()) ->
						result *= taken.multiplier();
				case GeneEffect.DamageFromBehind behind when isBehind(victim, source) -> result *= behind.multiplier();
				case GeneEffect.DamageFromFront front when source.getSourcePosition() != null && !isBehind(victim, source)
						&& front.hits().includes(source.getDirectEntity() instanceof Projectile) ->
						result *= front.multiplier();
				case GeneEffect.DamageFromEnchantment enchanted when hasEnchantment(source, enchanted.enchantment()) ->
						result *= enchanted.multiplier();
				case GeneEffect.DamageFrom from when source.getEntity() != null
						&& from.attackers().stream().anyMatch(type -> type.value() == source.getEntity().getType()) ->
						result *= from.multiplier();
				case GeneEffect.CriticalImmunity immunity when source.getEntity() instanceof Player
						&& victim.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).criticalTime()
								== victim.level().getGameTime() -> result /= CRITICAL_MULTIPLIER;
				default -> {
				}
			}
		}
		if (source.getEntity() instanceof LivingEntity attacker && attacker != victim) {
			for (GeneEffect effect : GeneEffects.of(attacker).toList()) {
				switch (effect) {
					case GeneEffect.DamageDealtWhen dealt when (!dealt.playersOnly() || victim instanceof Player)
							&& dealt.hits().includes(source.getDirectEntity() instanceof Projectile)
							&& test(attacker, dealt.when()) -> result *= dealt.multiplier();
					case GeneEffect.Lure lure when attacker.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE)
							.lureArmed() -> {
						result *= lure.multiplier();
						BehaviorState state = attacker.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
						attacker.setAttached(ModAttachments.STATE, state.withLure(state.lureNearTime(), false));
					}
					case GeneEffect.Rally rally ->
							result *= 1.0F + Math.min(rally.max(), rally.perAlly() * sameSpeciesNear(attacker, rally.radius()));
					default -> {
					}
				}
			}
			markCombat(attacker);
		}
		markCombat(victim);
		return result;
	}

	private static void markCombat(LivingEntity entity) {
		if (entity.level() instanceof ServerLevel level && entity.hasAttached(ModAttachments.GENES)) {
			entity.setAttached(ModAttachments.TRACE,
					entity.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).withCombat(level.getGameTime()));
		}
	}

	/**
	 * Runs the effects that act when the mob hits a target.
	 */
	public static void onHit(Mob mob, Entity target) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.IgniteTarget ignite when ignite.hits().includes(false)
						&& mob.getRandom().nextFloat() < ignite.chance() ->
						target.igniteForSeconds(ignite.seconds());
				case GeneEffect.EffectOnHit onHit when onHit.hits().includes(false) && target instanceof LivingEntity living
						&& mob.getRandom().nextFloat() < onHit.chance() && test(mob, onHit.when()) ->
						living.addEffect(new MobEffectInstance(onHit.effect(), Math.round(onHit.seconds() * 20.0F),
								onHit.amplifier()), mob);
				case GeneEffect.FreezeOnHit freeze when freeze.hits().includes(false) && target instanceof LivingEntity living
						&& mob.getRandom().nextFloat() < freeze.chance() -> freeze(living, freeze.seconds());
				case GeneEffect.CallOnHit call when call.hits().includes(false) && target instanceof Player player ->
						GeneBehaviors.callAllies(mob, call.radius(), player);
				case GeneEffect.PullDownOnHit pull when target instanceof Player player && player.isInWater() ->
						GeneBehaviors.pullDown(player, pull.blocks());
				case GeneEffect.DragUnder drag when target instanceof Player player && player.isInWater() ->
						mob.setAttached(ModAttachments.STATE, mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE)
								.withDrag(player.getId(), mob.level().getGameTime() + Math.round(drag.seconds() * 20.0F)));
				case GeneEffect.BreakBoats breaker when mob.level() instanceof ServerLevel level
						&& (target instanceof AbstractBoat || target.getVehicle() instanceof AbstractBoat) ->
						hitBoat(mob, level, target instanceof AbstractBoat boat ? boat : (AbstractBoat) target.getVehicle(),
								breaker);
				case GeneEffect.DrainFood drain when target instanceof Player player -> {
					player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - drain.amount()));
					mob.heal(drain.heal());
				}
				case GeneEffect.LeechXp leech when target instanceof Player player -> {
					int taken = Math.min(leech.amount(), player.totalExperience);
					if (taken > 0) {
						player.giveExperiencePoints(-taken);
						mob.setAttached(ModAttachments.COMBAT,
								mob.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE).withLeechedXp(taken));
					}
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Runs the effects that act when a projectile of the mob hits a target, for example a thrown trident.
	 */
	public static void onProjectileHit(Mob mob, LivingEntity target) {
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.EffectOnHit onHit when onHit.hits().includes(true)
						&& mob.getRandom().nextFloat() < onHit.chance() && test(mob, onHit.when()) ->
						target.addEffect(new MobEffectInstance(onHit.effect(), Math.round(onHit.seconds() * 20.0F),
								onHit.amplifier()), mob);
				case GeneEffect.FreezeOnHit freeze when freeze.hits().includes(true)
						&& mob.getRandom().nextFloat() < freeze.chance() -> freeze(target, freeze.seconds());
				case GeneEffect.ArrowKnockback knockback -> {
					Vec3 away = new Vec3(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
					if (away.lengthSqr() > 1.0E-4) {
						// An arrow pushes with about 0.4. The extra push makes the full push the multiplier times as far.
						Vec3 push = away.normalize().scale(KNOCKBACK * (knockback.multiplier() - 1.0F));
						target.push(push.x, 0.1, push.z);
						target.needsSync = true;
					}
				}
				case GeneEffect.CallOnHit call when call.hits().includes(true) && target instanceof Player player ->
						GeneBehaviors.callAllies(mob, call.radius(), player);
				case GeneEffect.IgniteTarget ignite when ignite.hits().includes(true)
						&& mob.getRandom().nextFloat() < ignite.chance() -> target.igniteForSeconds(ignite.seconds());
				case GeneEffect.LightningOnHit lightning when mob.level() instanceof ServerLevel level
						&& (!lightning.stormOnly() || level.isThundering()) && level.canSeeSky(target.blockPosition()) -> {
					LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
					if (bolt != null) {
						bolt.snapTo(target.getX(), target.getY(), target.getZ());
						level.addFreshEntity(bolt);
					}
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Runs the effects that act after the mob is hurt.
	 */
	public static void onHurt(LivingEntity entity, DamageSource source) {
		if (entity instanceof Mob wounded && wounded.level() instanceof ServerLevel woundedLevel) {
			healOnce(wounded, woundedLevel);
		}
		if (!(entity instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
				|| !(source.getEntity() instanceof LivingEntity attacker) || attacker.getType() == mob.getType()) {
			return;
		}
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		if (state.dragTarget() == attacker.getId()) {
			// The held player breaks free with a hit.
			mob.setAttached(ModAttachments.STATE, state.withDrag(-1, -1L));
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.CallOnHurt call when callReady(mob, level, call) -> {
					if (call.smoke()) {
						// A column of smoke that players can see from far away.
						level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, mob.getX(), mob.getY() + 2.0, mob.getZ(),
								6, 0.2, 1.5, 0.2, 0.01);
					}
					AABB area = mob.getBoundingBox().inflate(call.radius());
					for (Mob ally : level.getEntitiesOfClass(Mob.class, area, other -> other.getType() == mob.getType())) {
						ally.setTarget(attacker);
					}
				}
				case GeneEffect.GraveCall call when mob.getRandom().nextFloat() < call.chance() -> graveCall(mob, level, call);
				// The damage of thorns does not cause thorns, so that two mobs with thorns do not hurt each other forever.
				case GeneEffect.Thorns thorns when source.getDirectEntity() == attacker && !source.is(DamageTypes.THORNS) ->
						attacker.hurtServer(level, level.damageSources().thorns(mob), thorns.damage());
				case GeneEffect.EffectOnHurt onHurt -> effectOnHurt(mob, level, onHurt, attacker);
				case GeneEffect.FreezeAttacker freeze when source.getDirectEntity() == attacker ->
						freeze(attacker, freeze.seconds());
				default -> {
				}
			}
		}
	}

	/**
	 * Returns true if the call on hurt of the mob is out of its cooldown, and starts the next cooldown.
	 */
	private static boolean callReady(Mob mob, ServerLevel level, GeneEffect.CallOnHurt call) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		if (state.hurtCallTime() >= 0L && now - state.hurtCallTime() < Math.round(call.cooldownSeconds() * 20.0F)) {
			return false;
		}
		mob.setAttached(ModAttachments.STATE, state.withHurtCallTime(now));
		return true;
	}

	/**
	 * Returns true if the weapon of the attacker has the enchantment.
	 */
	public static boolean hasEnchantment(DamageSource source, ResourceKey<Enchantment> enchantment) {
		if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
			return false;
		}
		var holder = attacker.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment);
		return holder.isPresent()
				&& EnchantmentHelper.getItemEnchantmentLevel(holder.get(), attacker.getMainHandItem()) > 0;
	}

	/**
	 * Returns true if the target has an effect that makes mobs of the type of the mob ignore it.
	 */
	public static boolean ignores(Mob mob, LivingEntity target) {
		return GeneEffects.of(target).anyMatch(effect -> effect instanceof GeneEffect.IgnoredBy ignored
				&& ignored.mobs().contains(mob.getType().builtInRegistryHolder()));
	}

	/**
	 * Returns true if the source of the damage is behind the victim.
	 */
	public static boolean isBehind(LivingEntity victim, DamageSource source) {
		Vec3 from = source.getSourcePosition();
		if (from == null) {
			return false;
		}
		Vec3 facing = Vec3.directionFromRotation(0.0F, victim.yBodyRot);
		Vec3 toSource = from.subtract(victim.position()).multiply(1.0, 0.0, 1.0);
		return toSource.lengthSqr() > 1.0E-4 && toSource.normalize().dot(facing) < 0.0;
	}

	private static void hitBoat(Mob mob, ServerLevel level, AbstractBoat boat, GeneEffect.BreakBoats breaker) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		int hits = state.boatId() == boat.getId() ? state.boatHits() + 1 : 1;
		if (hits < breaker.hits()) {
			mob.setAttached(ModAttachments.STATE, state.withBoatHits(boat.getId(), hits));
			return;
		}
		mob.setAttached(ModAttachments.STATE, state.withBoatHits(-1, 0));
		// The riders fall into the water, and the boat breaks as if a player broke it.
		boat.ejectPassengers();
		boat.destroy(level, boat.getPickResult() == null ? Items.AIR : boat.getPickResult().getItem());
	}

	/**
	 * Heals the mob to full health one time in its life, when its health drops under the threshold.
	 */
	private static void healOnce(Mob mob, ServerLevel level) {
		if (!mob.isAlive() || mob.getAttachedOrElse(ModAttachments.HEAL_ONCE_USED, false)) {
			return;
		}
		var heal = GeneEffects.of(mob).filter(GeneEffect.HealOnce.class::isInstance).map(GeneEffect.HealOnce.class::cast)
				.findFirst();
		if (heal.isPresent() && mob.getHealth() < mob.getMaxHealth() * heal.get().threshold()) {
			mob.setHealth(mob.getMaxHealth());
			mob.setAttached(ModAttachments.HEAL_ONCE_USED, true);
			level.sendParticles(ParticleTypes.BUBBLE, mob.getX(), mob.getY(0.5), mob.getZ(), 30, 0.4, 0.6, 0.4, 0.1);
		}
	}

	private static void effectOnHurt(Mob mob, ServerLevel level, GeneEffect.EffectOnHurt onHurt, LivingEntity attacker) {
		BehaviorState state = mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE);
		long now = level.getGameTime();
		if (state.hurtEffectTime() >= 0L && now - state.hurtEffectTime() < Math.round(onHurt.cooldownSeconds() * 20.0F)) {
			return;
		}
		MobEffectInstance instance = new MobEffectInstance(onHurt.effect(), Math.round(onHurt.seconds() * 20.0F),
				onHurt.amplifier());
		if (onHurt.allyRadius() > 0.0F) {
			AABB area = mob.getBoundingBox().inflate(onHurt.allyRadius());
			for (Mob ally : level.getEntitiesOfClass(Mob.class, area, other -> other.getType() == mob.getType())) {
				ally.addEffect(new MobEffectInstance(instance), mob);
			}
		} else if (onHurt.playerRadius() > 0.0F) {
			AABB area = mob.getBoundingBox().inflate(onHurt.playerRadius());
			for (Player player : level.getEntitiesOfClass(Player.class, area)) {
				player.addEffect(new MobEffectInstance(instance), mob);
			}
		} else {
			attacker.addEffect(instance, mob);
		}
		if (onHurt.ink()) {
			level.sendParticles(ParticleTypes.SQUID_INK, mob.getX(), mob.getY(0.6), mob.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
		}
		mob.setAttached(ModAttachments.STATE, state.withHurtEffectTime(now));
	}

	private static void graveCall(Mob mob, ServerLevel level, GeneEffect.GraveCall call) {
		CombatRecord record = mob.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
		if (record.graveCalls() >= call.maxCalls()) {
			return;
		}
		Entity created = mob.getType().create(level, EntitySpawnReason.REINFORCEMENT);
		if (!(created instanceof Mob called)) {
			return;
		}
		int x = mob.getBlockX() + mob.getRandom().nextInt(Math.round(call.radius()) * 2 + 1) - Math.round(call.radius());
		int z = mob.getBlockZ() + mob.getRandom().nextInt(Math.round(call.radius()) * 2 + 1) - Math.round(call.radius());
		BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
		called.setPos(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
		// The called mob has no genes, so that the calls do not grow out of control.
		called.setAttached(ModAttachments.GENES, CarriedGenes.NONE);
		called.setTarget(mob.getTarget());
		level.addFreshEntity(called);
		mob.setAttached(ModAttachments.COMBAT, record.withGraveCall());
	}

	/**
	 * Runs the effects that act when the mob dies.
	 */
	public static void onDeath(Mob mob, ServerLevel level) {
		onDeath(mob, level, null);
	}

	/**
	 * Runs the effects that act when the mob dies. The source can be null.
	 */
	public static void onDeath(Mob mob, ServerLevel level, DamageSource source) {
		healAllies(mob, level);
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.DeathBlock death -> {
					BlockPos pos = mob.blockPosition();
					if (level.getBlockState(pos).isAir()
							&& level.getEntitiesOfClass(Player.class, new AABB(pos)).isEmpty()) {
						level.setBlockAndUpdate(pos, death.block().defaultBlockState());
					}
				}
				case GeneEffect.CallOnDeath call -> {
					LivingEntity killer = source != null && source.getEntity() instanceof LivingEntity living
							? living : mob.getLastHurtByMob();
					if (killer instanceof Player player) {
						GeneBehaviors.callAllies(mob, call.radius(), player);
					}
				}
				case GeneEffect.DeathBurst burst -> {
					AABB area = mob.getBoundingBox().inflate(burst.radius());
					for (Player player : level.getEntitiesOfClass(Player.class, area)) {
						player.hurtServer(level, level.damageSources().mobAttack(mob), burst.damage());
					}
					level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, mob.getX(), mob.getY(0.5), mob.getZ(),
							12, burst.radius() / 2.0, 0.3, burst.radius() / 2.0, 0.1);
				}
				case GeneEffect.DeathCloud cloud -> {
					AreaEffectCloud area = new AreaEffectCloud(level, mob.getX(), mob.getY(), mob.getZ());
					int duration = Math.round(cloud.cloudSeconds() * 20.0F);
					area.setRadius(cloud.radius());
					area.setDuration(duration);
					area.setRadiusPerTick(-cloud.radius() / duration);
					area.addEffect(new MobEffectInstance(cloud.effect(), Math.round(cloud.seconds() * 20.0F),
							cloud.amplifier()));
					level.addFreshEntity(area);
				}
				case GeneEffect.DeathEffect death when test(mob, death.when()) -> {
					AABB area = mob.getBoundingBox().inflate(death.radius());
					List<? extends LivingEntity> reached = death.allies()
							? level.getEntitiesOfClass(Mob.class, area, other -> other != mob && other.getType() == mob.getType())
							: level.getEntitiesOfClass(Player.class, area);
					for (LivingEntity living : reached) {
						living.addEffect(new MobEffectInstance(death.effect(), Math.round(death.seconds() * 20.0F),
								death.amplifier()), mob);
					}
					level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY(0.5), mob.getZ(), 20, death.radius() / 2.0, 0.5, death.radius() / 2.0, 0.05);
				}
				case GeneEffect.LeechXp leech -> {
					int leeched = mob.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE).leechedXp();
					if (leeched > 0) {
						ExperienceOrb.award(level, mob.position(), leeched);
					}
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Returns true if the mob has an effect of this class.
	 */
	public static boolean has(Entity entity, Class<? extends GeneEffect> type) {
		return GeneEffects.of(entity).anyMatch(type::isInstance);
	}

	/**
	 * True while the mod passes shared damage or shared healing to other mobs, so that the share does not repeat.
	 */
	private static final ThreadLocal<Boolean> SHARING = ThreadLocal.withInitial(() -> false);

	/**
	 * Gives a part of the damage to the mobs of the same species near the victim.
	 *
	 * @return the damage that went to the other mobs
	 */
	private static float shareDamage(LivingEntity victim, ServerLevel level, GeneEffect.ShareDamage share, float amount) {
		List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(share.radius()),
				other -> other != victim && other.isAlive() && other.getType() == victim.getType());
		if (allies.isEmpty() || amount <= 0.0F) {
			return 0.0F;
		}
		float shared = amount * share.fraction();
		SHARING.set(true);
		try {
			for (LivingEntity ally : allies) {
				ally.hurtServer(level, level.damageSources().generic(), shared / allies.size());
			}
		} finally {
			SHARING.set(false);
		}
		return shared;
	}

	/**
	 * Tells if a projectile misses the mob.
	 */
	public static boolean dodges(LivingEntity entity, DamageSource source) {
		return source.is(DamageTypeTags.IS_PROJECTILE) && dodges(entity, source.getDirectEntity());
	}

	/**
	 * Returns true if the projectile misses the entity. The roll comes from the two entities, so each check of the
	 * same projectile gives the same answer, and a missed projectile goes through the entity.
	 */
	public static boolean dodges(LivingEntity entity, Entity projectile) {
		if (projectile == null) {
			return false;
		}
		long seed = projectile.getUUID().getLeastSignificantBits() ^ entity.getUUID().getMostSignificantBits();
		float roll = RandomSource.create(seed).nextFloat();
		return GeneEffects.of(entity).anyMatch(effect -> effect instanceof GeneEffect.DodgeProjectiles dodge
				&& test(entity, dodge.when()) && roll < dodge.chance());
	}

	/**
	 * Returns true if a block of glass touches the side of the entity. A climber cannot climb glass.
	 */
	public static boolean touchesGlass(LivingEntity entity) {
		AABB box = entity.getBoundingBox().inflate(0.1, -0.1, 0.1);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
				BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
			var state = entity.level().getBlockState(pos);
			if (state.is(ConventionalBlockTags.GLASS_BLOCKS) || state.is(ConventionalBlockTags.GLASS_PANES)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the mob fell apart and waits to stand up again.
	 */
	public static boolean collapsed(LivingEntity entity) {
		long until = entity.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE).collapseUntil();
		return until >= 0L && entity.level().getGameTime() < until;
	}

	/**
	 * Lets a mob with the reassemble effect fall apart in place of its death, one time in its life. A weapon with
	 * Smite stops it.
	 *
	 * @return true if the mob does not die now
	 */
	public static boolean reassemble(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
				|| mob.getAttachedOrElse(ModAttachments.REASSEMBLE_USED, false)
				|| hasEnchantment(source, Enchantments.SMITE)) {
			return false;
		}
		var reassemble = GeneEffects.of(mob).filter(GeneEffect.Reassemble.class::isInstance)
				.map(GeneEffect.Reassemble.class::cast).findFirst();
		if (reassemble.isEmpty()) {
			return false;
		}
		long until = level.getGameTime() + Math.round(reassemble.get().seconds() * 20.0F);
		mob.setHealth(1.0F);
		mob.setAttached(ModAttachments.REASSEMBLE_USED, true);
		mob.setAttached(ModAttachments.STATE, mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE)
				.withCollapseUntil(until).withStunUntil(until));
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.BONE),
				mob.getX(), mob.getY(0.5), mob.getZ(), 30, 0.3, 0.5, 0.3, 0.1);
		return true;
	}

	/**
	 * Returns true if the mob ignores this damage while it is down: only a weapon with Smite hurts it then.
	 */
	public static boolean protectedWhileDown(LivingEntity entity, DamageSource source) {
		return collapsed(entity) && !hasEnchantment(source, Enchantments.SMITE);
	}

	/**
	 * Saves a mob from a lethal hit one time in its life, if it has the undying effect.
	 *
	 * @return true if the mob survives
	 */
	public static boolean undying(LivingEntity entity) {
		if (entity.getAttachedOrElse(ModAttachments.UNDYING_USED, false)) {
			return false;
		}
		var undying = GeneEffects.of(entity).filter(GeneEffect.Undying.class::isInstance)
				.map(GeneEffect.Undying.class::cast).findFirst();
		if (undying.isEmpty()) {
			return false;
		}
		entity.setHealth(Math.min(undying.get().health(), entity.getMaxHealth()));
		entity.setAttached(ModAttachments.UNDYING_USED, true);
		if (entity.level() instanceof ServerLevel level) {
			level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
					entity.getX(), entity.getY(0.5), entity.getZ(), 30, 0.3, 0.5, 0.3, 0.05);
		}
		return true;
	}

	/**
	 * Tells if a status effect does not affect the mob.
	 */
	public static boolean immune(LivingEntity entity, MobEffectInstance instance) {
		return GeneEffects.of(entity).anyMatch(effect -> effect instanceof GeneEffect.EffectImmunity immunity
				&& immunity.effects().contains(instance.getEffect()));
	}

	/**
	 * Returns a heal after the heal multipliers of the mob.
	 */
	public static float healAmount(LivingEntity entity, float amount) {
		float result = amount;
		for (GeneEffect effect : GeneEffects.of(entity).toList()) {
			if (effect instanceof GeneEffect.HealMultiplier multiplier) {
				result *= multiplier.multiplier();
			}
		}
		return result;
	}

	/**
	 * Gives the same heal to the mobs of the same species near a mob with shared healing.
	 */
	public static void shareHealing(LivingEntity entity, float amount) {
		if (SHARING.get() || amount <= 0.0F || !(entity.level() instanceof ServerLevel level)) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(entity).toList()) {
			if (effect instanceof GeneEffect.ShareHealing share) {
				SHARING.set(true);
				try {
					for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class,
							entity.getBoundingBox().inflate(share.radius()),
							other -> other != entity && other.isAlive() && other.getType() == entity.getType())) {
						ally.heal(amount);
					}
				} finally {
					SHARING.set(false);
				}
			}
		}
	}
}
