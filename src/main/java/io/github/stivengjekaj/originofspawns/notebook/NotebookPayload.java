package io.github.stivengjekaj.originofspawns.notebook;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The pages of the notebook: one page for each species. The server sends them when a player opens the notebook.
 */
public record NotebookPayload(List<Page> pages) implements CustomPacketPayload {
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

	/**
	 * One family name of a species, and how much the player knows about it.
	 */
	public record FamilyEntry(String key, FamilyResult.Kind kind, Knowledge knowledge) {
		private static final StreamCodec<RegistryFriendlyByteBuf, Knowledge> KNOWLEDGE_CODEC =
				ByteBufCodecs.VAR_INT.map(index -> Knowledge.values()[index], Knowledge::ordinal).cast();

		private static final StreamCodec<RegistryFriendlyByteBuf, FamilyResult.Kind> KIND_CODEC =
				ByteBufCodecs.VAR_INT.map(index -> FamilyResult.Kind.values()[index], FamilyResult.Kind::ordinal).cast();

		public static final StreamCodec<RegistryFriendlyByteBuf, FamilyEntry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8.cast(), FamilyEntry::key,
				KIND_CODEC, FamilyEntry::kind,
				KNOWLEDGE_CODEC, FamilyEntry::knowledge,
				FamilyEntry::new);

		/**
		 * Tells if the player knows the family.
		 */
		public boolean found() {
			return knowledge == Knowledge.KNOWN;
		}
	}

	/**
	 * The page of one species: its genes, with their frequencies, and its family names.
	 */
	public record Page(Identifier species, List<Entry> entries, List<FamilyEntry> families) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Page> STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC.cast(), Page::species,
				Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), Page::entries,
				FamilyEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), Page::families,
				Page::new);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NotebookPayload> STREAM_CODEC = StreamCodec.composite(
			Page.STREAM_CODEC.apply(ByteBufCodecs.list()), NotebookPayload::pages,
			NotebookPayload::new);

	@Override
	public Type<NotebookPayload> type() {
		return TYPE;
	}
}
