package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the cave spider genes use: the Poison of the bite, the spawners, and the pickaxes. It also
 * tests that the spider effects work on a cave spider.
 */
public class CaveSpiderEffectsTest {
	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void aToxinBiteGivesALongerAndStrongerPoison(GameTestHelper helper) {
		CaveSpider plain = caveSpiderWith(helper, new BlockPos(1, 1, 1));
		CaveSpider toxin = caveSpiderWith(helper, new BlockPos(1, 1, 4), "test_toxin");
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		Pig toxinTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		plain.doHurtTarget(helper.getLevel(), plainTarget);
		toxin.doHurtTarget(helper.getLevel(), toxinTarget);
		// On Normal, the vanilla bite gives Poison I for 7 seconds.
		MobEffectInstance vanilla = plainTarget.getEffect(MobEffects.POISON);
		helper.assertValueEqual(vanilla.getDuration(), 140, "the vanilla Poison time");
		helper.assertValueEqual(vanilla.getAmplifier(), 0, "the vanilla Poison level");
		MobEffectInstance stronger = toxinTarget.getEffect(MobEffects.POISON);
		helper.assertValueEqual(stronger.getDuration(), 210, "50% longer");
		helper.assertValueEqual(stronger.getAmplifier(), 1, "Poison II");
		helper.succeed();
	}

	@GameTest(padding = 24)
	public void aSpawnerIn16BlocksIsAState(GameTestHelper helper) {
		CaveSpider spider = caveSpiderWith(helper, new BlockPos(1, 1, 1));
		helper.assertFalse(GeneCombat.test(spider, When.NEAR_SPAWNER), "no spawner yet");
		helper.assertTrue(GeneCombat.test(spider, When.FAR_FROM_SPAWNER), "far from a spawner");
		// This spawner is 12 blocks away on each axis, so its chunk is in the search, but it is 17 blocks away.
		helper.setBlock(new BlockPos(13, 1, 13), Blocks.SPAWNER);
		helper.assertFalse(GeneCombat.test(spider, When.NEAR_SPAWNER), "a spawner 17 blocks away is too far");
		helper.setBlock(new BlockPos(1, 1, 14), Blocks.SPAWNER);
		helper.assertTrue(GeneCombat.test(spider, When.NEAR_SPAWNER), "a spawner 13 blocks away");
		helper.assertFalse(GeneCombat.test(spider, When.FAR_FROM_SPAWNER), "not far from a spawner");
		helper.setBlock(new BlockPos(1, 1, 14), Blocks.STONE);
		helper.assertFalse(GeneCombat.test(spider, When.NEAR_SPAWNER), "the spawner is gone");
		helper.succeed();
	}

	@GameTest
	public void aPickaxeDoesMoreDamageToAGeode(GameTestHelper helper) {
		CaveSpider geode = caveSpiderWith(helper, new BlockPos(1, 1, 1), "test_geode");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		var sources = helper.getLevel().damageSources();
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		helper.assertValueEqual(GeneCombat.modifyDamage(geode, sources.playerAttack(player), 4.0F), 4.0F, "a sword");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
		helper.assertValueEqual(GeneCombat.modifyDamage(geode, sources.playerAttack(player), 4.0F), 8.0F, "a pickaxe");
		Snowball snowball = EntityTypes.SNOWBALL.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		helper.assertValueEqual(GeneCombat.modifyDamage(geode, sources.thrown(snowball, player), 4.0F), 4.0F,
				"a thrown snowball is not a hit of the pickaxe");
		helper.succeed();
	}

