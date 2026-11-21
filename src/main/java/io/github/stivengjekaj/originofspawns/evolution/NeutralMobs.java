package io.github.stivengjekaj.originofspawns.evolution;

import java.util.Objects;
import java.util.UUID;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;

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

	/**
	 * Returns true if a call of the mob reaches the other mob: it is of the same species, and for a neutral fighter it
	 * has the same owner. A tame wolf calls only the wolves of its owner, and a wild wolf calls only wild wolves.
	 */
	public static boolean answersCall(Mob caller, Entity other) {
		return other.getType() == caller.getType() && (!isNeutralFighter(caller) || Objects.equals(owner(caller), owner(other)));
	}

	/**
	 * Returns true if the other entity is on the side of a tame mob: it is the owner, or it has the same owner.
	 */
	public static boolean friendly(Entity mob, Entity other) {
		UUID owner = isNeutralFighter(mob) ? owner(mob) : null;
		return owner != null && (owner.equals(other.getUUID()) || owner.equals(owner(other)));
	}

	/**
	 * Returns true if a gain that acts on players acts on the other entity. For a mob that is not a neutral fighter,
	 * this is each player. A wild neutral fighter acts only on a player that it targets, that it is angry at, or that
	 * hurt it. A tame mob acts on each living entity that it targets or that hurt it, as its vanilla bite does, but
	 * never on its owner or on a mob of the same owner.
	 */
	public static boolean isFoe(Entity mob, Entity other) {
		if (!(mob instanceof Mob fighter) || !isNeutralFighter(mob)) {
			return other instanceof Player;
		}
		if (!(other instanceof LivingEntity living) || other == mob) {
			return false;
		}
		boolean fought = fighter.getTarget() == living || fighter.getLastHurtByMob() == living;
		if (owner(mob) != null) {
			return fought && !friendly(mob, living);
		}
		if (!(living instanceof Player player)) {
			return false;
		}
		return fought || fighter instanceof NeutralMob neutral && fighter.level() instanceof ServerLevel level
				&& neutral.isAngryAt(player, level);
	}
}
