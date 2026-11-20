package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneSilverfish;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a silverfish: the wake of the hidden silverfish, the hide in stone, the gnaw, the eggs, and the
 * reused effects.
 */
public class SilverfishEffectsTest {
	static Silverfish silverfish(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.SILVERFISH, pos, genes);
	}

	/**
	 * Hurts the silverfish, and then runs its vanilla wake goal until the wake comes. The silverfish has no AI, so the
	 * test runs the goal itself.
	 */
	static void hurtAndWake(GameTestHelper helper, Silverfish silverfish, Pig attacker) {
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), 0.5F);
		WrappedGoal wake = ((MobAccessor) silverfish).originofspawns$goalSelector().getAvailableGoals().stream()
				.filter(goal -> goal.getGoal().getClass().getSimpleName().equals("SilverfishWakeUpFriendsGoal"))
				.findFirst().orElseThrow();
		for (int i = 0; i < 25; i++) {
			wake.getGoal().tick();
		}
	}

	@GameTest(padding = 16)
	public void aWakeRangeGeneWakesTheHiddenSilverfishFartherAway(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 3));
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1));
		// 12 blocks to the east: past the vanilla range of 10, in the gene range of 14.
		BlockPos far = new BlockPos(13, 1, 1);
		helper.setBlock(far, Blocks.INFESTED_STONE);
		hurtAndWake(helper, silverfish, pig);
		helper.assertBlockPresent(Blocks.INFESTED_STONE, far);
		silverfish.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_nest_alarm"))));
		hurtAndWake(helper, silverfish, pig);
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, far);
		helper.succeed();
	}

	@GameTest(padding = 16)
	public void aStoneAlarmWakesTheHiddenSilverfishWhenItSeesItsTarget(GameTestHelper helper) {
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1), "test_stone_alarm");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 1, 1))));
		silverfish.setTarget(player);
		BlockPos hidden = new BlockPos(1, 1, 6);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		var alarm = new GeneEffect.WakeOnSight(8, 10.0F);
		helper.assertTrue(GeneSilverfish.wakeOnSight(silverfish, helper.getLevel(), alarm), "the silverfish wakes the others");
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, hidden);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		helper.assertFalse(GeneSilverfish.wakeOnSight(silverfish, helper.getLevel(), alarm), "the next wake waits");
		helper.assertBlockPresent(Blocks.INFESTED_STONE, hidden);
		helper.succeed();
	}

	@GameTest(padding = 16)
	public void aDeathCryWakesTheHiddenSilverfishWhenABlastKillsIt(GameTestHelper helper) {
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1), "test_death_cry");
		BlockPos hidden = new BlockPos(1, 1, 7);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().explosion(null, null), 100.0F);
		helper.assertTrue(silverfish.isDeadOrDying(), "the blast kills the silverfish");
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, hidden);
		helper.succeed();
	}

	/**
	 * Puts stone on the six sides of the place, so that a silverfish there can hide in each direction.
	 */
	static void encase(GameTestHelper helper, BlockPos pos) {
		for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
			helper.setBlock(pos.relative(direction), Blocks.STONE);
		}
	}

	/**
	 * Returns the silverfish in the block at the place, other than the one that the test made.
	 */
	static java.util.List<Silverfish> cameOut(GameTestHelper helper, BlockPos pos, Silverfish old) {
		return helper.getLevel().getEntitiesOfClass(Silverfish.class,
				new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(1.5), other -> other != old);
	}

	@GameTest
	public void aSilverfishThatHidesInStoneComesOutWithItsGenesAndFullHealth(GameTestHelper helper) {
		BlockPos place = new BlockPos(2, 2, 2);
		encase(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_lodge_health");
		WrappedGoal merge = ((MobAccessor) silverfish).originofspawns$goalSelector().getAvailableGoals().stream()
				.filter(goal -> goal.getGoal().getClass().getSimpleName().equals("SilverfishMergeWithStoneGoal"))
				.findFirst().orElseThrow();
		// The vanilla goal hides with a chance at each try. Else it starts a walk, and the mob has no AI to walk.
		for (int i = 0; i < 500 && !silverfish.isRemoved(); i++) {
			if (merge.getGoal().canUse()) {
				merge.getGoal().start();
			}
		}
		helper.assertTrue(silverfish.isRemoved(), "the silverfish hides in the stone");
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		java.util.List<Silverfish> out = cameOut(helper, hidden, silverfish);
		helper.assertValueEqual(out.size(), 1, "one silverfish comes out");
		Silverfish next = out.getFirst();
		helper.assertValueEqual(next.getAttached(ModAttachments.GENES).genes(), java.util.List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_lodge_health")), "it has the genes of the first one");
		helper.assertValueEqual(next.getMaxHealth(), 12.0F, "its genes change its maximum health");
		helper.assertValueEqual(next.getHealth(), 12.0F, "it has full health");
		helper.succeed();
	}

	/**
	 * Fills a cube of stone round the place, with the place itself empty.
	 */
	static void bury(GameTestHelper helper, BlockPos pos) {
		for (BlockPos stone : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 3, 2))) {
			helper.setBlock(stone, stone.equals(pos) ? Blocks.AIR : Blocks.STONE);
		}
	}

	/**
	 * Hurts the silverfish. A second hit in the same second does only the damage above the first hit.
	 */
	static void hit(GameTestHelper helper, Silverfish silverfish, Pig attacker, float damage) {
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), damage);
	}

	@GameTest
	public void aDeepLodgeHidesInStoneUnderHalfHealthOneTimeInTheCooldown(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_deep_lodge");
		hit(helper, silverfish, pig, 1.0F);
		helper.assertFalse(silverfish.isRemoved(), "a hit that leaves half health or more does not make it hide");
		// The second hit does 4 damage more than the first: 3 health is left, under half of 8.
		hit(helper, silverfish, pig, 5.0F);
		helper.assertTrue(silverfish.isRemoved(), "a hit under half health makes it hide");
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		Silverfish next = cameOut(helper, hidden, silverfish).getFirst();
		helper.assertValueEqual(next.getHealth(), 8.0F, "it comes out with full health");
		hit(helper, next, pig, 5.0F);
		helper.assertTrue(next.isAlive() && !next.isRemoved(), "the cooldown of the hide goes on after it comes out");
		helper.succeed();
	}
}
