package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the spider genes use: the cobwebs, the leaps, the first hits, the light, and the pressure.
 */
public class SpiderEffectsTest {
	@GameTest
	public void aBaneKillCountsForTheBanePressure(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		var sources = helper.getLevel().damageSources();
		var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		ItemStack smite = new ItemStack(Items.IRON_SWORD);
		smite.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
		player.setItemSlot(EquipmentSlot.MAINHAND, smite);
		helper.assertFalse(Evolution.facts(spider, sources.playerAttack(player), helper.getLevel())
				.contains(DeathCondition.BANE), "Smite is not Bane of Arthropods");
		ItemStack bane = new ItemStack(Items.IRON_SWORD);
		bane.enchant(enchantments.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 1);
		player.setItemSlot(EquipmentSlot.MAINHAND, bane);
		helper.assertTrue(Evolution.facts(spider, sources.playerAttack(player), helper.getLevel())
				.contains(DeathCondition.BANE), "a Bane of Arthropods sword counts");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 24, maxTicks = 20)
	public void aPlainSpiderHasNormalSteps(GameTestHelper helper) {
		assertStepVolume(helper, 0.15F);
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 24, maxTicks = 20)
	public void aLoudSpiderHasLouderSteps(GameTestHelper helper) {
		assertStepVolume(helper, 0.3F, "test_spider_loud");
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 24, maxTicks = 20)
	public void aQuietSpiderMakesNoSteps(GameTestHelper helper) {
		assertStepVolume(helper, -1.0F, "test_spider_hush");
	}

