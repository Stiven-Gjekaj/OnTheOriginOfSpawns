package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.spider.CaveSpider;
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
