package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.DeathContext;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.Pressure;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Connects the evolution of the mod to the game.
 */
public final class Evolution {
	private static final long TICKS_PER_DAY = 24000L;

	/**
	 * The decay rate of a gene that the data packs no longer have.
	 */
	private static final float DEFAULT_DECAY_RATE = 0.01F;
	private Evolution() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register(Evolution::onLoad);
		ServerLivingEntityEvents.AFTER_DEATH.register(Evolution::onDeath);
		ServerTickEvents.END_SERVER_TICK.register(Evolution::decay);
	}

	private static void onLoad(Entity entity, ServerLevel level) {
		if (entity instanceof Mob mob && !mob.hasAttached(ModAttachments.GENES)) {
			rollGenes(mob, level);
		}
	}

	/**
	 * Gives a new mob its genes from the pool of its species in the category of its biome.
	 * A mob of a species that no gene can go to gets no genes.
	 */
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
		Map<Identifier, Gene> genes = new LinkedHashMap<>();
		for (Identifier id : rolled) {
			genes.put(id, registry.getValue(id));
		}
		mob.setAttached(ModAttachments.GENES, new CarriedGenes(rolled));
		GeneEffects.applyAttributes(mob, genes);
	}

	/**
	 * Counts a death for each pressure that it matches. Each gene that counters one of these pressures rises
	 * in the pool of the species, in the category of the biome where the mob died.
	 */
	public static void onDeath(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) {
			return;
		}
		DeathContext context = new DeathContext(source, mob.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE));
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
		Identifier species = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		BiomeCategory category = BiomeCategory.of(level.getBiome(mob.blockPosition()));
		GenePools pools = GenePools.get(level.getServer());
		for (Map.Entry<ResourceKey<Gene>, Gene> entry : level.registryAccess().lookupOrThrow(ModRegistries.GENE).entrySet()) {
			Gene gene = entry.getValue();
			if (gene.carriedBy(mob.getType()) && matched.contains(gene.counters())) {
				pools.select(species, category, entry.getKey().identifier(), gene.selectionRate());
			}
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
