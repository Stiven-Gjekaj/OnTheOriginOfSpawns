package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CrowdTracker;
import io.github.stivengjekaj.originofspawns.evolution.DeathTrace;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DeathFactsTest {
	@GameTest
	public void aDeathInWaterIsInWaterAndNotOnLand(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 1), net.minecraft.world.level.block.Blocks.WATER);
		Zombie wet = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Zombie dry = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		helper.runAfterDelay(2, () -> {
			Set<DeathCondition> inWater = Evolution.facts(wet, helper.getLevel().damageSources().generic(), helper.getLevel());
			Set<DeathCondition> onLand = Evolution.facts(dry, helper.getLevel().damageSources().generic(), helper.getLevel());
			helper.assertTrue(inWater.contains(DeathCondition.IN_WATER) && !inWater.contains(DeathCondition.ON_LAND), "in water");
			helper.assertTrue(onLand.contains(DeathCondition.ON_LAND) && !onLand.contains(DeathCondition.IN_WATER), "on land");
			helper.succeed();
		});
	}

	@GameTest
	public void freezingIsACold(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().freeze(), helper.getLevel());
		helper.assertTrue(facts.contains(DeathCondition.COLD), "freezing is cold");
		helper.succeed();
	}

	@GameTest
	public void nightFollowsTheDarkSky(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertValueEqual(facts.contains(DeathCondition.NIGHT), helper.getLevel().isDarkOutside(), "night");
		helper.succeed();
	}

	@GameTest
	public void aKillerInABoatIsABoatDeath(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		var boat = helper.spawn(EntityTypes.OAK_BOAT, new BlockPos(4, 1, 4));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(boat.position());
		player.startRiding(boat, true, false);
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().playerAttack(player),
				helper.getLevel());
		helper.assertTrue(player.getVehicle() == boat, "the player rides the boat");
		helper.assertTrue(facts.contains(DeathCondition.BOAT), "boat");
		helper.succeed();
	}

	@GameTest
	public void aWolfKillIsAWolfDeath(GameTestHelper helper) {
		var skeleton = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, new BlockPos(1, 1, 1));
		var wolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, new BlockPos(3, 1, 3));
		var sources = helper.getLevel().damageSources();
		helper.assertTrue(Evolution.facts(skeleton, sources.mobAttack(wolf), helper.getLevel()).contains(DeathCondition.WOLF),
				"a wolf kill");
		helper.assertFalse(Evolution.facts(skeleton, sources.generic(), helper.getLevel()).contains(DeathCondition.WOLF),
				"other damage");
		helper.succeed();
	}

	@GameTest
	public void aRecentShieldBlockIsAShieldDeath(GameTestHelper helper) {
		var skeleton = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		var source = helper.getLevel().damageSources().playerAttack(player);
		long now = helper.getLevel().getGameTime();
		helper.assertFalse(Evolution.facts(skeleton, source, helper.getLevel()).contains(DeathCondition.SHIELD),
				"a player that did not block");
		player.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.SHIELD_BLOCK, now - 100L);
		helper.assertTrue(Evolution.facts(skeleton, source, helper.getLevel()).contains(DeathCondition.SHIELD),
				"a block 5 seconds ago");
		player.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.SHIELD_BLOCK, now - 300L);
		helper.assertFalse(Evolution.facts(skeleton, source, helper.getLevel()).contains(DeathCondition.SHIELD),
				"a block 15 seconds ago");
		helper.succeed();
	}

	@GameTest
	public void cactiBerriesAndTridentsHaveTheirTags(GameTestHelper helper) {
		var sources = helper.getLevel().damageSources();
		var contact = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
				Identifier.fromNamespaceAndPath("originofspawns", "contact"));
		var trident = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
				Identifier.fromNamespaceAndPath("originofspawns", "trident"));
		helper.assertTrue(sources.cactus().is(contact), "a cactus is contact");
		helper.assertTrue(sources.sweetBerryBush().is(contact), "a berry bush is contact");
		helper.assertFalse(sources.generic().is(contact), "generic damage is not contact");
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		helper.assertTrue(sources.trident(zombie, zombie).is(trident), "a trident is a trident");
		helper.succeed();
	}

	@GameTest
	public void loneZombieIsAlone(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(facts.contains(DeathCondition.ALONE), "alone");
		helper.succeed();
	}

	@GameTest
	public void zombieWithANeighbourIsNotAlone(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 1));
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertFalse(facts.contains(DeathCondition.ALONE), "alone");
		helper.succeed();
	}

	@GameTest
	public void golemKillIsAGolemDeath(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(4, 1, 4));
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().mobAttack(golem),
				helper.getLevel());
		helper.assertTrue(facts.contains(DeathCondition.GOLEM), "golem");
		helper.succeed();
	}

	@GameTest
	public void criticalHitInTheSameTickIsCritical(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Evolution.markCritical(zombie);
		Set<DeathCondition> facts = Evolution.facts(zombie, helper.getLevel().damageSources().playerAttack(player),
				helper.getLevel());
		helper.assertTrue(facts.contains(DeathCondition.CRITICAL), "critical");
		helper.succeed();
	}

	@GameTest
	public void sunBurnThenFireIsSunlight(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Evolution.markSunBurn(zombie);
		helper.assertTrue(Evolution.facts(zombie, helper.getLevel().damageSources().onFire(), helper.getLevel())
				.contains(DeathCondition.SUNLIGHT), "fire after sun burn");
		helper.assertFalse(Evolution.facts(zombie, helper.getLevel().damageSources().generic(), helper.getLevel())
				.contains(DeathCondition.SUNLIGHT), "generic damage after sun burn");
		helper.succeed();
	}

	@GameTest
	public void twoSecondsInViewIsSpotted(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.setAttached(ModAttachments.TRACE, DeathTrace.NONE.seenBy(player.getUUID()));
		helper.assertFalse(Evolution.facts(zombie, helper.getLevel().damageSources().playerAttack(player),
				helper.getLevel()).contains(DeathCondition.SPOTTED), "after 1 second");
		zombie.setAttached(ModAttachments.TRACE, DeathTrace.NONE.seenBy(player.getUUID()).seenBy(player.getUUID()));
		helper.assertTrue(Evolution.facts(zombie, helper.getLevel().damageSources().playerAttack(player),
				helper.getLevel()).contains(DeathCondition.SPOTTED), "after 2 seconds");
		helper.succeed();
	}

	@GameTest
	public void thirdDeathMakesACrowdOfAllThree(GameTestHelper helper) {
		CrowdTracker tracker = new CrowdTracker();
		Identifier zombie = Identifier.withDefaultNamespace("zombie");
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(0, 0, 0), 0)).size(), 0, "first death");
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(3, 0, 0), 10)).size(), 0, "second death");
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(0, 0, 3), 20)).size(), 3, "third death");
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(1, 0, 1), 30)).size(), 1, "fourth death");
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(50, 0, 0), 40)).size(), 0, "far death");
		helper.succeed();
	}

	@GameTest
	public void deathsOutsideTheWindowAreNoCrowd(GameTestHelper helper) {
		CrowdTracker tracker = new CrowdTracker();
		Identifier zombie = Identifier.withDefaultNamespace("zombie");
		tracker.add(death(zombie, new Vec3(0, 0, 0), 0));
		tracker.add(death(zombie, new Vec3(1, 0, 0), 10));
		helper.assertValueEqual(tracker.add(death(zombie, new Vec3(2, 0, 0), 200)).size(), 0, "late death");
		helper.succeed();
	}

	@GameTest
	public void playerKillRaisesTheGenesOfTheKiller(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Drowned drowned = helper.spawnWithNoFreeWill(EntityTypes.DROWNED, pos);
		Identifier heatSkin = Identifier.fromNamespaceAndPath("originofspawns_test", "test_heat_skin");
		Identifier plague = Identifier.fromNamespaceAndPath("originofspawns_test", "test_plague");
		drowned.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(heatSkin)));
		Identifier species = Identifier.withDefaultNamespace("drowned");
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		float heatBefore = pools.frequency(species, category, heatSkin);
		float plagueBefore = pools.frequency(species, category, plague);

		Evolution.onPlayerKilled(helper.getLevel(), drowned, helper.getLevel().damageSources().mobAttack(drowned));

		helper.assertValueEqual(pools.frequency(species, category, heatSkin), heatBefore + 0.02F * (1 - heatBefore),
				"gene of the killer");
		helper.assertValueEqual(pools.frequency(species, category, plague), plagueBefore + 0.02F * (1 - plagueBefore),
				"player death gene");
		helper.succeed();
	}

	private static CrowdTracker.Death death(Identifier species, Vec3 pos, long time) {
		return new CrowdTracker.Death(Level.OVERWORLD, species, BiomeCategory.TEMPERATE, pos, time, false);
	}
}
