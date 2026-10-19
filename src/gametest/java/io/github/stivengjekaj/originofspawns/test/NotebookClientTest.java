package io.github.stivengjekaj.originofspawns.test;

import java.util.Set;

import io.github.stivengjekaj.originofspawns.client.screen.NotebookScreen;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
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
				player.setAttached(ModAttachments.DISCOVERED, new DiscoveredFamilies(Set.of(
						"family.originofspawns.zombie.predator", "family.originofspawns.zombie.stalker.predator",
						"family.originofspawns.zombie.horde.and.tank", "family.originofspawns.neutral")));
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BEAGLE_NOTEBOOK));
				player.getItemInHand(InteractionHand.MAIN_HAND).use(player.level(), player, InteractionHand.MAIN_HAND);
			});
			context.waitForScreen(NotebookScreen.class);
			int zombie = context.computeOnClient(client -> zombiePage(client));
			context.runOnClient(client -> ((NotebookScreen) client.gui.screen()).show(zombie, NotebookScreen.Section.GENES));
			context.waitTicks(2);
			context.takeScreenshot("notebook");
			context.runOnClient(client -> ((NotebookScreen) client.gui.screen()).scrollTo(100));
			context.waitTicks(2);
			context.takeScreenshot("notebook_scrolled");
			context.runOnClient(client -> ((NotebookScreen) client.gui.screen()).show(zombie, NotebookScreen.Section.FAMILIES));
			context.waitTicks(2);
			context.takeScreenshot("notebook_families");
			context.setScreen(() -> null);
			context.waitTicks(5);
			context.takeScreenshot("notebook_in_hand");
		}
	}

	private static int zombiePage(net.minecraft.client.Minecraft client) {
		return ((NotebookScreen) client.gui.screen()).speciesIndex(net.minecraft.resources.Identifier.withDefaultNamespace("zombie"));
	}
}
