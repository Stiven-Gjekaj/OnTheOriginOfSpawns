package io.github.stivengjekaj.originofspawns.test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Makes the gallery pictures for the store pages and the README. It runs only when the environment variable
 * ORIGINOFSPAWNS_GALLERY is set, and writes the pictures to assets/gallery. Each scene puts one mob of each family
 * of a species in a row, with three genes of that family.
 */
public class GalleryClientTest implements FabricClientGameTest {
	private static final Path OUTPUT = projectRoot().resolve("assets/gallery");

	/**
	 * Returns the folder of the project: the first folder above the run folder that has gradle.properties.
	 */
	private static Path projectRoot() {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (dir != null && !java.nio.file.Files.exists(dir.resolve("gradle.properties"))) {
			dir = dir.getParent();
		}
		return dir == null ? Path.of(System.getProperty("user.dir")) : dir;
	}

	private record Scene(String name, EntityType<? extends Mob> type, Block floor, List<List<String>> mobs) {
	}

	private static final List<Scene> SCENES = List.of(
			new Scene("zombies", EntityTypes.ZOMBIE, Blocks.GRASS_BLOCK, List.of(
					List.of("lunger", "frenzy", "bonerunner"),
					List.of("nightborn", "lurker", "hush"),
					List.of("ironhide", "thickskull", "bloat"),
					List.of("siege", "rally", "packcaller"),
					List.of("miasma", "leech", "sporebearer"))),
			new Scene("husks", EntityTypes.HUSK, Blocks.SAND, List.of(
					List.of("kilnskin", "sunstroke", "solar_flare"),
					List.of("scuttler", "sandblast", "duststorm"),
					List.of("wrapped", "undying", "tomb_guard"),
					List.of("packmule", "signal_fire", "nomad"),
					List.of("dehydrator", "salt_crust", "thirstbite"))),
			new Scene("drowned", EntityTypes.DROWNED, Blocks.PRISMARINE, List.of(
					List.of("torrent", "undertow", "breacher"),
					List.of("barbed", "volley", "stormcaller"),
					List.of("lure", "glowspot", "ink"),
					List.of("barnacle_crust", "coral_growth", "anchor"),
					List.of("chorus", "wreckcaller", "pearlheart"))));

	@Override
	public void runTest(ClientGameTestContext context) {
		if (System.getenv("ORIGINOFSPAWNS_GALLERY") == null) {
			return;
		}
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			var server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			context.runOnClient(client -> setHudHidden(client, true));
			for (Scene scene : SCENES) {
				server.runOnServer(s -> stage(s, scene));
				context.waitTicks(40);
				// The light under the new blocks updates a few ticks late, so a mob can catch fire first.
				server.runCommand("execute as @e[tag=originofspawns_gallery] run data merge entity @s {Fire:0s}");
				context.waitTicks(20);
				context.takeScreenshot(TestScreenshotOptions.of(scene.name()).withSize(1920, 1080)
						.disableCounterPrefix().withDestinationDir(OUTPUT));
				server.runCommand("kill @e[tag=originofspawns_gallery]");
				// The killed mobs drop items, which must not show in the next scene.
				server.runCommand("kill @e[type=item]");
			}
			infoPanel(context, singleplayer);
		}
	}

	/**
	 * Builds a floor and puts a row of mobs in front of the player.
	 */
	private static void stage(MinecraftServer server, Scene scene) {
		ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
		ServerLevel level = player.level();
		BlockPos base = BlockPos.containing(player.getX(), 120, player.getZ());
		// Stone holds up a floor of sand, which falls with nothing under it.
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-24, -2, -8), base.offset(24, -2, 24))) {
			level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
		}
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-24, -1, -8), base.offset(24, -1, 24))) {
			level.setBlock(pos, scene.floor().defaultBlockState(), 2);
		}
		player.connection.teleport(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0F, 12.0F);
		for (int i = 0; i < scene.mobs().size(); i++) {
			Mob mob = scene.type().create(level, EntitySpawnReason.COMMAND);
			mob.setNoAi(true);
			mob.setPersistenceRequired();
			mob.setItemSlot(EquipmentSlot.MAINHAND, scene.type() == EntityTypes.DROWNED && i == 1
					? new ItemStack(Items.TRIDENT) : ItemStack.EMPTY);
			mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			double x = (i - (scene.mobs().size() - 1) / 2.0) * 1.6;
			mob.snapTo(base.getX() + 0.5 + x, base.getY(), base.getZ() + 5.5, 180.0F - (float) x * 6.0F, 0.0F);
			mob.setYHeadRot(mob.getYRot());
			mob.setYBodyRot(mob.getYRot());
			mob.setAttached(ModAttachments.GENES, Evolution.carry(level, mob, scene.mobs().get(i).stream()
					.map(gene -> Identifier.fromNamespaceAndPath("originofspawns", gene)).toList()));
			mob.addTag("originofspawns_gallery");
			// A block high above the mob, out of the picture, takes away its view of the sky, so the sun does not
			// burn it. The light at the mob stays almost full.
			level.setBlock(mob.blockPosition().above(20), Blocks.STONE.defaultBlockState(), 2);
			level.addFreshEntity(mob);
		}
	}


	/**
	 * Shows the mob info panel over a husk whose family and genes the player knows.
	 */
	private static void infoPanel(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
		singleplayer.getServer().runOnServer(server -> {
			stage(server, new Scene("panel", EntityTypes.HUSK, Blocks.SAND,
					List.of(List.of("wrapped", "undying", "tomb_guard"))));
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			Mob husk = null;
			for (var entity : server.overworld().getAllEntities()) {
				if (entity instanceof Mob mob && mob.entityTags().contains("originofspawns_gallery")) {
					husk = mob;
				}
			}
			husk.snapTo(player.getX(), player.getY(), player.getZ() + 3.0, 180.0F, 0.0F);
			husk.setYHeadRot(180.0F);
			husk.setYBodyRot(180.0F);
			player.setAttached(ModAttachments.DISCOVERED, new DiscoveredFamilies(Set.of(
					husk.getAttached(ModAttachments.GENES).family().translationKey())));
			player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 20.0F);
		});
		context.runOnClient(client -> setHudHidden(client, false));
		context.waitTicks(40);
		singleplayer.getServer().runCommand(
				"execute as @e[tag=originofspawns_gallery] run data merge entity @s {Fire:0s}");
		context.waitTicks(20);
		// The HUD keeps the size of the window, so this picture has the size of the window.
		context.takeScreenshot(TestScreenshotOptions.of("info_panel").disableCounterPrefix().withDestinationDir(OUTPUT));
	}

	/**
	 * Hides the HUD, as the F1 key does. The HUD has no public setter, so this sets its field.
	 */
	private static void setHudHidden(net.minecraft.client.Minecraft client, boolean hidden) {
		try {
			var field = net.minecraft.client.gui.Hud.class.getDeclaredField("isHidden");
			field.setAccessible(true);
			field.setBoolean(client.gui.hud, hidden);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Cannot hide the HUD", exception);
		}
	}
}
