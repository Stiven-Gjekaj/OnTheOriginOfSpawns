package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.Family;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.mixin.AbstractArrowAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Applies the effects of the genes that a mob carries.
 */
public final class GeneEffects {
	private GeneEffects() {
	}

	/**
	 * A group of effects with one source: a gene, or one tier of a family.
	 *
	 * @param source the name of the source. It makes the ids of the attribute modifiers
	 */
	public record Source(String source, List<GeneEffect> effects) {
	}

	/**
	 * Returns the effects of all genes and of the family of the entity.
	 * A gene or a family that the data packs no longer have is skipped.
	 */
	public static Stream<GeneEffect> of(Entity entity) {
		CarriedGenes carried = entity.getAttached(ModAttachments.GENES);
		if (carried == null) {
			return Stream.empty();
		}
		return sources(entity.level().registryAccess(), carried).stream().flatMap(source -> source.effects().stream());
	}

	/**
	 * Returns the opacity of the mob when it is translucent now, from the light at the mob and the blocks around it.
	 * A mob that is not translucent now gives nothing. The client uses it to draw the mob.
	 */
	public static Optional<Float> translucentOpacity(Entity entity) {
		int light = entity.level().getMaxLocalRawBrightness(entity.blockPosition());
		return of(entity)
				.filter(effect -> effect instanceof GeneEffect.Translucent translucent && light <= translucent.maxLight()
						&& translucent.blocks().map(blocks -> inBlocks(entity, blocks)).orElse(true)
						&& (!translucent.nightOnly() || entity.level().isDarkOutside())
						&& (!(entity instanceof net.minecraft.world.entity.LivingEntity living)
								|| GeneCombat.test(living, translucent.when())))
				.map(effect -> ((GeneEffect.Translucent) effect).opacity())
				.min(Float::compare);
	}

	/**
	 * Returns the distance past which players do not see the entity now, if there is one. It is for a mob with a gene
	 * that hides it from far away, and for an arrow of a mob with ghost arrows while the arrow flies.
	 */
	public static Optional<Float> hiddenBeyond(Entity entity) {
		Float ghost = entity.getAttached(ModAttachments.GHOST_ARROW);
		if (ghost != null) {
			return entity instanceof AbstractArrow arrow && ((AbstractArrowAccessor) arrow).originofspawns$isInGround()
					? Optional.empty() : Optional.of(ghost);
		}
		if (!(entity instanceof LivingEntity living)) {
			return Optional.empty();
		}
		return of(entity).filter(effect -> effect instanceof GeneEffect.HiddenFromAfar hidden
						&& GeneCombat.test(living, hidden.when()))
				.map(effect -> ((GeneEffect.HiddenFromAfar) effect).blocks())
				.min(Float::compare);
	}

	private static boolean inBlocks(Entity entity, TagKey<Block> blocks) {
		BlockPos eyes = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
		return entity.level().getBlockState(entity.blockPosition()).is(blocks) || entity.level().getBlockState(eyes).is(blocks);
	}

	/**
	 * Returns the sources of effects of a mob: each gene, then the family tiers that its family result gives.
	 */
	public static List<Source> sources(RegistryAccess access, CarriedGenes carried) {
		List<Source> sources = new ArrayList<>();
		Registry<Gene> genes = access.lookupOrThrow(ModRegistries.GENE);
		for (Identifier id : carried.genes()) {
			Gene gene = genes.getValue(id);
			if (gene != null) {
				sources.add(new Source("gene/" + id.getNamespace() + "/" + id.getPath(), gene.effects().toList()));
			}
		}
		Registry<Family> families = access.lookupOrThrow(ModRegistries.FAMILY);
		FamilyResult result = carried.family();
		addFamily(sources, families, result.first(), result.kind().firstTier());
		addFamily(sources, families, result.second(), result.kind().secondTier());
		return sources;
	}

	private static void addFamily(List<Source> sources, Registry<Family> families, Optional<Identifier> id, int tier) {
		if (id.isEmpty() || tier == 0) {
			return;
		}
		Family family = families.getValue(id.get());
		if (family != null) {
			sources.add(new Source("family/" + id.get().getNamespace() + "/" + id.get().getPath() + "/" + tier,
					family.tier(tier).effects().toList()));
		}
	}

	/**
	 * Removes the attribute changes of the sources from a mob.
	 */
	public static void removeAttributes(Mob mob, List<Source> sources) {
		for (Source source : sources) {
			for (int index = 0; index < source.effects().size(); index++) {
				if (source.effects().get(index) instanceof GeneEffect.AttributeChange change) {
					AttributeInstance attribute = mob.getAttribute(change.attribute());
					if (attribute != null) {
						attribute.removeModifier(modifierId(source.source(), index));
					}
				}
			}
		}
	}

	/**
	 * Adds the attribute changes of the sources to a new mob, and fills its health to the new maximum.
	 */
	public static void applyAttributes(Mob mob, List<Source> sources) {
		for (Source source : sources) {
			for (int index = 0; index < source.effects().size(); index++) {
				if (source.effects().get(index) instanceof GeneEffect.AttributeChange change) {
					AttributeInstance attribute = mob.getAttribute(change.attribute());
					if (attribute != null) {
						attribute.addPermanentModifier(new AttributeModifier(
								modifierId(source.source(), index), change.amount(), change.operation()));
					}
				}
			}
		}
		mob.setHealth(mob.getMaxHealth());
	}

	public static Identifier modifierId(String source, int index) {
		return OriginOfSpawns.id(source + "/" + index);
	}

	public static Identifier modifierId(Identifier gene, int index) {
		return modifierId("gene/" + gene.getNamespace() + "/" + gene.getPath(), index);
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
