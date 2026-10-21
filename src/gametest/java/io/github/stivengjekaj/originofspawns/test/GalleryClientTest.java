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

	/**
	 * The scenes of the skeleton species. They are not in the picture of all families.
	 */
	private static final List<Scene> SKELETON_SCENES = List.of(
			new Scene("skeletons", EntityTypes.SKELETON, Blocks.COARSE_DIRT, List.of(
					List.of("high_ground", "quickdraw", "longdraw"),
					List.of("fleetbone", "bonedash", "backpedal"),
					List.of("bonewall", "thickbone", "reassemble"),
					List.of("phalanx", "bugler", "warcry"),
					List.of("hexmark", "soulfire", "witherhex"))),
			new Scene("strays", EntityTypes.STRAY, Blocks.SNOW_BLOCK, List.of(
					List.of("iceplate", "glacial_wall", "shatterhide"),
					List.of("gale", "hailshot", "frostwind"),
					List.of("driftline", "snowshoe", "iceskate"),
					List.of("icicle", "coldsnap", "frostbite"),
					List.of("mourner", "frozen_gaze", "hollow_cry"))),
			new Scene("bogged", EntityTypes.BOGGED, Blocks.MUD, List.of(
					List.of("shelfback", "puffball", "mushroom_shot"),
					List.of("leechbone", "wader", "mire_grip"),
					List.of("venom_gland", "antivenom", "bile_shot"),
					List.of("creeper_vines", "snare", "vine_swing"),
					List.of("lily_pad", "swamp_shroud", "barkskin"))),
			new Scene("wither_skeletons", EntityTypes.WITHER_SKELETON, Blocks.NETHER_BRICKS, List.of(
					List.of("executioner", "duelist", "parry"),
					List.of("ember_eyes", "cinderblade", "pyre"),
					List.of("skull_shot", "black_rot", "grave_chill"),
					List.of("brickplate", "brickshield", "sentinel"),
					List.of("vengeful_spirit", "soulstride", "soul_link"))),
			new Scene("parched", EntityTypes.PARCHED, Blocks.SANDSTONE, List.of(
					List.of("fumble", "exploit", "curse_cloud"),
					List.of("long_wind", "whirlwind_volley", "gust_shot"),
					List.of("circler", "gorge", "glide"),
					List.of("stinger", "pincers", "carapace"),
					List.of("amber_heart", "stoneskin", "petrified"))));

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
			for (Scene scene : java.util.stream.Stream.concat(SCENES.stream(), SKELETON_SCENES.stream()).toList()) {
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
				// Pyre leaves fire on the floor, which must not burn the mobs of the next scene.
				server.runCommand("execute at @p run fill ~-24 ~ ~-8 ~24 ~4 ~24 air replace minecraft:fire");
			}
			allFamilies(context, singleplayer);
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
			mob.setItemSlot(EquipmentSlot.MAINHAND, weapon(scene.type(), i));
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
	 * Returns the natural weapon of the mob at the place in the row: the trident of the second drowned, the stone
	 * sword of a wither skeleton, and the bow of the other skeletons.
	 */
	private static ItemStack weapon(EntityType<? extends Mob> type, int place) {
		if (type == EntityTypes.DROWNED) {
			return place == 1 ? new ItemStack(Items.TRIDENT) : ItemStack.EMPTY;
		}
		if (type == EntityTypes.WITHER_SKELETON) {
			return new ItemStack(Items.STONE_SWORD);
		}
		return SKELETON_SCENES.stream().anyMatch(scene -> scene.type() == type) ? new ItemStack(Items.BOW)
				: ItemStack.EMPTY;
	}

	/**
	 * Puts all 15 families in three rows, one row for each species on its own floor, and looks down on them at 45
	 * degrees.
	 */
	private static void allFamilies(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
		var server = singleplayer.getServer();
		server.runOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			ServerLevel level = player.level();
			BlockPos base = BlockPos.containing(player.getX(), 120, player.getZ());
			// A plain floor of grass covers the floors of the scenes before.
			for (BlockPos pos : BlockPos.betweenClosed(base.offset(-30, -1, -12), base.offset(30, -1, 40))) {
				level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
			}
			for (int row = 0; row < SCENES.size(); row++) {
				Scene scene = SCENES.get(row);
				int z = base.getZ() + 6 + row * 3;
				for (BlockPos pos : BlockPos.betweenClosed(base.getX() - 24, base.getY() - 2, z - 1,
						base.getX() + 24, base.getY() - 2, z + 1)) {
					level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
					level.setBlock(pos.above(), scene.floor().defaultBlockState(), 2);
				}
				for (int i = 0; i < scene.mobs().size(); i++) {
					List<String> genes = scene.mobs().get(i);
					// Duststorm and Sandblast aim at the player, which is the camera here.
					if (genes.contains("duststorm")) {
						genes = List.of("scuttler", "sinkhole", "dune_runner");
					}
					Mob mob = scene.type().create(level, EntitySpawnReason.COMMAND);
					mob.setNoAi(true);
					mob.setPersistenceRequired();
					mob.setItemSlot(EquipmentSlot.MAINHAND, scene.type() == EntityTypes.DROWNED && i == 1
							? new ItemStack(Items.TRIDENT) : ItemStack.EMPTY);
					mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
					double x = (i - (scene.mobs().size() - 1) / 2.0) * 2.0;
					mob.snapTo(base.getX() + 0.5 + x, base.getY(), z + 0.5, 180.0F, 0.0F);
					mob.setYHeadRot(180.0F);
					mob.setYBodyRot(180.0F);
					mob.setAttached(ModAttachments.GENES, Evolution.carry(level, mob, genes.stream()
							.map(gene -> Identifier.fromNamespaceAndPath("originofspawns", gene)).toList()));
					mob.addTag("originofspawns_gallery");
					level.setBlock(mob.blockPosition().above(30), Blocks.STONE.defaultBlockState(), 2);
					level.addFreshEntity(mob);
				}
			}
			// The camera is above and in front of the rows, and looks down at 45 degrees on their middle row.
			// The player flies, so that the camera does not fall while the scene settles.
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
			// At 45 degrees, the camera sees the middle row as far ahead of it as it is high above it.
			double height = 6.5;
			player.connection.teleport(base.getX() + 0.5, base.getY() + height - 1.6, base.getZ() + 9.5 - height,
					0.0F, 45.0F);
		});
		context.waitTicks(40);
		server.runCommand("execute as @e[tag=originofspawns_gallery] run data merge entity @s {Fire:0s}");
		server.runCommand("effect clear @a");
		// The smoke of the put out fires clears in about 3 seconds.
		context.waitTicks(80);
		context.takeScreenshot(TestScreenshotOptions.of("all_families").withSize(1920, 1080)
				.disableCounterPrefix().withDestinationDir(OUTPUT));
		server.runCommand("kill @e[tag=originofspawns_gallery]");
		server.runCommand("kill @e[type=item]");
		server.runOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.getAbilities().flying = false;
			player.onUpdateAbilities();
		});
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
