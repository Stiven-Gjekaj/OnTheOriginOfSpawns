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
}
