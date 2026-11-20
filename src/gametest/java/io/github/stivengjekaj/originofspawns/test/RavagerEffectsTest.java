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
}
