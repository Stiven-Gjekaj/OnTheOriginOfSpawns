package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
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

	@GameTest
	public void heightAndHealthStatesFollowTheMob(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(2, 2, 2));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		skeleton.setTarget(pig);
		pig.setPos(pig.getX(), skeleton.getY() - 2.0, pig.getZ());
		helper.assertTrue(GeneCombat.test(skeleton, When.TARGET_BELOW), "a target 2 blocks lower");
		helper.assertFalse(GeneCombat.test(skeleton, When.TARGET_ABOVE), "not higher");
		pig.setPos(pig.getX(), skeleton.getY() + 3.0, pig.getZ());
		helper.assertTrue(GeneCombat.test(skeleton, When.TARGET_ABOVE), "a target 3 blocks higher");
		pig.setPos(pig.getX(), skeleton.getY() + 1.0, pig.getZ());
		helper.assertFalse(GeneCombat.test(skeleton, When.TARGET_ABOVE), "a target 1 block higher");
		helper.assertFalse(GeneCombat.test(skeleton, When.WOUNDED), "full health");
		skeleton.setHealth(skeleton.getMaxHealth() / 2.0F - 1.0F);
		helper.assertTrue(GeneCombat.test(skeleton, When.WOUNDED), "under half health");
		helper.succeed();
	}

	@GameTest
	public void boneWallStopsArrowsFromTheFront(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(2, 1, 2), "test_bonewall");
		skeleton.setYBodyRot(0.0F);
		Skeleton shooter = skeletonWith(helper, new BlockPos(0, 1, 0));
		var level = helper.getLevel();
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(level, shooter, new ItemStack(Items.ARROW),
				new ItemStack(Items.BOW));
		// A body rotation of 0 faces the south, to positive z.
		arrow.setPos(skeleton.position().add(0.0, 1.0, 2.0));
		helper.assertValueEqual(GeneCombat.modifyDamage(skeleton, level.damageSources().arrow(arrow, shooter), 4.0F),
				2.0F, "an arrow from the front");
		arrow.setPos(skeleton.position().add(0.0, 1.0, -2.0));
		helper.assertValueEqual(GeneCombat.modifyDamage(skeleton, level.damageSources().arrow(arrow, shooter), 4.0F),
				4.0F, "an arrow from behind");
		helper.succeed();
	}

	@GameTest
	public void holyWardResistsSmiteAndWolves(GameTestHelper helper) {
		Skeleton warded = skeletonWith(helper, new BlockPos(1, 1, 1), "test_holy_ward");
		Skeleton plain = skeletonWith(helper, new BlockPos(1, 1, 4));
		var level = helper.getLevel();
		// The attacker has no genes, so that only the genes of the warded skeleton change the damage.
		Skeleton attacker = skeletonWith(helper, new BlockPos(3, 1, 1));
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		helper.assertValueEqual(GeneCombat.modifyDamage(warded, level.damageSources().mobAttack(attacker), 4.0F), 4.0F,
				"a plain hit");
		sword.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SMITE), 3);
		attacker.setItemSlot(EquipmentSlot.MAINHAND, sword);
		helper.assertValueEqual(GeneCombat.modifyDamage(warded, level.damageSources().mobAttack(attacker), 4.0F), 2.0F,
				"a hit with a Smite sword");
		var wolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, new BlockPos(3, 1, 3));
		wolf.setTarget(warded);
		helper.assertTrue(wolf.getTarget() == null, "the wolf ignores the warded skeleton");
		wolf.setTarget(plain);
		helper.assertTrue(wolf.getTarget() == plain, "the wolf targets a plain skeleton");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void reassembleStandsUpOnce(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_reassemble");
		// The sun must not burn the skeleton while the test waits.
		skeleton.addEffect(new net.minecraft.world.effect.MobEffectInstance(
				net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 400));
		var level = helper.getLevel();
		skeleton.hurtServer(level, level.damageSources().generic(), 1000.0F);
		helper.assertTrue(skeleton.isAlive(), "the skeleton falls apart, and does not die");
		helper.assertTrue(GeneCombat.collapsed(skeleton), "the skeleton is down");
		helper.runAfterDelay(40, () -> {
			helper.assertFalse(GeneCombat.collapsed(skeleton), "the skeleton stands up");
			helper.assertValueEqual(skeleton.getHealth(), skeleton.getMaxHealth() / 2.0F, "half health");
			skeleton.hurtServer(level, level.damageSources().generic(), 1000.0F);
			helper.assertFalse(skeleton.isAlive(), "the second death is final");
			helper.succeed();
		});
	}

	@GameTest
	public void smiteStopsTheReturn(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(1, 1, 1), "test_reassemble");
		Skeleton attacker = skeletonWith(helper, new BlockPos(3, 1, 1));
		var level = helper.getLevel();
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		sword.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SMITE), 3);
		attacker.setItemSlot(EquipmentSlot.MAINHAND, sword);
		skeleton.hurtServer(level, level.damageSources().mobAttack(attacker), 1000.0F);
		helper.assertFalse(skeleton.isAlive(), "a Smite hit is final");
		helper.succeed();
	}

	@GameTest
	public void barrageMakesTheGroupShootTogether(GameTestHelper helper) {
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 6));
		List<Skeleton> group = List.of(skeletonWith(helper, new BlockPos(0, 1, 0), "test_barrage"),
				skeletonWith(helper, new BlockPos(2, 1, 0), "test_barrage"),
				skeletonWith(helper, new BlockPos(4, 1, 0), "test_barrage"));
		group.forEach(skeleton -> skeleton.setTarget(pig));
		group.getFirst().performRangedAttack(pig, 1.0F);
		int arrows = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, pig.getBoundingBox().inflate(16.0)).size();
		helper.assertValueEqual(arrows, 3, "arrows from one shot of the group");
		helper.succeed();
	}

	@GameTest
	public void warcrySpeedsUpTheAllies(GameTestHelper helper) {
		Skeleton crier = skeletonWith(helper, new BlockPos(1, 1, 1), "test_warcry");
		Skeleton ally = skeletonWith(helper, new BlockPos(4, 1, 4));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 1));
		GeneCombat.onHurt(crier, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertTrue(ally.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the ally gets Speed");
		helper.assertFalse(pig.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the attacker does not");
		helper.succeed();
	}

	@GameTest
	public void spiderRiderSpawnsOnASpider(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(2, 1, 2), "test_spider_rider");
		GeneBehaviors.onSpawn(skeleton, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(skeleton.getVehicle() instanceof net.minecraft.world.entity.monster.spider.Spider,
				"the skeleton rides a spider");
		helper.succeed();
	}

	@GameTest
	public void aDrawnBowAimedAtTheMobCounts(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(2, 1, 6));
		var player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 1))));
		player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, skeleton.getEyePosition());
		helper.assertFalse(GeneBehaviors.isAimedAt(skeleton, player, 12.0F), "a player with no bow drawn");
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
		helper.assertTrue(GeneBehaviors.isAimedAt(skeleton, player, 12.0F), "a player that draws a bow at it");
		player.setYRot(player.getYRot() + 90.0F);
		player.setYHeadRot(player.getYRot());
		helper.assertFalse(GeneBehaviors.isAimedAt(skeleton, player, 12.0F), "a player that looks away");
		helper.succeed();
	}

	@GameTest
	public void broodSkeletonsGetTheirBows(GameTestHelper helper) {
		Skeleton skeleton = skeletonWith(helper, new BlockPos(2, 1, 2), "test_brood");
		GeneBehaviors.onSpawn(skeleton, helper.getLevel(), EntitySpawnReason.NATURAL);
		List<Skeleton> extras = helper.getLevel().getEntitiesOfClass(Skeleton.class, skeleton.getBoundingBox().inflate(3.0),
				other -> other != skeleton);
		helper.assertValueEqual(extras.size(), 1, "extra skeletons");
		helper.assertTrue(extras.getFirst().getMainHandItem().is(Items.BOW), "the extra skeleton has a bow");
		helper.succeed();
	}

	@GameTest
	public void drawTimeFollowsTheRateAndTheDrawGenes(GameTestHelper helper) {
		Skeleton plain = skeletonWith(helper, new BlockPos(1, 1, 1));
		Skeleton fast = skeletonWith(helper, new BlockPos(3, 1, 1), "test_volley");
		Skeleton slow = skeletonWith(helper, new BlockPos(1, 1, 3), "test_slow_draw");
		helper.assertValueEqual(GeneBehaviors.drawTicks(plain, 20), 20, "a plain draw");
		helper.assertValueEqual(GeneBehaviors.drawTicks(fast, 20), 10, "two times the rate draws in half the time");
		helper.assertValueEqual(GeneBehaviors.drawTicks(slow, 20), 25, "a draw 25% slower");
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
