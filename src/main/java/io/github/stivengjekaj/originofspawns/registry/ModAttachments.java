package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.DeathTrace;

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

	private ModAttachments() {
	}

	/**
	 * Loads this class, so that the attachment types register.
	 */
	public static void register() {
	}
}
