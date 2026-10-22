package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

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
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
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
