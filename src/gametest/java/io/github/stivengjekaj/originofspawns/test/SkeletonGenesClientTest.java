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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Shows skeletons with genes, and checks that the skeleton species get the textures with the gene marks. From left
 * to right: a plain skeleton, a skeleton with Venomtip and High Ground, a skeleton with Barrage and Soulfire, a
 * stray with Venomtip and gray cloth, and a wither skeleton with Venomtip.
 */
public class SkeletonGenesClientTest implements FabricClientGameTest {
	private static Identifier gene(String name) {
		return Identifier.fromNamespaceAndPath("originofspawns", name);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// At night the skeletons do not burn.
			singleplayer.getServer().runCommand("time set midnight");
			context.runOnClient(client -> client.options.gamma().set(1.0));
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 15.0F);
				Vec3 front = player.position().add(0.0, 0.0, 3.5);
				spawn(player, EntityTypes.SKELETON, front.add(2.4, 0.0, 0.0), List.of());
				spawn(player, EntityTypes.SKELETON, front.add(1.2, 0.0, 0.0), List.of(gene("venomtip"), gene("high_ground")));
				spawn(player, EntityTypes.SKELETON, front, List.of(gene("barrage"), gene("soulfire")));
				spawn(player, EntityTypes.STRAY, front.add(-1.2, 0.0, 0.0), List.of(gene("venomtip"),
						Identifier.fromNamespaceAndPath("originofspawns_test", "test_cloth_dye")));
				spawn(player, EntityTypes.WITHER_SKELETON, front.add(-2.6, 0.0, 0.4), List.of(gene("venomtip")));
			});
			context.waitTicks(40);
			context.takeScreenshot("skeleton_genes");
			for (String texture : List.of("skeleton", "stray", "stray_overlay", "wither_skeleton")) {
				Identifier base = Identifier.withDefaultNamespace("textures/entity/skeleton/" + texture + ".png");
				if (!context.computeOnClient(client -> GeneTextures.wasBakedFrom(base))) {
					throw new AssertionError("No gene marks on " + base);
				}
			}
		}
	}

	private static void spawn(ServerPlayer player, EntityType<? extends Mob> type, Vec3 pos, List<Identifier> genes) {
		Mob mob = type.create(player.level(), EntitySpawnReason.COMMAND);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		mob.setPos(pos);
		mob.setYRot(180.0F);
		mob.setYHeadRot(180.0F);
		mob.setYBodyRot(180.0F);
		mob.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(mob);
	}
}
