package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.pig.Pig;

/**
 * Checks the genes of a camel husk: its dash at the target of its rider, the effects of a dash, its spit, and the
 * reused effects.
 */
public class CamelHuskEffectsTest {
	/**
	 * Makes a camel husk on the ground with the test genes and a husk on it that targets the pig.
	 */
	static CamelHusk camelWithRider(GameTestHelper helper, BlockPos pos, Pig target, String... genes) {
		CamelHusk camel = MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, pos, genes);
		camel.setOnGround(true);
		Mob husk = MountEffectsTest.mob(helper, EntityTypes.HUSK, pos);
		husk.startRiding(camel, true, false);
		husk.setTarget(target);
		return camel;
	}

	static double along(CamelHusk camel) {
		return Math.hypot(camel.getDeltaMovement().x, camel.getDeltaMovement().z);
	}

	@GameTest
	public void aDasherDashesAtTheTargetOfItsRiderOneTimeInTheInterval(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 7));
		CamelHusk camel = camelWithRider(helper, new BlockPos(2, 1, 1), pig, "test_dash");
		var dash = new GeneEffect.MountDash(8.0F, 16.0F);
		helper.assertTrue(GeneBehaviors.mountDash(camel, helper.getLevel(), dash), "the camel husk dashes");
		helper.assertTrue(camel.getDeltaMovement().z > 1.0, "the dash goes south, at the pig");
		helper.assertTrue(Math.abs(camel.getDeltaMovement().x) < 0.1, "the dash goes straight at the pig");
		camel.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
		helper.assertFalse(GeneBehaviors.mountDash(camel, helper.getLevel(), dash), "the next dash waits");
		CamelHusk lone = MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, new BlockPos(5, 1, 1), "test_dash");
		lone.setOnGround(true);
		helper.assertFalse(GeneBehaviors.mountDash(lone, helper.getLevel(), dash), "a camel husk with no rider does not dash");
		helper.succeed();
	}

	@GameTest
	public void longDashGoesTwoTimesAsFar(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 7));
		CamelHusk plain = camelWithRider(helper, new BlockPos(1, 1, 1), pig, "test_dash");
		CamelHusk longer = camelWithRider(helper, new BlockPos(6, 1, 1), pig, "test_long_dash");
		var dash = new GeneEffect.MountDash(8.0F, 16.0F);
		GeneBehaviors.mountDash(plain, helper.getLevel(), dash);
		GeneBehaviors.mountDash(longer, helper.getLevel(), dash);
		helper.assertTrue(Math.abs(along(longer) - 2.0 * along(plain)) < 0.01, "two times the push along the ground");
		helper.succeed();
	}

	@GameTest
	public void quickDashDashesFiftyPercentMoreOften(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 7));
		CamelHusk plain = camelWithRider(helper, new BlockPos(1, 1, 1), pig, "test_dash");
		CamelHusk quick = camelWithRider(helper, new BlockPos(6, 1, 1), pig, "test_quick_dash");
		// The last dash was 6 seconds ago: more than 8 / 1.5 seconds, but less than 8 seconds.
		long sixSecondsAgo = helper.getLevel().getGameTime() - 120L;
		for (CamelHusk camel : java.util.List.of(plain, quick)) {
			camel.setAttached(ModAttachments.EFFECT_TIMES, java.util.Map.of(new GeneEffect.MountDash(0.0F, 0.0F), sixSecondsAgo));
		}
		var dash = new GeneEffect.MountDash(8.0F, 16.0F);
		helper.assertFalse(GeneBehaviors.mountDash(plain, helper.getLevel(), dash), "the plain dasher waits 8 seconds");
		helper.assertTrue(GeneBehaviors.mountDash(quick, helper.getLevel(), dash), "the quick dasher waits 5.3 seconds");
		helper.succeed();
	}

	/**
	 * Returns the blocks that the camel rises with its upward speed now, with the vanilla gravity.
	 */
	static double rise(CamelHusk camel) {
		double speed = camel.getDeltaMovement().y;
		double y = 0.0;
		double top = 0.0;
		for (int tick = 0; tick < 200 && speed > 0.0; tick++) {
			y += speed;
			top = Math.max(top, y);
			speed = (speed - 0.08) * 0.98;
		}
		return top;
	}

	@GameTest
	public void sandLeapMakesEachDashTwoBlocksHigherAndSafeFromAFall(GameTestHelper helper) {
		var level = helper.getLevel();
		CamelHusk plain = MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, new BlockPos(1, 1, 1));
		CamelHusk leaper = MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, new BlockPos(6, 1, 1), "test_sand_leap");
		for (CamelHusk camel : java.util.List.of(plain, leaper)) {
			((io.github.stivengjekaj.originofspawns.mixin.CamelAccessor) camel).originofspawns$dash(1.0F,
					new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0));
		}
		helper.assertTrue(Math.abs(rise(leaper) - rise(plain) - 2.0) < 0.05, "the dash of a player goes 2 blocks higher");
		var fall = level.damageSources().fall();
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(leaper, fall, 6.0F), 6.0F,
				"a fall with no dash hurts");
		leaper.handleStartJump(90);
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(leaper, fall, 6.0F), 0.0F,
				"no fall damage after the dash of a player");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(plain, fall, 6.0F), 6.0F,
				"a plain camel husk takes fall damage");
		helper.succeed();
	}

	@GameTest
	public void ramDashHitsWhatTheDashTouchesOneTimeAndNotItsRider(GameTestHelper helper) {
		var level = helper.getLevel();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 3));
		CamelHusk camel = camelWithRider(helper, new BlockPos(3, 1, 3), pig, "test_ram_dash");
		Mob husk = (Mob) camel.getFirstPassenger();
		var hit = new GeneEffect.DashHit(4.0F);
		helper.assertValueEqual(GeneBehaviors.dashHit(camel, level, hit), 0, "no hit with no dash");
		camel.handleStartJump(90);
		helper.assertValueEqual(GeneBehaviors.dashHit(camel, level, hit), 1, "the dash hits the pig that it touches");
		helper.assertValueEqual(pig.getMaxHealth() - pig.getHealth(), 4.0F, "the dash does 4 damage");
		helper.assertValueEqual(husk.getHealth(), husk.getMaxHealth(), "the rider is safe");
		helper.assertValueEqual(GeneBehaviors.dashHit(camel, level, hit), 0, "one hit in each dash");
		helper.succeed();
	}

	@GameTest
	public void dustDashBlindsTheMobsNearTheStartButNotItsRider(GameTestHelper helper) {
		Pig near = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 5));
		Pig far = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 9));
		CamelHusk camel = camelWithRider(helper, new BlockPos(3, 1, 3), near, "test_dust_dash");
		Mob husk = (Mob) camel.getFirstPassenger();
		camel.handleStartJump(90);
		helper.assertTrue(near.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the near pig is blind");
		helper.assertFalse(far.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the far pig is not blind");
		helper.assertFalse(husk.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the rider is not blind");
		helper.succeed();
	}

	@GameTest
	public void spearChargeMakesTheNextHitOfTheRiderAfterADashStronger(GameTestHelper helper) {
		var level = helper.getLevel();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 7));
		CamelHusk camel = camelWithRider(helper, new BlockPos(3, 1, 3), pig, "test_spear_charge");
		Mob husk = (Mob) camel.getFirstPassenger();
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(pig,
				level.damageSources().mobAttack(husk), 4.0F), 4.0F, "no more damage before a dash");
		camel.handleStartJump(90);
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(pig,
				level.damageSources().mobAttack(husk), 4.0F), 6.0F, "the first hit after the dash does 50% more damage");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(pig,
				level.damageSources().mobAttack(husk), 4.0F), 4.0F, "the second hit is plain");
		helper.succeed();
	}

	@GameTest
	public void sandSpitSpitsAtTheTargetOfTheRiderForTwoDamageAndSlowness(GameTestHelper helper) {
		var level = helper.getLevel();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 7));
		CamelHusk camel = camelWithRider(helper, new BlockPos(3, 1, 2), pig, "test_sand_spit");
		var spit = new GeneEffect.RiderTargetSpit(6.0F, 2.0F, 16.0F,
				java.util.Optional.of(net.minecraft.world.effect.MobEffects.SLOWNESS), 2.0F, 0);
		helper.assertTrue(GeneBehaviors.riderTargetSpit(camel, level, spit), "the camel husk spits");
		var spits = level.getEntitiesOfClass(net.minecraft.world.entity.projectile.LlamaSpit.class, camel.getBoundingBox().inflate(4.0));
		helper.assertValueEqual(spits.size(), 1, "one spit flies");
		helper.assertTrue(spits.get(0).getDeltaMovement().z > 0.5, "the spit flies at the pig");
		helper.assertFalse(GeneBehaviors.riderTargetSpit(camel, level, spit), "the next spit waits 6 seconds");
		pig.hurtServer(level, level.damageSources().spit(spits.get(0), camel), 1.0F);
		helper.assertValueEqual(pig.getMaxHealth() - pig.getHealth(), 2.0F, "the spit does 2 damage");
		helper.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "the spit gives Slowness");
		spits.forEach(net.minecraft.world.entity.Entity::discard);
		CamelHusk lone = MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, new BlockPos(6, 1, 2), "test_sand_spit");
		helper.assertFalse(GeneBehaviors.riderTargetSpit(lone, level, spit), "a camel husk with no rider does not spit");
		helper.succeed();
	}

	static final ReusedEffects.Maker CAMEL = (helper, pos, genes) -> MountEffectsTest.mob(helper, EntityTypes.CAMEL_HUSK, pos, genes);

	@GameTest
	public void theReusedEffectsWorkOnACamelHusk(GameTestHelper helper) {
		ReusedEffects.healOnce(helper, CAMEL);
		ReusedEffects.rootFeet(helper, CAMEL);
		ReusedEffects.translucent(helper, CAMEL);
		ReusedEffects.sidestep(helper, CAMEL);
		ReusedEffects.damageTaken(helper, CAMEL);
		helper.succeed();
	}

	@GameTest
	public void aCamelHuskRegeneratesOutOfCombat(GameTestHelper helper) {
		ReusedEffects.regenerate(helper, CAMEL);
	}

	@GameTest
	public void aSpeedWhenGeneMakesACamelHuskFaster(GameTestHelper helper) {
		ReusedEffects.speedWhenWounded(helper, CAMEL);
	}
}
