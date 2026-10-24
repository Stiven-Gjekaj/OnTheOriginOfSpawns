package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
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
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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
