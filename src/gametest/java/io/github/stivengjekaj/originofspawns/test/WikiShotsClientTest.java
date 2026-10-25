package io.github.stivengjekaj.originofspawns.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;

/**
 * Takes the pictures of the mobs for the wiki. It runs only when the environment variable ORIGINOFSPAWNS_WIKI_SHOTS is
 * set. For each species, it shows a plain mob, a mob with each gene, and a mob with all the genes of each family, from
 * the front at 3/4 and from the back at 3/4.
 * <p>
 * Each view is taken two times: in a room of black concrete and in a room of white concrete. The tool
 * tools/matte_wiki_shots.py finds the transparency of each pixel from the two pictures, and from two pictures of the
 * empty rooms. The raw pictures go to build/wiki_shots, with names such as zombie__gene__cinder__front__black.
 * <p>
 * The environment variable ORIGINOFSPAWNS_WIKI_SPECIES limits the pictures to some species, for example
 * "creeper,parched". With no value, the test takes the pictures of all species.
 * <p>
 * The mob exists only on the client. A mob on the server would run the effects of its genes, and an aura such as
 * Darkness or Nausea would change the picture.
 */
public class WikiShotsClientTest implements FabricClientGameTest {
	private static final Path OUTPUT = projectRoot().resolve("build/wiki_shots");
	private static final List<EntityType<? extends Mob>> SPECIES = List.of(EntityTypes.ZOMBIE, EntityTypes.HUSK,
			EntityTypes.DROWNED, EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.BOGGED, EntityTypes.WITHER_SKELETON,
			EntityTypes.PARCHED, EntityTypes.CREEPER, EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER, EntityTypes.PILLAGER,
			EntityTypes.VINDICATOR, EntityTypes.EVOKER, EntityTypes.WITCH,
			EntityTypes.ENDERMAN, EntityTypes.ENDERMITE, EntityTypes.SLIME,
			EntityTypes.MAGMA_CUBE);
	private static final int SHOT_ID = -424242;
	private static final int WIDTH = 720;
	private static final int HEIGHT = 960;
	/**
	 * The yaw of the mob for each view. The camera looks south, so a yaw of 180 faces the camera.
	 */
	private static final float FRONT = 145.0F;
	private static final float BACK = -35.0F;

	private record Subject(String species, String name, List<Identifier> genes) {
	}

