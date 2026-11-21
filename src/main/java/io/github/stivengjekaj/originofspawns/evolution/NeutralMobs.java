package io.github.stivengjekaj.originofspawns.evolution;

import java.util.UUID;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;

/**
 * The rules of the neutral mobs that fight the player, such as the wolf. A tame mob keeps its genes, gets no new genes,
 * and its death does not count in the pool. An iron golem that a player builds gets no genes.
 */
public final class NeutralMobs {
	/**
	 * The neutral species that fight the player. Only these species use the tame rule.
	 */
	public static final TagKey<EntityType<?>> NEUTRAL_FIGHTERS =
			TagKey.create(Registries.ENTITY_TYPE, OriginOfSpawns.id("neutral_fighters"));

	private NeutralMobs() {
	}

	/**
	 * Returns true if the entity is of a species in the tag {@code originofspawns:neutral_fighters}.
	 */
	public static boolean isNeutralFighter(Entity entity) {
		return entity.getType().builtInRegistryHolder().is(NEUTRAL_FIGHTERS);
	}

	/**
	 * Returns true if the mob is a neutral fighter with an owner, for example a tame wolf or a tame llama.
	 */
	public static boolean isTame(Entity entity) {
		return isNeutralFighter(entity) && owner(entity) != null;
	}

	/**
	 * Returns true if the mob is out of the pool: it is tame, or it is an iron golem that a player built. Such a mob
	 * gets no new genes, and its death and its kills do not count in the pool.
	 */
	public static boolean outsidePool(Entity entity) {
		return isTame(entity) || isNeutralFighter(entity) && entity instanceof IronGolem golem && golem.isPlayerCreated();
	}

	/**
	 * Returns the UUID of the owner of the entity, or null if it has no owner.
	 */
	public static UUID owner(Entity entity) {
		if (entity instanceof OwnableEntity ownable) {
			EntityReference<LivingEntity> reference = ownable.getOwnerReference();
			return reference == null ? null : reference.getUUID();
		}
		return null;
	}
}
