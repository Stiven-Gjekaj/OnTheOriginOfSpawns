package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the skeleton genes use: the bow, the arrows, and the bones.
 */
public class SkeletonEffectsTest {
	@GameTest
	public void throwRangeCanCountForAnyTarget(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_eagle_eye");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		helper.assertValueEqual(GeneBehaviors.throwRangeMultiplier(skeleton, pig), 1.5F, "a target on land");
		helper.succeed();
	}

	@GameTest
	public void keepDistanceWalksBackFromANearTarget(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_backpedal");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		skeleton.setTarget(pig);
		helper.assertTrue(GeneBehaviors.keepsDistance(skeleton), "a target 3 blocks away");
		pig.setPos(skeleton.position().add(0.0, 0.0, 10.0));
		helper.assertFalse(GeneBehaviors.keepsDistance(skeleton), "a target 10 blocks away");
		helper.succeed();
	}

	@GameTest
	public void noSpreadShootsEachArrowTheSameWay(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_deadeye");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 1, 5));
		skeleton.performRangedAttack(pig, 1.0F);
		skeleton.performRangedAttack(pig, 1.0F);
		List<AbstractArrow> arrows = helper.getLevel().getEntitiesOfClass(AbstractArrow.class,
				skeleton.getBoundingBox().inflate(3.0));
		helper.assertValueEqual(arrows.size(), 2, "arrows");
		helper.assertTrue(arrows.get(0).getDeltaMovement().distanceTo(arrows.get(1).getDeltaMovement()) < 1.0E-6,
				"the two arrows fly the same way");
		helper.succeed();
	}

	@GameTest
	public void arrowDamageCountsOnlyForArrows(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_longdraw");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		var sources = helper.getLevel().damageSources();
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(helper.getLevel(), skeleton,
				new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, sources.arrow(arrow, skeleton), 4.0F), 6.0F, "an arrow");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, sources.mobAttack(skeleton), 4.0F), 4.0F, "a punch");
		helper.succeed();
	}

	@GameTest
	public void fireArrowsSetTheTargetOnFire(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_soulfire");
		Pig shot = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		Pig punched = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(helper.getLevel(), skeleton,
				new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
		shot.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(arrow, skeleton), 1.0F);
		GeneCombat.onHit(skeleton, punched);
		helper.assertTrue(shot.isOnFire(), "the shot pig burns");
		helper.assertFalse(punched.isOnFire(), "the punched pig does not burn");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void piercingArrowsGoThroughAShield(GameTestHelper helper) {
		Skeleton plain = skeletonWith(helper, new BlockPos(1, 1, 4));
		Skeleton breaker = skeletonWith(helper, new BlockPos(3, 1, 4), "test_shieldbreaker");
		var player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 1))));
		player.setYRot(0.0F);
		player.setYHeadRot(0.0F);
		player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
		player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
		// A shield blocks only after it is up for a short time. A mock player is not ticked by the server.
		for (int i = 0; i < 10; i++) {
			player.doTick();
		}
		helper.assertTrue(player.isBlocking(), "the player holds the shield up");
		var level = helper.getLevel();
		var plainArrow = new net.minecraft.world.entity.projectile.arrow.Arrow(level, plain, new ItemStack(Items.ARROW),
				new ItemStack(Items.BOW));
		var breakerArrow = new net.minecraft.world.entity.projectile.arrow.Arrow(level, breaker,
				new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
		helper.assertTrue(player.applyItemBlocking(level, level.damageSources().arrow(plainArrow, plain), 4.0F) > 0.0F,
				"the shield blocks a plain arrow");
		helper.assertValueEqual(player.getAttached(ModAttachments.SHIELD_BLOCK), level.getGameTime(),
				"the block counts for the shield pressure");
		helper.assertValueEqual(player.applyItemBlocking(level, level.damageSources().arrow(breakerArrow, breaker), 4.0F),
				0.0F, "the shield does not block a piercing arrow");
		helper.succeed();
	}

	/**
	 * Makes a skeleton with a bow, test genes, and no AI. The genes go on before the skeleton enters the level, so it
	 * gets nothing from the pool.
	 */
	static Skeleton skeletonWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Skeleton skeleton = EntityTypes.SKELETON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		skeleton.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		skeleton.setNoAi(true);
		skeleton.setPersistenceRequired();
		skeleton.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(skeleton);
		return skeleton;
	}
}
