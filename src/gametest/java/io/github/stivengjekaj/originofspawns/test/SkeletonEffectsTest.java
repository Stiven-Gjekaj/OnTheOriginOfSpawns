package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
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
