package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the hoglin: the throw, the run, the hide, the herd, and the fears. Each hoglin and each thrown
 * target stays inside the test area.
 */
public class HoglinEffectsTest {
	@GameTest
	public void theHitGenesRunOnTheHitOfTheTusks(GameTestHelper helper) {
		Hoglin hoglin = hoglinWith(helper, new BlockPos(1, 1, 4), "test_slime_hits");
		Zoglin zoglin = ZoglinEffectsTest.zoglinWith(helper, new BlockPos(6, 1, 4), "test_slime_hits");
		Pig first = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 4));
		Pig second = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		hoglin.doHurtTarget(helper.getLevel(), first);
		zoglin.doHurtTarget(helper.getLevel(), second);
		helper.assertTrue(first.hasEffect(MobEffects.SLOWNESS), "the hoglin hit gives Slowness");
		helper.assertTrue(second.hasEffect(MobEffects.SLOWNESS), "the zoglin hit gives Slowness");
		helper.succeed();
	}

	@GameTest
	public void theThrowGenesChangeTheThrowOfTheHit(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		Hoglin plain = hoglinWith(helper, new BlockPos(2, 1, 4));
		plain.doHurtTarget(helper.getLevel(), pig);
		helper.assertTrue(pig.getDeltaMovement().y > 0.0, "the vanilla hit throws the pig up");
		plain.discard();
		pig.discard();
		Pig still = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		Hoglin none = hoglinWith(helper, new BlockPos(2, 1, 4), "test_no_toss");
		none.doHurtTarget(helper.getLevel(), still);
		helper.assertValueEqual(still.getDeltaMovement(), Vec3.ZERO, "a throw of 0 throws the pig nowhere");
		// The vanilla throw is random, so the test gives the hit a known throw and checks the change.
		Hoglin sky = hoglinWith(helper, new BlockPos(6, 1, 6), "test_sky_toss");
		Pig thrown = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		thrown.setDeltaMovement(0.2, 0.3, 0.0);
		GeneCombat.afterTuskHit(sky, helper.getLevel(), thrown, Vec3.ZERO, null, true);
		helper.assertTrue(thrown.getDeltaMovement().distanceTo(new Vec3(0.3, 0.6, 0.0)) < 0.0001,
				"two times as high and 1.5 times as far, not " + thrown.getDeltaMovement());
		helper.succeed();
	}

	@GameTest
	public void shieldHookStopsTheShieldWithTheHitOfTheTusks(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		Hoglin hoglin = hoglinWith(helper, new BlockPos(4, 1, 1), "test_guard_crusher");
		hoglin.snapTo(player.position().add(-1.2, 0.0, 0.0), -90.0F, 0.0F);
		hoglin.doHurtTarget(helper.getLevel(), player);
		helper.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "the shield stops for a time");
		helper.succeed();
	}

	@GameTest
	public void overworldHideMakesTheHoglinTurnLater(GameTestHelper helper) {
		Hoglin plain = hoglinWith(helper, new BlockPos(1, 1, 1));
		Hoglin hide = hoglinWith(helper, new BlockPos(5, 1, 5), "test_overworld_hide");
		for (Hoglin hoglin : new Hoglin[] {plain, hide}) {
			hoglin.setNoAi(false);
			hoglin.setTimeInOverworld(300);
			((MobAccessor) hoglin).originofspawns$customServerAiStep(helper.getLevel());
		}
		helper.assertTrue(plain.isRemoved(), "a plain hoglin turns after 15 seconds");
		helper.assertFalse(hide.isRemoved(), "Overworld Hide does not turn after 15 seconds");
		hide.setTimeInOverworld(1200);
		((MobAccessor) hide).originofspawns$customServerAiStep(helper.getLevel());
		helper.assertTrue(hide.isRemoved(), "Overworld Hide turns after 60 seconds");
		helper.getLevel().getEntitiesOfClass(Zoglin.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(10.0))
				.forEach(Entity::discard);
		helper.succeed();
	}

	/**
	 * Adds an adult hoglin with the test genes and no AI. The genes go on before it enters the level, so it gets no
	 * genes from the pool.
	 */
	static Hoglin hoglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Hoglin hoglin = EntityTypes.HOGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		hoglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		hoglin.setNoAi(true);
		hoglin.setPersistenceRequired();
		hoglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(hoglin);
		return hoglin;
	}
}
