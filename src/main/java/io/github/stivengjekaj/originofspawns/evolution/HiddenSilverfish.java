package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The silverfish with genes that hide in blocks. A silverfish that hides in a block keeps its genes there, and the
 * silverfish that comes out of the block gets them back. The world saves the hidden silverfish in
 * {@code data/originofspawns/hidden_silverfish.dat}.
 */
public final class HiddenSilverfish extends SavedData {
	/**
	 * A silverfish in a block.
	 *
	 * @param genes the genes of the silverfish
	 * @param extra the number of other silverfish that come out of the block with it. They get genes from the pool
	 * @param hideTime the game time of the hide
	 */
	public record Entry(ResourceKey<Level> dimension, BlockPos pos, CarriedGenes genes, int extra, long hideTime) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Entry::dimension),
				BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
				CarriedGenes.CODEC.fieldOf("genes").forGetter(Entry::genes),
				Codec.INT.optionalFieldOf("extra", 0).forGetter(Entry::extra),
				Codec.LONG.optionalFieldOf("hide_time", -1L).forGetter(Entry::hideTime)
		).apply(instance, Entry::new));

		boolean at(ServerLevel level, BlockPos other) {
			return dimension.equals(level.dimension()) && pos.equals(other);
		}
	}

	public static final Codec<HiddenSilverfish> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Entry.CODEC.listOf().fieldOf("silverfish").forGetter(hidden -> hidden.entries)
	).apply(instance, HiddenSilverfish::new));

	public static final SavedDataType<HiddenSilverfish> TYPE =
			new SavedDataType<>(OriginOfSpawns.id("hidden_silverfish"), HiddenSilverfish::new, CODEC, null);

	private final List<Entry> entries = new ArrayList<>();

	public HiddenSilverfish() {
	}

	private HiddenSilverfish(List<Entry> entries) {
		this.entries.addAll(entries);
	}

	public static HiddenSilverfish get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	/**
	 * Keeps a silverfish in the block at the place. It takes the place of an older silverfish in the same block.
	 */
	public void put(ServerLevel level, BlockPos pos, CarriedGenes genes, int extra) {
		entries.removeIf(entry -> entry.at(level, pos));
		entries.add(new Entry(level.dimension(), pos.immutable(), genes, extra, level.getGameTime()));
		setDirty();
	}

	/**
	 * Takes the silverfish out of the block at the place.
	 */
	public Optional<Entry> take(ServerLevel level, BlockPos pos) {
		for (Entry entry : entries) {
			if (entry.at(level, pos)) {
				entries.remove(entry);
				setDirty();
				return Optional.of(entry);
			}
		}
		return Optional.empty();
	}

	public Optional<Entry> find(ServerLevel level, BlockPos pos) {
		return entries.stream().filter(entry -> entry.at(level, pos)).findFirst();
	}
}
