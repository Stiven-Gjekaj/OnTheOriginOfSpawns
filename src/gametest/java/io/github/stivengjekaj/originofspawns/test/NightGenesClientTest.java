package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

/**
 * Shows the genes that change at night: Shade, which is translucent, and the glowing fuse of Death Throes.
 * The screenshot is for a person to check. From left to right: plain, Shade, and Death Throes.
 */
public class NightGenesClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("time set midnight");
			singleplayer.getServer().runCommand("weather clear");
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 10.0F);
				Vec3 front = player.position().add(0.0, 0.0, 3.0);
				spawn(player, front.add(1.2, 0.0, 0.0), List.of());
				spawn(player, front, List.of(Identifier.fromNamespaceAndPath("originofspawns", "shade")));
				spawn(player, front.add(-1.2, 0.0, 0.0), List.of(Identifier.fromNamespaceAndPath("originofspawns", "death_throes")));
			});
			context.waitTicks(40);
			int light = context.computeOnClient(client -> client.level.getMaxLocalRawBrightness(client.player.blockPosition()));
			if (light > 7) {
				throw new AssertionError("The test place is too bright for Shade: light " + light);
			}
			context.takeScreenshot("night_genes");
		}
	}

	private static void spawn(ServerPlayer player, Vec3 pos, List<Identifier> genes) {
		Zombie zombie = EntityTypes.ZOMBIE.create(player.level(), EntitySpawnReason.COMMAND);
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.setPos(pos);
		zombie.setYRot(150.0F);
		zombie.setYHeadRot(150.0F);
		zombie.setYBodyRot(150.0F);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(zombie);
	}
}
