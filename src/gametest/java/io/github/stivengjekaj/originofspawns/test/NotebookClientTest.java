package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.client.screen.NotebookScreen;
import io.github.stivengjekaj.originofspawns.registry.ModItems;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Uses the Beagle Notebook in the game, and checks that its screen opens.
 */
public class NotebookClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("originofspawns pool set minecraft:zombie hot originofspawns:cinder 0.42");
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BEAGLE_NOTEBOOK));
				player.getItemInHand(InteractionHand.MAIN_HAND).use(player.level(), player, InteractionHand.MAIN_HAND);
			});
			context.waitForScreen(NotebookScreen.class);
			context.takeScreenshot("notebook");
			context.setScreen(() -> null);
			context.waitTicks(5);
			context.takeScreenshot("notebook_in_hand");
		}
	}
}
