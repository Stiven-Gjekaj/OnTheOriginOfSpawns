package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.LurkGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BehaviorEffectsTest {
	@GameTest
	public void silenceAndLoudJoinTheirEffects(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_quiet", "test_loud");
		GeneEffect.Silence silence = GeneBehaviors.silence(zombie);
		helper.assertValueEqual(silence, new GeneEffect.Silence(true, true, false, false), "silence");
		helper.assertValueEqual(GeneBehaviors.volumeMultiplier(zombie), 2.0F, "volume");
		helper.succeed();
	}

	@GameTest
	public void attackIntervalScales(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_fast_attacks");
		helper.assertValueEqual(GeneBehaviors.attackIntervalMultiplier(zombie), 0.5F, "multiplier");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void regenerateHealsAnIdleZombie(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_regen");
		zombie.setHealth(10.0F);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(zombie.getHealth() > 10.0F, "health was " + zombie.getHealth());
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 160)
	public void auraGivesThePlayerTheEffect(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_aura");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		helper.runAfterDelay(70, () -> player.setPos(zombie.position()));
		helper.runAfterDelay(120, () -> {
			helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "the player has weakness");
			helper.succeed();
		});
	}

	@GameTest
	public void glowingMakesTheZombieGlow(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_glow");
		GeneBehaviors.installGoals(zombie);
		helper.assertTrue(zombie.hasGlowingTag(), "the zombie glows");
		helper.succeed();
	}

	@GameTest
	public void runtMakesASmallBaby(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_runt");
		double adultHealth = zombie.getAttributeValue(Attributes.MAX_HEALTH);
		GeneBehaviors.onSpawn(zombie, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(zombie.isBaby(), "the zombie is a baby");
		helper.assertValueEqual(zombie.getAttributeValue(Attributes.MAX_HEALTH), adultHealth / 2.0, "maximum health");
		helper.succeed();
	}

	@GameTest
	public void zombiesHaveNoVanillaBabies(GameTestHelper helper) {
		int zombieBabies = 0;
		int huskBabies = 0;
		for (int i = 0; i < 200; i++) {
			Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.NATURAL);
			zombie.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(helper.absolutePos(BlockPos.ZERO)),
					EntitySpawnReason.NATURAL, null);
			zombieBabies += zombie.isBaby() ? 1 : 0;
			Husk husk = EntityTypes.HUSK.create(helper.getLevel(), EntitySpawnReason.NATURAL);
			husk.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(helper.absolutePos(BlockPos.ZERO)),
					EntitySpawnReason.NATURAL, null);
			huskBabies += husk.isBaby() ? 1 : 0;
		}
		helper.assertValueEqual(zombieBabies, 0, "zombie babies");
		helper.assertTrue(huskBabies > 0, "husks still have babies");
		helper.succeed();
	}

	@GameTest
	public void broodSpawnsAZombieWithTheSameGenes(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(2, 1, 2), "test_brood");
		AABB area = new AABB(zombie.blockPosition()).inflate(3);
		int before = helper.getLevel().getEntitiesOfClass(Zombie.class, area).size();
		GeneBehaviors.onSpawn(zombie, helper.getLevel(), EntitySpawnReason.NATURAL);
		List<Zombie> zombies = helper.getLevel().getEntitiesOfClass(Zombie.class, area);
		helper.assertValueEqual(zombies.size(), before + 1, "zombies");
		helper.assertTrue(zombies.stream().filter(other -> other != zombie)
				.anyMatch(other -> zombie.getAttached(ModAttachments.GENES).equals(other.getAttached(ModAttachments.GENES))),
				"the new zombie has the same genes");
		helper.succeed();
	}

	@GameTest
	public void killedPlayerRisesWithTheHead(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_raise");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 1, 3))));
		GeneBehaviors.raiseVictim(zombie, player, helper.getLevel());
		List<Zombie> raised = helper.getLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(1),
				other -> other != zombie);
		helper.assertValueEqual(raised.size(), 1, "raised zombies");
		helper.assertTrue(raised.getFirst().getItemBySlot(EquipmentSlot.HEAD).is(Items.PLAYER_HEAD), "it wears a head");
		helper.assertTrue(raised.getFirst().getItemBySlot(EquipmentSlot.HEAD).has(DataComponents.PROFILE), "the head has a profile");
		helper.assertValueEqual(raised.getFirst().getAttached(ModAttachments.GENES), zombie.getAttached(ModAttachments.GENES),
				"genes");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void callOnSightCallsTheZombiesNear(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_call_sight");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.setTarget(player);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(ally.getTarget() == player, "the ally targets the player");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 100)
	public void frenzyTargetsAnotherSpecies(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_frenzy_target");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(zombie.getTarget() instanceof Pig, "the zombie targets the pig");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 160)
	public void huntFindsAnInvisiblePlayer(GameTestHelper helper) {
		// The mock server player is always creative, so the test asks the check directly.
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_hunt");
		GeneEffect.Hunt hunt = new GeneEffect.Hunt(8.0F, 1.0F);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(zombie.position().add(4.0, 0.0, 0.0));
		helper.assertFalse(GeneBehaviors.isHunted(zombie, player, hunt), "a visible healthy player");
		player.setInvisible(true);
		helper.assertTrue(GeneBehaviors.isHunted(zombie, player, hunt), "an invisible player at 4 blocks");
		player.setPos(zombie.position().add(12.0, 0.0, 0.0));
		helper.assertFalse(GeneBehaviors.isHunted(zombie, player, hunt), "an invisible player at 12 blocks");
		player.setInvisible(false);
		player.setHealth(4.0F);
		helper.assertTrue(GeneBehaviors.isHunted(zombie, player, hunt), "a wounded player at 12 blocks");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void speedChangesInWater(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, Blocks.WATER);
		Zombie zombie = zombieWith(helper, pos, "test_swim_slow");
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(zombie.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(OriginOfSpawns.id("speed_when/0")),
					"the water modifier is on");
			helper.succeed();
		});
	}

	@GameTest
	public void goalsAreInstalled(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_leap", "test_lurk", "test_siege");
		GeneBehaviors.installGoals(zombie);
		var goals = ((MobAccessor) zombie).originofspawns$goalSelector().getAvailableGoals();
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof LeapGoal), "leap goal");
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof LurkGoal), "lurk goal");
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof SiegeGoal), "siege goal");
		helper.succeed();
	}

	@GameTest
	public void siegeFindsTheGlassOnTheWayToTheTarget(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Zombie zombie = zombieWith(helper, pos, "test_siege");
		helper.setBlock(pos.east(), Blocks.GLASS);
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos.east(4));
		zombie.setTarget(pig);
		SiegeGoal goal = new SiegeGoal(zombie, new GeneEffect.Siege(10.0F));
		helper.assertTrue(goal.canUse(), "the goal finds the glass");
		helper.setBlock(pos.east(), Blocks.AIR);
		helper.assertFalse(goal.canUse(), "no glass, no goal");
		helper.succeed();
	}

	@GameTest
	public void leapStartsInItsRange(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_leap");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 1));
		zombie.setTarget(pig);
		// A zombie with no AI does not update its ground state, so the test sets it.
		zombie.setOnGround(true);
		LeapGoal goal = new LeapGoal(zombie, new GeneEffect.Leap(3.0F, 5.0F, 5.0F, 1.0F));
		helper.assertTrue(goal.canUse(), "the leap starts at 4 blocks");
		pig.setPos(zombie.position().add(1.0, 0.0, 0.0));
		helper.assertFalse(goal.canUse(), "no leap at 1 block");
		helper.succeed();
	}

	@GameTest
	public void lurkWaitsOnlyWhenWatchedFromAfar(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_lurk");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		// The player stands 5 blocks south of the zombie. A yaw of 180 faces north, to the zombie.
		player.setPos(zombie.getX(), zombie.getY(), zombie.getZ() + 5.0);
		player.setYRot(180.0F);
		player.setXRot(0.0F);
		helper.assertTrue(LurkGoal.isWatchedFromAfar(zombie, player, 3.0), "watched from 5 blocks");
		player.setYRot(0.0F);
		helper.assertFalse(LurkGoal.isWatchedFromAfar(zombie, player, 3.0), "player looks away");
		player.setYRot(180.0F);
		helper.assertFalse(LurkGoal.isWatchedFromAfar(zombie, player, 6.0), "player is in the radius");
		helper.succeed();
	}

	private static Zombie zombieWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		return zombie;
	}
}
