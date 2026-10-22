package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.DeathTrace;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.network.codec.ByteBufCodecs;

/**
 * The data that the mod attaches to entities.
 */
public final class ModAttachments {
	public static final AttachmentType<CombatRecord> COMBAT =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("combat"), CombatRecord.CODEC);

	/**
	 * The genes of a mob. The client needs them to draw the mob.
	 */
	public static final AttachmentType<CarriedGenes> GENES = AttachmentRegistry.create(OriginOfSpawns.id("genes"),
			builder -> builder.persistent(CarriedGenes.CODEC)
					.syncWith(CarriedGenes.STREAM_CODEC, AttachmentSyncPredicate.all()));

	/**
	 * Short-lived facts for the death of a mob. The game does not save them.
	 */
	public static final AttachmentType<DeathTrace> TRACE = AttachmentRegistry.create(OriginOfSpawns.id("trace"));

	/**
	 * Short-lived state of the behavior effects. The game does not save it.
	 */
	public static final AttachmentType<BehaviorState> STATE = AttachmentRegistry.create(OriginOfSpawns.id("state"));

	/**
	 * How a mob spawned. The genes need it one time, when the mob first joins a world.
	 */
	public static final AttachmentType<net.minecraft.world.entity.EntitySpawnReason> SPAWN_REASON =
			AttachmentRegistry.create(OriginOfSpawns.id("spawn_reason"));

	/**
	 * The families that a player found. The player keeps them after death. Only the player gets them on the client,
	 * for the mob info panel.
	 */
	public static final AttachmentType<DiscoveredFamilies> DISCOVERED = AttachmentRegistry.create(
			OriginOfSpawns.id("discovered"), builder -> builder.persistent(DiscoveredFamilies.CODEC).copyOnDeath()
					.syncWith(DiscoveredFamilies.STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

	/**
	 * The game time when a mob first got its genes. Living evolution needs the age of the mob.
	 */
	public static final AttachmentType<Long> BORN =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("born"), com.mojang.serialization.Codec.LONG);

	/**
	 * True when a mob used its one survival of a lethal hit.
	 */
	public static final AttachmentType<Boolean> UNDYING_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("undying_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * True when a mob used its one heal to full health.
	 */
	public static final AttachmentType<Boolean> HEAL_ONCE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("heal_once_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * The game time when a player last blocked a hit with a shield. The game does not save it.
	 */
	public static final AttachmentType<Long> SHIELD_BLOCK = AttachmentRegistry.create(OriginOfSpawns.id("shield_block"));

	/**
	 * True when a mob used its one fall apart and return.
	 */
	public static final AttachmentType<Boolean> REASSEMBLE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("reassemble_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * The game time of the last fan shot of a mob.
	 */
	public static final AttachmentType<Long> FAN_SHOT_TIME = AttachmentRegistry.create(OriginOfSpawns.id("fan_shot_time"));

	/**
	 * On an arrow: the distance past which players do not see the arrow while it flies. The clients get it.
	 */
	public static final AttachmentType<Float> GHOST_ARROW = AttachmentRegistry.create(OriginOfSpawns.id("ghost_arrow"),
			builder -> builder.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.all()));

	/**
	 * The game time of the last shear of a bogged.
	 */
	public static final AttachmentType<Long> SHEAR_TIME = AttachmentRegistry.createPersistent(OriginOfSpawns.id("shear_time"),
			com.mojang.serialization.Codec.LONG);

	/**
	 * The game time of the last parry of a mob.
	 */
	public static final AttachmentType<Long> PARRY_TIME = AttachmentRegistry.create(OriginOfSpawns.id("parry_time"));

	/**
	 * The game time of the last melee hit of a mob on each player, by the UUID of the player.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Long>> PLAYER_HITS =
			AttachmentRegistry.create(OriginOfSpawns.id("player_hits"));

	/**
	 * The game time of the last skull shot of a mob.
	 */
	public static final AttachmentType<Long> SKULL_TIME = AttachmentRegistry.create(OriginOfSpawns.id("skull_time"));

	/**
	 * On a player: the game time of the last item that a gene made the player drop.
	 */
	public static final AttachmentType<Long> DISARM_TIME = AttachmentRegistry.create(OriginOfSpawns.id("disarm_time"));

	/**
	 * The game time of the last cobweb of the web trail of a mob.
	 */
	public static final AttachmentType<Long> WEB_TRAIL_TIME = AttachmentRegistry.create(OriginOfSpawns.id("web_trail_time"));

	/**
	 * The game time of the last use of each effect on hurt of a mob. Each effect has its own cooldown.
	 */
	public static final AttachmentType<java.util.Map<io.github.stivengjekaj.originofspawns.definition.GeneEffect.EffectOnHurt, Long>>
			HURT_EFFECT_TIMES = AttachmentRegistry.create(OriginOfSpawns.id("hurt_effect_times"));

	/**
	 * True when a chain reaction lit the fuse of a creeper. Such a creeper does not light more creepers.
	 */
	public static final AttachmentType<Boolean> CHAIN_LIT =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("chain_lit"), com.mojang.serialization.Codec.BOOL);

	/**
	 * On a lightning bolt: true when the bolt does not charge the creepers that it hits.
	 */
	public static final AttachmentType<Boolean> NO_CHARGE = AttachmentRegistry.create(OriginOfSpawns.id("no_charge"));

	/**
	 * The game time when each player last looked at a mob, by the UUID of the player. The game does not save it.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Long>> LOOK_TIMES =
			AttachmentRegistry.create(OriginOfSpawns.id("look_times"));

	private ModAttachments() {
	}

	/**
	 * Loads this class, so that the attachment types register.
	 */
	public static void register() {
	}
}
