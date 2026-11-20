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

	/**
	 * Runs the vanilla attack goal of the shulker at the target until its first shot, and returns the wait to the next
	 * shot. The random source of the shulker starts at the same seed each time, so the vanilla wait is the same.
	 */
	static int waitAfterFirstShot(Shulker shulker, Pig target) {
		shulker.setTarget(target);
		net.minecraft.world.entity.ai.goal.Goal attack = ((io.github.stivengjekaj.originofspawns.mixin.MobAccessor) shulker)
				.originofspawns$goalSelector().getAvailableGoals().stream().map(goal -> goal.getGoal())
				.filter(goal -> goal.getClass().getSimpleName().equals("ShulkerAttackGoal")).findFirst().orElseThrow();
		attack.start();
		shulker.getRandom().setSeed(7L);
		// The first shot comes after 20 ticks.
		for (int i = 0; i < 20; i++) {
			attack.tick();
		}
		return ((io.github.stivengjekaj.originofspawns.mixin.ShulkerAttackGoalAccessor) attack).originofspawns$attackTime();
	}

	@GameTest
	public void aThrowRateGeneChangesTheWaitBetweenTwoShots(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		int vanilla = waitAfterFirstShot(shulker(helper, new BlockPos(1, 1, 1)), pig);
		int slow = waitAfterFirstShot(shulker(helper, new BlockPos(1, 1, 4), "test_slow_lid"), pig);
		helper.assertTrue(vanilla >= 20 && vanilla <= 110, "the vanilla wait is 1 to 5.5 seconds");
		helper.assertValueEqual(slow, vanilla * 2, "half the throw rate waits two times as long");
		helper.succeed();
	}

	@GameTest
	public void aDoubleShotShootsTwoBulletsAtEachThirdAttack(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		Shulker shulker = shulker(helper, new BlockPos(1, 1, 1), "test_double_shot");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.countShot(shulker, pig), "the first attack");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.countShot(shulker, pig), "the second attack");
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.countShot(shulker, pig),
				"the third attack shoots a second bullet");
		var bullets = helper.getLevel().getEntitiesOfClass(ShulkerBullet.class, shulker.getBoundingBox().inflate(2.0));
		helper.assertValueEqual(bullets.size(), 1, "one more bullet");
		helper.succeed();
	}

	@GameTest
	public void shellGenesChangeTheArmorAndTheDamageWhileTheShellIsOpenOrClosed(GameTestHelper helper) {
		Shulker shulker = shulker(helper, new BlockPos(1, 1, 1), "test_bunker_shell");
		var lid = (io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker;
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		var shot = helper.getLevel().damageSources().mobProjectile(bullet(helper, shulker(helper, new BlockPos(1, 1, 4)), pig), pig);
		lid.originofspawns$setRawPeekAmount(0);
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.tick(shulker, helper.getLevel());
		// A closed vanilla shulker has 20 armor.
		helper.assertValueEqual(shulker.getArmorValue(), 16, "4 less armor while closed");
		helper.assertValueEqual(GeneCombat.modifyDamage(shulker, shot, 10.0F), 10.0F, "full projectile damage while closed");
		lid.originofspawns$setRawPeekAmount(100);
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.tick(shulker, helper.getLevel());
		helper.assertValueEqual(shulker.getArmorValue(), 8, "8 armor while open");
		helper.assertValueEqual(GeneCombat.modifyDamage(shulker, shot, 10.0F), 7.0F, "30% less projectile damage while open");
		helper.succeed();
	}

	@GameTest
	public void aSnapLidBitesAPlayerNextToItWhileItIsOpen(GameTestHelper helper) {
		Shulker shulker = shulker(helper, new BlockPos(2, 1, 2), "test_snap_lid");
		var lid = (io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker;
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(3, 1, 2))));
		var snap = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.LidSnap(4.0F, 3.0F);
		lid.originofspawns$setRawPeekAmount(0);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.lidSnap(shulker, helper.getLevel(), snap),
				"a closed shulker does not snap");
		lid.originofspawns$setRawPeekAmount(100);
		shulker.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.SHOT_TIME, helper.getLevel().getGameTime());
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.lidSnap(shulker, helper.getLevel(), snap),
				"a shulker does not snap right after a shot");
		shulker.removeAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.SHOT_TIME);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.lidSnap(shulker, helper.getLevel(), snap),
				"an open shulker snaps");
		helper.assertValueEqual(player.getMaxHealth() - player.getHealth(), 4.0F, "the snap does 4 damage");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.lidSnap(shulker, helper.getLevel(), snap),
				"the next snap waits");
		helper.succeed();
	}

	// A teleport goes up to 8 blocks, so the test keeps the other tests away.
	@GameTest(padding = 12)
	public void aQuickBlinkTeleportsAtFullHealth(GameTestHelper helper) {
		// A zombie teleports to a free place on the ground, so one hurt shows the teleport.
		var zombie = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE, new BlockPos(1, 1, 1), "test_quick_blink");
		BlockPos zombieBefore = zombie.blockPosition();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 1.0F);
		helper.assertTrue(zombie.getHealth() > zombie.getMaxHealth() / 2.0F, "the zombie has more than half health");
		helper.assertFalse(zombie.blockPosition().equals(zombieBefore), "the hurt zombie teleports");
		// The vanilla teleport of a shulker tries 5 random places, and works only with AI.
		Shulker shulker = shulker(helper, new BlockPos(3, 1, 3), "test_quick_blink");
		shulker.setNoAi(false);
		BlockPos before = shulker.blockPosition();
		for (int i = 0; i < 40 && shulker.blockPosition().equals(before); i++) {
			io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.teleportOnHurt(shulker, helper.getLevel());
		}
		helper.assertFalse(shulker.blockPosition().equals(before), "the shulker uses its vanilla teleport");
		helper.succeed();
	}

	@GameTest(padding = 12, maxTicks = 100)
	public void aBlinkShutStaysClosedForThreeSecondsAfterATeleport(GameTestHelper helper) {
		Shulker shulker = shulker(helper, new BlockPos(3, 1, 3), "test_blink_shut");
		var lid = (io.github.stivengjekaj.originofspawns.mixin.ShulkerAccessor) shulker;
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		lid.originofspawns$setRawPeekAmount(100);
		shulker.setNoAi(false);
		BlockPos before = shulker.blockPosition();
		for (int i = 0; i < 40 && shulker.blockPosition().equals(before); i++) {
			lid.originofspawns$teleportSomewhere();
		}
		shulker.setNoAi(true);
		helper.assertFalse(shulker.blockPosition().equals(before), "the shulker teleports");
		helper.assertTrue(lid.originofspawns$isClosed(), "the shulker is closed after the teleport");
		helper.assertValueEqual(shulker.getArmorValue(), 20, "the closed shell has its armor");
		lid.originofspawns$setRawPeekAmount(100);
		helper.assertTrue(lid.originofspawns$isClosed(), "the shulker does not open in the 3 seconds");
		shulker.setTarget(pig);
		helper.runAfterDelay(61, () -> {
			io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.tickShut(shulker);
			helper.assertFalse(lid.originofspawns$isClosed(), "after 3 seconds, a shulker with a target opens");
			helper.succeed();
		});
	}

	@GameTest(padding = 12)
	public void aSightBlinkTeleportsToAPlaceThatSeesItsTarget(GameTestHelper helper) {
		for (BlockPos wall : BlockPos.betweenClosed(new BlockPos(0, 1, 4), new BlockPos(4, 3, 4))) {
			helper.setBlock(wall, net.minecraft.world.level.block.Blocks.STONE);
		}
		Shulker shulker = shulker(helper, new BlockPos(2, 1, 2));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 6));
		shulker.setTarget(pig);
		helper.assertFalse(shulker.hasLineOfSight(pig), "the wall hides the pig");
		var sight = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.SightTeleport(5.0F);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.sightTeleport(shulker, helper.getLevel(), sight),
				"the shulker waits 5 seconds");
		shulker.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.BLIND_SINCE,
				helper.getLevel().getGameTime() - 100L);
		boolean moved = false;
		for (int i = 0; i < 40 && !moved; i++) {
			moved = io.github.stivengjekaj.originofspawns.evolution.GeneShulkers.sightTeleport(shulker, helper.getLevel(), sight);
		}
		helper.assertTrue(moved, "after 5 seconds, the shulker teleports");
		helper.assertTrue(shulker.hasLineOfSight(pig), "the shulker sees the pig from its new place");
		helper.assertValueEqual(shulker.getTarget(), pig, "the shulker keeps its target");
		helper.succeed();
	}
}