	/**
	 * Makes a spider with the genes walk a step on stone, and checks the volume of the step. A volume of -1 means
	 * that the server sent no step.
	 */
	static void assertStepVolume(GameTestHelper helper, float volume, String... genes) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		helper.setBlock(new BlockPos(2, 0, 1), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 0, 1), Blocks.STONE);
		CreeperEffectsTest.Listener listener = CreeperEffectsTest.listener(helper);
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), genes);
		helper.runAfterDelay(2, () -> spider.move(MoverType.SELF, new Vec3(2.0, -0.1, 0.0)));
		helper.runAfterDelay(5, () -> {
			helper.assertValueEqual(listener.volumeOf(SoundEvents.SPIDER_STEP), volume, "the step");
			spider.discard();
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 80)
	public void aWebHitPutsACobwebThatGoesAway(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), "test_web_hit");
		Pig inAir = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		helper.setBlock(new BlockPos(5, 0, 1), Blocks.GRASS_BLOCK);
		helper.setBlock(new BlockPos(5, 1, 1), Blocks.SHORT_GRASS);
		Pig inGrass = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		Pig shot = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 1));
		GeneCombat.onHit(spider, inAir);
		GeneCombat.onHit(spider, inGrass);
		GeneCombat.onProjectileHit(spider, shot);
		helper.assertBlockPresent(Blocks.COBWEB, new BlockPos(3, 1, 1));
		helper.assertBlockPresent(Blocks.SHORT_GRASS, new BlockPos(5, 1, 1));
		helper.assertBlockPresent(Blocks.AIR, new BlockPos(7, 1, 1));
		// The cobweb lasts 1 second, and the game checks the times once each second.
		helper.runAfterDelay(50, () -> {
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 1, 1));
			helper.assertFalse(TemporaryBlocks.isTemporary(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 1))),
					"the record of the cobweb goes away");
			helper.succeed();
		});
	}

	@GameTest
	public void aPlayerBreaksAGeneCobwebWithNoString(GameTestHelper helper) {
		BlockPos temporary = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos real = helper.absolutePos(new BlockPos(4, 1, 1));
		TemporaryBlocks.place(helper.getLevel(), temporary, Blocks.COBWEB.defaultBlockState(), 600);
		helper.getLevel().setBlock(real, Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
		player.gameMode.destroyBlock(temporary);
		player.gameMode.destroyBlock(real);
		helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1));
		helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 1, 1));
		helper.assertValueEqual(stringNear(helper, temporary), 0, "no string from the gene cobweb");
		helper.assertTrue(stringNear(helper, real) > 0, "a vanilla cobweb drops string");
		helper.succeed();
	}

	@GameTest
	public void aWebTrailLeavesACobwebWhileTheSpiderWalks(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), "test_web_trail");
		GeneEffect.WebTrail trail = new GeneEffect.WebTrail(10.0F, 30.0F);
		spider.setDeltaMovement(Vec3.ZERO);
		helper.assertFalse(GeneBehaviors.webTrail(spider, helper.getLevel(), trail), "no cobweb while it stands");
		spider.setDeltaMovement(0.2, 0.0, 0.0);
		helper.assertTrue(GeneBehaviors.webTrail(spider, helper.getLevel(), trail), "a cobweb while it walks");
		helper.assertBlockPresent(Blocks.COBWEB, new BlockPos(1, 1, 1));
		spider.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 1))), 0.0F, 0.0F);
		spider.setDeltaMovement(0.2, 0.0, 0.0);
		helper.assertFalse(GeneBehaviors.webTrail(spider, helper.getLevel(), trail), "one cobweb in each interval");
		helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 1, 1));
		helper.succeed();
	}

	@GameTest(padding = 16)
	public void aWebShotHitsATargetInItsRange(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), "test_web_shot");
		GeneEffect.WebShot shot = new GeneEffect.WebShot(4.0F, 12.0F, 8.0F, 30.0F);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		spider.setTarget(pig);
		helper.assertFalse(GeneBehaviors.webShot(spider, helper.getLevel(), shot), "the target is too near");
		pig.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(15, 1, 1))), 0.0F, 0.0F);
		helper.assertFalse(GeneBehaviors.webShot(spider, helper.getLevel(), shot), "the target is too far");
		pig.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 1, 1))), 0.0F, 0.0F);
		helper.assertTrue(GeneBehaviors.webShot(spider, helper.getLevel(), shot), "the target is in the range");
		helper.assertBlockPresent(Blocks.COBWEB, new BlockPos(7, 1, 1));
		pig.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(9, 1, 1))), 0.0F, 0.0F);
		helper.assertFalse(GeneBehaviors.webShot(spider, helper.getLevel(), shot), "one shot in each cooldown");
		helper.assertBlockPresent(Blocks.AIR, new BlockPos(9, 1, 1));
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theFirstHitAfterAVanillaLeapDoesMoreDamage(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), "test_leap_strike");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		var bite = helper.getLevel().damageSources().mobAttack(spider);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 4.0F), 4.0F, "no bonus before a leap");
		// The vanilla leap needs the spider on the ground, and it starts on a roll of 1 in 3. A mob with no AI does
		// not move, so the test puts it on the ground.
		helper.runAfterDelay(5, () -> {
			spider.setOnGround(true);
			spider.setTarget(pig);
			LeapAtTargetGoal leap = (LeapAtTargetGoal) ((MobAccessor) spider).originofspawns$goalSelector()
					.getAvailableGoals().stream().map(WrappedGoal::getGoal)
					.filter(LeapAtTargetGoal.class::isInstance).findFirst().orElseThrow();
			boolean ready = false;
			for (int i = 0; i < 100 && !ready; i++) {
				ready = leap.canUse();
			}
			helper.assertTrue(ready, "the spider can leap");
			leap.start();
			helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 4.0F), 6.0F, "the first hit after the leap");
			helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 4.0F), 4.0F, "the second hit has no bonus");
			leap.start();
			spider.setAttached(ModAttachments.LEAP_TIME, helper.getLevel().getGameTime() - 100L);
			helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 4.0F), 4.0F, "no bonus long after the leap");
			helper.succeed();
		});
	}

	@GameTest
	public void theFirstHitOnAPlayerDoesMoreDamage(GameTestHelper helper) {
		Spider spider = spiderWith(helper, new BlockPos(1, 1, 1), "test_first_strike");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		var bite = helper.getLevel().damageSources().mobAttack(spider);
		helper.assertValueEqual(GeneCombat.modifyDamage(player, bite, 4.0F), 6.0F, "the first hit on the player");
		helper.assertValueEqual(GeneCombat.modifyDamage(player, bite, 4.0F), 4.0F, "the next hit has no bonus");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 4.0F), 4.0F, "a pig is not a player");
		spider.setAttached(ModAttachments.STRIKE_TIMES,
				java.util.Map.of(player.getUUID(), helper.getLevel().getGameTime() - 300L));
		helper.assertValueEqual(GeneCombat.modifyDamage(player, bite, 4.0F), 6.0F, "a first hit again after 10 seconds");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void aDayHunterHuntsInBrightLight(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 2, 1), Blocks.LIGHT);
		helper.setBlock(new BlockPos(4, 2, 1), Blocks.LIGHT);
		Spider plain = spiderWith(helper, new BlockPos(1, 1, 1));
		Spider hunter = spiderWith(helper, new BlockPos(4, 1, 1), "test_day_hunter");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(2, 1, 4))));
		// The light spreads in the next ticks.
		helper.succeedWhen(() -> {
			helper.assertTrue(plain.getLightLevelDependentMagicValue() >= 0.5F, "the plain spider is in bright light");
			helper.assertTrue(hunter.getLightLevelDependentMagicValue() >= 0.5F, "the hunter is in bright light");
			helper.assertFalse(findsTarget(plain), "a plain spider does not look for a target in bright light");
			helper.assertTrue(findsTarget(hunter), "the hunter looks for a target in bright light");
			helper.assertTrue(dropsTarget(plain, player), "a plain spider stops its attack in bright light");
			helper.assertFalse(dropsTarget(hunter, player), "the hunter does not stop its attack");
		});
	}

	/**
	 * Returns true if a target goal of the spider can start in 200 tries. A target goal tries on a roll of 1 in 5.
	 */
	static boolean findsTarget(Spider spider) {
		List<Goal> goals = ((MobAccessor) spider).originofspawns$targetSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(goal -> goal.getClass().getSimpleName().equals("SpiderTargetGoal"))
				.toList();
		for (int i = 0; i < 200; i++) {
			if (goals.stream().anyMatch(Goal::canUse)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the attack goal of the spider stops in 2000 tries. In bright light it stops on a roll of 1 in
	 * 100.
	 */
	static boolean dropsTarget(Spider spider, LivingEntity target) {
		Goal attack = ((MobAccessor) spider).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(MeleeAttackGoal.class::isInstance).findFirst().orElseThrow();
		for (int i = 0; i < 2000; i++) {
			spider.setTarget(target);
			if (!attack.canContinueToUse()) {
				return true;
			}
		}
		return false;
	}

	@GameTest
	public void aMotherReleasesTwoCaveSpidersThatAttackTheKiller(GameTestHelper helper) {
		Spider mother = spiderWith(helper, new BlockPos(1, 1, 1), "test_mother");
		ServerPlayer killer = CreeperEffectsTest.survivalPlayer(helper).player();
		killer.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 1))));
		mother.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(killer), 100.0F);
		List<CaveSpider> young = helper.getLevel().getEntitiesOfClass(CaveSpider.class, mother.getBoundingBox().inflate(3.0));
		helper.assertValueEqual(young.size(), 2, "two cave spiders");
		Identifier motherGene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_mother");
		for (CaveSpider cave : young) {
			helper.assertTrue(cave.getTarget() == killer, "the cave spider attacks the killer");
			helper.assertFalse(cave.getAttachedOrElse(ModAttachments.GENES, new CarriedGenes(List.of())).genes()
					.contains(motherGene), "the cave spider has no genes of the spider");
		}
		helper.succeed();
	}

	static int stringNear(GameTestHelper helper, BlockPos pos) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.0),
				item -> item.getItem().is(Items.STRING)).size();
	}

	static Spider spiderWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Spider spider = EntityTypes.SPIDER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		spider.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		spider.setNoAi(true);
		spider.setPersistenceRequired();
		spider.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(spider);
		return spider;
	}
}
