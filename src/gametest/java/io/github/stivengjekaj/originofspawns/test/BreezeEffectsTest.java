package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.LongJumpAccessor;
import io.github.stivengjekaj.originofspawns.mixin.ProjectileAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a breeze: its wind charges, its jumps, and the reused effects.
 */
public class BreezeEffectsTest {
	/**
	 * The place where the test wind charges burst. The pig is 1 block away, so the blast pushes it.
	 */
	static final BlockPos BURST = new BlockPos(3, 1, 3);

	@GameTest
	public void hardGustMakesTheWindChargesPushFarther(GameTestHelper helper) {
		double plain = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east()), breezeWith(helper, new BlockPos(6, 1, 6)));
		double hard = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east()),
				breezeWith(helper, new BlockPos(6, 1, 6), "test_hard_gust"));
		helper.assertTrue(plain > 0.0, "the plain charge pushes the pig");
		helper.assertTrue(Math.abs(hard / plain - 2.0) < 0.01, "two times the push, not " + hard / plain);
		helper.succeed();
	}

	@GameTest
	public void golemGustPushesOnlyTheGolemsFarther(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(6, 1, 6), "test_golem_gust");
		double plainGolem = pushOf(helper, golemAt(helper), breezeWith(helper, new BlockPos(6, 1, 1)));
		double golem = pushOf(helper, golemAt(helper), breeze);
		double plainPig = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east()), breezeWith(helper, new BlockPos(6, 1, 1)));
		double pig = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east()), breeze);
		helper.assertTrue(plainGolem > 0.0, "the plain charge pushes the golem");
		helper.assertTrue(Math.abs(golem / plainGolem - 2.0) < 0.01, "two times the push of a golem, not " + golem / plainGolem);
		helper.assertTrue(Math.abs(pig / plainPig - 1.0) < 0.01, "the same push of a pig, not " + pig / plainPig);
		helper.succeed();
	}

	@GameTest
	public void liftGustThrowsThePushedTargetUp(GameTestHelper helper) {
		Pig plainPig = CreeperEffectsTest.pigAt(helper, BURST.east());
		plainPig.setDeltaMovement(Vec3.ZERO);
		burst(helper, breezeWith(helper, new BlockPos(6, 1, 6)));
		double plain = plainPig.getDeltaMovement().y;
		plainPig.discard();
		Pig pig = CreeperEffectsTest.pigAt(helper, BURST.east());
		pig.setDeltaMovement(Vec3.ZERO);
		burst(helper, breezeWith(helper, new BlockPos(6, 1, 6), "test_lift_gust"));
		helper.assertTrue(pig.getDeltaMovement().y - plain > 0.79, "a speed up of 0.8 for 4 blocks, not "
				+ (pig.getDeltaMovement().y - plain));
		helper.succeed();
	}

	@GameTest
	public void chillWindGivesTheEffectToThePushedEntities(GameTestHelper helper) {
		Pig plainPig = CreeperEffectsTest.pigAt(helper, BURST.east());
		burst(helper, breezeWith(helper, new BlockPos(6, 1, 6)));
		helper.assertTrue(plainPig.getActiveEffects().isEmpty(), "a plain charge gives no effect");
		plainPig.discard();
		Pig pig = CreeperEffectsTest.pigAt(helper, BURST.east());
		burst(helper, breezeWith(helper, new BlockPos(6, 1, 6), "test_concussion"));
		helper.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "the pushed pig gets the effect");
		helper.succeed();
	}

	@GameTest
	public void wideGustMakesTheBlastOfTheWindChargesLarger(GameTestHelper helper) {
		// The pig is 4 blocks away. The vanilla blast reaches 6 blocks, and the larger one reaches 9 blocks.
		double plain = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east(4)), breezeWith(helper, new BlockPos(1, 1, 6)));
		double wide = pushOf(helper, CreeperEffectsTest.pigAt(helper, BURST.east(4)),
				breezeWith(helper, new BlockPos(1, 1, 6), "test_wide_gust"));
		helper.assertTrue(plain > 0.0, "the plain blast reaches the pig");
		helper.assertTrue(wide > plain * 1.3, "the larger blast pushes the far pig more, " + wide + " and " + plain);
		helper.succeed();
	}

	@GameTest
	public void cutWindMakesTheWindChargesDoMoreDamage(GameTestHelper helper) {
		IronGolem plainTarget = golemAt(helper);
		hit(helper, breezeWith(helper, new BlockPos(6, 1, 6)), plainTarget);
		helper.assertValueEqual(plainTarget.getMaxHealth() - plainTarget.getHealth(), 1.0F, "a plain charge does 1 damage");
		plainTarget.discard();
		IronGolem target = golemAt(helper);
		hit(helper, breezeWith(helper, new BlockPos(6, 1, 6), "test_cut_wind"), target);
		helper.assertValueEqual(target.getMaxHealth() - target.getHealth(), 5.0F, "4 more damage");
		helper.succeed();
	}

	@GameTest
	public void rapidWindMakesTheShotChargeAndWaitShorter(GameTestHelper helper) {
		// The test gene doubles the throw rate.
		Breeze plain = shooter(helper, new BlockPos(1, 1, 1));
		Breeze rapid = shooter(helper, new BlockPos(5, 1, 5), "test_quick_burst");
		var shoot = new net.minecraft.world.entity.monster.breeze.Shoot();
		long now = helper.getLevel().getGameTime();
		helper.assertTrue(shoot.tryStart(helper.getLevel(), plain, now), "the plain breeze starts a shot");
		helper.assertValueEqual(plain.getBrain().getTimeUntilExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING), 15L, "the charge");
		shoot.doStop(helper.getLevel(), plain, now);
		helper.assertValueEqual(plain.getBrain().getTimeUntilExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN), 10L, "the cooldown");
		var rapidShoot = new net.minecraft.world.entity.monster.breeze.Shoot();
		helper.assertTrue(rapidShoot.tryStart(helper.getLevel(), rapid, now), "the rapid breeze starts a shot");
		helper.assertValueEqual(rapid.getBrain().getTimeUntilExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING), 8L, "half the charge");
		rapidShoot.doStop(helper.getLevel(), rapid, now);
		helper.assertValueEqual(rapid.getBrain().getTimeUntilExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN), 5L, "half the cooldown");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void trueAimShootsTheWindChargesWithNoSpread(GameTestHelper helper) {
		// The vanilla aim has a random spread. With no spread, each charge goes on the line to the pig.
		double worst = 0.0;
		for (int i = 0; i < 5; i++) {
			worst = Math.max(worst, aimError(helper, "test_true_aim"));
		}
		helper.assertTrue(worst < 1.0E-4, "no spread, but an error of " + worst);
		double plain = 0.0;
		for (int i = 0; i < 5; i++) {
			plain = Math.max(plain, aimError(helper));
		}
		helper.assertTrue(plain > 1.0E-3, "the plain aim has a spread");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void fastChargeMakesTheWindChargesFaster(GameTestHelper helper) {
		// The random spread also changes the speed a little, so both breezes shoot with no spread.
		var plain = shoot(helper, shooter(helper, new BlockPos(1, 1, 1), "test_true_aim"));
		var fast = shoot(helper, shooter(helper, new BlockPos(1, 1, 5), "test_true_aim", "test_swift_shot"));
		double ratio = fast.getFirst().getDeltaMovement().length() / plain.getFirst().getDeltaMovement().length();
		helper.assertTrue(Math.abs(ratio - 1.5) < 0.001, "1.5 times the speed, not " + ratio);
		plain.forEach(BreezeWindCharge::discard);
		fast.forEach(BreezeWindCharge::discard);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void twinChargeShootsTwoWindChargesAtOnce(GameTestHelper helper) {
		helper.assertValueEqual(shoot(helper, shooter(helper, new BlockPos(1, 1, 1))).size(), 1, "a plain breeze shoots one");
		var twin = shoot(helper, shooter(helper, new BlockPos(1, 1, 5), "test_true_aim", "test_twin_shot"));
		helper.assertValueEqual(twin.size(), 2, "wind charges in a twin shot");
		Vec3 first = twin.get(0).getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
		Vec3 second = twin.get(1).getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
		double degrees = Math.toDegrees(Math.acos(Math.min(1.0, first.dot(second))));
		helper.assertTrue(Math.abs(degrees - 10.0) < 0.5, "the charges are 10 degrees apart, not " + degrees);
		twin.forEach(BreezeWindCharge::discard);
		helper.succeed();
	}

	/**
	 * Makes a breeze shoot one wind charge at a pig, and returns the angle in radians between the charge and the line
	 * that the shot aims at.
	 */
	static double aimError(GameTestHelper helper, String... genes) {
		Breeze breeze = shooter(helper, new BlockPos(1, 1, 3), genes);
		LivingEntity pig = breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
		var charges = shoot(helper, breeze);
		Vec3 aim = new Vec3(pig.getX() - breeze.getX(), pig.getY(0.3) - breeze.getFiringYPosition(), pig.getZ() - breeze.getZ())
				.normalize();
		double error = Math.acos(Math.min(1.0, charges.getFirst().getDeltaMovement().normalize().dot(aim)));
		charges.forEach(BreezeWindCharge::discard);
		breeze.discard();
		pig.discard();
		return error;
	}

	/**
	 * Makes the breeze shoot now, with the vanilla shot behavior, and returns its new wind charges.
	 */
	static java.util.List<BreezeWindCharge> shoot(GameTestHelper helper, Breeze breeze) {
		var shoot = new net.minecraft.world.entity.monster.breeze.Shoot();
		long now = helper.getLevel().getGameTime();
		helper.assertTrue(shoot.tryStart(helper.getLevel(), breeze, now), "the breeze starts a shot");
		breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING);
		shoot.tickOrStop(helper.getLevel(), breeze, now);
		return helper.getLevel().getEntitiesOfClass(BreezeWindCharge.class, helper.getBounds().inflate(4.0),
				charge -> charge.getOwner() == breeze);
	}

	@GameTest
	public void skyHopChangesTheLongJumpOfABreeze(GameTestHelper helper) {
		Vec3 plain = jumpOf(helper, breezeWith(helper, new BlockPos(1, 1, 1)));
		Vec3 sky = jumpOf(helper, breezeWith(helper, new BlockPos(1, 1, 1), "test_sky_hop"));
		helper.assertTrue(plain.horizontalDistance() > 0.0, "the plain breeze jumps");
		helper.assertTrue(Math.abs(sky.horizontalDistance() / plain.horizontalDistance() - 1.5) < 0.001,
				"1.5 times the speed along the ground");
		helper.assertTrue(Math.abs(sky.y / plain.y - 1.2) < 0.001, "1.2 times the speed up");
		helper.assertValueEqual(restAfterLanding(helper, breezeWith(helper, new BlockPos(1, 1, 1))), 10L, "the plain rest");
		helper.assertValueEqual(restAfterLanding(helper, breezeWith(helper, new BlockPos(1, 1, 1), "test_sky_hop")), 7L,
				"30% less rest");
		helper.succeed();
	}

	@GameTest
	public void crashDownHurtsTheMobsNearWhenTheBreezeLands(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_crash_down");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 3));
		Breeze other = breezeWith(helper, new BlockPos(3, 1, 5));
		restAfterLanding(helper, breeze);
		helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth() - 3.0F, "the pig near takes 3 damage");
		helper.assertValueEqual(other.getHealth(), other.getMaxHealth(), "another breeze takes no damage");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void windRideShootsAtTheTopOfTheJump(GameTestHelper helper) {
		Breeze breeze = shooter(helper, new BlockPos(1, 1, 3));
		breeze.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
		breeze.setDeltaMovement(0.0, 0.3, 0.0);
		helper.assertFalse(GeneBehaviors.jumpShot(breeze, helper.getLevel()), "no shot while the breeze rises");
		breeze.setDeltaMovement(0.0, -0.01, 0.0);
		helper.assertTrue(GeneBehaviors.jumpShot(breeze, helper.getLevel()), "a shot at the top of the jump");
		helper.assertFalse(GeneBehaviors.jumpShot(breeze, helper.getLevel()), "one shot in each jump");
		var charges = helper.getLevel().getEntitiesOfClass(BreezeWindCharge.class, helper.getBounds().inflate(4.0),
				charge -> charge.getOwner() == breeze);
		helper.assertValueEqual(charges.size(), 1, "wind charges");
		charges.forEach(BreezeWindCharge::discard);
		breeze.setPose(net.minecraft.world.entity.Pose.STANDING);
		GeneBehaviors.jumpShot(breeze, helper.getLevel());
		breeze.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
		helper.assertTrue(GeneBehaviors.jumpShot(breeze, helper.getLevel()), "a new jump has a new shot");
		helper.getLevel().getEntitiesOfClass(BreezeWindCharge.class, helper.getBounds().inflate(4.0)).forEach(BreezeWindCharge::discard);
		helper.succeed();
	}

	@GameTest
	public void galeGuardPushesOnlyTheMeleeAttackerBack(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_gale_guard");
		Pig attacker = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 3));
		Pig other = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 5));
		var level = helper.getLevel();
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(level, attacker,
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW), null);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, level.damageSources().arrow(arrow, attacker));
		arrow.discard();
		helper.assertValueEqual(attacker.getDeltaMovement().horizontalDistance(), 0.0, "an arrow does not count");
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, level.damageSources().mobAttack(attacker));
		helper.assertTrue(attacker.getDeltaMovement().x > 1.0, "the melee attacker flies back");
		helper.assertValueEqual(other.getDeltaMovement().horizontalDistance(), 0.0, "the other pig does not move");
		helper.succeed();
	}

	@GameTest
	public void stormGalePushesOneTimeUnderThirtyPercentHealth(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_storm_gale");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 3));
		var hit = helper.getLevel().damageSources().mobAttack(pig);
		breeze.setHealth(breeze.getMaxHealth() / 2.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertValueEqual(pig.getDeltaMovement().horizontalDistance(), 0.0, "no gale at half health");
		breeze.setHealth(breeze.getMaxHealth() / 5.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertTrue(pig.getDeltaMovement().x > 1.0, "the gale pushes the pig 3 blocks away");
		pig.setDeltaMovement(Vec3.ZERO);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertValueEqual(pig.getDeltaMovement().horizontalDistance(), 0.0, "the gale acts one time in its life");
		helper.succeed();
	}

	@GameTest
	public void spinUpMakesEachHitRaiseTheThrowRate(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_spin_up");
		var hit = helper.getLevel().damageSources().generic();
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(breeze, 100), 100, "no change with no hits");
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(breeze, 110), 100, "10% more often after one hit");
		for (int i = 0; i < 6; i++) {
			io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		}
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(breeze, 140), 100, "at most 40% more often");
		breeze.setAttached(ModAttachments.SPIN_TIME, helper.getLevel().getGameTime() - 201L);
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(breeze, 100), 100, "the hits end after 10 seconds");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void eyeOfStormShootsAtEachPlayerNear(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(1, 1, 1));
		var first = CreeperEffectsTest.survivalPlayer(helper).player();
		first.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 1))));
		var second = CreeperEffectsTest.survivalPlayer(helper).player();
		second.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 5))));
		var charge = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.ChargeEachPlayer(5.0F, 10.0F);
		helper.assertValueEqual(GeneBehaviors.chargeEachPlayer(breeze, helper.getLevel(), charge), 2, "one charge for each player");
		helper.assertValueEqual(GeneBehaviors.chargeEachPlayer(breeze, helper.getLevel(), charge), 0, "the next waits 10 seconds");
		helper.getLevel().getEntitiesOfClass(BreezeWindCharge.class, helper.getBounds().inflate(4.0)).forEach(BreezeWindCharge::discard);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void thunderCallHitsAPlayerUnderAnOpenSky(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(1, 1, 1));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 1))));
		var call = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.LightningCall(5.0F, 30.0F);
		helper.assertTrue(GeneBehaviors.lightningCall(breeze, helper.getLevel(), call) == player, "the lightning hits the player");
		var bolts = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class, helper.getBounds().inflate(4.0));
		helper.assertValueEqual(bolts.size(), 1, "lightning bolts");
		bolts.forEach(net.minecraft.world.entity.LightningBolt::discard);
		helper.assertTrue(GeneBehaviors.lightningCall(breeze, helper.getLevel(), call) == null, "the next waits 30 seconds");
		helper.succeed();
	}

	@GameTest
	public void lastDraftJumpsAwayOneTimeUnderThirtyPercentHealth(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_last_draft");
		Pig attacker = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 3));
		var hit = helper.getLevel().damageSources().mobAttack(attacker);
		breeze.setHealth(breeze.getMaxHealth() / 2.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertValueEqual(breeze.getDeltaMovement().horizontalDistance(), 0.0, "no jump at half health");
		breeze.setHealth(breeze.getMaxHealth() / 5.0F);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertTrue(breeze.getDeltaMovement().x > 1.0 && breeze.getDeltaMovement().y > 0.0, "the breeze jumps away");
		breeze.setDeltaMovement(Vec3.ZERO);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze, hit);
		helper.assertValueEqual(breeze.getDeltaMovement().horizontalDistance(), 0.0, "one jump in its life");
		helper.succeed();
	}

	@GameTest
	public void sideHopStepsAsideFromAMeleeHit(GameTestHelper helper) {
		Breeze breeze = breezeWith(helper, new BlockPos(3, 1, 3), "test_side_hop");
		Pig attacker = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		Vec3 before = breeze.position();
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze,
				helper.getLevel().damageSources().mobAttack(attacker));
		helper.assertValueEqual(Math.round(Math.abs(breeze.getX() - before.x)), 2L, "the breeze steps 2 blocks aside");
		helper.assertTrue(Math.abs(breeze.getZ() - before.z) < 0.6, "at a right angle to the attacker");
		Vec3 after = breeze.position();
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(breeze,
				helper.getLevel().damageSources().mobAttack(attacker));
		helper.assertTrue(breeze.position().equals(after), "the next step waits 8 seconds");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void disarmGustMakesThePlayerDropTheItemInTheOffHand(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))));
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.STONE_SWORD));
		player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.TORCH));
		hit(helper, breezeWith(helper, new BlockPos(1, 1, 1), "test_disarm_gust"), player);
		helper.assertTrue(player.getOffhandItem().isEmpty(), "the player drops the torch in the off hand");
		helper.assertTrue(player.getMainHandItem().is(net.minecraft.world.item.Items.STONE_SWORD), "the sword stays");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void shieldGustStopsTheShieldThatStopsTheWindCharge(GameTestHelper helper) {
		var player = CreeperEffectsTest.shieldedPlayer(helper);
		// The test player faces west, so the charge comes from the west.
		BreezeWindCharge charge = new BreezeWindCharge(breezeWith(helper, new BlockPos(1, 1, 1), "test_shield_scorch"),
				helper.getLevel());
		charge.setPos(player.getEyePosition().add(-1.0, 0.0, 0.0));
		helper.getLevel().addFreshEntity(charge);
		((ProjectileAccessor) charge).originofspawns$onHit(new net.minecraft.world.phys.EntityHitResult(player));
		helper.assertTrue(player.getCooldowns().isOnCooldown(player.getOffhandItem()), "the wind charge stops the shield");
		helper.succeed();
	}

	@GameTest
	public void theReusedDamageEffectsWorkOnABreeze(GameTestHelper helper) {
		ReusedEffects.damageTaken(helper, BreezeEffectsTest::breezeWith);
		ReusedEffects.damageToGolems(helper, BreezeEffectsTest::breezeWith);
		helper.succeed();
	}

	@GameTest
	public void swimHopJumpsInWaterAtFullSpeed(GameTestHelper helper) {
		double plain = jumpStepInWater(helper, breezeWith(helper, new BlockPos(1, 1, 1)), new BlockPos(1, 1, 1));
		double hop = jumpStepInWater(helper, breezeWith(helper, new BlockPos(5, 1, 5), "test_swim_hop"), new BlockPos(5, 1, 5));
		helper.assertTrue(plain < 0.45, "the water slows a plain jump, to " + plain);
		helper.assertTrue(Math.abs(hop - 0.5) < 1.0E-6, "the jump keeps its full speed, not " + hop);
		helper.succeed();
	}

	/**
	 * Puts the breeze in water in a long jump at 0.5 blocks a tick along the ground, moves it one tick, and returns its
	 * new speed along the ground.
	 */
	static double jumpStepInWater(GameTestHelper helper, Breeze breeze, BlockPos pos) {
		helper.setBlock(pos, net.minecraft.world.level.block.Blocks.WATER);
		helper.setBlock(pos.above(), net.minecraft.world.level.block.Blocks.WATER);
		breeze.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
		breeze.setDiscardFriction(true);
		breeze.setDeltaMovement(0.5, 0.0, 0.0);
		// The base tick finds the water around the breeze.
		breeze.baseTick();
		breeze.setDeltaMovement(0.5, 0.0, 0.0);
		helper.assertTrue(breeze.isInWater(), "the breeze is in water");
		breeze.travel(Vec3.ZERO);
		return breeze.getDeltaMovement().horizontalDistance();
	}

	@GameTest
	public void windMirrorSendsTheArrowStraightBackHarder(GameTestHelper helper) {
		var shooter = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, new BlockPos(1, 1, 3));
		// A new skeleton gets genes from the pool, and they can change its damage. The test shooter has none.
		shooter.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.List.of()));
		var plain = mirror(helper, breezeWith(helper, new BlockPos(5, 1, 1)), shooter);
		helper.assertTrue(plain.getDeltaMovement().length() < 1.0, "a plain breeze sends the arrow back slower");
		helper.assertValueEqual(GeneCombat.modifyDamage(shooter, helper.getLevel().damageSources().arrow(plain, shooter), 4.0F),
				4.0F, "a plain arrow back does its damage");
		var arrow = mirror(helper, breezeWith(helper, new BlockPos(5, 1, 5), "test_wind_mirror"), shooter);
		helper.assertTrue(Math.abs(arrow.getDeltaMovement().length() - 1.5) < 1.0E-6, "the arrow keeps its speed");
		Vec3 toShooter = shooter.getBoundingBox().getCenter().subtract(arrow.position()).normalize();
		helper.assertTrue(arrow.getDeltaMovement().normalize().dot(toShooter) > 0.9999, "the arrow flies at the shooter");
		helper.assertValueEqual(GeneCombat.modifyDamage(shooter, helper.getLevel().damageSources().arrow(arrow, shooter), 4.0F),
				8.0F, "two times the damage");
		helper.succeed();
	}

	/**
	 * Shoots an arrow of the shooter at the breeze, at 1.5 blocks a tick, and lets the breeze send it back.
	 */
	static net.minecraft.world.entity.projectile.arrow.Arrow mirror(GameTestHelper helper, Breeze breeze, LivingEntity shooter) {
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(helper.getLevel(), shooter,
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW), null);
		arrow.setPos(breeze.position().add(-1.0, 1.0, 0.0));
		arrow.setDeltaMovement(1.5, 0.0, 0.0);
		helper.getLevel().addFreshEntity(arrow);
		arrow.deflect(breeze.deflection(arrow), breeze, net.minecraft.world.entity.EntityReference.of(shooter), false, 1.0);
		return arrow;
	}

	/**
	 * Makes the breeze jump to a place 4 blocks away at the end of its inhale, and returns the speed of the jump. The
	 * random source has a fixed seed, so each breeze picks the same angle.
	 */
	static Vec3 jumpOf(GameTestHelper helper, Breeze breeze) {
		breeze.setNoGravity(false);
		breeze.setOnGround(true);
		breeze.getRandom().setSeed(42L);
		breeze.setPose(net.minecraft.world.entity.Pose.INHALING);
		breeze.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, helper.absolutePos(new BlockPos(5, 1, 1)));
		((LongJumpAccessor) new net.minecraft.world.entity.monster.breeze.LongJump())
				.originofspawns$tick(helper.getLevel(), breeze, helper.getLevel().getGameTime());
		Vec3 jump = breeze.getDeltaMovement();
		breeze.discard();
		return jump;
	}

	/**
	 * Makes the breeze land from a long jump, and returns the ticks of its rest before the next jump.
	 */
	static long restAfterLanding(GameTestHelper helper, Breeze breeze) {
		breeze.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
		breeze.setOnGround(true);
		((LongJumpAccessor) new net.minecraft.world.entity.monster.breeze.LongJump())
				.originofspawns$tick(helper.getLevel(), breeze, helper.getLevel().getGameTime());
		long rest = breeze.getBrain().getTimeUntilExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
		breeze.discard();
		return rest;
	}

	/**
	 * Makes a breeze that is ready to shoot at a pig: it has the pig as its target and the memory of a shot.
	 */
	static Breeze shooter(GameTestHelper helper, BlockPos pos, String... genes) {
		Breeze breeze = breezeWith(helper, pos, genes);
		Pig pig = CreeperEffectsTest.pigAt(helper, pos.east(2));
		breeze.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, pig);
		breeze.getBrain().setMemory(MemoryModuleType.BREEZE_SHOOT, net.minecraft.util.Unit.INSTANCE);
		return breeze;
	}

	/**
	 * Makes a wind charge of the breeze hit the entity directly.
	 */
	static void hit(GameTestHelper helper, Breeze breeze, LivingEntity target) {
		BreezeWindCharge charge = new BreezeWindCharge(breeze, helper.getLevel());
		charge.setPos(target.position().add(-1.0, 1.0, 0.0));
		helper.getLevel().addFreshEntity(charge);
		((ProjectileAccessor) charge).originofspawns$onHit(new net.minecraft.world.phys.EntityHitResult(target));
	}

	static IronGolem golemAt(GameTestHelper helper) {
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, BURST.east());
		golem.setNoGravity(true);
		return golem;
	}

	/**
	 * Makes a wind charge of the breeze burst at the test place, and returns the speed that the blast gave the
	 * entity. The entity is removed after.
	 */
	static double pushOf(GameTestHelper helper, LivingEntity entity, Breeze breeze) {
		entity.setDeltaMovement(Vec3.ZERO);
		burst(helper, breeze);
		double push = entity.getDeltaMovement().length();
		entity.discard();
		return push;
	}

	/**
	 * Makes a wind charge of the breeze burst on the top of the floor at the test place.
	 */
	static BreezeWindCharge burst(GameTestHelper helper, Breeze breeze) {
		BreezeWindCharge charge = new BreezeWindCharge(breeze, helper.getLevel());
		Vec3 place = helper.absoluteVec(Vec3.atBottomCenterOf(BURST));
		charge.setPos(place);
		helper.getLevel().addFreshEntity(charge);
		((ProjectileAccessor) charge).originofspawns$onHit(new BlockHitResult(place, Direction.UP,
				helper.absolutePos(BURST.below()), false));
		return charge;
	}

	static Breeze breezeWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Breeze breeze = EntityTypes.BREEZE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		breeze.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		breeze.setNoAi(true);
		breeze.setPersistenceRequired();
		breeze.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(breeze);
		return breeze;
	}
}
