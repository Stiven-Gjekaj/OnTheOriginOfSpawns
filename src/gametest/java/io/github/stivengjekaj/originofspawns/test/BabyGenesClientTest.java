package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
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
				// A drowned has split limbs, so the marks on its left arm and left leg need their own copy.
				spawnDrowned(player, front.add(0.9, 0.0, 2.6), false, List.of(gene("cinder"), gene("ironhide")));
				spawnDrowned(player, front.add(-0.9, 0.0, 2.2), true, List.of(gene("cinder"), gene("ironhide")));
				var husk = EntityTypes.HUSK.create(player.level(), EntitySpawnReason.COMMAND);
				husk.setNoAi(true);
				husk.setPersistenceRequired();
				husk.setPos(front.add(2.6, 0.0, 2.2));
				husk.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(gene("kilnskin"), gene("wrapped"))));
				player.level().addFreshEntity(husk);
			});
			context.waitTicks(40);
			context.takeScreenshot("baby_genes");
			// The husk and the drowned choose their own body textures, so each needs its own marks.
			for (String texture : List.of("husk", "drowned")) {
				Identifier body = Identifier.withDefaultNamespace("textures/entity/zombie/" + texture + ".png");
				if (!context.computeOnClient(client -> GeneTextures.wasBakedFrom(body))) {
					throw new AssertionError("No gene marks on " + body);
				}
			}
			// The outer layer of the drowned covers its body, so the marks must be on the outer layer too.
			for (String texture : List.of("drowned_outer_layer", "drowned_outer_layer_baby")) {
				Identifier outer = Identifier.withDefaultNamespace("textures/entity/zombie/" + texture + ".png");
				if (!context.computeOnClient(client -> GeneTextures.wasBakedFrom(outer))) {
					throw new AssertionError("No gene marks on " + outer);
				}
			}
		}
	}

	private static void spawnDrowned(ServerPlayer player, Vec3 pos, boolean baby, List<Identifier> genes) {
		var drowned = EntityTypes.DROWNED.create(player.level(), EntitySpawnReason.COMMAND);
		drowned.setNoAi(true);
		drowned.setPersistenceRequired();
		drowned.setBaby(baby);
		drowned.setPos(pos);
		drowned.setYRot(150.0F);
		drowned.setYHeadRot(150.0F);
		drowned.setYBodyRot(150.0F);
		drowned.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(drowned);
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
