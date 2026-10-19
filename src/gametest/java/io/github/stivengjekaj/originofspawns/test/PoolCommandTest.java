package io.github.stivengjekaj.originofspawns.test;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

public class PoolCommandTest {
	private static final Identifier HUSK = Identifier.withDefaultNamespace("husk");
	private static final Identifier GENE = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");

	@GameTest
	public void setChangesThePoolAndGetCountsIt(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		int set = run(helper, "originofspawns pool set minecraft:husk temperate originofspawns_test:test_heat_skin 0.4");
		helper.assertValueEqual(set, 1, "set result");
		helper.assertValueEqual(GenePools.get(server).frequency(HUSK, BiomeCategory.TEMPERATE, GENE), 0.4F, "frequency");
		int count = run(helper, "originofspawns pool get minecraft:husk temperate");
		helper.assertValueEqual(count, 1, "genes in the pool");
		helper.succeed();
	}

	@GameTest
	public void setRefusesUnknownInput(GameTestHelper helper) {
		assertFails(helper, "originofspawns pool set minecraft:husk lava originofspawns_test:test_heat_skin 0.4");
		assertFails(helper, "originofspawns pool set minecraft:husk hot originofspawns_test:no_such_gene 0.4");
		assertFails(helper, "originofspawns pool set minecraft:husk hot originofspawns_test:test_heat_skin 2");
		helper.assertValueEqual(GenePools.get(helper.getLevel().getServer()).pool(HUSK, BiomeCategory.HOT).size(), 0,
				"genes in the hot pool");
		helper.succeed();
	}

	@GameTest
	public void playersWithoutPermissionCannotUseIt(GameTestHelper helper) {
		CommandSourceStack player = source(helper).withPermission(LevelBasedPermissionSet.ALL);
		try {
			helper.getLevel().getServer().getCommands().getDispatcher()
					.execute("originofspawns pool get minecraft:husk hot", player);
		} catch (CommandSyntaxException exception) {
			helper.succeed();
			return;
		}
		throw helper.assertionException("A player with no permission used the command");
	}

	private static CommandSourceStack source(GameTestHelper helper) {
		return helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput();
	}

	private static int run(GameTestHelper helper, String command) {
		try {
			return helper.getLevel().getServer().getCommands().getDispatcher().execute(command, source(helper));
		} catch (CommandSyntaxException exception) {
			throw helper.assertionException("Command failed: %s: %s", command, exception.getMessage());
		}
	}

	private static void assertFails(GameTestHelper helper, String command) {
		try {
			helper.getLevel().getServer().getCommands().getDispatcher().execute(command, source(helper));
		} catch (CommandSyntaxException exception) {
			return;
		}
		throw helper.assertionException("Command did not fail: %s", command);
	}
}
