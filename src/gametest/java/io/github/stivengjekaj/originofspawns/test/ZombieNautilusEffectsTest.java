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
}
