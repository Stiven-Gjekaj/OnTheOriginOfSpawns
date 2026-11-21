package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

/**
 * Checks the genes of a creaking: the pressures of its heart, its twitch while a player looks at it, its heart, its
 * wake, and the reused effects.
 */
public class CreakingEffectsTest {
	static final Identifier CREAKING = Identifier.withDefaultNamespace("creaking");

	/**
	 * A creaking heart at the place, and the creaking that it protects.
	 */
	record Bound(CreakingHeartBlockEntity heart, Creaking creaking) {
	}

	/**
	 * Puts an awake creaking heart at the heart place, and a creaking with the test genes that the heart protects at the
	 * creaking place.
	 */
	static Bound bound(GameTestHelper helper, BlockPos heartPos, BlockPos creakingPos, String... genes) {
		helper.setBlock(heartPos, Blocks.CREAKING_HEART.defaultBlockState()
				.setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE));
		CreakingHeartBlockEntity heart = (CreakingHeartBlockEntity) helper.getBlockEntity(heartPos, CreakingHeartBlockEntity.class);
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, creakingPos, genes);
		creaking.setTransient(helper.absolutePos(heartPos));
		heart.setCreakingInfo(creaking);
		return new Bound(heart, creaking);
	}

	static float frequency(GameTestHelper helper, Creaking creaking, String gene) {
		return GenePools.get(helper.getLevel().getServer()).frequency(CREAKING,
				BiomeCategory.of(helper.getLevel().getBiome(creaking.blockPosition())),
				Identifier.fromNamespaceAndPath("originofspawns_test", gene));
	}

	@GameTest
	public void aBlastThatBreaksTheHeartKillsTheCreakingAndCountsForTheBlastPressure(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4));
		float before = frequency(helper, bound.creaking(), "test_heart_blast");
		float melee = frequency(helper, bound.creaking(), "test_heart_player");
		bound.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertTrue(bound.creaking().isDeadOrDying(), "the creaking dies when its heart breaks");
		helper.assertTrue(frequency(helper, bound.creaking(), "test_heart_blast") > before, "the blast counts for the blast pressure");
		helper.assertValueEqual(frequency(helper, bound.creaking(), "test_heart_player"), melee, "a blast is not a hit of a player");
		helper.succeed();
	}

	@GameTest
	public void aPlayerThatBreaksTheHeartIsTheKillerOfTheCreaking(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		// A test player stays in its test area, so that it does not stand in the way of the other tests.
		player.setNoGravity(true);
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 4))));
		float before = frequency(helper, bound.creaking(), "test_heart_player");
		bound.heart().removeProtector(player.damageSources().playerAttack(player));
		helper.assertTrue(bound.creaking().isDeadOrDying(), "the creaking dies when its heart breaks");
		helper.assertTrue(frequency(helper, bound.creaking(), "test_heart_player") > before,
				"the break counts as a hit of the player");
		helper.assertValueEqual(bound.creaking().getKillCredit(), player, "the player is the killer");
		helper.succeed();
	}

	@GameTest
	public void aHeartBreakCountsForHeartFarOnlyWhenTheCreakingIsFarFromItsHeart(GameTestHelper helper) {
		Bound near = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4));
		float before = frequency(helper, near.creaking(), "test_far_from_heart");
		near.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertTrue(near.creaking().getAttachedOrElse(io.github.stivengjekaj.originofspawns.registry.ModAttachments.HEART_BREAK,
				false), "the heart break marks the creaking");
		helper.assertValueEqual(frequency(helper, near.creaking(), "test_far_from_heart"), before,
				"a creaking 4 blocks from its heart does not count for heart_far");
		// A far heart would sit out of the test area, so this part asks the facts of the death with a far home.
		Creaking far = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(6, 1, 6));
		far.setTransient(helper.absolutePos(new BlockPos(6, 1, 26)));
		far.setAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.HEART_BREAK, true);
		var blast = helper.getLevel().damageSources().explosion(null, null);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.Evolution.facts(far, blast, helper.getLevel())
				.contains(io.github.stivengjekaj.originofspawns.definition.DeathCondition.HEART_FAR),
				"a creaking 20 blocks from its heart counts for heart_far");
		far.removeAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.HEART_BREAK);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.Evolution.facts(far, blast, helper.getLevel())
				.contains(io.github.stivengjekaj.originofspawns.definition.DeathCondition.HEART_FAR),
				"a death with no heart break does not count for heart_far");
		far.discard();
		helper.succeed();
	}

	/**
	 * A survival player in the test area, so that it does not stand in the way of the other tests.
	 */
	static net.minecraft.server.level.ServerPlayer player(GameTestHelper helper, BlockPos pos) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)));
		return player;
	}

	@GameTest
	public void aHeartBreakAfterATrailCountsForHeartTrail(GameTestHelper helper) {
		Bound plain = bound(helper, new BlockPos(1, 1, 1), new BlockPos(3, 1, 1));
		float plainBefore = frequency(helper, plain.creaking(), "test_trail_seen");
		plain.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertValueEqual(frequency(helper, plain.creaking(), "test_trail_seen"), plainBefore,
				"a break with no trail does not count for heart_trail");
		Bound hurt = bound(helper, new BlockPos(1, 1, 5), new BlockPos(3, 1, 5));
		var player = player(helper, new BlockPos(5, 1, 5));
		hurt.creaking().hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(hurt.creaking().isAlive(), "the heart keeps the creaking alive");
		float before = frequency(helper, hurt.creaking(), "test_trail_seen");
		hurt.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertTrue(frequency(helper, hurt.creaking(), "test_trail_seen") > before,
				"a break after the trail counts for heart_trail");
		helper.succeed();
	}

	/**
	 * Gives the creaking a target. A creaking keeps its target in its brain.
	 */
	static void target(Creaking creaking, net.minecraft.world.entity.LivingEntity target) {
		creaking.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, target);
	}

	/**
	 * Makes the creaking awake and still, as when a player looks at it.
	 */
	static void watch(Creaking creaking) {
		creaking.setIsActive(true);
		creaking.getEntityData().set(io.github.stivengjekaj.originofspawns.mixin.CreakingAccessor.originofspawns$canMoveData(), false);
	}

	@GameTest
	public void aJitterStepTakesOneStepWhileAPlayerLooksAtIt(GameTestHelper helper) {
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(1, 1, 3), "test_jitter_step");
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 3));
		target(creaking, pig);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.watchedStep(creaking),
				"a creaking that no player looks at takes no step");
		watch(creaking);
		double before = creaking.getX();
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.watchedStep(creaking),
				"a watched creaking takes a step");
		helper.assertTrue(Math.abs(creaking.getX() - before - 1.0) < 0.01, "the step is one block toward the pig");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.watchedStep(creaking),
				"the next step waits");
		helper.succeed();
	}

	/**
	 * Makes the player look at the creaking, as the brain of the creaking sees it.
	 */
	static void lookAt(net.minecraft.server.level.ServerPlayer player, Creaking creaking) {
		creaking.setIsActive(true);
		creaking.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_PLAYERS,
				java.util.List.<net.minecraft.world.entity.player.Player>of(player));
		player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, creaking.getEyePosition());
	}

	@GameTest(maxTicks = 60)
	public void aTrailTwitchMovesForOneSecondWhenAPlayerHurtsIt(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(3, 1, 3), "test_trail_twitch");
		var player = player(helper, new BlockPos(3, 1, 6));
		lookAt(player, bound.creaking());
		helper.assertFalse(bound.creaking().checkCanMove(), "the look of the player holds the creaking still");
		bound.creaking().hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(bound.creaking().checkCanMove(), "after the hurt, the creaking moves while the player looks");
		helper.runAfterDelay(21, () -> {
			helper.assertFalse(bound.creaking().checkCanMove(), "after 1 second, the look holds it still again");
			helper.succeed();
		});
	}

	@GameTest
	public void aHeartTwitchMovesWhenAPlayerComesNearItsHeart(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(1, 1, 6));
		var twitch = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.HeartTwitch(4.0F, 3.0F, 10.0F);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartTwitch(bound.creaking(), twitch),
				"no player is near the heart");
		var player = player(helper, new BlockPos(3, 1, 1));
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartTwitch(bound.creaking(), twitch),
				"a player comes near the heart");
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.free(bound.creaking()),
				"the creaking moves also while a player looks at it");
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartTwitch(bound.creaking(), twitch),
				"a player that stays near the heart does not come again");
		player.discard();
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aShadowCreepIgnoresAFarLookInTheDark(GameTestHelper helper) {
		Creaking dark = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(1, 1, 1), "test_shadow_creep");
		Creaking lit = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(6, 1, 1), "test_shadow_creep");
		// A light of level 6 at the lit creaking does not reach the dark one, 5 blocks away. The light spreads in the next
		// ticks. The test area has no other block light.
		helper.setBlock(new BlockPos(6, 1, 1), net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState()
				.setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 6));
		helper.runAfterDelay(10, () -> {
			// The test gene ignores a look from more than 3 blocks, so that the players stay in the test area.
			var far = player(helper, new BlockPos(1, 1, 6));
			lookAt(far, dark);
			helper.assertTrue(dark.checkCanMove(), "the look from 5 blocks in the dark does not stop it");
			var farLit = player(helper, new BlockPos(6, 1, 6));
			lookAt(farLit, lit);
			helper.assertFalse(lit.checkCanMove(), "in the light, the far look stops it");
			var near = player(helper, new BlockPos(1, 1, 3));
			lookAt(near, dark);
			helper.assertFalse(dark.checkCanMove(), "the look from 2 blocks still stops it");
			helper.succeed();
		});
	}
}