	private static Path projectRoot() {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (dir != null && !Files.exists(dir.resolve("gradle.properties"))) {
			dir = dir.getParent();
		}
		return dir == null ? Path.of(System.getProperty("user.dir")) : dir;
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		if (System.getenv("ORIGINOFSPAWNS_WIKI_SHOTS") == null) {
			return;
		}
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			var server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule advance_time false");
			server.runCommand("gamerule advance_weather false");
			context.runOnClient(client -> {
				setHudHidden(client, true);
				client.options.entityShadows().set(false);
				client.options.bobView().set(false);
				client.options.fov().set(50);
			});
			BlockPos base = server.computeOnServer(s -> {
				ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
				BlockPos origin = BlockPos.containing(player.getX(), 150, player.getZ());
				player.getAbilities().mayfly = true;
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
				player.connection.teleport(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0.0F, 8.0F);
				return origin;
			});
			for (Block wall : List.of(Blocks.CONCRETE.black(), Blocks.CONCRETE.white())) {
				String background = wall == Blocks.CONCRETE.black() ? "black" : "white";
				server.runOnServer(s -> room(s.overworld(), base, wall));
				context.waitTicks(40);
				shoot(context, "background__" + background);
				for (EntityType<? extends Mob> type : selectedSpecies()) {
					for (Subject subject : subjects(singleplayer, type)) {
						CarriedGenes carried = server.computeOnServer(s -> carry(s.overworld(), type, subject.genes()));
						for (String view : List.of("front", "back")) {
							float yaw = view.equals("front") ? FRONT : BACK;
							context.runOnClient(client -> place(client, type, base, yaw, carried));
							context.waitTicks(3);
							shoot(context, subject.species() + "__" + subject.name() + "__" + view + "__" + background);
						}
					}
				}
				context.runOnClient(client -> client.level.removeEntity(SHOT_ID, Entity.RemovalReason.DISCARDED));
			}
		}
	}

	/**
	 * Returns the species in ORIGINOFSPAWNS_WIKI_SPECIES, or all species if the variable has no value.
	 */
	private static List<EntityType<? extends Mob>> selectedSpecies() {
		String names = System.getenv("ORIGINOFSPAWNS_WIKI_SPECIES");
		if (names == null || names.isBlank()) {
			return SPECIES;
		}
		List<String> wanted = java.util.Arrays.stream(names.split(",")).map(String::trim).toList();
		return SPECIES.stream().filter(type -> wanted.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()))
				.toList();
	}

	/**
	 * Returns the plain mob, each gene, and each family of the species. A family shows all its genes together.
	 */
	private static List<Subject> subjects(TestSingleplayerContext singleplayer, EntityType<? extends Mob> type) {
		return singleplayer.getServer().computeOnServer(server -> {
			String species = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
			Identifier speciesId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
			var genes = server.registryAccess().lookupOrThrow(ModRegistries.GENE);
			var families = server.registryAccess().lookupOrThrow(ModRegistries.FAMILY);
			List<Subject> subjects = new ArrayList<>();
			subjects.add(new Subject(species, "plain__plain", List.of()));
			List<Identifier> geneIds = new ArrayList<>();
			for (var entry : genes.entrySet()) {
				Identifier id = entry.getKey().identifier();
				if (id.getNamespace().equals("originofspawns") && entry.getValue().species().contains(type.builtInRegistryHolder())) {
					geneIds.add(id);
				}
			}
			geneIds.sort(null);
			for (Identifier id : geneIds) {
				subjects.add(new Subject(species, "gene__" + id.getPath(), List.of(id)));
			}
			for (var entry : families.entrySet()) {
				Identifier familyId = entry.getKey().identifier();
				if (!familyId.getNamespace().equals("originofspawns") || !familyId.getPath().startsWith(species + "/")) {
					continue;
				}
				List<Identifier> members = geneIds.stream()
						.filter(id -> genes.getValue(id).family().equals(familyId)).toList();
				subjects.add(new Subject(species, "family__" + familyId.getPath().substring(species.length() + 1), members));
			}
			return subjects;
		});
	}

	/**
	 * Returns the genes of a mob with these genes, with its family, as a mob of the species would carry them.
	 */
	private static CarriedGenes carry(ServerLevel level, EntityType<? extends Mob> type, List<Identifier> genes) {
		Mob mob = type.create(level, EntitySpawnReason.COMMAND);
		return Evolution.carry(level, mob, genes);
	}

	/**
	 * Puts a mob that exists only on the client in the middle of the room, with the yaw and the genes.
	 */
	private static void place(net.minecraft.client.Minecraft client, EntityType<? extends Mob> type, BlockPos base,
			float yaw, CarriedGenes carried) {
		client.level.removeEntity(SHOT_ID, Entity.RemovalReason.DISCARDED);
		Mob mob = type.create(client.level, EntitySpawnReason.COMMAND);
		mob.setId(SHOT_ID);
		// The natural weapons: the stone sword of the wither skeleton, the bow of the other skeleton species, and the
		// crossbow of the pillager. The vindicator holds no axe, because the raised axe covers its face.
		mob.setItemSlot(EquipmentSlot.MAINHAND, mob instanceof net.minecraft.world.entity.monster.skeleton.WitherSkeleton
				? new ItemStack(Items.STONE_SWORD)
				: mob instanceof net.minecraft.world.entity.monster.skeleton.AbstractSkeleton ? new ItemStack(Items.BOW)
				: mob.getType() == EntityTypes.PILLAGER ? new ItemStack(Items.CROSSBOW)
				: ItemStack.EMPTY);
		// A vindicator crosses its arms until it attacks. The attack pose shows the marks and the models on the arms.
		if (mob.getType() == EntityTypes.VINDICATOR) {
			mob.setAggressive(true);
		}
		// A slime and a magma cube show at size 2, so that the marks are large enough to see.
		if (mob instanceof net.minecraft.world.entity.monster.cubemob.AbstractCubeMob cube) {
			cube.setSize(2, true);
		}
		// An evoker crosses its arms until it casts. The spell pose shows the marks and the models on the arms.
		if (mob.getType() == EntityTypes.EVOKER) {
			castSpell(mob);
		}
		// A tall mob, such as the enderman, stands farther away, so that the top of its head is in the picture.
		double depth = mob.getBbHeight() > 2.2F ? 5.5 : 3.5;
		mob.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + depth, yaw, 0.0F);
		mob.setYHeadRot(yaw);
		mob.yHeadRotO = yaw;
		mob.setYBodyRot(yaw);
		mob.yBodyRotO = yaw;
		mob.setAttached(ModAttachments.GENES, carried);
		client.level.addEntity(mob);
	}

	/**
	 * Builds a closed room of one block around the camera and the mob, with full light inside.
	 */
	private static void room(ServerLevel level, BlockPos base, Block wall) {
		BlockPos from = base.offset(-6, -1, -3);
		BlockPos to = base.offset(6, 6, 9);
		for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
			boolean edge = pos.getX() == from.getX() || pos.getX() == to.getX() || pos.getY() == from.getY()
					|| pos.getY() == to.getY() || pos.getZ() == from.getZ() || pos.getZ() == to.getZ();
			level.setBlock(pos, edge ? wall.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
		}
		// Light blocks give full light and cannot be seen.
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-4, 0, -1), base.offset(4, 5, 7))) {
			if ((pos.getX() + pos.getY() + pos.getZ()) % 3 == 0 && !pos.equals(base) && !pos.equals(base.above())) {
				level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 2);
			}
		}
	}

	private static void shoot(ClientGameTestContext context, String name) {
		// The spell of an evoker makes particles that cover its hands and its head.
		context.runOnClient(client -> client.particleEngine.clearParticles());
		context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT).disableCounterPrefix()
				.withDestinationDir(OUTPUT));
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

	/**
	 * Raises the arms of an evoker for its spell. The spell enum is not public, so the shot sets the synced value of the
	 * fang spell. The mob of the shot does not tick, so no goal ends the spell.
	 */
	private static void castSpell(Mob mob) {
		try {
			var field = net.minecraft.world.entity.monster.illager.SpellcasterIllager.class
					.getDeclaredField("DATA_SPELL_CASTING_ID");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			var data = (net.minecraft.network.syncher.EntityDataAccessor<Byte>) field.get(null);
			mob.getEntityData().set(data, (byte) 2);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot set the spell of the evoker", exception);
		}
	}
}
