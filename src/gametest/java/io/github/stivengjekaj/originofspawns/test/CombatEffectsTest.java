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
import net.minecraft.world.effect.MobEffectInstance;
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
	public void callOnHurtWaitsForItsCooldown(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_call_cooldown");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		Player first = helper.makeMockPlayer(GameType.SURVIVAL);
		Player second = helper.makeMockPlayer(GameType.SURVIVAL);
		GeneCombat.onHurt(zombie, helper.getLevel().damageSources().playerAttack(first));
		helper.assertTrue(ally.getTarget() == first, "the first call");
		GeneCombat.onHurt(zombie, helper.getLevel().damageSources().playerAttack(second));
		helper.assertTrue(ally.getTarget() == first, "no second call in the cooldown");
		helper.succeed();
	}

	@GameTest
	public void groupStateUsesItsRadius(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_linked_close");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 7));
		var source = helper.getLevel().damageSources().generic();
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, source, 4.0F), 4.0F, "an ally 6 blocks away");
		ally.setPos(zombie.position().add(0.0, 0.0, 3.0));
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, source, 4.0F), 2.0F, "an ally 3 blocks away");
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

	@GameTest
	public void aDodgedProjectileDoesNoDamage(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_dodge");
		float health = zombie.getHealth();
		helper.assertFalse(zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().thrown(zombie, null), 4.0F),
				"the projectile hits");
		helper.assertValueEqual(zombie.getHealth(), health, "health after a dodge");
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 4.0F);
		helper.assertValueEqual(zombie.getHealth(), health - 4.0F, "other damage still hits");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aDodgedArrowGoesThrough(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(2, 1, 1), "test_dodge");
		float health = zombie.getHealth();
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(helper.getLevel(),
				zombie.getX(), zombie.getY(0.5), zombie.getZ() + 3.0, new net.minecraft.world.item.ItemStack(
						net.minecraft.world.item.Items.ARROW), null);
		arrow.setNoGravity(true);
		arrow.setDeltaMovement(0.0, 0.0, -1.0);
		helper.getLevel().addFreshEntity(arrow);
		helper.runAfterDelay(8, () -> {
			helper.assertValueEqual(zombie.getHealth(), health, "the zombie health");
			helper.assertTrue(arrow.getZ() < zombie.getZ() - 1.0, "the arrow goes past the zombie");
			helper.succeed();
		});
	}

	@GameTest
	public void undyingSurvivesOneLethalHit(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_undying");
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		helper.assertTrue(zombie.isAlive(), "the first lethal hit");
		helper.assertValueEqual(zombie.getHealth(), 4.0F, "health after the first lethal hit");
		// Shortly after a hit, a mob takes only the part of a new hit that is bigger than the last hit.
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 2000.0F);
		helper.assertFalse(zombie.isAlive(), "the second lethal hit");
		helper.succeed();
	}

	@GameTest
	public void effectImmunityStopsTheEffect(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_poison_immune");
		zombie.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
		zombie.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100));
		helper.assertFalse(zombie.hasEffect(MobEffects.POISON), "poison");
		helper.assertTrue(zombie.hasEffect(MobEffects.SLOWNESS), "an effect that is not in the list");
		helper.succeed();
	}

	@GameTest
	public void sharedDamageSplitsBetweenTheGroup(GameTestHelper helper) {
		Zombie hit = clean(helper, new BlockPos(1, 1, 1), "test_share_damage");
		Zombie ally = clean(helper, new BlockPos(3, 1, 1), "test_share_damage");
		float full = hit.getHealth();
		hit.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 10.0F);
		helper.assertValueEqual(hit.getHealth(), full - 5.0F, "the hit zombie takes half");
		helper.assertValueEqual(ally.getHealth(), full - 5.0F, "the ally takes the other half");
		helper.succeed();
	}

	@GameTest
	public void healsAreMultipliedAndShared(GameTestHelper helper) {
		Zombie half = clean(helper, new BlockPos(1, 1, 1), "test_half_heal");
		half.setHealth(10.0F);
		half.heal(4.0F);
		helper.assertValueEqual(half.getHealth(), 12.0F, "a heal at half strength");
		Zombie healer = clean(helper, new BlockPos(1, 1, 3), "test_share_heal");
		Zombie ally = clean(helper, new BlockPos(3, 1, 3), "test_share_heal");
		healer.setHealth(10.0F);
		ally.setHealth(10.0F);
		healer.heal(2.0F);
		helper.assertValueEqual(ally.getHealth(), 12.0F, "the ally gets the same heal");
		helper.assertValueEqual(healer.getHealth(), 12.0F, "the healer does not get a second heal back");
		helper.succeed();
	}

	@GameTest
	public void drainFoodTakesFoodAndHeals(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_drain");
		zombie.setHealth(10.0F);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getFoodData().setFoodLevel(20);
		GeneCombat.onHit(zombie, player);
		helper.assertValueEqual(player.getFoodData().getFoodLevel(), 18, "food");
		helper.assertValueEqual(zombie.getHealth(), 11.0F, "health of the zombie");
		helper.succeed();
	}

	@GameTest
	public void deathEffectReachesThePlayersNear(GameTestHelper helper) {
		Zombie zombie = clean(helper, new BlockPos(1, 1, 1), "test_death_blind");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(zombie.position().add(2.0, 0.0, 0.0));
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		helper.assertTrue(player.hasEffect(MobEffects.BLINDNESS), "blindness");
		helper.succeed();
	}

	/**
	 * Makes a zombie with one test gene. The gene goes on before the zombie enters the level, so it gets nothing from
	 * the pool.
	 */
	private static Zombie clean(GameTestHelper helper, BlockPos pos, String gene) {
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		zombie.setAttached(ModAttachments.GENES,
				new CarriedGenes(List.of(Identifier.fromNamespaceAndPath("originofspawns_test", gene))));
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zombie);
		return zombie;
	}

	private static Zombie zombieWith(GameTestHelper helper, BlockPos pos, String gene) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
		zombie.setAttached(ModAttachments.GENES,
				new CarriedGenes(List.of(Identifier.fromNamespaceAndPath("originofspawns_test", gene))));
		return zombie;
	}
}
