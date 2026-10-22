package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the pillager genes use: the crossbow, the rockets, the patrol, and the raid. It also tests
 * that the reused effects work on a pillager.
 */
public class PillagerEffectsTest {
	@GameTest
	public void aDeathInARaidCountsForTheRaidPressure(GameTestHelper helper) {
		Pillager raider = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager patroller = pillagerWith(helper, new BlockPos(4, 1, 1));
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.NORMAL);
		raid.joinRaid(helper.getLevel(), 1, raider, null, true);
		var source = helper.getLevel().damageSources().generic();
		raider.hurtServer(helper.getLevel(), source, 100.0F);
		patroller.hurtServer(helper.getLevel(), source, 100.0F);
		// The raid lets the raider go when it dies, so only the record of the death can tell.
		helper.assertTrue(raider.getCurrentRaid() == null, "the raid let the raider go");
		helper.assertTrue(Evolution.facts(raider, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death in a raid");
		helper.assertFalse(Evolution.facts(patroller, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death out of a raid");
		helper.succeed();
	}

	@GameTest
	public void theDrawTimeChangesTheLoadTimeOfTheCrossbow(GameTestHelper helper) {
		Pillager plain = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager quick = pillagerWith(helper, new BlockPos(3, 1, 1), "test_quick_charge");
		ItemStack crossbow = new ItemStack(Items.CROSSBOW);
		helper.assertValueEqual(CrossbowItem.getChargeDuration(crossbow, plain), 25, "the vanilla load time");
		helper.assertValueEqual(CrossbowItem.getChargeDuration(crossbow, quick), 18, "30% faster");
		helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void aFastShooterShootsItsCrossbowSooner(GameTestHelper helper) {
		// The goal sees the target for 5 ticks first. Then the plain pillager loads for 25 ticks and waits 20 to 39
		// ticks, so it shoots at 52 or later. The fast one shoots 4 times as often, so it loads for 6 ticks and waits 5
		// to 10 ticks, and it shoots at 23 or sooner. With the vanilla load or the vanilla wait, it shoots at 32 or later.
		long plain = firstShotTick(helper, new BlockPos(1, 1, 1));
		long fast = firstShotTick(helper, new BlockPos(1, 1, 6), "test_fast_crossbow");
		helper.runAfterDelay(90, () -> {
			helper.assertTrue(SHOTS.containsKey(plain) && SHOTS.get(plain) >= 50, "the plain shot, at " + SHOTS.get(plain));
			helper.assertTrue(SHOTS.containsKey(fast) && SHOTS.get(fast) <= 25, "the fast shot, at " + SHOTS.get(fast));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 120)
	public void theRangeGenesChangeTheRangeOfTheCrossbow(GameTestHelper helper) {
		// The vanilla range is 8 blocks, so a target 6 blocks away is in it. With half the range, it is not.
		long plain = firstShotTick(helper, new BlockPos(1, 1, 1), 6);
		long near = firstShotTick(helper, new BlockPos(1, 1, 6), 6, "test_short_bolt");
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(SHOTS.containsKey(plain), "the plain pillager shoots");
			helper.assertFalse(SHOTS.containsKey(near), "the pillager with half the range does not shoot so far");
			helper.succeed();
		});
	}

	/**
	 * The tick of the first shot of each pillager that {@link #firstShotTick} watches, by the id of the watch.
	 */
	static final java.util.Map<Long, Long> SHOTS = new java.util.concurrent.ConcurrentHashMap<>();
	static final java.util.concurrent.atomic.AtomicLong WATCHES = new java.util.concurrent.atomic.AtomicLong();

	/**
	 * Makes a pillager with the genes and a pig 5 blocks from it, or at the distance. Each tick runs its crossbow goal, and the tick of its
	 * first arrow goes in {@link #SHOTS}.
	 *
	 * @return the id of the watch
	 */
	static long firstShotTick(GameTestHelper helper, BlockPos pos, String... genes) {
		return firstShotTick(helper, pos, 5, genes);
	}

	static long firstShotTick(GameTestHelper helper, BlockPos pos, int distance, String... genes) {
		Pillager pillager = pillagerWith(helper, pos, genes);
		Pig pig = CreeperEffectsTest.pigAt(helper, pos.east(distance));
		pillager.setTarget(pig);
		Goal goal = crossbowGoal(pillager);
		long watch = WATCHES.incrementAndGet();
		long start = helper.getLevel().getGameTime();
		helper.onEachTick(() -> {
			if (SHOTS.containsKey(watch)) {
				return;
			}
			goal.tick();
			if (!helper.getLevel().getEntitiesOfClass(AbstractArrow.class, pillager.getBoundingBox().inflate(8.0),
					arrow -> arrow.getOwner() == pillager).isEmpty()) {
				SHOTS.put(watch, helper.getLevel().getGameTime() - start);
			}
		});
		return watch;
	}

	static Goal crossbowGoal(Pillager pillager) {
		return ((MobAccessor) pillager).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(RangedCrossbowAttackGoal.class::isInstance).findFirst().orElseThrow();
	}

	static Pillager pillagerWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Pillager pillager = EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		pillager.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
		pillager.setNoAi(true);
		pillager.setPersistenceRequired();
		pillager.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(pillager);
		return pillager;
	}
}
