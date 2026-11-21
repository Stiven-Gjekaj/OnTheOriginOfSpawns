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

	@GameTest(maxTicks = 60)
	public void aFearTwitchIgnoresTheLookOfThePlayerThatItHit(GameTestHelper helper) {
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(2, 1, 1), "test_fear_twitch");
		var hit = player(helper, new BlockPos(2, 1, 4));
		lookAt(hit, creaking);
		helper.assertFalse(creaking.checkCanMove(), "the look of the player stops it before the hit");
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(creaking, hit);
		helper.assertTrue(creaking.checkCanMove(), "after the hit, the look of that player does not stop it");
		var other = player(helper, new BlockPos(4, 1, 4));
		lookAt(other, creaking);
		helper.assertFalse(creaking.checkCanMove(), "the look of another player still stops it");
		helper.runAfterDelay(42, () -> {
			lookAt(hit, creaking);
			helper.assertFalse(creaking.checkCanMove(), "after 2 seconds, the look of the first player stops it again");
			helper.succeed();
		});
	}

	@GameTest
	public void aShieldStareIgnoresTheLookOfAPlayerWithAShieldUp(GameTestHelper helper) {
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(2, 1, 1), "test_shield_stare");
		var player = player(helper, new BlockPos(2, 1, 4));
		lookAt(player, creaking);
		helper.assertFalse(creaking.checkCanMove(), "the look of a player with no shield up stops it");
		player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,
				new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
		player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
		for (int i = 0; i < 10; i++) {
			player.doTick();
		}
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(2, 1, 4))));
		lookAt(player, creaking);
		helper.assertTrue(player.isBlocking(), "the player holds the shield up");
		helper.assertTrue(creaking.checkCanMove(), "the look of a player with a shield up does not stop it");
		helper.succeed();
	}

	@GameTest
	public void aBlinkStrikeAndASlipStepActWhenAPlayerLooksAway(GameTestHelper helper) {
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(2, 1, 1), "test_blink_strike");
		player(helper, new BlockPos(2, 1, 2));
		creaking.getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_COOLING_DOWN, true, 40L);
		var speed = creaking.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
		double plain = speed.getValue();
		watch(creaking);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.tickLook(creaking), "the player looks");
		creaking.getEntityData().set(io.github.stivengjekaj.originofspawns.mixin.CreakingAccessor.originofspawns$canMoveData(), true);
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.tick(creaking, helper.getLevel());
		helper.assertFalse(creaking.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_COOLING_DOWN),
				"Blink Strike: the look away ends the attack cooldown");
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.tick(creaking, helper.getLevel());
		helper.assertTrue(Math.abs(speed.getValue() - plain * 1.5) < 1.0E-6, "Slip Step: 50% more speed after the look away");
		helper.succeed();
	}

	@GameTest
	public void aHardHeartIsSlowerToBreakAndABlastDoesNotBreakIt(GameTestHelper helper) {
		Bound plain = bound(helper, new BlockPos(1, 1, 1), new BlockPos(1, 1, 3));
		Bound hard = bound(helper, new BlockPos(5, 1, 1), new BlockPos(5, 1, 3), "test_hard_heart");
		var player = player(helper, new BlockPos(3, 1, 1));
		float plainProgress = plain.heart().getBlockState().getDestroyProgress(player, helper.getLevel(), plain.heart().getBlockPos());
		float hardProgress = hard.heart().getBlockState().getDestroyProgress(player, helper.getLevel(), hard.heart().getBlockPos());
		helper.assertTrue(Math.abs(hardProgress * 3.0F - plainProgress) < 1.0E-6, "the hard heart takes 3 times as long to break");
		var explosion = new net.minecraft.world.level.ServerExplosion(helper.getLevel(), null, null, null,
				net.minecraft.world.phys.Vec3.atCenterOf(hard.heart().getBlockPos()), 4.0F, false,
				net.minecraft.world.level.Explosion.BlockInteraction.DESTROY);
		hard.heart().getBlockState().onExplosionHit(helper.getLevel(), hard.heart().getBlockPos(), explosion, (stack, pos) -> {
		});
		plain.heart().getBlockState().onExplosionHit(helper.getLevel(), plain.heart().getBlockPos(), explosion, (stack, pos) -> {
		});
		helper.assertFalse(plain.creaking().isAlive() && !plain.creaking().isRemoved(), "the same blast breaks a plain heart");
		helper.assertTrue(hard.creaking().isAlive(), "a blast does not break the hard heart");
		helper.assertBlockPresent(Blocks.CREAKING_HEART, new BlockPos(5, 1, 1));
		helper.succeed();
	}

	@GameTest
	public void aThornHeartHurtsThePlayerThatBreaksTheHeart(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4), "test_thorn_heart");
		var player = player(helper, new BlockPos(2, 1, 1));
		bound.heart().removeProtector(player.damageSources().playerAttack(player));
		helper.assertValueEqual(player.getMaxHealth() - player.getHealth(), 4.0F, "the player takes 4 damage");
		helper.assertTrue(bound.creaking().isDeadOrDying(), "the damage does not stop the break");
		helper.succeed();
	}

	@GameTest
	public void aSplinterBurstHurtsThePlayersNearTheCreakingWhenItsHeartBreaks(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4), "test_splinter_burst");
		var near = player(helper, new BlockPos(5, 1, 5));
		var far = player(helper, new BlockPos(1, 1, 6));
		Creaking other = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(4, 1, 5));
		bound.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertValueEqual(near.getMaxHealth() - near.getHealth(), 4.0F, "a player in 3 blocks takes 4 damage");
		helper.assertValueEqual(far.getHealth(), far.getMaxHealth(), "a player farther away takes no damage");
		helper.assertTrue(other.isAlive(), "another creaking takes no damage");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void aHeartLeashKeepsTheCreakingNearerToItsHeart(GameTestHelper helper) {
		Bound plain = bound(helper, new BlockPos(1, 1, 1), new BlockPos(1, 1, 8));
		Bound leashed = bound(helper, new BlockPos(6, 1, 1), new BlockPos(6, 1, 8), "test_heart_leash");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartRange(plain.creaking()), 32.0,
				"a vanilla creaking walks in 32 blocks of its heart");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartRange(leashed.creaking()), 4.0,
				"the genes keep it in 4 blocks, the smallest range");
		// The heart checks its creaking about one time each second. 7 blocks is past the leashed range of 4 and 2.
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(leashed.creaking().isRemoved(), "the heart lets the leashed creaking go");
			helper.assertFalse(plain.creaking().isRemoved(), "the plain creaking stays");
			helper.succeed();
		});
	}

	@GameTest
	public void aCloseGuardWalksBackToItsHeartWhenAPlayerComesNear(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(1, 1, 7));
		var guard = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.HeartGuard(3.0F, 1.5F, 10.0F);
		var player = player(helper, new BlockPos(6, 1, 6));
		bound.creaking().activate(player);
		helper.assertFalse(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartGuard(bound.creaking(), guard),
				"no player is near the heart");
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(2, 1, 2))));
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartGuard(bound.creaking(), guard),
				"a player comes near the heart");
		helper.assertFalse(bound.creaking().isActive(), "the creaking stops its attack");
		var walk = bound.creaking().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET).orElseThrow();
		helper.assertValueEqual(walk.getTarget().currentBlockPosition(), helper.absolutePos(new BlockPos(1, 1, 1)),
				"the creaking walks to its heart");
		helper.assertValueEqual(walk.getSpeedModifier(), 1.5F, "it walks with 50% more speed");
		helper.succeed();
	}

	/**
	 * Puts a heart in pale oak logs, lets a player hurt its creaking, and returns the resin clumps that grew.
	 */
	static long resinAfterHurt(GameTestHelper helper, BlockPos corner, Bound[] out, String... genes) {
		for (BlockPos log : BlockPos.betweenClosed(corner, corner.offset(2, 2, 2))) {
			helper.setBlock(log, Blocks.PALE_OAK_LOG);
		}
		Bound bound = bound(helper, corner.offset(1, 1, 1), corner.offset(1, 0, 4), genes);
		out[0] = bound;
		var player = player(helper, corner.offset(3, 0, 4));
		bound.creaking().hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1.0F);
		return java.util.stream.StreamSupport.stream(
				BlockPos.betweenClosed(corner.offset(-1, -1, -1), corner.offset(3, 3, 3)).spliterator(), false)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.RESIN_CLUMP)).count();
	}

	@GameTest(padding = 4)
	public void aHiddenHeartShowsNoTrailAndGrowsNoResin(GameTestHelper helper) {
		Bound[] plain = new Bound[1];
		Bound[] hidden = new Bound[1];
		long plainResin = resinAfterHurt(helper, new BlockPos(1, 1, 1), plain);
		long hiddenResin = resinAfterHurt(helper, new BlockPos(7, 1, 1), hidden, "test_hidden_heart");
		helper.assertTrue(plain[0].creaking().hasAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.TRAIL_TIME),
				"a plain heart shows the trail");
		helper.assertTrue(plainResin > 0L, "a plain heart grows resin");
		helper.assertFalse(hidden[0].creaking().hasAttached(io.github.stivengjekaj.originofspawns.registry.ModAttachments.TRAIL_TIME),
				"the hidden heart shows no trail");
		helper.assertValueEqual(hiddenResin, 0L, "the hidden heart grows no resin");
		helper.succeed();
	}

	@GameTest(padding = 4)
	public void aDarkHeartLooksDormantAndStillGrowsResin(GameTestHelper helper) {
		Bound[] dark = new Bound[1];
		// The heart is dormant, as a dark heart looks at night.
		long resin = resinAfterHurtDormant(helper, new BlockPos(1, 1, 1), dark, "test_dark_heart");
		helper.assertTrue(resin > 0L, "a dark heart that looks dormant still grows resin");
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.darkHeart(dark[0].heart()),
				"the heart knows that its creaking has a dark heart");
		Bound[] plain = new Bound[1];
		long plainResin = resinAfterHurtDormant(helper, new BlockPos(7, 1, 1), plain);
		helper.assertValueEqual(plainResin, 0L, "a plain dormant heart grows no resin");
		helper.succeed();
	}

	/**
	 * Puts a dormant heart in pale oak logs, lets a player hurt its creaking, and returns the resin clumps that grew.
	 */
	static long resinAfterHurtDormant(GameTestHelper helper, BlockPos corner, Bound[] out, String... genes) {
		for (BlockPos log : BlockPos.betweenClosed(corner, corner.offset(2, 2, 2))) {
			helper.setBlock(log, Blocks.PALE_OAK_LOG);
		}
		Bound bound = bound(helper, corner.offset(1, 1, 1), corner.offset(1, 0, 4), genes);
		helper.setBlock(corner.offset(1, 1, 1), helper.getBlockState(corner.offset(1, 1, 1))
				.setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT));
		out[0] = bound;
		var player = player(helper, corner.offset(3, 0, 4));
		bound.creaking().hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1.0F);
		return java.util.stream.StreamSupport.stream(
				BlockPos.betweenClosed(corner.offset(-1, -1, -1), corner.offset(3, 3, 3)).spliterator(), false)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.RESIN_CLUMP)).count();
	}

	@GameTest
	public void aHeartSenseWakesAtAPlayerNearItsHeart(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(6, 1, 6));
		var sense = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.HeartSense(3.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartSense(bound.creaking(), sense) == null,
				"no player is near the heart");
		var player = player(helper, new BlockPos(2, 1, 3));
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.heartSense(bound.creaking(), sense),
				player, "a player near the heart wakes the creaking");
		helper.assertTrue(bound.creaking().isActive(), "the creaking is awake");
		helper.assertValueEqual(bound.creaking().getTarget(), player, "the creaking targets the player");
		helper.succeed();
	}

	@GameTest
	public void aBlastSenseWakesAtTheNearestPlayerWhenTntIsLitNearItsHeart(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(6, 1, 6), "test_blast_sense");
		var player = player(helper, new BlockPos(5, 1, 2));
		var far = new net.minecraft.world.entity.item.PrimedTnt(helper.getLevel(), helper.absoluteVec(new net.minecraft.world.phys.Vec3(7.5, 1, 1.5)).x,
				helper.absolutePos(new BlockPos(7, 1, 1)).getY(), helper.absoluteVec(new net.minecraft.world.phys.Vec3(7.5, 1, 1.5)).z, null);
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.onTntLit(helper.getLevel(), far), 0,
				"TNT out of the radius of the heart does not wake the creaking");
		helper.assertFalse(bound.creaking().isActive(), "the creaking sleeps");
		var tnt = helper.spawn(EntityTypes.TNT, new BlockPos(3, 1, 1));
		helper.assertTrue(bound.creaking().isActive(), "lit TNT near the heart wakes the creaking");
		helper.assertValueEqual(bound.creaking().getTarget(), player, "the creaking targets the nearest player");
		tnt.discard();
		helper.succeed();
	}

	@GameTest
	public void aLoneWatchWakesAtAPlayerOnlyWhenThePlayerIsAlone(GameTestHelper helper) {
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(1, 1, 1));
		var watch = new io.github.stivengjekaj.originofspawns.definition.GeneEffect.LoneWatch(5.0F);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.loneWatch(creaking, watch) == null,
				"no player is near the creaking");
		var first = player(helper, new BlockPos(4, 1, 1));
		var second = player(helper, new BlockPos(1, 1, 4));
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.loneWatch(creaking, watch) == null,
				"two players are near the creaking");
		helper.assertFalse(creaking.isActive(), "the creaking sleeps with two players near it");
		second.discard();
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCreakings.loneWatch(creaking, watch), first,
				"one player alone wakes the creaking");
		helper.assertTrue(creaking.isActive(), "the creaking is awake");
		helper.assertValueEqual(creaking.getTarget(), first, "the creaking targets the lone player");
		helper.succeed();
	}

	/**
	 * Lets the player look at the sleeping creaking and returns true if the look wakes it.
	 */
	static boolean lookWakes(net.minecraft.server.level.ServerPlayer player, Creaking creaking) {
		creaking.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_PLAYERS,
				java.util.List.<net.minecraft.world.entity.player.Player>of(player));
		player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, creaking.getEyePosition());
		creaking.checkCanMove();
		return creaking.isActive();
	}

	@GameTest
	public void aLookWakeRangeChangesTheDistanceOfAWakeLook(GameTestHelper helper) {
		Creaking plain = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(1, 1, 1));
		Creaking gene = MountEffectsTest.mob(helper, EntityTypes.CREAKING, new BlockPos(1, 1, 5), "test_short_stare");
		helper.assertTrue(lookWakes(player(helper, new BlockPos(6, 1, 1)), plain), "a look from 5 blocks wakes a plain creaking");
		helper.assertFalse(lookWakes(player(helper, new BlockPos(6, 1, 5)), gene), "a look from 5 blocks does not reach 3 blocks");
		helper.assertTrue(lookWakes(player(helper, new BlockPos(3, 1, 5)), gene), "a look from 2 blocks wakes it");
		helper.succeed();
	}
}
