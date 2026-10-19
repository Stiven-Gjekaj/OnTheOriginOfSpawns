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
 * Shows baby zombies with gene marks and gene models, for a person to check. From left to right: a plain baby,
 * Runt with Cinder and Ironhide, Lurker with Thickskull, Bloat with Frenzy, Death Throes with Bonerunner and Burrower,
 * and an adult with the same genes as the second baby.
 */
public class BabyGenesClientTest implements FabricClientGameTest {
	private static Identifier gene(String name) {
		return Identifier.fromNamespaceAndPath("originofspawns", name);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// At night the zombies do not burn. Full brightness keeps them easy to see.
			singleplayer.getServer().runCommand("time set midnight");
			context.runOnClient(client -> client.options.gamma().set(1.0));
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 20.0F);
				Vec3 front = player.position().add(0.0, 0.0, 2.6);
				spawn(player, front.add(2.5, 0.0, 0.0), true, List.of());
				spawn(player, front.add(1.5, 0.0, 0.0), true, List.of(gene("runt"), gene("cinder"), gene("ironhide")));
				spawn(player, front.add(0.5, 0.0, 0.0), true, List.of(gene("lurker"), gene("thickskull")));
				spawn(player, front.add(-0.5, 0.0, 0.0), true, List.of(gene("bloat"), gene("frenzy")));
				spawn(player, front.add(-1.5, 0.0, 0.0), true,
						List.of(gene("death_throes"), gene("bonerunner"), gene("burrower")));
				spawn(player, front.add(-2.7, 0.0, 0.6), false, List.of(gene("runt"), gene("cinder"), gene("ironhide")));
			});
			context.waitTicks(40);
			context.takeScreenshot("baby_genes");
		}
	}

	private static void spawn(ServerPlayer player, Vec3 pos, boolean baby, List<Identifier> genes) {
		Zombie zombie = EntityTypes.ZOMBIE.create(player.level(), EntitySpawnReason.COMMAND);
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.setBaby(baby);
		zombie.setPos(pos);
		zombie.setYRot(160.0F);
		zombie.setYHeadRot(160.0F);
		zombie.setYBodyRot(160.0F);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(zombie);
	}
}
