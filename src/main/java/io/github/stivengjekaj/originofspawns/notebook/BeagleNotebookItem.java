package io.github.stivengjekaj.originofspawns.notebook;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The Beagle Notebook. It shows a page for each species: its genes and its families.
 */
public class BeagleNotebookItem extends Item {
	public BeagleNotebookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, NotebookPages.pages(serverPlayer));
		}
		return InteractionResult.SUCCESS;
	}
}
