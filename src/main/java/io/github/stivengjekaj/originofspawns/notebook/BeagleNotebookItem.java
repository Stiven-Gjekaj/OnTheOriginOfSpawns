package io.github.stivengjekaj.originofspawns.notebook;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The Beagle Notebook. In milestone 1, it shows one page: the gene frequencies of the zombie.
 */
public class BeagleNotebookItem extends Item {
	public BeagleNotebookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, NotebookPages.page(serverPlayer.level().getServer(), EntityTypes.ZOMBIE));
		}
		return InteractionResult.SUCCESS;
	}
}
