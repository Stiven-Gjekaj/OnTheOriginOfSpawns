package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The blocks that genes put in the world for a short time, for example the cobweb of a spider. Each block goes away
 * at its time and drops nothing. A player that breaks one also gets no drops. The world saves the blocks in
 * {@code data/originofspawns/temporary_blocks.dat}, so a block that waits when the server stops still goes away.
 */
public final class TemporaryBlocks extends SavedData {
	/**
	 * A block that goes away at the game time.
	 */
	public record Entry(ResourceKey<Level> dimension, BlockPos pos, Block block, long until) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Entry::dimension),
				BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
				BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Entry::block),
				Codec.LONG.fieldOf("until").forGetter(Entry::until)
		).apply(instance, Entry::new));

		boolean at(ServerLevel level, BlockPos other) {
			return dimension.equals(level.dimension()) && pos.equals(other);
		}
	}

	public static final Codec<TemporaryBlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Entry.CODEC.listOf().fieldOf("blocks").forGetter(blocks -> blocks.entries)
	).apply(instance, TemporaryBlocks::new));

	public static final SavedDataType<TemporaryBlocks> TYPE =
			new SavedDataType<>(OriginOfSpawns.id("temporary_blocks"), TemporaryBlocks::new, CODEC, null);

	/**
	 * The game checks the times once each second.
	 */
	private static final int CHECK_TICKS = 20;

	private final List<Entry> entries = new ArrayList<>();

	public TemporaryBlocks() {
	}

	private TemporaryBlocks(List<Entry> entries) {
		this.entries.addAll(entries);
	}

	public static TemporaryBlocks get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			if (level.getGameTime() % CHECK_TICKS == 0L) {
				get(level.getServer()).removeExpired(level);
			}
		});
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
				!(level instanceof ServerLevel serverLevel) || !get(serverLevel.getServer()).breakWithoutDrops(serverLevel, pos));
	}

	/**
	 * Puts a temporary block in the world for the ticks. The block goes only in air, so it replaces no block.
	 *
	 * @return true if the block went in the world
	 */
	public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int ticks) {
		if (!level.getBlockState(pos).isAir() || !level.setBlock(pos, state, Block.UPDATE_ALL)) {
			return false;
		}
		TemporaryBlocks blocks = get(level.getServer());
		blocks.entries.removeIf(entry -> entry.at(level, pos));
		blocks.entries.add(new Entry(level.dimension(), pos.immutable(), state.getBlock(), level.getGameTime() + ticks));
		blocks.setDirty();
		return true;
	}

	/**
	 * Returns true if a gene put the block at the place, and it did not go away yet.
	 */
	public static boolean isTemporary(ServerLevel level, BlockPos pos) {
		return get(level.getServer()).entries.stream().anyMatch(entry -> entry.at(level, pos));
	}

	private void removeExpired(ServerLevel level) {
		long now = level.getGameTime();
		// A block in a chunk that is not loaded waits until the chunk loads.
		boolean changed = entries.removeIf(entry -> {
			if (!entry.dimension().equals(level.dimension()) || entry.until() > now || !level.isLoaded(entry.pos())) {
				return false;
			}
			if (level.getBlockState(entry.pos()).is(entry.block())) {
				level.setBlock(entry.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
			return true;
		});
		if (changed) {
			setDirty();
		}
	}

	/**
	 * Removes a temporary block that a player breaks, with no drops.
	 *
	 * @return true if the block was temporary
	 */
	private boolean breakWithoutDrops(ServerLevel level, BlockPos pos) {
		boolean temporary = entries.removeIf(entry -> entry.at(level, pos) && level.getBlockState(pos).is(entry.block()));
		if (temporary) {
			setDirty();
			level.removeBlock(pos, false);
		}
		return temporary;
	}
}
