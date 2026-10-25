package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GenePiglins;
import io.github.stivengjekaj.originofspawns.mixin.HoglinAiAccessor;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.SensorAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.HoglinSpecificSensor;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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

	@GameTest
	public void theFearGenesStopTheFearsOfAHoglinOneByOne(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 4), Blocks.WARPED_FUNGUS);
		Hoglin plain = hoglinWith(helper, new BlockPos(3, 1, 1));
		Hoglin eater = hoglinWith(helper, new BlockPos(3, 1, 7), "test_warp_eater");
		helper.assertTrue(GenePiglins.hoglinRepellent(helper.getLevel(), plain).isPresent(), "a plain hoglin fears the fungus");
		helper.assertTrue(GenePiglins.hoglinRepellent(helper.getLevel(), eater).isEmpty(), "Warp Eater does not fear it");
		helper.setBlock(new BlockPos(5, 1, 4), Blocks.RESPAWN_ANCHOR);
		helper.assertTrue(GenePiglins.hoglinRepellent(helper.getLevel(), eater)
				.filter(pos -> helper.getLevel().getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)).isPresent(),
				"Warp Eater still fears the respawn anchor");
		// The sensor of the hoglin uses the same search.
		((SensorAccessor) new HoglinSpecificSensor()).originofspawns$doTick(helper.getLevel(), eater);
		helper.assertTrue(eater.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT)
				.filter(pos -> helper.getLevel().getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)).isPresent(),
				"the sensor finds the respawn anchor, not the fungus");
		helper.succeed();
	}

	@GameTest
	public void fungusGuardGuardsTheHoglinsNearItFromTheFungus(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 4), Blocks.WARPED_FUNGUS);
		Hoglin herd = hoglinWith(helper, new BlockPos(3, 1, 1));
		helper.assertTrue(GenePiglins.hoglinRepellent(helper.getLevel(), herd).isPresent(), "alone, the hoglin fears the fungus");
		hoglinWith(helper, new BlockPos(6, 1, 1), "test_fungus_guard");
		helper.assertTrue(GenePiglins.hoglinRepellent(helper.getLevel(), herd).isEmpty(), "near the guard, it does not");
		helper.succeed();
	}

	@GameTest
	public void warpEaterEatsTheFungusOnTheGround(GameTestHelper helper) {
		Hoglin eater = hoglinWith(helper, new BlockPos(4, 1, 4), "test_warp_eater");
		eater.setHealth(10.0F);
		ItemEntity fungus = new ItemEntity(helper.getLevel(), eater.getX(), eater.getY(), eater.getZ(),
				new ItemStack(Items.WARPED_FUNGUS));
		helper.getLevel().addFreshEntity(fungus);
		helper.assertTrue(GeneBehaviors.eatItems(eater, helper.getLevel(),
				new GeneEffect.EatItems(HolderSet.direct(Items.WARPED_FUNGUS.builtInRegistryHolder()), 2.0F)), "it eats");
		helper.assertTrue(fungus.isRemoved(), "the fungus is gone");
		helper.assertValueEqual(eater.getHealth(), 12.0F, "the fungus heals 2 health");
		helper.succeed();
	}

	@GameTest
	public void steadyNervesDoesNotFearPiglinsAndAttacksThem(GameTestHelper helper) {
		Hoglin plain = hoglinWith(helper, new BlockPos(1, 1, 1));
		Hoglin steady = hoglinWith(helper, new BlockPos(1, 1, 6), "test_steady_nerves");
		for (Hoglin hoglin : new Hoglin[] {plain, steady}) {
			hoglin.getBrain().setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, 3);
			hoglin.getBrain().setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, 0);
		}
		helper.assertTrue(HoglinAiAccessor.originofspawns$piglinsOutnumberHoglins(plain), "a plain hoglin fears the piglins");
		helper.assertFalse(HoglinAiAccessor.originofspawns$piglinsOutnumberHoglins(steady), "Steady Nerves does not");
		Piglin piglin = PiglinEffectsTest.piglinWith(helper, new BlockPos(5, 1, 6));
		helper.assertTrue(GeneBehaviors.huntMobs(steady, helper.getLevel(),
				new GeneEffect.HuntMobs(HolderSet.direct(EntityTypes.PIGLIN.builtInRegistryHolder()))), "it finds the piglin");
		helper.assertTrue(steady.getTarget() == piglin, "it attacks the piglin");
		helper.succeed();
	}

	@GameTest
	public void theChaseStatesFollowThePlayerTarget(GameTestHelper helper) {
		Hoglin hoglin = hoglinWith(helper, new BlockPos(1, 1, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(hoglin.position().add(2.0, 0.0, 0.0));
		hoglin.setTarget(player);
		helper.assertFalse(GeneCombat.test(hoglin, GeneEffect.When.CHASING_PLAYER), "a player 2 blocks away");
		player.setPos(hoglin.position().add(6.0, 0.0, 0.0));
		helper.assertTrue(GeneCombat.test(hoglin, GeneEffect.When.CHASING_PLAYER), "a player 6 blocks away");
		helper.assertFalse(GeneCombat.test(hoglin, GeneEffect.When.TARGET_HOLDS_WARPED_FUNGUS), "no fungus");
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WARPED_FUNGUS));
		helper.assertTrue(GeneCombat.test(hoglin, GeneEffect.When.TARGET_HOLDS_WARPED_FUNGUS), "fungus in the off hand");
		helper.succeed();
	}

	@GameTest
	public void ramHitsHarderAfterARunOfSixBlocks(GameTestHelper helper) {
		Hoglin hoglin = hoglinWith(helper, new BlockPos(1, 1, 4), "test_ram");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		var hit = helper.getLevel().damageSources().mobAttack(hoglin);
		// The run counts the moves of each tick. The test moves the hoglin one block each time.
		GeneBehaviors.countRun(hoglin);
		for (int i = 0; i < 3; i++) {
			hoglin.snapTo(hoglin.position().add(1.0, 0.0, 0.0), 0.0F, 0.0F);
			GeneBehaviors.countRun(hoglin);
		}
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "no extra damage after 3 blocks");
		GeneBehaviors.countRun(hoglin);
		helper.assertTrue(hoglin.getAttached(ModAttachments.RUN_BLOCKS) == null, "a stop ends the run");
		hoglin.setAttached(ModAttachments.RUN_BLOCKS, 6.5);
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pig, hit, 10.0F) - 13.0F) < 0.001F, "30% more after 6 blocks");
		GeneCombat.onHit(hoglin, pig);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "the hit ends the run");
		helper.succeed();
	}

	@GameTest
	public void bigLitterSpawnsABabyHoglinWithIt(GameTestHelper helper) {
		Hoglin hoglin = hoglinWith(helper, new BlockPos(4, 1, 4), "test_big_litter");
		// A vanilla hoglin spawns as a baby with a 20% chance, so the test counts 10 spawns.
		int babies = 0;
		for (int i = 0; i < 10; i++) {
			GeneBehaviors.onSpawn(hoglin, helper.getLevel(), EntitySpawnReason.NATURAL);
			var litter = helper.getLevel().getEntitiesOfClass(Hoglin.class, hoglin.getBoundingBox().inflate(3.0),
					other -> other != hoglin);
			helper.assertValueEqual(litter.size(), 1, "one more hoglin");
			if (litter.get(0).isBaby()) {
				babies++;
			}
			litter.get(0).discard();
		}
		helper.assertValueEqual(babies, 10, "each extra hoglin is a baby");
		helper.succeed();
	}

	@GameTest
	public void mourningHerdGivesStrengthWhenAHoglinDiesNear(GameTestHelper helper) {
		Hoglin mourner = hoglinWith(helper, new BlockPos(1, 1, 1), "test_mourning_herd");
		Hoglin dead = hoglinWith(helper, new BlockPos(5, 1, 5));
		GeneCombat.onDeath(dead, helper.getLevel());
		helper.assertTrue(mourner.hasEffect(MobEffects.STRENGTH), "the hoglin gets Strength");
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
