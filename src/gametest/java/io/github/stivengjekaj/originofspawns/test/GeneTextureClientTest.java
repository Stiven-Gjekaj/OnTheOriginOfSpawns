package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.client.render.GeneModels;
import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.visual.GeneModel;

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
 * Starts the game, shows zombies with genes, and checks that they get a baked texture.
 */
public class GeneTextureClientTest implements FabricClientGameTest {
	private static final Identifier CINDER = Identifier.fromNamespaceAndPath("originofspawns", "cinder");
	private static final Identifier IRONHIDE = Identifier.fromNamespaceAndPath("originofspawns", "ironhide");
	private static final Identifier BONERUNNER = Identifier.fromNamespaceAndPath("originofspawns", "bonerunner");
	private static final Identifier TEST_GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");
	private static final Identifier TEST_PLAIN_GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "test_swift");
	private static final Identifier ZOMBIE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("time set midnight");
			context.runOnClient(client -> client.options.gamma().set(1.0));
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.setYRot(0.0F);
				player.setXRot(10.0F);
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 10.0F);
				Vec3 front = player.position().add(0.0, 0.0, 3.0);
				spawn(player, front.add(-1.2, 0.0, 0.0), List.of());
				spawn(player, front, List.of(CINDER));
				spawn(player, front.add(1.2, 0.0, 0.0), List.of(CINDER, IRONHIDE));
				spawn(player, front.add(2.4, 0.0, 0.0), List.of(BONERUNNER));
				spawn(player, front.add(-2.4, 0.0, 0.0), List.of(CINDER, IRONHIDE, BONERUNNER));
			});
			context.waitTicks(40);

			boolean synced = context.computeOnClient(client -> client.level.getEntitiesOfClass(Zombie.class,
					client.player.getBoundingBox().inflate(8.0)).stream().anyMatch(zombie -> {
						CarriedGenes genes = zombie.getAttached(ModAttachments.GENES);
						return genes != null && genes.genes().contains(IRONHIDE);
					}));
			if (!synced) {
				throw new AssertionError("The client does not have the genes of the zombie");
			}
			Identifier baked = context.computeOnClient(client -> GeneTextures.textureFor(ZOMBIE_TEXTURE, List.of(TEST_GENE)));
			if (baked.equals(ZOMBIE_TEXTURE)) {
				throw new AssertionError("A gene with a visual gives the vanilla texture");
			}
			Identifier plain = context.computeOnClient(client -> GeneTextures.textureFor(ZOMBIE_TEXTURE, List.of(TEST_PLAIN_GENE)));
			if (!plain.equals(ZOMBIE_TEXTURE)) {
				throw new AssertionError("A gene with no visual changes the texture");
			}
			Identifier again = context.computeOnClient(client -> GeneTextures.textureFor(ZOMBIE_TEXTURE, List.of(TEST_GENE)));
			if (!again.equals(baked)) {
				throw new AssertionError("The same genes give a second texture");
			}
			boolean glowSplit = context.computeOnClient(client -> {
				GeneModels.Baked fuse = GeneModels.get(Identifier.fromNamespaceAndPath("originofspawns_test", "test_fuse"));
				return fuse != null && fuse.parts().get(GeneModel.Bone.BODY).hasChild("part0")
						&& !fuse.parts().get(GeneModel.Bone.BODY).hasChild("part1")
						&& fuse.glowing().get(GeneModel.Bone.BODY).hasChild("part1")
						&& !fuse.glowing().get(GeneModel.Bone.BODY).hasChild("part0");
			});
			if (!glowSplit) {
				throw new AssertionError("The glowing part of a gene model is not apart from the other parts");
			}
			// The screenshot shows the real genes for a person to check. From left to right: Bonerunner,
			// Cinder with Ironhide, Cinder, plain, and all three genes.
			context.takeScreenshot("zombie_genes");
		}
	}

	private static void spawn(ServerPlayer player, Vec3 pos, List<Identifier> genes) {
		Zombie zombie = EntityTypes.ZOMBIE.create(player.level(), EntitySpawnReason.COMMAND);
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.setPos(pos);
		zombie.setYRot(180.0F);
		zombie.setYHeadRot(180.0F);
		zombie.setYBodyRot(180.0F);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(zombie);
	}
}
