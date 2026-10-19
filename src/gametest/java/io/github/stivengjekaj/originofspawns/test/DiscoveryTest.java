package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
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
 * A player finds the families of the mobs near the player.
 */
public class DiscoveryTest {
	private static final Identifier NEAR = Identifier.fromNamespaceAndPath("originofspawns_test", "near");
	private static final Identifier FAR = Identifier.fromNamespaceAndPath("originofspawns_test", "far");

	@GameTest
	public void playerFindsOnlyTheNearFamilies(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
		player.setAttached(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		FamilyResult near = new FamilyResult(Kind.PURE, Optional.of(NEAR), Optional.empty());
		FamilyResult far = new FamilyResult(Kind.PURE, Optional.of(FAR), Optional.empty());
		spawn(helper, player.position().add(5.0, 0.0, 0.0), near);
		spawn(helper, player.position().add(0.0, 0.0, Discovery.RADIUS + 4.0), far);

		helper.assertValueEqual(Discovery.discover(player), 1, "new families");
		DiscoveredFamilies found = player.getAttached(ModAttachments.DISCOVERED);
		helper.assertTrue(found.contains(near.translationKey()), "the near family is found");
		helper.assertFalse(found.contains(far.translationKey()), "the far family is found");
		helper.assertValueEqual(Discovery.discover(player), 0, "a second scan finds nothing new");
		helper.succeed();
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

	private static void spawn(GameTestHelper helper, Vec3 pos, FamilyResult family) {
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(), family));
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.snapTo(pos, 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zombie);
	}
}
