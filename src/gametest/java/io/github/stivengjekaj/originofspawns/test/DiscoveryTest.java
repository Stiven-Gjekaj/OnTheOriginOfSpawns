package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;
import io.github.stivengjekaj.originofspawns.notebook.Discovery;
import io.github.stivengjekaj.originofspawns.notebook.Knowledge;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

/**
 * A player learns a family only when the player kills a mob of it, or when a mob of it kills the player.
 */
public class DiscoveryTest {
	private static final Identifier NEAR = Identifier.fromNamespaceAndPath("originofspawns_test", "near");
	private static final Identifier FAR = Identifier.fromNamespaceAndPath("originofspawns_test", "far");

	@GameTest
	public void playerLearnsTheFamilyOfAMobThatItKills(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		FamilyResult family = new FamilyResult(Kind.PURE, Optional.of(NEAR), Optional.empty());
		Zombie zombie = spawn(helper, player.position().add(2.0, 0.0, 0.0), family);
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1000.0F);
		helper.assertFalse(zombie.isAlive(), "the zombie is dead");
		helper.assertTrue(found(player).contains(family.translationKey()), "the family of the kill is found");
		helper.succeed();
	}

	@GameTest
	public void playerLearnsTheFamilyOfAMobThatKillsIt(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		FamilyResult family = new FamilyResult(Kind.PURE, Optional.of(FAR), Optional.empty());
		Zombie zombie = spawn(helper, player.position().add(2.0, 0.0, 0.0), family);
		Evolution.onDeath(player, helper.getLevel().damageSources().mobAttack(zombie));
		helper.assertTrue(found(player).contains(family.translationKey()), "the family of the killer is found");
		helper.succeed();
	}

	@GameTest
	public void playerLearnsNothingFromAMobThatDiesNearIt(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		FamilyResult family = new FamilyResult(Kind.PURE, Optional.of(NEAR), Optional.empty());
		Zombie zombie = spawn(helper, player.position().add(2.0, 0.0, 0.0), family);
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		helper.assertFalse(found(player).contains(family.translationKey()), "a death near the player teaches nothing");
		helper.assertFalse(Discovery.learn(player, spawn(helper, player.position(), FamilyResult.NEUTRAL))
				&& Discovery.learn(player, spawn(helper, player.position(), FamilyResult.NEUTRAL)),
				"the same family is new only one time");
		helper.succeed();
	}

	private static ServerPlayer player(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
		player.setAttached(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		return player;
	}

	private static DiscoveredFamilies found(ServerPlayer player) {
		return player.getAttachedOrElse(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
	}

	@GameTest
	public void playerSeesAFamilyAfterEightSecondsNearIt(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		FamilyResult family = new FamilyResult(Kind.PURE, Optional.of(NEAR), Optional.empty());
		spawn(helper, player.position().add(3.0, 0.0, 0.0), family);
		for (int second = 1; second < Discovery.SEEN_SECONDS; second++) {
			Discovery.watch(player);
		}
		helper.assertValueEqual(found(player).knowledge(family.translationKey()), Knowledge.NONE, "after 7 seconds");
		Discovery.watch(player);
		helper.assertValueEqual(found(player).knowledge(family.translationKey()), Knowledge.SEEN, "after 8 seconds");
		helper.succeed();
	}

	@GameTest
	public void aKillTurnsASeenFamilyIntoAKnownFamily(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		FamilyResult family = new FamilyResult(Kind.PURE, Optional.of(NEAR), Optional.empty());
		Zombie zombie = spawn(helper, player.position().add(3.0, 0.0, 0.0), family);
		helper.assertTrue(Discovery.see(player, zombie), "the family is new");
		helper.assertTrue(Discovery.learn(player, zombie), "the seen family is not known yet");
		helper.assertValueEqual(found(player).knowledge(family.translationKey()), Knowledge.KNOWN, "known");
		helper.assertTrue(found(player).seen().isEmpty(), "a known family is not in the seen set");
		helper.assertFalse(Discovery.see(player, zombie), "seeing a known family changes nothing");
		helper.succeed();
	}

	@GameTest
	public void discoveredFamiliesReadFromTheirCodec(GameTestHelper helper) {
		var ops = com.mojang.serialization.JsonOps.INSTANCE;
		DiscoveredFamilies found = new DiscoveredFamilies(java.util.Set.of("b", "a"), java.util.Set.of("c"));
		var json = DiscoveredFamilies.CODEC.encodeStart(ops, found).getOrThrow();
		helper.assertValueEqual(json.toString(), "{\"known\":[\"a\",\"b\"],\"seen\":[\"c\"]}", "the saved form");
		helper.assertValueEqual(DiscoveredFamilies.CODEC.parse(ops, json).getOrThrow(), found, "read back");
		// The format of 0.2.0-alpha is a plain list of known families.
		var old = com.google.gson.JsonParser.parseString("[\"a\",\"b\"]");
		helper.assertValueEqual(DiscoveredFamilies.CODEC.parse(ops, old).getOrThrow(),
				new DiscoveredFamilies(java.util.Set.of("a", "b")), "the old format");
		helper.succeed();
	}

	private static Zombie spawn(GameTestHelper helper, Vec3 pos, FamilyResult family) {
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(), family));
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.snapTo(pos, 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zombie);
		return zombie;
	}
}
