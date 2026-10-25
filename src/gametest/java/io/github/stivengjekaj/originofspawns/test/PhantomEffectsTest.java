package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.PhantomAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the phantom: the dive, the flight, the sun, the cry, and the flock. Each phantom stays inside
 * the test area, under the roof.
 */
public class PhantomEffectsTest {
	@GameTest
	public void swiftWingMakesTheFlightFaster(GameTestHelper helper) {
		Phantom plain = phantomWith(helper, new BlockPos(1, 3, 1));
		Phantom swift = phantomWith(helper, new BlockPos(1, 3, 6), "test_swift_wing");
		double[] speeds = new double[2];
		Phantom[] phantoms = {plain, swift};
		for (int i = 0; i < 2; i++) {
			Phantom phantom = phantoms[i];
			phantom.setYRot(-90.0F);
			((PhantomAccessor) phantom).originofspawns$setMoveTargetPoint(phantom.position().add(5.0, 0.0, 0.0));
			phantom.setDeltaMovement(Vec3.ZERO);
			phantom.getMoveControl().tick();
			speeds[i] = phantom.getDeltaMovement().length();
		}
		helper.assertTrue(speeds[0] > 0.0, "the phantom flies");
		helper.assertTrue(Math.abs(speeds[1] / speeds[0] - 1.3) < 0.001, "30% more flying speed, not " + speeds[1] / speeds[0]);
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aMobUnderARoofDoesNotCountAsBurningInTheSun(GameTestHelper helper) {
		// A stone block over the phantom stops the sun, at each time of day. The barrier roof lets the sky light in.
		helper.setBlock(new BlockPos(4, 4, 4), Blocks.STONE);
		// The light under the stone changes some ticks later.
		helper.runAfterDelay(10, () -> {
			Phantom phantom = phantomWith(helper, new BlockPos(4, 2, 4));
			for (int i = 0; i < 40; i++) {
				phantom.aiStep();
			}
			helper.assertValueEqual(phantom.getRemainingFireTicks(), 0, "the sun does not burn the phantom");
			helper.assertFalse(Evolution.isSunBurning(phantom, helper.getLevel().getGameTime()),
					"a phantom that the sun does not burn is not burning in the sun");
			helper.succeed();
		});
	}

	@GameTest
	public void sunShadeLetsTheSunBurnOnlyOneTimeInEachInterval(GameTestHelper helper) {
		Phantom plain = phantomWith(helper, new BlockPos(1, 3, 1));
		Phantom shade = phantomWith(helper, new BlockPos(6, 3, 6), "test_sun_shade");
		var fire = helper.getLevel().damageSources().onFire();
		helper.assertFalse(GeneCombat.sunShaded(shade, fire), "no shade while the sun does not burn it");
		Evolution.markSunBurn(plain);
		Evolution.markSunBurn(shade);
		helper.assertFalse(GeneCombat.sunShaded(plain, fire), "the sun burns a plain phantom each time");
		helper.assertFalse(GeneCombat.sunShaded(plain, fire), "and again");
		helper.assertFalse(GeneCombat.sunShaded(shade, fire), "the first burn comes");
		helper.assertTrue(GeneCombat.sunShaded(shade, fire), "the next burn waits");
		helper.assertFalse(GeneCombat.sunShaded(shade, helper.getLevel().damageSources().generic()), "other damage comes");
		shade.setAttached(ModAttachments.SUN_SHADE_TIME, helper.getLevel().getGameTime() - 80L);
		helper.assertFalse(GeneCombat.sunShaded(shade, fire), "the burn comes again after 4 seconds");
		helper.succeed();
	}

	@GameTest
	public void silentWingMakesThePhantomSilent(GameTestHelper helper) {
		Phantom plain = phantomWith(helper, new BlockPos(1, 3, 1));
		Phantom silent = phantomWith(helper, new BlockPos(6, 3, 6), "test_silent_wing");
		GeneBehaviors.installGoals(plain);
		GeneBehaviors.installGoals(silent);
		helper.assertFalse(plain.isSilent(), "a plain phantom makes sounds");
		helper.assertTrue(silent.isSilent(), "Silent Wing makes no sound");
		helper.succeed();
	}

	@GameTest
	public void feintStepsAsideInTheAir(GameTestHelper helper) {
		Phantom phantom = phantomWith(helper, new BlockPos(4, 3, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		// The eyes of the player are at the height of the phantom, so the bow aims at it.
		player.setPos(phantom.position().add(0.0, phantom.getEyeHeight() - player.getEyeHeight(), 5.0));
		CreeperEffectsTest.face(player, 180.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		player.startUsingItem(InteractionHand.MAIN_HAND);
		Vec3 before = phantom.position();
		helper.assertTrue(GeneBehaviors.sidestep(phantom, helper.getLevel(), new GeneEffect.Sidestep(12.0F, 3.0F, 8.0F, true)),
				"the phantom steps aside");
		helper.assertValueEqual(Math.round(Math.abs(phantom.getX() - before.x)), 3L, "the side distance");
		helper.assertValueEqual(phantom.getY(), before.y, "it stays at the same height in the air");
		helper.succeed();
	}

	@GameTest
	public void swarmDiveMakesThePhantomsNearTargetTheSameMob(GameTestHelper helper) {
		Phantom caller = phantomWith(helper, new BlockPos(1, 3, 1), "test_swarm_dive");
		Phantom other = phantomWith(helper, new BlockPos(6, 3, 6));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		caller.setTarget(player);
		GeneBehaviors.swoopCall(caller);
		helper.assertTrue(other.getTarget() == player, "the other phantom targets the player");
		helper.succeed();
	}

	@GameTest
	public void theReusedBiteEffectsWorkOnAPhantom(GameTestHelper helper) {
		ReusedEffects.meleeHitGenes(helper, PhantomEffectsTest::phantomWith);
		ReusedEffects.knockback(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedShieldBreakWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.meleeShieldBreak(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDamageEffectsWorkOnAPhantom(GameTestHelper helper) {
		ReusedEffects.damageToGolems(helper, PhantomEffectsTest::phantomWith);
		ReusedEffects.damageTaken(helper, PhantomEffectsTest::phantomWith);
		ReusedEffects.smiteResist(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void theReusedFirstStrikeWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.firstStrike(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedSightEffectsWorkOnAPhantom(GameTestHelper helper) {
		ReusedEffects.translucent(helper, PhantomEffectsTest::phantomWith);
		ReusedEffects.seeInvisible(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedAuraWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.playerAura(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedCallOnHurtWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.callOnHurt(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedCallOnSightWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.callOnSight(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedGroupDamageWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.groupedAndAlone(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDeathEffectWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.deathEffectOnAllies(helper, PhantomEffectsTest::phantomWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedRallyWorksOnAPhantom(GameTestHelper helper) {
		ReusedEffects.rally(helper, PhantomEffectsTest::phantomWith, "test_rally_cry");
		helper.succeed();
	}

	@GameTest
	public void bigFlockSpawnsAPhantomWithGenesFromThePool(GameTestHelper helper) {
		Phantom phantom = phantomWith(helper, new BlockPos(4, 3, 4), "test_big_litter");
		GeneBehaviors.onSpawn(phantom, helper.getLevel(), EntitySpawnReason.NATURAL);
		var flock = helper.getLevel().getEntitiesOfClass(Phantom.class, phantom.getBoundingBox().inflate(3.0),
				other -> other != phantom);
		helper.assertValueEqual(flock.size(), 1, "one more phantom");
		helper.assertFalse(phantom.getAttached(ModAttachments.GENES).equals(flock.get(0).getAttached(ModAttachments.GENES)),
				"the extra phantom has genes from the pool, not the genes of the first");
		helper.succeed();
	}

	/**
	 * Adds a phantom with the test genes and no AI. The genes go on before it enters the level, so it gets no genes
	 * from the pool.
	 */
	static Phantom phantomWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Phantom phantom = EntityTypes.PHANTOM.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		phantom.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		phantom.setNoAi(true);
		phantom.setPersistenceRequired();
		phantom.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(phantom);
		return phantom;
	}
}
