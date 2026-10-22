package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.DeathContext;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.Pressure;
import io.github.stivengjekaj.originofspawns.notebook.Discovery;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Connects the evolution of the mod to the game.
 */
public final class Evolution {
	private static final long TICKS_PER_DAY = 24000L;
	/**
	 * The species that get no random weapon and no random armor when they spawn.
	 */
	public static final TagKey<EntityType<?>> NO_RANDOM_EQUIPMENT =
			TagKey.create(Registries.ENTITY_TYPE, OriginOfSpawns.id("no_random_equipment"));

	/**
	 * The species that spawn no babies in vanilla ways. Their babies come only from a gene, such as Runt.
	 */
	/**
	 * The species that ride no mob in vanilla ways, for example the spider that carries a skeleton. A gene can still
	 * make a jockey.
	 */
	public static final TagKey<EntityType<?>> NO_VANILLA_JOCKEYS =
			TagKey.create(Registries.ENTITY_TYPE, OriginOfSpawns.id("no_vanilla_jockeys"));

	public static final TagKey<EntityType<?>> NO_VANILLA_BABIES =
			TagKey.create(Registries.ENTITY_TYPE, OriginOfSpawns.id("no_vanilla_babies"));

	/**
	 * The species that do not pick up items, unless a gene lets them.
	 */
	public static final TagKey<EntityType<?>> NO_ITEM_PICKUP =
			TagKey.create(Registries.ENTITY_TYPE, OriginOfSpawns.id("no_item_pickup"));

	private static final long SUN_BURN_TICKS = 200L;
	private static final int SPOTTED_SECONDS = 2;
	private static final double SPOT_MIN_DISTANCE = 8.0;
	private static final double SPOT_RANGE = 48.0;
	/**
	 * The cosine of the half angle of the view of a player, about 70 degrees.
	 */
	private static final double SPOT_VIEW_COS = 0.34;
	/**
	 * A look at a mob counts for the unseen state for 3 seconds.
	 */
	private static final long LOOK_TICKS = 60L;
	/**
	 * A player looks at a mob only in this range.
	 */
	private static final double LOOK_RANGE = 32.0;
	private static final CrowdTracker CROWDS = new CrowdTracker();

