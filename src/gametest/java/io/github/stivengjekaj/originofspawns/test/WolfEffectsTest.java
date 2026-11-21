package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.evolution.LivingEvolution;
import io.github.stivengjekaj.originofspawns.evolution.NeutralMobs;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Checks the genes of a wolf: the pool, the tame rule, the babies, the anger, the calls, and the reused effects.
 */
public class WolfEffectsTest {
	static final Identifier WOLF = Identifier.withDefaultNamespace("wolf");
	static final Identifier PELT = Identifier.fromNamespaceAndPath("originofspawns_test", "test_wolf_pelt");

	static Wolf wolf(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.WOLF, pos, genes);
	}

	static BiomeCategory category(GameTestHelper helper, BlockPos pos) {
		return BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
	}

	@GameTest
	public void aWildWolfGetsGenesFromThePool(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		Wolf wolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, pos);
		pools.setFrequency(WOLF, category, PELT, before);
		CarriedGenes genes = wolf.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null && genes.genes().contains(PELT), "the wolf takes the gene from the pool");
		helper.succeed();
	}

	@GameTest
	public void eachDeathOfAWildWolfCountsAngryOrNot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		Wolf calm = wolf(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		calm.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(calm.isDeadOrDying(), "the calm wolf dies");
		float afterCalm = pools.frequency(WOLF, category, PELT);
		helper.assertTrue(afterCalm > before, "the death of a calm wolf raises the fire gene");
		Wolf angry = wolf(helper, new BlockPos(4, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		angry.setPersistentAngerTarget(EntityReference.of(player));
		angry.startPersistentAngerTimer();
		helper.assertTrue(angry.isAngry(), "the second wolf is angry");
		angry.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(pools.frequency(WOLF, category, PELT) > afterCalm, "the death of an angry wolf raises it too");
		helper.succeed();
	}

	static void tame(Wolf wolf, net.minecraft.world.entity.player.Player owner) {
		wolf.setTame(true, true);
		wolf.setOwnerReference(EntityReference.of(owner));
	}

	@GameTest
	public void aTameWolfKeepsItsGenesAndGetsNoNewGenes(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf wolf = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_pelt");
		wolf.tame(player);
		helper.assertValueEqual(wolf.getAttached(ModAttachments.GENES).genes(), java.util.List.of(PELT),
				"the tame wolf keeps its genes");
		helper.assertTrue(NeutralMobs.outsidePool(wolf), "the tame wolf is out of the pool");
		wolf.setAttached(ModAttachments.BORN, helper.getLevel().getGameTime() - 2L * LivingEvolution.MIN_AGE);
		helper.assertFalse(LivingEvolution.canChange(wolf, helper.getLevel()), "a tame wolf never changes its genes");

		BlockPos pos = new BlockPos(4, 1, 4);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		Wolf old = EntityTypes.WOLF.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		old.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		old.setNoAi(true);
		tame(old, player);
		helper.getLevel().addFreshEntity(old);
		pools.setFrequency(WOLF, category, PELT, before);
		helper.assertFalse(old.hasAttached(ModAttachments.GENES), "a tame wolf with no genes gets none when it loads");
		helper.succeed();
	}

	@GameTest
	public void theMaximumHealthOfATameWolfIsFortyBeforeTheGenes(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf wolf = wolf(helper, new BlockPos(2, 1, 2), "test_wolf_frail");
		// The game puts the attribute genes on a new mob when it rolls the genes. The test mob gets its genes direct.
		io.github.stivengjekaj.originofspawns.evolution.GeneEffects.applyAttributes(wolf,
				io.github.stivengjekaj.originofspawns.evolution.GeneEffects.sources(helper.getLevel().registryAccess(),
						wolf.getAttached(ModAttachments.GENES)));
		helper.assertTrue(Math.abs(wolf.getMaxHealth() - 7.2F) < 0.001F, "the wild wolf has 8 health less 10%");
		wolf.tame(player);
		helper.assertTrue(Math.abs(wolf.getMaxHealth() - 36.0F) < 0.001F, "the tame wolf has 40 health less 10%");
		helper.succeed();
	}

	@GameTest
	public void theDeathAndTheKillsOfATameWolfDoNotCount(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf hunter = wolf(helper, pos, "test_wolf_pelt");
		tame(hunter, owner);
		var victim = CreeperEffectsTest.survivalPlayer(helper).player();
		float before = pools.frequency(WOLF, category, PELT);
		victim.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(hunter), 1000.0F);
		helper.assertTrue(victim.isDeadOrDying(), "the tame wolf kills a player");
		helper.assertValueEqual(pools.frequency(WOLF, category, PELT), before, "the kill of a tame wolf does not count");
		Wolf pet = wolf(helper, new BlockPos(4, 1, 4), "test_wolf_pelt");
		tame(pet, owner);
		pet.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(pet.isDeadOrDying(), "the tame wolf dies");
		helper.assertValueEqual(pools.frequency(WOLF, category, PELT), before, "the death of a tame wolf does not count");
		helper.succeed();
	}

	@GameTest
	public void aBredBabyTakesGenesFromThePoolAlsoWhenItIsTame(GameTestHelper helper) {
		BlockPos pos = new BlockPos(3, 1, 3);
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf mother = wolf(helper, pos);
		Wolf father = wolf(helper, new BlockPos(4, 1, 3));
		tame(mother, owner);
		tame(father, owner);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		mother.spawnChildFromBreeding(helper.getLevel(), father);
		pools.setFrequency(WOLF, category, PELT, before);
		var babies = helper.getLevel().getEntitiesOfClass(Wolf.class, mother.getBoundingBox().inflate(2.0), Wolf::isBaby);
		helper.assertValueEqual(babies.size(), 1, "the parents make one baby");
		Wolf baby = babies.getFirst();
		helper.assertTrue(baby.isTame(), "the baby of tame wolves is tame");
		CarriedGenes genes = baby.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null && genes.genes().contains(PELT), "the bred baby takes the gene from the pool");
		helper.succeed();
	}
}
