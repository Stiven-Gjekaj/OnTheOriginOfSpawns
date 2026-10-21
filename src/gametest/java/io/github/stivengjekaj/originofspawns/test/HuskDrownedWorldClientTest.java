package io.github.stivengjekaj.originofspawns.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Plays the husk and the drowned in a real world, with their real AI and the real spawn path. It checks the
 * equipment rules, the conversion to a drowned, the song of a siren, a thrown barbed trident, and the names of all
 * the genes and families.
 */
public class HuskDrownedWorldClientTest implements FabricClientGameTest {
	private static final String TAG = "originofspawns_world_test";
	private static final Set<String> SPECIES = Set.of("zombie", "husk", "drowned", "skeleton", "stray", "bogged", "wither_skeleton", "parched");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			var server = singleplayer.getServer();
			server.runCommand("time set midnight");
			server.runCommand("weather clear");
			BlockPos base = server.computeOnServer(HuskDrownedWorldClientTest::buildArena);
			context.waitTicks(10);

			checkSpawnEquipment(singleplayer, base);
			checkConversion(singleplayer, base);
			checkSirenSong(singleplayer, base);
			checkBarbedTrident(singleplayer, base);
			checkNames(context);
		}
	}

	/**
	 * Builds a stone floor high in the air with a pool of water 3 blocks deep, and puts the player on it.
	 *
	 * @return the floor block under the player
	 */
	private static BlockPos buildArena(MinecraftServer server) {
		ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
		ServerLevel level = player.level();
		BlockPos base = player.blockPosition().above(40);
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-16, -4, -16), base.offset(16, 0, 16))) {
			level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
		}
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(4, -2, 4), base.offset(12, 0, 12))) {
			level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
		}
		player.setGameMode(GameType.SURVIVAL);
		// The player cannot die, so that the mobs can attack it for the whole test.
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100000, 4, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100000, 0, false, false));
		player.connection.teleport(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 0.0F, 20.0F);
		return base;
	}

	/**
	 * Summons many husks and drowned, as a command does. Checks that they have genes of their own species, that
	 * the husks hold nothing, that the drowned hold only their natural items, and that babies come only from a
	 * baby gene.
	 */
	private static void checkSpawnEquipment(TestSingleplayerContext singleplayer, BlockPos base) {
		var server = singleplayer.getServer();
		BlockPos corner = base.offset(-12, 1, -12);
		for (int i = 0; i < 40; i++) {
			int x = corner.getX() + i % 8;
			int z = corner.getZ() + i / 8;
			String type = i % 2 == 0 ? "husk" : "drowned";
			server.runCommand("summon minecraft:" + type + " " + x + " " + corner.getY() + " " + z
					+ " {NoAI:1b,PersistenceRequired:1b,Tags:[\"" + TAG + "\"]}");
		}
		List<String> problems = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			List<String> found = new ArrayList<>();
			var genes = level.registryAccess().lookupOrThrow(ModRegistries.GENE);
			List<Mob> mobs = level.getEntitiesOfClass(Mob.class, new AABB(corner).inflate(16),
					mob -> mob.entityTags().contains(TAG));
			if (mobs.size() != 40) {
				found.add("summoned 40 mobs, found " + mobs.size());
			}
			int withGenes = 0;
			for (Mob mob : mobs) {
				String name = EntityType.getKey(mob.getType()).getPath();
				CarriedGenes carried = mob.getAttached(ModAttachments.GENES);
				if (carried == null) {
					found.add(name + " has no genes attachment");
					continue;
				}
				if (!carried.genes().isEmpty()) {
					withGenes++;
				}
				for (Identifier id : carried.genes()) {
					Gene gene = genes.getValue(id);
					if (gene == null || !gene.carriedBy(mob.getType())) {
						found.add(name + " carries " + id + ", which is not its gene");
					}
				}
				ItemStack hand = mob.getMainHandItem();
				// A husk that rides a camel husk has a natural iron spear.
				boolean naturalHand = hand.isEmpty() || mob instanceof Drowned
						&& (hand.is(Items.TRIDENT) || hand.is(Items.FISHING_ROD))
						|| mob.isPassenger() && hand.is(Items.IRON_SPEAR);
				if (!naturalHand) {
					found.add(name + " holds " + hand);
				}
				for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
						EquipmentSlot.FEET)) {
					if (!mob.getItemBySlot(slot).isEmpty()) {
						found.add(name + " wears " + mob.getItemBySlot(slot));
					}
				}
				boolean babyGene = carried.genes().stream().anyMatch(id -> Set.of("sandling", "fry", "runt")
						.contains(id.getPath()));
				if (mob.isBaby() && !babyGene) {
					found.add(name + " is a baby with no baby gene");
				}
				if (mob.canPickUpLoot() && carried.genes().stream().noneMatch(id -> id.getPath().equals("packmule"))) {
					found.add(name + " picks up items with no gene for it");
				}
			}
			found.add(0, "INFO " + withGenes + " of " + mobs.size() + " mobs have genes");
			mobs.forEach(Mob::discard);
			return found;
		});
		report("spawn equipment", problems);
	}

	/**
	 * Puts a zombie with zombie genes under water near its conversion time. The drowned that it becomes must have
	 * only drowned genes.
	 */
	private static void checkConversion(TestSingleplayerContext singleplayer, BlockPos base) {
		var server = singleplayer.getServer();
		BlockPos bottom = base.offset(8, -2, 8);
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
			zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(
					Identifier.fromNamespaceAndPath("originofspawns", "ironhide"),
					Identifier.fromNamespaceAndPath("originofspawns", "rally"))));
			// A zombie with no AI does not convert, so this zombie has its AI. It stays under the water.
			zombie.setPersistenceRequired();
			zombie.addTag(TAG);
			zombie.snapTo(Vec3.atBottomCenterOf(bottom), 0.0F, 0.0F);
			level.addFreshEntity(zombie);
			// A zombie starts to convert after 600 ticks under water. The test starts it near that time.
			zombie.setInWaterTime(599);
		});
		server.waitFor(s -> !s.overworld().getEntitiesOfClass(Drowned.class, new AABB(bottom).inflate(4),
				mob -> mob.entityTags().contains(TAG)).isEmpty(), 600);
		List<String> problems = server.computeOnServer(s -> {
			List<String> found = new ArrayList<>();
			var genes = s.overworld().registryAccess().lookupOrThrow(ModRegistries.GENE);
			for (Drowned drowned : s.overworld().getEntitiesOfClass(Drowned.class, new AABB(bottom).inflate(4),
					mob -> mob.entityTags().contains(TAG))) {
				CarriedGenes carried = drowned.getAttached(ModAttachments.GENES);
				if (carried == null) {
					found.add("the converted drowned has no genes attachment");
					continue;
				}
				for (Identifier id : carried.genes()) {
					Gene gene = genes.getValue(id);
					if (gene == null || !gene.carriedBy(EntityTypes.DROWNED)) {
						found.add("the converted drowned keeps " + id);
					}
				}
				found.add(0, "INFO the converted drowned has " + carried.genes());
				drowned.discard();
			}
			return found;
		});
		report("conversion", problems);
	}

	/**
	 * Puts the player in the pool near a drowned with Siren Song. The player must get Slowness.
	 */
	private static void checkSirenSong(TestSingleplayerContext singleplayer, BlockPos base) {
		var server = singleplayer.getServer();
		server.runOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.removeEffect(MobEffects.SLOWNESS);
			player.connection.teleport(base.getX() + 8.5, base.getY() - 1, base.getZ() + 8.5, 0.0F, 0.0F);
			spawnDrowned(s.overworld(), base.offset(0, 1, 0), List.of("siren_song"), ItemStack.EMPTY, true);
		});
		int ticks = server.waitFor(s -> s.getPlayerList().getPlayers().getFirst().hasEffect(MobEffects.SLOWNESS), 200);
		report("siren song", List.of("INFO the player in water got Slowness after " + ticks + " ticks"));
		server.runOnServer(s -> {
			discardTagged(s.overworld(), base);
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.removeEffect(MobEffects.SLOWNESS);
			player.connection.teleport(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 0.0F, 20.0F);
		});
	}

	/**
	 * Lets a drowned with Barbed and a trident attack the player on land, with its real AI. A thrown trident that
	 * hits must give Slowness.
	 */
	private static void checkBarbedTrident(TestSingleplayerContext singleplayer, BlockPos base) {
		var server = singleplayer.getServer();
		server.runOnServer(s -> spawnDrowned(s.overworld(), base.offset(0, 1, 7), List.of("barbed", "volley"),
				new ItemStack(Items.TRIDENT), false));
		int ticks = server.waitFor(s -> s.getPlayerList().getPlayers().getFirst().hasEffect(MobEffects.SLOWNESS), 600);
		report("barbed trident", List.of("INFO a thrown trident gave Slowness after " + ticks + " ticks"));
		server.runOnServer(s -> discardTagged(s.overworld(), base));
	}

	/**
	 * Checks that the client has a name for each gene and each family of the zombie, the husk, and the drowned,
	 * with all the minor families and the hybrids.
	 */
	private static void checkNames(ClientGameTestContext context) {
		List<String> missing = context.computeOnClient(client -> {
			Language language = Language.getInstance();
			var access = client.level.registryAccess();
			List<String> keys = new ArrayList<>();
			for (var id : access.lookupOrThrow(ModRegistries.GENE).keySet()) {
				if (id.getNamespace().equals("originofspawns")) {
					keys.add("gene.originofspawns." + id.getPath());
				}
			}
			for (String species : SPECIES) {
				List<String> families = access.lookupOrThrow(ModRegistries.FAMILY).keySet().stream()
						.filter(id -> id.getNamespace().equals("originofspawns") && id.getPath().startsWith(species + "/"))
						.map(id -> id.getPath().substring(species.length() + 1)).sorted().toList();
				for (String first : families) {
					keys.add("family.originofspawns." + species + "." + first);
					for (String second : families) {
						if (!first.equals(second)) {
							keys.add("family.originofspawns." + species + "." + first + "." + second);
						}
						if (first.compareTo(second) < 0) {
							keys.add("family.originofspawns." + species + "." + first + ".and." + second);
						}
					}
				}
			}
			return keys.stream().filter(key -> !language.has(key)).toList();
		});
		report("names", missing);
	}

	private static void spawnDrowned(ServerLevel level, BlockPos pos, List<String> genes, ItemStack hand,
			boolean noAi) {
		Drowned drowned = EntityTypes.DROWNED.create(level, EntitySpawnReason.COMMAND);
		drowned.setAttached(ModAttachments.GENES, new CarriedGenes(genes.stream()
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns", gene)).toList()));
		drowned.setItemSlot(EquipmentSlot.MAINHAND, hand);
		drowned.setNoAi(noAi);
		drowned.setPersistenceRequired();
		drowned.addTag(TAG);
		drowned.snapTo(Vec3.atBottomCenterOf(pos), 180.0F, 0.0F);
		level.addFreshEntity(drowned);
	}

	private static void discardTagged(ServerLevel level, BlockPos base) {
		level.getEntitiesOfClass(Mob.class, new AABB(base).inflate(24), mob -> mob.entityTags().contains(TAG))
				.forEach(Mob::discard);
	}

	/**
	 * Prints the lines that start with INFO, and fails with all the other lines.
	 */
	private static void report(String check, List<String> lines) {
		lines.stream().filter(line -> line.startsWith("INFO ")).forEach(line -> System.out.println(
				"[world test] " + check + ": " + line.substring(5)));
		List<String> problems = lines.stream().filter(line -> !line.startsWith("INFO ")).toList();
		if (!problems.isEmpty()) {
			throw new AssertionError(check + ": " + String.join("; ", problems));
		}
		System.out.println("[world test] " + check + ": ok");
	}
}
