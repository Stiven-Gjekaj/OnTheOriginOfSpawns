package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.CreeperAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the creeper genes use: the blast, the fuse, the charge, and the states of a creeper.
 */
public class CreeperEffectsTest {
	/**
	 * The test environment for blasts: no block breaks, and players take the normal damage.
	 */
	static final String BLASTS = "originofspawns_test:blasts";

	@GameTest
	public void aChargedCreeperIsInTheChargedState(GameTestHelper helper) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1));
		helper.assertFalse(GeneCombat.test(creeper, When.CHARGED), "a plain creeper is not charged");
		charge(creeper);
		helper.assertTrue(GeneCombat.test(creeper, When.CHARGED), "a charged creeper");
		var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 1));
		helper.assertFalse(GeneCombat.test(zombie, When.CHARGED), "a zombie is never charged");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 16)
	public void aLargerBlastReachesFarther(GameTestHelper helper) {
		Creeper plain = creeperWith(helper, new BlockPos(1, 1, 1));
		Creeper big = creeperWith(helper, new BlockPos(1, 1, 12), "test_big_bang");
		// The blast of a plain creeper reaches 6 blocks. A pig at 8 blocks is out of its reach.
		Pig nearPlain = pigAt(helper, new BlockPos(9, 1, 1));
		Pig nearBig = pigAt(helper, new BlockPos(9, 1, 12));
		explode(plain);
		explode(big);
		helper.assertValueEqual(nearPlain.getHealth(), nearPlain.getMaxHealth(), "the plain blast misses the pig");
		helper.assertTrue(!nearBig.isAlive() || nearBig.getHealth() < nearBig.getMaxHealth(),
				"the larger blast reaches the pig");
		helper.succeed();
	}

	@GameTest
	public void aBlastSizeCanNeedTheCharge(GameTestHelper helper) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1), "test_overcharge");
		helper.assertValueEqual(GeneBlasts.sizeMultiplier(creeper), 1.0F, "no change without the charge");
		charge(creeper);
		helper.assertValueEqual(GeneBlasts.sizeMultiplier(creeper), 1.25F, "a larger blast with the charge");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8, maxTicks = 100)
	public void aLongerFuseExplodesLater(GameTestHelper helper) {
		Creeper slow = creeperWith(helper, new BlockPos(1, 1, 1), "test_long_fuse");
		helper.assertValueEqual(GeneBlasts.fuseTicks(slow, 30), 60, "two times the vanilla fuse");
		slow.ignite();
		// The vanilla fuse ends after 30 ticks.
		helper.runAfterDelay(45, () -> {
			helper.assertFalse(slow.isRemoved(), "the slow creeper still burns its fuse");
			helper.assertTrue(slow.getSwelling(1.0F) > 0.5F, "the fuse burns");
		});
		helper.runAfterDelay(70, () -> {
			helper.assertTrue(slow.isRemoved(), "the slow creeper exploded after 60 ticks");
			helper.succeed();
		});
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void damageEffectsChangeTheBlastDamage(GameTestHelper helper) {
		// Each blast has the same place, so only the genes change the damage.
		float plain = blastDamageToGolem(helper);
		float soft = blastDamageToGolem(helper, "test_soft_blast");
		helper.assertTrue(plain > 0.0F, "the plain blast hurts the golem");
		helper.assertTrue(Math.abs(soft - plain * 0.5F) < 0.01F, "half the damage, was " + soft + " of " + plain);
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void moreBlastDamageToATag(GameTestHelper helper) {
		float plain = blastDamageToGolem(helper);
		float breaker = blastDamageToGolem(helper, "test_golem_breaker");
		helper.assertTrue(Math.abs(breaker - plain * 2.0F) < 0.01F, "two times the damage, was " + breaker + " of " + plain);
		helper.succeed();
	}

	/**
	 * Explodes a creeper with the genes 4 blocks from a new iron golem, and returns the health that the golem lost.
	 * The golem goes away after the blast.
	 */
	static float blastDamageToGolem(GameTestHelper helper, String... genes) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 2), genes);
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 2));
		float before = golem.getHealth();
		explode(creeper);
		float lost = before - golem.getHealth();
		golem.discard();
		return lost;
	}

	static void explode(Creeper creeper) {
		((CreeperAccessor) creeper).originofspawns$explode();
	}

	static Pig pigAt(GameTestHelper helper, BlockPos pos) {
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
		pig.setNoGravity(true);
		return pig;
	}

	static void charge(Creeper creeper) {
		creeper.getEntityData().set(CreeperAccessor.originofspawns$poweredData(), true);
	}

	static Creeper creeperWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Creeper creeper = EntityTypes.CREEPER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		creeper.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		creeper.setNoAi(true);
		creeper.setPersistenceRequired();
		creeper.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(creeper);
		return creeper;
	}
}
