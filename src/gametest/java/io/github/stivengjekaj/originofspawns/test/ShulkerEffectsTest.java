package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

/**
 * Checks the genes of a shulker: its bullets, its shell, its teleports, its split, and the reused effects.
 */
public class ShulkerEffectsTest {
	static Shulker shulker(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.SHULKER, pos, genes);
	}

	/**
	 * Makes a bullet of the shulker at the target, as the attack of the shulker does.
	 */
	static ShulkerBullet bullet(GameTestHelper helper, Shulker shulker, Pig target) {
		return new ShulkerBullet(helper.getLevel(), shulker, target, Direction.Axis.Y);
	}

	@GameTest
	public void aHotShotAddsTwoDamageBeforeTheMultipliers(GameTestHelper helper) {
		Shulker shulker = shulker(helper, new BlockPos(1, 1, 1), "test_hot_shot");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		var shot = helper.getLevel().damageSources().mobProjectile(bullet(helper, shulker, pig), shulker);
		// (4 + 2) * 1.5.
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, shot, 4.0F), 9.0F, "the bullet does 2 more damage, then 50% more");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().mobAttack(shulker), 4.0F),
				4.0F, "a hit with no bullet does not change");
		helper.succeed();
	}

	static double stepSpeed(ShulkerBullet bullet) {
		var steps = (io.github.stivengjekaj.originofspawns.mixin.ShulkerBulletAccessor) bullet;
		return Math.sqrt(steps.originofspawns$targetDeltaX() * steps.originofspawns$targetDeltaX()
				+ steps.originofspawns$targetDeltaY() * steps.originofspawns$targetDeltaY()
				+ steps.originofspawns$targetDeltaZ() * steps.originofspawns$targetDeltaZ());
	}

	@GameTest
	public void aSeekerShotFliesThirtyPercentFaster(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		Shulker plain = shulker(helper, new BlockPos(1, 1, 1));
		Shulker seeker = shulker(helper, new BlockPos(1, 1, 4), "test_seeker_shot");
		helper.assertTrue(Math.abs(stepSpeed(bullet(helper, plain, pig)) - 0.15) < 1.0E-6, "a vanilla bullet step is 0.15");
		helper.assertTrue(Math.abs(stepSpeed(bullet(helper, seeker, pig)) - 0.195) < 1.0E-6, "a seeker bullet step is 0.195");
		helper.succeed();
	}

	static void hit(ShulkerBullet bullet, net.minecraft.world.entity.Entity target) {
		((io.github.stivengjekaj.originofspawns.mixin.ShulkerBulletAccessor) bullet).originofspawns$onHitEntity(
				new net.minecraft.world.phys.EntityHitResult(target));
	}

	@GameTest
	public void liftGenesMakeTheLevitationOfABulletLongerAndStronger(GameTestHelper helper) {
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		Pig liftTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		hit(bullet(helper, shulker(helper, new BlockPos(1, 1, 1)), plainTarget), plainTarget);
		hit(bullet(helper, shulker(helper, new BlockPos(1, 1, 4), "test_steep_lift"), liftTarget), liftTarget);
		var vanilla = plainTarget.getEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
		var lifted = liftTarget.getEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
		helper.assertValueEqual(vanilla.getDuration(), 200, "the vanilla Levitation lasts 10 seconds");
		helper.assertValueEqual(vanilla.getAmplifier(), 0, "the vanilla Levitation is Levitation I");
		helper.assertValueEqual(lifted.getDuration(), 400, "the gene Levitation lasts 20 seconds");
		helper.assertValueEqual(lifted.getAmplifier(), 1, "the gene Levitation is Levitation II");
		helper.succeed();
	}
}