	/**
	 * The decay rate of a gene that the data packs no longer have.
	 */
	private static final float DEFAULT_DECAY_RATE = 0.01F;
	private Evolution() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register(Evolution::onLoad);
		ServerLivingEntityEvents.AFTER_DEATH.register(Evolution::onDeath);
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			GeneCombat.onHurt(entity, source);
			GeneBlasts.onBlastHit(entity, source, damageTaken, blocked);
			// An arrow that a shield stops, or that does no damage, gives none of its effects.
			if (!blocked && damageTaken > 0.0F && source.getDirectEntity() instanceof Projectile
					&& source.getEntity() instanceof Mob mob && mob != entity && mob.hasAttached(ModAttachments.GENES)) {
				GeneCombat.onProjectileHit(mob, entity);
				GeneCombat.breakArmor(mob, entity, damageTaken, true);
			}
			if (!blocked && damageTaken > 0.0F && source.getDirectEntity() instanceof Mob mob && mob != entity
					&& mob.hasAttached(ModAttachments.GENES)) {
				GeneCombat.breakArmor(mob, entity, damageTaken, false);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(Evolution::decay);
		DelayedTasks.register();
		TemporaryBlocks.register();
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> CROWDS.clear());
	}

	/**
	 * The time after a block with a shield in which a kill counts for the shield pressure.
	 */
	private static final long SHIELD_TICKS = 200L;

	/**
	 * Returns true if the entity holds its shield up now, or blocked a hit with it in the last 10 seconds.
	 */
	public static boolean usedShield(LivingEntity entity, long now) {
		long blocked = entity.getAttachedOrElse(ModAttachments.SHIELD_BLOCK, -SHIELD_TICKS - 1L);
		return entity.isBlocking() || now - blocked <= SHIELD_TICKS;
	}

	private static void onLoad(Entity entity, ServerLevel level) {
		if (!(entity instanceof Mob mob)) {
			return;
		}
		// A mob that spawned now gets no item pickup. A gene can give it back in onSpawn.
		if (mob.hasAttached(ModAttachments.SPAWN_REASON) && mob.getType().builtInRegistryHolder().is(NO_ITEM_PICKUP)) {
			mob.setCanPickUpLoot(false);
		}
		if (!mob.hasAttached(ModAttachments.GENES)) {
			rollGenes(mob, level);
			if (mob.hasAttached(ModAttachments.GENES)) {
				EntitySpawnReason reason = mob.getAttachedOrElse(ModAttachments.SPAWN_REASON, EntitySpawnReason.LOAD);
				GeneBehaviors.onSpawn(mob, level, reason);
			}
		}
		// The genes exist now, so the captain of a new patrol or raid wave can be chosen.
		if (mob instanceof net.minecraft.world.entity.monster.PatrollingMonster patroller
				&& mob.hasAttached(ModAttachments.SPAWN_REASON)) {
			Captains.onJoin(patroller, level);
		}
		if (mob.hasAttached(ModAttachments.GENES)) {
			// A mob from an older version has no birth time, so its age starts now.
			if (!mob.hasAttached(ModAttachments.BORN)) {
				mob.setAttached(ModAttachments.BORN, level.getGameTime());
			}
			GeneBehaviors.installGoals(mob);
		}
	}

	/**
	 * Gives a new mob its genes from the pool of its species in the category of its biome.
	 * A mob of a species that no gene can go to gets no genes.
	 */
	/**
	 * Makes the carried genes of a mob from a list of genes: it finds the family from the tags of the genes and their
	 * frequencies in the pool of the mob.
	 */
	public static CarriedGenes carry(ServerLevel level, Mob mob, List<Identifier> genes) {
		Registry<Gene> registry = level.registryAccess().lookupOrThrow(ModRegistries.GENE);
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		GenePools pools = GenePools.get(level.getServer());
		List<FamilyResult.TaggedGene> tagged = new ArrayList<>();
		for (Identifier id : genes) {
			Gene gene = registry.getValue(id);
			if (gene != null) {
				tagged.add(new FamilyResult.TaggedGene(gene.family(), gene.subFamily(),
						pools.frequency(species, category, id)));
			}
		}
		return new CarriedGenes(genes, FamilyResult.of(tagged));
	}

	public static void rollGenes(Mob mob, ServerLevel level) {
		Registry<Gene> registry = level.registryAccess().lookupOrThrow(ModRegistries.GENE);
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		GenePools pools = GenePools.get(level.getServer());

		List<GeneRoller.Candidate> candidates = new ArrayList<>();
		for (Map.Entry<ResourceKey<Gene>, Gene> entry : registry.entrySet()) {
			if (entry.getValue().carriedBy(mob.getType())) {
				Identifier id = entry.getKey().identifier();
				candidates.add(new GeneRoller.Candidate(id, pools.frequency(species, category, id),
						entry.getValue().mutationRate()));
			}
		}
		if (candidates.isEmpty()) {
			return;
		}

		List<Identifier> rolled = GeneRoller.roll(candidates, mob.getRandom());
		CarriedGenes carried = carry(level, mob, rolled);
		mob.setAttached(ModAttachments.GENES, carried);
		GeneEffects.applyAttributes(mob, GeneEffects.sources(level.registryAccess(), carried));
	}

	/**
	 * Counts a death for each pressure that it matches. Each gene that counters one of these pressures rises
	 * in the pool of the species, in the category of the biome where the mob died.
	 * A player that a mob kills also makes the genes of the killer rise.
	 */
	public static void onDeath(LivingEntity entity, DamageSource source) {
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		if (entity instanceof Player && source.getEntity() instanceof Mob killer) {
			if (entity instanceof ServerPlayer victim) {
				Discovery.learn(victim, killer);
			}
			onPlayerKilled(level, killer, source);
			raiseVictim(entity, source);
		}
		if (!(entity instanceof Mob mob)) {
			return;
		}
		if (source.getEntity() instanceof ServerPlayer killer) {
			Discovery.learn(killer, mob);
		}
		GeneCombat.onDeath(mob, level, source);
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		select(level, mob.getType(), category, new DeathContext(source, facts(mob, source, level)));

		List<CrowdTracker.Death> crowd = CROWDS.add(new CrowdTracker.Death(level.dimension(), species, category,
				mob.position(), level.getGameTime(), false));
		for (CrowdTracker.Death death : crowd) {
			select(level, mob.getType(), death.category(), new DeathContext(source, Set.of(DeathCondition.CROWD)));
		}
	}

	/**
	 * Raises the genes that a killer mob carries, and counts the player death pressure for its species.
	 */
	public static void onPlayerKilled(ServerLevel level, Mob killer, DamageSource source) {
		CarriedGenes carried = killer.getAttached(ModAttachments.GENES);
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(killer.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(killer.blockPosition()));
		if (carried != null) {
			Registry<Gene> genes = level.registryAccess().lookupOrThrow(ModRegistries.GENE);
			GenePools pools = GenePools.get(level.getServer());
			for (Identifier id : carried.genes()) {
				Gene gene = genes.getValue(id);
				if (gene != null) {
					pools.select(species, category, id, gene.selectionRate());
				}
			}
		}
		select(level, killer.getType(), category, new DeathContext(source, Set.of(DeathCondition.PLAYER_DEATH)));
	}

	private static void raiseVictim(LivingEntity entity, DamageSource source) {
		if (entity instanceof Player player && source.getEntity() instanceof Mob killer
				&& player.level() instanceof ServerLevel level) {
			GeneBehaviors.raiseVictim(killer, player, level);
		}
	}

	/**
	 * Raises each gene of the species that counters a pressure that the context matches.
	 */
	public static void select(ServerLevel level, EntityType<?> type, BiomeCategory category, DeathContext context) {
		Set<ResourceKey<Pressure>> matched = new HashSet<>();
		for (Map.Entry<ResourceKey<Pressure>, Pressure> entry
				: level.registryAccess().lookupOrThrow(ModRegistries.PRESSURE).entrySet()) {
			if (entry.getValue().matches(context)) {
				matched.add(entry.getKey());
			}
		}
		if (matched.isEmpty()) {
			return;
		}
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		GenePools pools = GenePools.get(level.getServer());
		for (Map.Entry<ResourceKey<Gene>, Gene> entry : level.registryAccess().lookupOrThrow(ModRegistries.GENE).entrySet()) {
			Gene gene = entry.getValue();
			if (gene.carriedBy(type) && matched.contains(gene.counters())) {
				pools.select(species, category, entry.getKey().identifier(), gene.selectionRate());
			}
		}
	}

	/**
	 * Finds the facts of the death of a mob, except the crowd fact. The crowd fact needs the other deaths.
	 */
	public static Set<DeathCondition> facts(Mob mob, DamageSource source, ServerLevel level) {
		Set<DeathCondition> facts = EnumSet.noneOf(DeathCondition.class);
		long now = level.getGameTime();
		CombatRecord combat = mob.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
		DeathTrace trace = mob.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE);
		if (combat.targetedPlayer() && !combat.damagedPlayer()) {
			facts.add(DeathCondition.UNREACHED);
		}
		if (source.is(DamageTypeTags.IS_FIRE) && isSunBurning(mob, now)) {
			facts.add(DeathCondition.SUNLIGHT);
		}
		if (source.getEntity() instanceof Player player) {
			if (trace.criticalTime() == now) {
				facts.add(DeathCondition.CRITICAL);
			}
			if (trace.spotter().filter(player.getUUID()::equals).isPresent() && trace.spottedSeconds() >= SPOTTED_SECONDS) {
				facts.add(DeathCondition.SPOTTED);
			}
		}
		if (source.getEntity() instanceof IronGolem) {
			facts.add(DeathCondition.GOLEM);
		}
		facts.add(mob.isInWater() ? DeathCondition.IN_WATER : DeathCondition.ON_LAND);
		boolean coldBiome = BiomeCategory.of(level.getBiome(mob.blockPosition())) == BiomeCategory.COLD;
		if (coldBiome || source.is(DamageTypeTags.IS_FREEZING)) {
			facts.add(DeathCondition.COLD);
		}
		if (!coldBiome) {
			facts.add(DeathCondition.WARM);
		}
		if (GeneCombat.snowingOn(mob)) {
			facts.add(DeathCondition.SNOW);
		}
		if (BiomeCategory.of(level.getBiome(mob.blockPosition())) != BiomeCategory.WET) {
			facts.add(DeathCondition.DRY);
		}
		if (GeneCombat.hasEnchantment(source, net.minecraft.world.item.enchantment.Enchantments.SMITE)) {
			facts.add(DeathCondition.SMITE);
		}
		if (GeneCombat.hasEnchantment(source, net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS)) {
			facts.add(DeathCondition.BANE);
		}
		if (mob.getAttachedOrElse(ModAttachments.RAID_DEATH, false)) {
			facts.add(DeathCondition.RAID);
		}
		if (mob instanceof net.minecraft.world.entity.monster.skeleton.Bogged bogged && bogged.isSheared()) {
			facts.add(DeathCondition.SHEARS);
		}
		if (mob instanceof net.minecraft.world.entity.monster.Creeper creeper
				&& (creeper.isIgnited() || creeper.getSwellDir() > 0)) {
			facts.add(DeathCondition.FUSE);
		}
		if (level.isDarkOutside()) {
			facts.add(DeathCondition.NIGHT);
		} else {
			facts.add(DeathCondition.DAY);
		}
		if (source.getEntity() != null && source.getEntity().getVehicle() instanceof AbstractBoat) {
			facts.add(DeathCondition.BOAT);
		}
		if (source.getEntity() instanceof Wolf) {
			facts.add(DeathCondition.WOLF);
		}
		if (source.getEntity() instanceof LivingEntity killer && usedShield(killer, level.getGameTime())) {
			facts.add(DeathCondition.SHIELD);
		}
		AABB area = mob.getBoundingBox().inflate(CrowdTracker.RADIUS);
		if (level.getEntities(mob.getType(), area, other -> other != mob && other.isAlive()).isEmpty()) {
			facts.add(DeathCondition.ALONE);
		}
		return facts;
	}

	/**
	 * Records each second if a player sees a mob from far away. Only mobs with genes need it.
	 */
	public static void trackSpotter(Mob mob, ServerLevel level) {
		Player player = level.getNearestPlayer(mob, SPOT_RANGE);
		DeathTrace trace = mob.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE);
		DeathTrace next = player != null && isSpottedBy(mob, player) ? trace.seenBy(player.getUUID()) : trace.unseen();
		if (!next.equals(trace)) {
			mob.setAttached(ModAttachments.TRACE, next);
		}
	}

	/**
	 * Records the time for each player near the mob who looks at it now. The unseen state needs the last look.
	 * A record older than the look time goes away. Only a mob whose effects use the unseen state keeps a record.
	 */
	public static void trackLooks(Mob mob, ServerLevel level) {
		if (!GeneEffects.usesState(mob, GeneEffect.When.UNSEEN)) {
			mob.removeAttached(ModAttachments.LOOK_TIMES);
			return;
		}
		long now = level.getGameTime();
		Map<java.util.UUID, Long> before = mob.getAttachedOrElse(ModAttachments.LOOK_TIMES, Map.of());
		Map<java.util.UUID, Long> looks = new java.util.HashMap<>();
		before.forEach((player, time) -> {
			if (now - time <= LOOK_TICKS) {
				looks.put(player, time);
			}
		});
		for (Player player : level.players()) {
			if (!player.isSpectator() && looksAt(player, mob)) {
				looks.put(player.getUUID(), now);
			}
		}
		if (!looks.equals(before)) {
			mob.setAttached(ModAttachments.LOOK_TIMES, looks);
		}
	}

	/**
	 * Returns true if the mob is in the view of the player now, in the look range, with nothing between them.
	 */
	public static boolean looksAt(LivingEntity viewer, LivingEntity mob) {
		Vec3 toMob = mob.getEyePosition().subtract(viewer.getEyePosition());
		double distance = toMob.length();
		if (distance > LOOK_RANGE) {
			return false;
		}
		return (distance < 1.0E-4 || viewer.getViewVector(1.0F).dot(toMob.scale(1.0 / distance)) >= SPOT_VIEW_COS)
				&& viewer.hasLineOfSight(mob);
	}

	/**
	 * Returns true if the viewer did not see the mob: the mob is behind the viewer, or the viewer did not look at it
	 * in the last 3 seconds. Only the looks of players are recorded.
	 */
	public static boolean unseen(LivingEntity mob, LivingEntity viewer) {
		Vec3 facing = viewer.getViewVector(1.0F).multiply(1.0, 0.0, 1.0);
		Vec3 toMob = mob.position().subtract(viewer.position()).multiply(1.0, 0.0, 1.0);
		if (facing.lengthSqr() > 1.0E-4 && toMob.lengthSqr() > 1.0E-4 && facing.normalize().dot(toMob.normalize()) < 0.0) {
			return true;
		}
		if (looksAt(viewer, mob)) {
			return false;
		}
		Long last = mob.getAttachedOrElse(ModAttachments.LOOK_TIMES, Map.of()).get(viewer.getUUID());
		return last == null || mob.level().getGameTime() - last > LOOK_TICKS;
	}

	private static boolean isSpottedBy(Mob mob, Player player) {
		Vec3 toMob = mob.getEyePosition().subtract(player.getEyePosition());
		double distance = toMob.length();
		if (distance <= SPOT_MIN_DISTANCE || distance > SPOT_RANGE) {
			return false;
		}
		double facing = player.getViewVector(1.0F).dot(toMob.scale(1.0 / distance));
		return facing >= SPOT_VIEW_COS && player.hasLineOfSight(mob);
	}

	/**
	 * Tells if the sun set the mob on fire a short time ago, so that its fire comes from the sun.
	 */
	public static boolean isSunBurning(LivingEntity mob, long now) {
		long time = mob.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).sunBurnTime();
		return time >= 0L && now - time <= SUN_BURN_TICKS;
	}

	public static void markSunBurn(Mob mob) {
		if (mob.level() instanceof ServerLevel level) {
			mob.setAttached(ModAttachments.TRACE,
					mob.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).withSunBurn(level.getGameTime()));
		}
	}

	public static void markCritical(Entity target) {
		if (target.level() instanceof ServerLevel level) {
			target.setAttached(ModAttachments.TRACE,
					target.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).withCritical(level.getGameTime()));
		}
	}

	private static void decay(MinecraftServer server) {
		long day = server.overworld().getGameTime() / TICKS_PER_DAY;
		GenePools pools = GenePools.get(server);
		if (day == pools.lastDecayDay()) {
			return;
		}
		Registry<Gene> genes = server.registryAccess().lookupOrThrow(ModRegistries.GENE);
		pools.decayTo(day, id -> {
			Gene gene = genes.getValue(id);
			return gene != null ? gene.decayRate() : DEFAULT_DECAY_RATE;
		});
	}
}
