package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * The effects of genes and families that act in combat: on damage, on a hit, on a hurt, and on a death.
 */
public final class GeneCombat {
	private static final long AMBUSH_TICKS = 200L;
	private static final int DARK_LIGHT = 6;
	private static final int BRIGHT_LIGHT = 12;
	private static final double ALONE_RADIUS = 8.0;
	private static final int DEEP_BLOCKS = 10;
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
		return switch (when) {
			case ALWAYS -> true;
			case ALONE -> sameSpeciesNear(entity, ALONE_RADIUS) == 0;
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
			case GROUPED -> sameSpeciesNear(entity, ALONE_RADIUS) > 0;
			case ON_LAND -> !entity.isInWater();
			case DEEP -> entity.getY() <= entity.level().getSeaLevel() - DEEP_BLOCKS;
			case ABOVE_SEA_LEVEL -> entity.getY() >= entity.level().getSeaLevel();
			case STORM -> entity.level().isThundering();
			case SUNLIT -> entity.level().isBrightOutside() && !entity.isInWaterOrRain()
					&& entity.level().canSeeSky(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
		};
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
				case GeneEffect.DamageTakenWhen taken when test(victim, taken.when()) -> result *= taken.multiplier();
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
					case GeneEffect.DamageDealtWhen dealt when test(attacker, dealt.when()) -> result *= dealt.multiplier();
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
				case GeneEffect.IgniteTarget ignite when mob.getRandom().nextFloat() < ignite.chance() ->
						target.igniteForSeconds(ignite.seconds());
				case GeneEffect.EffectOnHit onHit when target instanceof LivingEntity living
						&& mob.getRandom().nextFloat() < onHit.chance() && test(mob, onHit.when()) ->
						living.addEffect(new MobEffectInstance(onHit.effect(), Math.round(onHit.seconds() * 20.0F),
								onHit.amplifier()), mob);
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
	 * Runs the effects that act after the mob is hurt.
	 */
	public static void onHurt(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
				|| !(source.getEntity() instanceof LivingEntity attacker) || attacker.getType() == mob.getType()) {
			return;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
				case GeneEffect.CallOnHurt call -> {
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
				default -> {
				}
			}
		}
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
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			switch (effect) {
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
					for (Player player : level.getEntitiesOfClass(Player.class, area)) {
						player.addEffect(new MobEffectInstance(death.effect(), Math.round(death.seconds() * 20.0F),
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
		if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
			return false;
		}
		return GeneEffects.of(entity).anyMatch(effect -> effect instanceof GeneEffect.DodgeProjectiles dodge
				&& test(entity, dodge.when()) && entity.getRandom().nextFloat() < dodge.chance());
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
			level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.5), entity.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
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
