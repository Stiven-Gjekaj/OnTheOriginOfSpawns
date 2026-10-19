package io.github.stivengjekaj.originofspawns.notebook;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The notebook page of one species. The server sends it when a player opens the notebook.
 */
public record NotebookPayload(Identifier species, List<Entry> entries) implements CustomPacketPayload {
	public static final Type<NotebookPayload> TYPE = new Type<>(OriginOfSpawns.id("notebook"));

	/**
	 * The frequency of one gene in one biome category.
	 */
	public record Entry(BiomeCategory category, Identifier gene, float frequency) {
		private static final StreamCodec<RegistryFriendlyByteBuf, BiomeCategory> CATEGORY_CODEC =
				ByteBufCodecs.VAR_INT.map(index -> BiomeCategory.values()[index], BiomeCategory::ordinal).cast();

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				CATEGORY_CODEC, Entry::category,
				Identifier.STREAM_CODEC.cast(), Entry::gene,
				ByteBufCodecs.FLOAT.cast(), Entry::frequency,
				Entry::new);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NotebookPayload> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC.cast(), NotebookPayload::species,
			Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), NotebookPayload::entries,
			NotebookPayload::new);

	@Override
	public Type<NotebookPayload> type() {
		return TYPE;
	}
}
