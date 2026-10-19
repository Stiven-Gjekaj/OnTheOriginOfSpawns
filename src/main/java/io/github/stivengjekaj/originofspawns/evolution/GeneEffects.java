package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Applies the effects of the genes that a mob carries.
 */
public final class GeneEffects {
	private GeneEffects() {
	}

	/**
	 * Returns the effects of all genes that the entity carries. A gene that the data packs no longer have is skipped.
	 */
	public static Stream<GeneEffect> of(Entity entity) {
		CarriedGenes carried = entity.getAttached(ModAttachments.GENES);
		if (carried == null || carried.genes().isEmpty()) {
			return Stream.empty();
		}
		Registry<Gene> genes = entity.level().registryAccess().lookupOrThrow(ModRegistries.GENE);
		return carried.genes().stream()
				.map(genes::getValue)
				.filter(gene -> gene != null)
				.flatMap(Gene::effects);
	}

	/**
	 * Adds the attribute changes of the genes to a new mob, and fills its health to the new maximum.
	 */
	public static void applyAttributes(Mob mob, Map<Identifier, Gene> genes) {
		for (Map.Entry<Identifier, Gene> entry : genes.entrySet()) {
			List<GeneEffect> effects = entry.getValue().effects().toList();
			for (int index = 0; index < effects.size(); index++) {
				if (effects.get(index) instanceof GeneEffect.AttributeChange change) {
					AttributeInstance attribute = mob.getAttribute(change.attribute());
					if (attribute != null) {
						attribute.addPermanentModifier(new AttributeModifier(
								modifierId(entry.getKey(), index), change.amount(), change.operation()));
					}
				}
			}
		}
		mob.setHealth(mob.getMaxHealth());
	}

	public static Identifier modifierId(Identifier gene, int index) {
		return OriginOfSpawns.id("gene/" + gene.getNamespace() + "/" + gene.getPath() + "/" + index);
	}

	/**
	 * Returns the damage after the damage_taken effects of the genes.
	 */
	public static float modifyDamageTaken(LivingEntity entity, DamageSource source, float amount) {
		float result = amount;
		for (GeneEffect effect : (Iterable<GeneEffect>) of(entity)::iterator) {
			if (effect instanceof GeneEffect.DamageTaken taken && source.is(taken.damageTypes())) {
				result *= taken.multiplier();
			}
		}
		return result;
	}

	/**
	 * Runs the effects that act when the mob hits a target.
	 */
	public static void onHit(Mob mob, Entity target) {
		for (GeneEffect effect : (Iterable<GeneEffect>) of(mob)::iterator) {
			if (effect instanceof GeneEffect.IgniteTarget ignite && mob.getRandom().nextFloat() < ignite.chance()) {
				target.igniteForSeconds(ignite.seconds());
			}
		}
	}

	/**
	 * Runs the effects that act each tick.
	 */
	public static void tick(LivingEntity entity, ServerLevel level) {
		List<GeneEffect> effects = new ArrayList<>();
		of(entity).forEach(effects::add);
		for (GeneEffect effect : effects) {
			if (effect instanceof GeneEffect.WaterDamage water) {
				if (entity.tickCount % water.interval() == 0 && entity.isInWaterOrRain()) {
					entity.hurtServer(level, level.damageSources().drown(), water.amount());
				}
			} else if (effect instanceof GeneEffect.AirLoss loss) {
				if (entity.isUnderWater() && !entity.canBreatheUnderwater()) {
					entity.setAirSupply(Math.max(entity.getAirSupply() - loss.extra(), -20));
				}
			}
		}
	}
}
