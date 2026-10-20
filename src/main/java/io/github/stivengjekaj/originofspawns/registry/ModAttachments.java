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

	private ModAttachments() {
	}

	/**
	 * Loads this class, so that the attachment types register.
	 */
	public static void register() {
	}
}