	@GameTest
	public void aNestWeaverPutsACobwebAtTheTarget(GameTestHelper helper) {
		CaveSpider weaver = caveSpiderWith(helper, new BlockPos(1, 1, 1), "test_cave_web");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		GeneCombat.onHit(weaver, pig);
		helper.assertBlockPresent(Blocks.COBWEB, new BlockPos(3, 1, 1));
		helper.assertTrue(TemporaryBlocks.isTemporary(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 1))),
				"the cobweb goes away later");
		helper.succeed();
	}

	@GameTest(padding = 8)
	public void anEggLayerFromASpawnerSpawnsOneMoreCaveSpider(GameTestHelper helper) {
		// Cave spiders come from spawners, so a spawner spawn must give the brood too.
		helper.assertValueEqual(broodAfter(helper, new BlockPos(2, 1, 2), EntitySpawnReason.SPAWNER), 2,
				"a spawner spawn gives one more cave spider");
		helper.assertValueEqual(broodAfter(helper, new BlockPos(10, 1, 2), EntitySpawnReason.TRIAL_SPAWNER), 2,
				"a trial spawner spawn gives one more cave spider");
		helper.assertValueEqual(broodAfter(helper, new BlockPos(2, 1, 10), EntitySpawnReason.MOB_SUMMONED), 1,
				"a summoned cave spider gives no brood");
		helper.succeed();
	}

	/**
	 * Spawns an Egg Layer cave spider for the reason, and returns the number of cave spiders with its gene near it.
	 */
	static int broodAfter(GameTestHelper helper, BlockPos pos, EntitySpawnReason reason) {
		CaveSpider layer = caveSpiderWith(helper, pos, "test_egg_layer");
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_egg_layer");
		GeneBehaviors.onSpawn(layer, helper.getLevel(), reason);
		return helper.getLevel().getEntitiesOfClass(CaveSpider.class, layer.getBoundingBox().inflate(3.0),
				other -> other.getAttachedOrElse(ModAttachments.GENES, CarriedGenes.NONE).genes().contains(gene)).size();
	}

	@GameTest
	public void aSwarmMindSharesTheDamageWithTheCaveSpidersNearIt(GameTestHelper helper) {
		CaveSpider hit = caveSpiderWith(helper, new BlockPos(1, 1, 1), "test_swarm_mind");
		CaveSpider ally = caveSpiderWith(helper, new BlockPos(3, 1, 1), "test_swarm_mind");
		float full = hit.getHealth();
		hit.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 6.0F);
		helper.assertValueEqual(hit.getHealth(), full - 3.0F, "the hit cave spider takes half");
		helper.assertValueEqual(ally.getHealth(), full - 3.0F, "the ally takes the other half");
		helper.succeed();
	}

	@GameTest
	public void aJukeCaveSpiderStepsAsideFromAnAimedBow(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			helper.setBlock(new BlockPos(x, 0, 1), Blocks.STONE);
		}
		CaveSpider spider = caveSpiderWith(helper, new BlockPos(4, 1, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setPos(spider.position().add(0.0, 0.0, 5.0));
		CreeperEffectsTest.face(player, 180.0F);
		var juke = new GeneEffect.Sidestep(12.0F, 3.0F, 10.0F, true);
		helper.assertFalse(GeneBehaviors.sidestep(spider, helper.getLevel(), juke), "no bow, no step");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		player.startUsingItem(InteractionHand.MAIN_HAND);
		Vec3 before = spider.position();
		helper.assertTrue(GeneBehaviors.sidestep(spider, helper.getLevel(), juke), "the cave spider steps aside");
		helper.assertValueEqual(Math.round(Math.abs(spider.getX() - before.x)), 3L, "the side distance");
		helper.succeed();
	}

	@GameTest
	public void aGapRunnerIsSmaller(GameTestHelper helper) {
		CaveSpider plain = caveSpiderWith(helper, new BlockPos(1, 1, 1));
		CaveSpider small = caveSpiderWith(helper, new BlockPos(4, 1, 1), "test_gap_runner");
		GeneEffects.applyAttributes(small, GeneEffects.sources(helper.getLevel().registryAccess(),
				small.getAttached(ModAttachments.GENES)));
		helper.assertTrue(Math.abs(small.getScale() - 0.8F) < 0.001F, "the scale is 0.8, was " + small.getScale());
		// The game changes the box in the next tick, when it reads the changed attributes.
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(Math.abs(small.getBbWidth() - plain.getBbWidth() * 0.8F) < 0.001F,
					"the box is 20% narrower, was " + small.getBbWidth());
			helper.succeed();
		});
	}

	static CaveSpider caveSpiderWith(GameTestHelper helper, BlockPos pos, String... genes) {
		CaveSpider spider = EntityTypes.CAVE_SPIDER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		spider.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		spider.setNoAi(true);
		spider.setPersistenceRequired();
		spider.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(spider);
		return spider;
	}
}
