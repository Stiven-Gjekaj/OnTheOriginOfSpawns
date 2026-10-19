package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;

/**
 * Puts a zombie with genes under the crosshair, and shows the mob info panel.
 */
public class MobInfoClientTest implements FabricClientGameTest {
	private static Identifier mod(String path) {
		return Identifier.fromNamespaceAndPath("originofspawns", path);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("time set noon");
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
				Zombie zombie = EntityTypes.ZOMBIE.create(player.level(), EntitySpawnReason.COMMAND);
				zombie.setNoAi(true);
				zombie.setPersistenceRequired();
				zombie.setPos(player.position().add(0.0, 0.0, 2.5));
				zombie.setYRot(180.0F);
				zombie.setYHeadRot(180.0F);
				zombie.setYBodyRot(180.0F);
				zombie.setAttached(ModAttachments.GENES, new CarriedGenes(
						List.of(mod("lunger"), mod("frenzy"), mod("cinder"), mod("ironhide")),
						new FamilyResult(FamilyResult.Kind.MINOR, Optional.of(mod("zombie/predator")),
								Optional.of(mod("zombie/stalker")))));
				player.level().addFreshEntity(zombie);
			});
			context.waitTicks(40);
			boolean aimed = context.computeOnClient(client -> client.crosshairPickEntity instanceof Zombie);
			if (!aimed) {
				throw new AssertionError("The zombie is not under the crosshair");
			}
			context.takeScreenshot("mob_info");
		}
	}
}
