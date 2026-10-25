package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
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
