package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.mixin.RavagerAccessor;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Ravager;

/**
 * Checks the genes of a ravager: its roar, its stun, its charge, its rider, and the reused effects.
 */
public class RavagerEffectsTest {
	static Ravager ravager(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.RAVAGER, pos, genes);
	}

	static void roar(Ravager ravager) {
		((RavagerAccessor) ravager).originofspawns$roar();
	}

	static float lost(Pig pig) {
		return pig.getMaxHealth() - pig.getHealth();
	}

	@GameTest
	public void aLoudRoarReachesEightBlocks(GameTestHelper helper) {
		Ravager plain = ravager(helper, new BlockPos(1, 1, 1));
		Pig far = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 8));
		roar(plain);
		helper.assertValueEqual(lost(far), 0.0F, "the vanilla roar does not reach 6 blocks");
		Ravager loud = ravager(helper, new BlockPos(1, 1, 2), "test_loud_roar");
		plain.discard();
		roar(loud);
		helper.assertTrue(lost(far) > 0.0F, "the loud roar reaches 6 blocks");
		helper.succeed();
	}

	@GameTest
	public void aRoarDamageGeneActsWhileItsStateIsTrue(GameTestHelper helper) {
		Ravager ravager = ravager(helper, new BlockPos(1, 1, 1), "test_wounded_roar");
		Pig first = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 4));
		roar(ravager);
		helper.assertValueEqual(lost(first), 6.0F, "the roar does 6 damage at full health");
		first.discard();
		ravager.setHealth(10.0F);
		Pig second = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 4));
		roar(ravager);
		helper.assertValueEqual(lost(second), 9.0F, "the roar does 50% more damage under half health");
		helper.succeed();
	}

	@GameTest
	public void roarEffectsGoToTheTargetsAndToTheRaidersNear(GameTestHelper helper) {
		Ravager ravager = ravager(helper, new BlockPos(1, 1, 1), "test_war_roar");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 4));
		var pillager = MountEffectsTest.mob(helper, EntityTypes.PILLAGER, new BlockPos(5, 1, 1));
		roar(ravager);
		var slowness = pig.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
		helper.assertTrue(slowness != null && slowness.getAmplifier() == 1, "Fear Roar: the pig gets Slowness II");
		helper.assertTrue(pillager.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "War Roar: the pillager gets Strength");
		helper.assertFalse(pig.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "the pig is not a raider");
		helper.assertFalse(ravager.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "the ravager itself gets no Strength");
		helper.succeed();
	}

	@GameTest
	public void aRageRoarRoarsWhenAProjectileHurtsIt(GameTestHelper helper) {
		Ravager ravager = ravager(helper, new BlockPos(2, 1, 2), "test_rage_roar");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		ravager.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 1.0F);
		helper.assertValueEqual(ravager.getRoarTick(), 0, "a melee hit gives no roar");
		// A second ravager, because the first one does not take a second hit so soon.
		Ravager shot = ravager(helper, new BlockPos(2, 1, 5), "test_rage_roar");
		var arrow = EntityTypes.ARROW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		shot.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(arrow, pig), 2.0F);
		helper.assertValueEqual(shot.getRoarTick(), 20, "an arrow starts a roar with no stun");
		helper.succeed();
	}

	@GameTest
	public void aVictoryRoarRoarsAndHealsAfterItKillsAPlayer(GameTestHelper helper) {
		Ravager ravager = ravager(helper, new BlockPos(2, 1, 2), "test_victory_roar");
		ravager.setHealth(50.0F);
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(ravager), 1000.0F);
		helper.assertTrue(player.isDeadOrDying(), "the ravager kills the player");
		helper.assertValueEqual(ravager.getHealth(), 60.0F, "the ravager heals 10 health at once");
		helper.assertValueEqual(ravager.getRoarTick(), 20, "the ravager roars");
		helper.succeed();
	}

	static void block(GameTestHelper helper, Ravager ravager, net.minecraft.world.entity.LivingEntity defender) {
		((RavagerAccessor) ravager).originofspawns$blockedByItem(defender,
				helper.getLevel().damageSources().mobAttack(ravager), 12.0F, true);
	}

	@GameTest
	public void aStunChanceGeneChangesTheChanceOfAStunFromAShieldBlock(GameTestHelper helper) {
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneRavagers.stunChance(
				ravager(helper, new BlockPos(1, 1, 1))), 0.5, "the vanilla chance is 0.5");
		Ravager easy = ravager(helper, new BlockPos(5, 1, 5), "test_easy_stun");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneRavagers.stunChance(easy), 1.0,
				"the largest chance counts");
		block(helper, easy, CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 2)));
		helper.assertValueEqual(easy.getStunnedTick(), 40, "a shield block always stuns it");
		helper.succeed();
	}

	@GameTest
	public void aSteadyStanceIsNeverStunnedAndPushesThePlayerBack(GameTestHelper helper) {
		Ravager steady = ravager(helper, new BlockPos(2, 1, 2), "test_steady_stance");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 5));
		pig.setNoGravity(false);
		block(helper, steady, pig);
		helper.assertValueEqual(steady.getStunnedTick(), 0, "a shield block does not stun it, also with a stun chance of 1");
		helper.assertTrue(pig.getDeltaMovement().horizontalDistance() > 0.5, "it pushes the defender back hard");
		helper.succeed();
	}

	@GameTest
	public void aNoRoarGeneStopsTheRoarAtTheEndOfAStun(GameTestHelper helper) {
		Ravager plain = ravager(helper, new BlockPos(1, 1, 1));
		Ravager quiet = ravager(helper, new BlockPos(5, 1, 5), "test_no_roar");
		((RavagerAccessor) plain).originofspawns$setStunnedTick(1);
		((RavagerAccessor) quiet).originofspawns$setStunnedTick(1);
		plain.aiStep();
		quiet.aiStep();
		helper.assertValueEqual(plain.getRoarTick(), 20, "a vanilla ravager roars at the end of a stun");
		helper.assertValueEqual(quiet.getRoarTick(), 0, "the ravager with the gene does not roar");
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneRavagers.roarNow(quiet),
				"a roar with no stun still works");
		helper.succeed();
	}
}
