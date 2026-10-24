package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
import io.github.stivengjekaj.originofspawns.evolution.GeneThrows;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the enderman genes use: the teleports, the stare, the thrown block, and the water. It also
 * tests that the reused effects work on an enderman. Each teleport goes to a place inside the test area.
 */
public class EndermanEffectsTest {
	@GameTest
	public void teleportToTargetGoesNextToTheTargetOneTimeInEachCooldown(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		enderman.setTarget(pig);
		var teleport = new GeneEffect.TeleportToTarget(6.0F, GeneEffect.When.ALWAYS, 3.0F);
		helper.assertTrue(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "the enderman teleports");
		helper.assertTrue(enderman.distanceTo(pig) < 3.0F, "the enderman is next to the pig, at " + enderman.distanceTo(pig));
		helper.assertTrue(enderman.hasAttached(ModAttachments.TELEPORT_TIME), "the teleport is marked");
		enderman.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))), 0.0F, 0.0F);
		helper.assertFalse(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "the teleport waits for its cooldown");
		helper.succeed();
	}

	@GameTest
	public void aTeleportWithNoFreePlaceDoesNotStartTheCooldown(GameTestHelper helper) {
		// A ring of stone fills each place next to the pig, also the places at the edge of the test area.
		for (int x = 1; x <= 7; x++) {
			for (int z = 1; z <= 7; z++) {
				if (x != 4 || z != 4) {
					for (int y = 1; y <= 3; y++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
					}
				}
			}
		}
		Enderman enderman = endermanWith(helper, new BlockPos(0, 1, 0));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		enderman.setTarget(pig);
		var teleport = new GeneEffect.TeleportToTarget(6.0F, GeneEffect.When.ALWAYS, 3.0F);
		helper.assertFalse(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport), "no free place, no teleport");
		// A gap of two blocks to the west makes one free place, 1.5 blocks from the pig.
		for (int y = 1; y <= 3; y++) {
			helper.setBlock(new BlockPos(2, y, 4), Blocks.AIR);
			helper.setBlock(new BlockPos(3, y, 4), Blocks.AIR);
		}
		helper.assertTrue(GeneTeleports.toTarget(enderman, helper.getLevel(), teleport),
				"the cooldown did not start, so the next try teleports");
		helper.assertTrue(enderman.getX() < pig.getX() && enderman.distanceTo(pig) < 2.0F, "the enderman is in the gap");
		helper.succeed();
	}

	@GameTest
	public void farStepTeleportsOnlyWhenTheTargetIsOutOfReach(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1), "test_far_step");
		enderman.setOnGround(true);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		enderman.setTarget(pig);
		helper.assertFalse(GeneCombat.test(enderman, GeneEffect.When.TARGET_UNREACHABLE), "the enderman can walk to the pig");
		// A wall of stone around the enderman stops each path.
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				if (x != 1 || z != 1) {
					for (int y = 1; y <= 4; y++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
					}
				}
			}
		}
		helper.assertTrue(GeneCombat.test(enderman, GeneEffect.When.TARGET_UNREACHABLE), "the wall stops the enderman");
		helper.succeed();
	}

	@GameTest
	public void shadowStepTeleportsBehindTheTargetAfterAHit(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(4, 1, 6), "test_shadow_step");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 4));
		// The pig faces south, to the enderman. Behind the pig is north.
		pig.setYRot(0.0F);
		GeneCombat.onHit(enderman, pig);
		helper.assertTrue(enderman.getZ() < pig.getZ(), "the enderman is behind the pig");
		helper.assertTrue(enderman.distanceTo(pig) < 2.0F, "the enderman is next to the pig");
		enderman.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 6))), 0.0F, 0.0F);
		GeneCombat.onHit(enderman, pig);
		helper.assertTrue(enderman.getZ() > pig.getZ(), "the next hit in the cooldown does not teleport");
		Enderman plain = endermanWith(helper, new BlockPos(1, 1, 6));
		GeneCombat.onHit(plain, pig);
		helper.assertTrue(plain.getZ() > pig.getZ(), "a plain enderman does not teleport");
		helper.succeed();
	}

	@GameTest
	public void pearlHeartTeleportsAwayAndHealsOneTime(GameTestHelper helper) {
		// The range of the test gene is 3 blocks, so each place of the escape is in the test area.
		Enderman enderman = endermanWith(helper, new BlockPos(4, 1, 4), "test_pearl_heart");
		DamageSource hit = helper.getLevel().damageSources().generic();
		enderman.setHealth(20.0F);
		GeneCombat.onHurt(enderman, hit);
		helper.assertValueEqual(enderman.getHealth(), 20.0F, "no escape at half health");
		Vec3 before = enderman.position();
		enderman.setHealth(8.0F);
		GeneCombat.onHurt(enderman, hit);
		helper.assertValueEqual(enderman.getHealth(), 18.0F, "the escape heals 10 health");
		helper.assertTrue(enderman.position().distanceTo(before) > 0.01, "the enderman teleports away");
		helper.assertTrue(enderman.position().distanceTo(before) < 6.0, "the escape stays in its range");
		enderman.setHealth(8.0F);
		GeneCombat.onHurt(enderman, hit);
		helper.assertValueEqual(enderman.getHealth(), 8.0F, "only one escape in its life");
		helper.succeed();
	}

	@GameTest
	public void blastStepTeleportsAwayFromAnExplosionOneTimeInEachCooldown(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(4, 1, 4), "test_blast_step");
		// A vanilla enderman teleports up to 32 blocks after a hit with no living attacker. The pig is the attacker
		// here, so only the gene teleports, and it stays in the test area.
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		DamageSource blast = helper.getLevel().damageSources().explosion(pig, pig);
		float full = enderman.getHealth();
		Vec3 before = enderman.position();
		helper.assertFalse(enderman.hurtServer(helper.getLevel(), blast, 10.0F), "the explosion does no damage");
		helper.assertValueEqual(enderman.getHealth(), full, "the enderman keeps its health");
		helper.assertTrue(enderman.position().distanceTo(before) > 0.01, "the enderman teleports away");
		helper.assertFalse(GeneTeleports.fromBlast(enderman, blast), "the next explosion in the cooldown hurts");
		helper.assertFalse(GeneTeleports.fromBlast(enderman, helper.getLevel().damageSources().mobAttack(pig)),
				"a hit that is not an explosion does not teleport");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void rainStepAimsTheRandomTeleportAtTheTargetInWater(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1), "test_rain_step");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		enderman.setTarget(pig);
		helper.assertTrue(GeneTeleports.aimedTarget(enderman) == null, "on dry land the teleport stays random");
		// The water hurts the enderman, and the hurt starts its teleport. The teleport aims at the pig, so it stays in
		// the test area.
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.WATER);
		helper.succeedWhen(() -> helper.assertTrue(enderman.distanceTo(pig) < 3.0F,
				"the enderman teleports next to the pig, it is " + enderman.distanceTo(pig) + " away"));
	}

	@GameTest
	public void suddenStepHitsHarderOnTheFirstHitAfterATeleport(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(2, 1, 2), "test_sudden_step");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		DamageSource hit = helper.getLevel().damageSources().mobAttack(enderman);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "no teleport, no extra damage");
		// The vanilla teleport to a fixed place marks the time too.
		helper.assertTrue(enderman.teleport(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 5))).x,
				enderman.getY(), helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 5))).z),
				"the enderman teleports");
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(pig, hit, 10.0F) - 13.0F) < 0.001F, "30% more damage");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "only the first hit");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void dreadGazeGivesDarknessToAPlayerThatStares(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(4, 1, 5), "test_dread_gaze");
		// The vanilla follow range is 64 blocks, and it reaches the players of other tests.
		enderman.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 1))));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, enderman.getEyePosition());
		var dread = new GeneEffect.StareEffect(MobEffects.DARKNESS, 4.0F, 0);
		helper.assertTrue(GeneCombat.staredBy(enderman, player), "the player stares at the enderman");
		helper.assertValueEqual(GeneBehaviors.stareEffect(enderman, helper.getLevel(), dread), 1, "one player stares");
		helper.assertTrue(player.hasEffect(MobEffects.DARKNESS), "the player gets Darkness");
		player.removeAllEffects();
		player.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition().add(0.0, 0.0, -4.0));
		helper.assertValueEqual(GeneBehaviors.stareEffect(enderman, helper.getLevel(), dread), 0,
				"a player that looks away does not stare");
		player.lookAt(EntityAnchorArgument.Anchor.EYES, enderman.getEyePosition());
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
		helper.assertValueEqual(GeneBehaviors.stareEffect(enderman, helper.getLevel(), dread), 0,
				"a carved pumpkin stops the stare");
		player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
		// The behavior tick of the enderman gives the effect of its gene.
		helper.succeedWhen(() -> helper.assertTrue(player.hasEffect(MobEffects.DARKNESS), "the gene gives Darkness"));
	}

	@GameTest
	public void farGazeSeesAStareFromTwoTimesAsFar(GameTestHelper helper) {
		// A follow range of 2 blocks tests the same code as the vanilla 64 blocks, and it stays in the test area.
		Enderman plain = endermanWith(helper, new BlockPos(2, 1, 5));
		Enderman far = endermanWith(helper, new BlockPos(5, 1, 5), "test_far_gaze");
		plain.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2.0);
		far.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2.0);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 2))));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, plain.getEyePosition());
		helper.assertFalse(GeneCombat.staredBy(plain, player), "a plain enderman does not see a stare from 3 blocks");
		helper.assertFalse(lookGoal(plain).canUse(), "a plain enderman does not get angry");
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 2))));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, far.getEyePosition());
		helper.assertTrue(GeneCombat.staredBy(far, player), "the far gaze sees a stare from 3 blocks");
		helper.assertTrue(lookGoal(far).canUse(), "the enderman gets angry");
		helper.succeed();
	}

	@GameTest
	public void hollowEyesSeesTheStareThroughACarvedPumpkin(GameTestHelper helper) {
		Enderman plain = endermanWith(helper, new BlockPos(2, 1, 5));
		Enderman hollow = endermanWith(helper, new BlockPos(5, 1, 5), "test_hollow_eyes");
		plain.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0);
		hollow.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 2))));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, plain.getEyePosition());
		helper.assertFalse(GeneCombat.staredBy(plain, player), "the pumpkin stops the stare at a plain enderman");
		helper.assertFalse(lookGoal(plain).canUse(), "a plain enderman does not get angry");
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 2))));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, hollow.getEyePosition());
		helper.assertTrue(GeneCombat.staredBy(hollow, player), "the hollow eyes see the stare");
		helper.assertTrue(lookGoal(hollow).canUse(), "the enderman gets angry");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void blockThrowThrowsTheCarriedBlockAtTheTargetInItsRange(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 4));
		enderman.setCarriedBlock(Blocks.DIRT.defaultBlockState());
		var throwing = new GeneEffect.BlockThrow(4.0F, 4.0F, 7.0F, 8.0F);
		Pig near = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		enderman.setTarget(near);
		helper.assertTrue(GeneThrows.throwBlock(enderman, helper.getLevel(), throwing) == null, "the pig is too near");
		near.discard();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		enderman.setTarget(pig);
		helper.assertTrue(GeneThrows.throwBlock(enderman, helper.getLevel(), throwing) != null, "the enderman throws");
		helper.assertTrue(enderman.getCarriedBlock() == null, "the enderman lets go of the block");
		enderman.setCarriedBlock(Blocks.DIRT.defaultBlockState());
		helper.assertTrue(GeneThrows.throwBlock(enderman, helper.getLevel(), throwing) == null, "the throw waits for its cooldown");
		helper.succeedWhen(() -> helper.assertValueEqual(pig.getHealth(), 6.0F, "the block does 4 damage to the pig"));
	}

	@GameTest(maxTicks = 60)
	public void aThrownBlockPutsTheBlockDownOnlyWhenItMay(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1));
		GeneThrows.spawn(helper.getLevel(), helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 3, 2))),
				Blocks.DIRT.defaultBlockState(), Vec3.ZERO, enderman, 4.0F, true);
		GeneThrows.spawn(helper.getLevel(), helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 3, 6))),
				Blocks.DIRT.defaultBlockState(), Vec3.ZERO, enderman, 4.0F, false);
		helper.succeedWhen(() -> {
			// The blocks fall to the floor of the test area, at the height 0.
			helper.assertBlockPresent(Blocks.DIRT, new BlockPos(5, 0, 2));
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(FallingBlockEntity.class,
					new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0)).isEmpty(), "both blocks landed");
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(5, 0, 6));
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0)).isEmpty(), "a thrown block drops no item");
		});
	}

	@GameTest(maxTicks = 60)
	public void heavyBlockAndDustThrowChangeTheThrownBlock(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 4), "test_heavy_block", "test_dust_throw");
		enderman.setCarriedBlock(Blocks.DIRT.defaultBlockState());
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		enderman.setTarget(pig);
		helper.assertTrue(GeneThrows.throwBlock(enderman, helper.getLevel(),
				new GeneEffect.BlockThrow(4.0F, 4.0F, 7.0F, 8.0F)) != null, "the enderman throws");
		helper.succeedWhen(() -> {
			helper.assertValueEqual(pig.getHealth(), 2.0F, "the heavy block does 8 damage");
			helper.assertTrue(pig.hasEffect(MobEffects.BLINDNESS), "the dust gives Blindness");
		});
	}

	@GameTest
	public void shieldBreakerStopsTheShieldThatBlocksTheBlock(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1), "test_shield_breaker");
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		// The block starts next to the player, on the side of the shield, so it hits at its first tick.
		FallingBlockEntity block = GeneThrows.spawn(helper.getLevel(), player.position().add(-0.6, 1.0, 0.0),
				Blocks.DIRT.defaultBlockState(), Vec3.ZERO, enderman, 4.0F, false);
		helper.assertTrue(GeneThrows.hit(block, helper.getLevel()), "the block hits the player");
		helper.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "the shield stops for a time");
		helper.assertFalse(player.isBlocking(), "the player cannot block now");
		helper.succeed();
	}

	@GameTest
	public void stoneGripPicksUpStone(GameTestHelper helper) {
		Enderman plain = endermanWith(helper, new BlockPos(2, 1, 2));
		Enderman gripper = endermanWith(helper, new BlockPos(5, 1, 5), "test_stone_grip");
		stoneAround(helper, new BlockPos(2, 1, 2));
		stoneAround(helper, new BlockPos(5, 1, 5));
		helper.assertTrue(helper.getLevel().getGameRules().get(GameRules.MOB_GRIEFING), "the test world allows mob griefing");
		// The goal picks a random place near the enderman each tick, so the test counts many ticks.
		var plainGoal = takeGoal(plain);
		var gripGoal = takeGoal(gripper);
		for (int i = 0; i < 200 && gripper.getCarriedBlock() == null; i++) {
			plainGoal.tick();
			gripGoal.tick();
		}
		helper.assertTrue(plain.getCarriedBlock() == null, "a plain enderman does not pick up stone");
		helper.assertTrue(gripper.getCarriedBlock() != null && gripper.getCarriedBlock().is(Blocks.STONE),
				"the enderman picks up stone");
		helper.succeed();
	}

	/**
	 * Fills the blocks right next to the place with stone, 3 blocks high, and leaves the place for the enderman.
	 */
	static void stoneAround(GameTestHelper helper, BlockPos pos) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				for (int y = 0; y <= 2; y++) {
					if (x != 0 || z != 0) {
						helper.setBlock(pos.offset(x, y, z), Blocks.STONE);
					}
				}
			}
		}
	}

	static net.minecraft.world.entity.ai.goal.Goal takeGoal(Enderman enderman) {
		return ((MobAccessor) enderman).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
				.filter(goal -> goal.getClass().getSimpleName().equals("EndermanTakeBlockGoal")).findFirst().orElseThrow();
	}

	@GameTest
	public void burdenGivesArmorOnlyWhileTheEndermanCarriesABlock(GameTestHelper helper) {
		Enderman enderman = endermanWith(helper, new BlockPos(2, 1, 2), "test_burden");
		double base = enderman.getAttributeValue(Attributes.ARMOR);
		GeneBehaviors.tick(enderman, helper.getLevel());
		helper.assertValueEqual(enderman.getAttributeValue(Attributes.ARMOR), base, "no armor with empty hands");
		enderman.setCarriedBlock(Blocks.DIRT.defaultBlockState());
		helper.assertTrue(GeneCombat.test(enderman, GeneEffect.When.CARRYING_BLOCK), "the enderman carries a block");
		GeneBehaviors.tick(enderman, helper.getLevel());
		helper.assertValueEqual(enderman.getAttributeValue(Attributes.ARMOR), base + 4.0, "4 more armor with a block");
		enderman.setCarriedBlock(null);
		GeneBehaviors.tick(enderman, helper.getLevel());
		helper.assertValueEqual(enderman.getAttributeValue(Attributes.ARMOR), base, "the armor goes with the block");
		helper.succeed();
	}

	@GameTest
	public void voidPunchTeleportsTheTarget(GameTestHelper helper) {
		// The test gene teleports at each hit, in 3 blocks, so each place is in the test area.
		Enderman enderman = endermanWith(helper, new BlockPos(1, 1, 1), "test_void_punch");
		Enderman plain = endermanWith(helper, new BlockPos(1, 1, 6));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
		Vec3 before = pig.position();
		GeneCombat.onHit(plain, pig);
		helper.assertValueEqual(pig.position(), before, "a plain hit does not teleport the pig");
		GeneCombat.onHit(enderman, pig);
		helper.assertTrue(pig.position().distanceTo(before) > 0.01, "the hit teleports the pig");
		helper.assertTrue(pig.position().distanceTo(before) < 6.0, "the pig stays in the range");
		helper.succeed();
	}

	static net.minecraft.world.entity.ai.goal.Goal lookGoal(Enderman enderman) {
		return ((MobAccessor) enderman).originofspawns$targetSelector().getAvailableGoals().stream()
				.map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
				.filter(goal -> goal.getClass().getSimpleName().equals("EndermanLookForPlayerGoal")).findFirst().orElseThrow();
	}

	static Enderman endermanWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Enderman enderman = EntityTypes.ENDERMAN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		enderman.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		enderman.setNoAi(true);
		enderman.setPersistenceRequired();
		enderman.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(enderman);
		return enderman;
	}
}
