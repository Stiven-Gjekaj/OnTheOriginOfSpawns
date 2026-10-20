package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult;
import io.github.stivengjekaj.originofspawns.evolution.FamilyResult.Kind;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;
import io.github.stivengjekaj.originofspawns.notebook.Discovery;
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
	public void discoveredFamiliesReadFromTheirCodec(GameTestHelper helper) {
		DiscoveredFamilies found = new DiscoveredFamilies(java.util.Set.of("b", "a"));
		var json = DiscoveredFamilies.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, found).getOrThrow();
		helper.assertValueEqual(json.toString(), "[\"a\",\"b\"]", "sorted list");
		helper.assertValueEqual(DiscoveredFamilies.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow(),
				found, "read back");
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
