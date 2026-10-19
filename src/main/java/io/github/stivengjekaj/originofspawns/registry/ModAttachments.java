package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;

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

	private ModAttachments() {
	}

	/**
	 * Loads this class, so that the attachment types register.
	 */
	public static void register() {
	}
}
