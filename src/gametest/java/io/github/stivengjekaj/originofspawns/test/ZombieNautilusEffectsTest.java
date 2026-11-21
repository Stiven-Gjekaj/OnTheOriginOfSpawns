package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.ChargeAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;

/**
 * Checks the genes of a zombie nautilus: its charge, its ink, its shell, and the reused effects.
 */
public class ZombieNautilusEffectsTest {
	/**
	 * Makes a zombie nautilus with the test genes and a drowned on it that targets the target.
	 */
	static ZombieNautilus nautilusWithRider(GameTestHelper helper, BlockPos pos, LivingEntity target, String... genes) {
		ZombieNautilus nautilus = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, pos, genes);
		Mob drowned = MountEffectsTest.mob(helper, EntityTypes.DROWNED, pos);
		drowned.startRiding(nautilus, true, false);
		drowned.setTarget(target);
		return nautilus;
	}

	/**
	 * Makes the vanilla charge attack of a zombie nautilus.
	 */
	static ChargeAttack vanillaCharge() {
		return new ChargeAttack(80, TargetingConditions.forCombat(), 0.5F, 2.0F, 12.0, 11.0, SoundEvents.ZOMBIE_NAUTILUS_DASH);
	}

	@GameTest
	public void aRammerChargesAtTheTargetOfItsRider(GameTestHelper helper) {
		var level = helper.getLevel();
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 7));
		ZombieNautilus nautilus = nautilusWithRider(helper, new BlockPos(2, 1, 1), pig, "test_rider_charge");
		var charge = new GeneEffect.RiderTargetCharge(11.0F);
		helper.assertTrue(GeneBehaviors.riderTargetCharge(nautilus, charge), "the zombie nautilus takes the target of its rider");
		helper.assertTrue(nautilus.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == pig,
				"the attack target is the pig");
		helper.assertFalse(GeneBehaviors.riderTargetCharge(nautilus, charge), "no new target while it has one");
		ChargeAttack attack = vanillaCharge();
		helper.assertTrue(attack.tryStart(level, nautilus, level.getGameTime()), "the vanilla charge starts on land");
		attack.tickOrStop(level, nautilus, level.getGameTime());
		helper.assertTrue(nautilus.getDeltaMovement().z > 0.3, "the charge goes south, at the pig");
		ZombieNautilus lone = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(6, 1, 1), "test_rider_charge");
		helper.assertFalse(GeneBehaviors.riderTargetCharge(lone, charge), "a zombie nautilus with no rider takes no target");
		helper.succeed();
	}

	/**
	 * Runs the vanilla charge of the zombie nautilus at the target for some ticks. The target stands where the
	 * nautilus is, so the first tick hits it.
	 */
	static void chargeAt(GameTestHelper helper, ZombieNautilus nautilus, LivingEntity target) {
		var level = helper.getLevel();
		nautilus.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
		ChargeAttack attack = vanillaCharge();
		helper.assertTrue(attack.tryStart(level, nautilus, level.getGameTime()), "the charge starts");
		attack.tickOrStop(level, nautilus, level.getGameTime());
	}

	@GameTest
	public void hullRamBreaksTheBoatAndShieldRamStopsTheShieldOfTheTarget(GameTestHelper helper) {
		var level = helper.getLevel();
		var boat = EntityTypes.OAK_BOAT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		boat.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(2, 1, 4))), 0.0F, 0.0F);
		level.addFreshEntity(boat);
		var sailor = CreeperEffectsTest.survivalPlayer(helper).player();
		sailor.setPos(boat.position());
		sailor.startRiding(boat);
		ZombieNautilus rammer = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(2, 1, 4), "test_hull_ram");
		rammer.snapTo(sailor.position(), 0.0F, 0.0F);
		chargeAt(helper, rammer, sailor);
		helper.assertTrue(sailor.getHealth() < sailor.getMaxHealth(), "the charge hits the player in the boat");
		helper.assertFalse(boat.isAlive(), "the charge breaks the boat");
		var guard = CreeperEffectsTest.shieldedPlayer(helper);
		ZombieNautilus other = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(5, 1, 1), "test_hull_ram");
		other.snapTo(guard.position(), 0.0F, 0.0F);
		chargeAt(helper, other, guard);
		helper.assertTrue(guard.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.SHIELD)), "the charge stops the shield");
		helper.succeed();
	}

	@GameTest
	public void quickRamChargesMoreOftenAndTideChargePushesTwoTimesAsFar(GameTestHelper helper) {
		var plainPig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 2));
		var quickPig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 2));
		ZombieNautilus plain = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(1, 1, 1));
		ZombieNautilus quick = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(6, 1, 1), "test_quick_ram");
		plain.snapTo(plainPig.position().add(0.0, 0.0, -0.5), 0.0F, 0.0F);
		quick.snapTo(quickPig.position().add(0.0, 0.0, -0.5), 0.0F, 0.0F);
		chargeAt(helper, plain, plainPig);
		chargeAt(helper, quick, quickPig);
		helper.assertValueEqual(plain.getBrain().getMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS).orElse(0), 80,
				"a plain charge waits 80 ticks");
		helper.assertValueEqual(quick.getBrain().getMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS).orElse(0), 53,
				"a quick charge waits 80 / 1.5 ticks");
		double plainPush = Math.hypot(plainPig.getDeltaMovement().x, plainPig.getDeltaMovement().z);
		double quickPush = Math.hypot(quickPig.getDeltaMovement().x, quickPig.getDeltaMovement().z);
		helper.assertTrue(plainPush > 0.1, "the plain charge pushes the pig");
		helper.assertTrue(quickPush > plainPush * 1.5, "the tide charge pushes the pig much farther");
		helper.succeed();
	}

	@GameTest
	public void breathRamTakesThreeSecondsOfAirFromATargetInWater(GameTestHelper helper) {
		helper.setBlock(new BlockPos(4, 1, 4), net.minecraft.world.level.block.Blocks.WATER);
		var swimmer = CreeperEffectsTest.survivalPlayer(helper).player();
		swimmer.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))));
		swimmer.baseTick();
		helper.assertTrue(swimmer.isInWater(), "the player is in water");
		var walker = CreeperEffectsTest.survivalPlayer(helper).player();
		walker.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))));
		ZombieNautilus rammer = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(6, 1, 6), "test_breath_ram");
		int full = swimmer.getAirSupply();
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(rammer, swimmer);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(rammer, walker);
		helper.assertValueEqual(swimmer.getAirSupply(), full - 60, "the hit takes 3 seconds of air in water");
		helper.assertValueEqual(walker.getAirSupply(), full, "a player on land loses no air");
		helper.succeed();
	}

	@GameTest
	public void wakeJetGivesSpeedAfterAChargeAndInkRushAfterInk(GameTestHelper helper) {
		var level = helper.getLevel();
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 2));
		ZombieNautilus jetter = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(2, 1, 1), "test_wake_jet");
		jetter.snapTo(pig.position().add(0.0, 0.0, -0.5), 0.0F, 0.0F);
		helper.assertFalse(jetter.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "no Speed before the charge");
		chargeAt(helper, jetter, pig);
		helper.assertTrue(jetter.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "Speed after the charge");
		helper.assertFalse(jetter.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "no ink effect before ink");
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(jetter.position().add(1.0, 0.0, 0.0));
		jetter.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the ink blinds the player");
		helper.assertTrue(jetter.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "the effect after ink");
		helper.succeed();
	}

	/**
	 * Hurts the nautilus as if its last ink was 6 seconds ago, and returns true if the far player got Blindness.
	 */
	static boolean inkAfterSixSeconds(GameTestHelper helper, ZombieNautilus nautilus, net.minecraft.world.entity.player.Player near,
			net.minecraft.world.entity.player.Player far) {
		var level = helper.getLevel();
		var ink = io.github.stivengjekaj.originofspawns.evolution.GeneEffects.of(nautilus)
				.filter(GeneEffect.EffectOnHurt.class::isInstance).findFirst().orElseThrow();
		nautilus.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.HURT_EFFECT_TIMES,
				java.util.Map.of((GeneEffect.EffectOnHurt) ink, level.getGameTime() - 120L));
		far.removeAllEffects();
		float before = nautilus.getHealth();
		// The vanilla hurt cooldown lets a second hit in it hurt only with more damage than the first hit.
		nautilus.hurtServer(level, level.damageSources().playerAttack(near), 3.0F);
		helper.assertTrue(nautilus.getHealth() < before, "the second hit hurts the nautilus");
		return far.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
	}

	@GameTest
	public void theInkGenesAddAnEffectReachFartherAndComeMoreOften(GameTestHelper helper) {
		var level = helper.getLevel();
		var near = CreeperEffectsTest.survivalPlayer(helper).player();
		var far = CreeperEffectsTest.survivalPlayer(helper).player();
		near.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(2, 1, 2))));
		far.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 7))));
		ZombieNautilus plain = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(1, 1, 1), "test_plain_ink");
		plain.hurtServer(level, level.damageSources().playerAttack(near), 1.0F);
		helper.assertTrue(near.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the plain ink blinds the near player");
		helper.assertFalse(far.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the plain ink does not reach 6 blocks");
		helper.assertFalse(near.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "the plain ink gives no Slowness");
		helper.assertFalse(inkAfterSixSeconds(helper, plain, near, far), "the plain ink waits 10 seconds");
		plain.discard();
		near.removeAllEffects();
		ZombieNautilus inky = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(1, 1, 1), "test_ink_mods");
		inky.hurtServer(level, level.damageSources().playerAttack(near), 1.0F);
		helper.assertTrue(far.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the wide ink reaches 6 blocks");
		helper.assertTrue(near.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "the thick ink gives Slowness");
		helper.assertTrue(far.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "and to each player that it reaches");
		helper.assertTrue(inkAfterSixSeconds(helper, inky, near, far), "the quick ink waits only 5 seconds");
		helper.succeed();
	}

	@GameTest
	public void inkScreenMakesTheNautilusTranslucentForFourSecondsAfterAProjectile(GameTestHelper helper) {
		var level = helper.getLevel();
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 7));
		ZombieNautilus hit = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(1, 1, 1), "test_ink_screen");
		ZombieNautilus shot = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(6, 1, 1), "test_ink_screen");
		hit.hurtServer(level, level.damageSources().mobAttack(pig), 1.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneEffects.translucentOpacity(hit).isEmpty(),
				"a melee hit does not make it translucent");
		var snowball = new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(level, pig,
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SNOWBALL));
		shot.hurtServer(level, level.damageSources().thrown(snowball, pig), 1.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneEffects.translucentOpacity(shot).isPresent(),
				"a projectile makes it translucent");
		helper.assertValueEqual(shot.getAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.TRANSLUCENT_UNTIL),
				level.getGameTime() + 80L, "for 4 seconds");
		shot.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.TRANSLUCENT_UNTIL, level.getGameTime());
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneEffects.translucentOpacity(shot).isEmpty(),
				"it is not translucent after the 4 seconds");
		helper.succeed();
	}

	static final ReusedEffects.Maker NAUTILUS = (helper, pos, genes) -> MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, pos, genes);

	@GameTest
	public void theReusedEffectsWorkOnAZombieNautilus(GameTestHelper helper) {
		ReusedEffects.armorAttribute(helper, NAUTILUS);
		ReusedEffects.thorns(helper, NAUTILUS);
		ReusedEffects.healOnce(helper, NAUTILUS);
		ReusedEffects.sidestep(helper, NAUTILUS);
		ReusedEffects.damageTaken(helper, NAUTILUS);
		ReusedEffects.sunShade(helper, NAUTILUS);
		helper.succeed();
	}

	@GameTest
	public void aZombieNautilusOnLandHopsToTheWater(GameTestHelper helper) {
		ReusedEffects.shoreHop(helper, NAUTILUS);
		helper.succeed();
	}

	// The heal comes one time each second, so the test waits longer than one second.
	@GameTest(maxTicks = 60)
	public void aZombieNautilusRegeneratesOutOfCombat(GameTestHelper helper) {
		ReusedEffects.regenerate(helper, NAUTILUS);
	}

	@GameTest
	public void theAttackDamageAttributeChangesTheDamageOfTheCharge(GameTestHelper helper) {
		var plainPig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 2));
		var rammerPig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 2));
		ZombieNautilus plain = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(1, 1, 1));
		ZombieNautilus rammer = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_NAUTILUS, new BlockPos(6, 1, 1), "test_rammer");
		io.github.stivengjekaj.originofspawns.evolution.GeneEffects.applyAttributes(rammer,
				io.github.stivengjekaj.originofspawns.evolution.GeneEffects.sources(helper.getLevel().registryAccess(),
						rammer.getAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.GENES)));
		plain.snapTo(plainPig.position().add(0.0, 0.0, -0.5), 0.0F, 0.0F);
		rammer.snapTo(rammerPig.position().add(0.0, 0.0, -0.5), 0.0F, 0.0F);
		chargeAt(helper, plain, plainPig);
		chargeAt(helper, rammer, rammerPig);
		helper.assertValueEqual(plainPig.getMaxHealth() - plainPig.getHealth(), 3.0F, "a plain charge does 3 damage");
		helper.assertTrue(Math.abs(rammerPig.getMaxHealth() - rammerPig.getHealth() - 3.9F) < 0.001F,
				"30% more attack damage makes the charge do 3.9 damage");
		helper.succeed();
	}
}
