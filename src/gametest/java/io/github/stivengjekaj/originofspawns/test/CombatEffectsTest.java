package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

public class CombatEffectsTest {
	@GameTest
	public void ambushDoublesOnlyTheFirstHit(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_ambush");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		var source = helper.getLevel().damageSources().mobAttack(zombie);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, source, 4.0F), 8.0F, "first hit");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, source, 4.0F), 4.0F, "second hit");
		helper.succeed();
	}

	@GameTest
	public void aloneWeakensTheDamage(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_alone_weak");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		var source = helper.getLevel().damageSources().mobAttack(zombie);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, source, 4.0F), 2.0F, "alone");
		helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(2, 1, 1));
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, source, 4.0F), 4.0F, "with a neighbour");
		helper.succeed();
	}

	@GameTest
	public void rallyAddsDamageForEachAlly(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_rally");
		helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(2, 1, 1));
		helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 2));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		float damage = GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().mobAttack(zombie), 10.0F);
		helper.assertTrue(Math.abs(damage - 12.0F) < 0.001F, "2 allies give 20% more, was " + damage);
		helper.succeed();
	}

	@GameTest
	public void golemsDoMoreDamage(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_golem_bait");
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(3, 1, 3));
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, helper.getLevel().damageSources().mobAttack(golem), 4.0F),
				6.0F, "golem damage");
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, helper.getLevel().damageSources().generic(), 4.0F),
				4.0F, "other damage");
		helper.succeed();
	}

	@GameTest
	public void criticalHitsDoNoExtraDamage(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_crit_immune");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Evolution.markCritical(zombie);
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, helper.getLevel().damageSources().playerAttack(player),
				6.0F), 4.0F, "critical damage");
		helper.succeed();
	}

	@GameTest
	public void hitGivesTheStatusEffect(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_hunger_hit");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.doHurtTarget(helper.getLevel(), player);
		helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "the player has hunger");
		helper.assertValueEqual(player.getEffect(MobEffects.HUNGER).getAmplifier(), 1, "amplifier");
		helper.succeed();
	}

	@GameTest
	public void leechTakesXpAndDropsItWhenItDies(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_leech");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.giveExperiencePoints(10);
		zombie.doHurtTarget(helper.getLevel(), player);
		helper.assertValueEqual(player.totalExperience, 7, "player XP");
		helper.assertValueEqual(zombie.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE).leechedXp(), 3,
				"leeched XP");
		helper.assertValueEqual(zombie.getExperienceReward(helper.getLevel(), player), 0, "normal XP");
		GeneCombat.onDeath(zombie, helper.getLevel());
		int orbs = helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, zombie.getBoundingBox().inflate(2))
				.stream().mapToInt(ExperienceOrb::getValue).sum();
		helper.assertValueEqual(orbs, 3, "XP in the orbs");
		helper.succeed();
	}

	@GameTest
	public void hurtCallsTheZombiesNear(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_call");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(ally.getTarget() == player, "the ally targets the player");
		helper.succeed();
	}

	@GameTest
	public void graveCallStopsAtItsMaximum(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_grave");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		for (int i = 0; i < 4; i++) {
			GeneCombat.onHurt(zombie, helper.getLevel().damageSources().playerAttack(player));
		}
		helper.assertValueEqual(zombie.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE).graveCalls(), 2,
				"grave calls");
		helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void deathBurstHurtsNearPlayers(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_burst");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		// A player that just joined cannot take damage for 60 ticks.
		helper.runAfterDelay(80, () -> {
			player.setPos(zombie.position());
			float health = player.getHealth();
			GeneCombat.onDeath(zombie, helper.getLevel());
			helper.assertValueEqual(player.getHealth(), health - 4.0F, "player health");
			helper.succeed();
		});
	}

	@GameTest
	public void deathCloudStaysWhereTheZombieDied(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_cloud");
		GeneCombat.onDeath(zombie, helper.getLevel());
		List<AreaEffectCloud> clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class,
				new AABB(zombie.blockPosition()).inflate(1));
		helper.assertValueEqual(clouds.size(), 1, "clouds");
		helper.succeed();
	}

	@GameTest
	public void killedVillagerAlwaysTurns(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_convert");
		Villager villager = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new BlockPos(3, 1, 3));
		zombie.killedEntity(helper.getLevel(), villager, helper.getLevel().damageSources().mobAttack(zombie));
		helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ZombieVillager.class,
				new AABB(villager.blockPosition()).inflate(2)).isEmpty(), "a zombie villager exists");
		helper.succeed();
	}

	private static Zombie zombieWith(GameTestHelper helper, BlockPos pos, String gene) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
		zombie.setAttached(ModAttachments.GENES,
				new CarriedGenes(List.of(Identifier.fromNamespaceAndPath("originofspawns_test", gene))));
		return zombie;
	}
}
