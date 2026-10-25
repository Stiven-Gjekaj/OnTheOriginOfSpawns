package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;
import io.github.stivengjekaj.originofspawns.mixin.AbstractCubeMobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the cube parts of the genes on a slime: the split, the hop, the landing, the growth, and the touch attack. It
 * also tests that the reused effects work on a slime at each size. Each cube stays inside the test area.
 */
public class SlimeEffectsTest {
	@GameTest(maxTicks = 60)
	public void thePiecesOfASplitKeepTheGenesOfTheSlime(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 4, "test_sticky_hit");
		CarriedGenes genes = slime.getAttached(ModAttachments.GENES);
		slime.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		// A dead mob leaves the level after its death time, and the split comes then.
		helper.succeedWhen(() -> {
			List<AbstractCubeMob> pieces = piecesNear(helper, slime);
			helper.assertTrue(pieces.size() >= 2 && pieces.size() <= 4, "the slime splits into 2 to 4 pieces, " + pieces.size());
			for (AbstractCubeMob piece : pieces) {
				helper.assertValueEqual(piece.getSize(), 2, "each piece has half the size");
				helper.assertTrue(genes.equals(piece.getAttached(ModAttachments.GENES)), "each piece has the genes of the slime");
			}
		});
	}

	@GameTest
	public void aTouchHitOfASlimeRunsItsHitGenes(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 2, "test_sticky_hit");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		((AbstractCubeMobAccessor) slime).originofspawns$dealDamage(pig);
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the touch hurts the pig");
		helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "the hit gives Slowness");
		helper.succeed();
	}

	@GameTest
	public void greatBlobSpawnsAtSizeFourWithItsChance(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 1, "test_great_blob");
		GeneBehaviors.onSpawn(slime, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertValueEqual(slime.getSize(), 4, "a chance of 1 gives size 4");
		var half = new GeneEffect.SpawnSize(4, 0.5F);
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.49F, 0.9F), 4, "a roll under the chance gives size 4");
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.5F, 0.0F), 1, "the other sizes share the rest");
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.5F, 0.99F), 2, "the other sizes share the rest");
		// Many rolls give the sizes in about the chances of the gene.
		var random = net.minecraft.util.RandomSource.create(1L);
		int[] count = new int[5];
		for (int i = 0; i < 1000; i++) {
			count[GeneCubes.spawnSize(half, random.nextFloat(), random.nextFloat())]++;
		}
		helper.assertTrue(count[4] > 430 && count[4] < 570, "about half the cubes are size 4, " + count[4]);
		helper.assertTrue(count[1] > 180 && count[2] > 180 && count[3] == 0, "sizes 1 and 2 share the rest");
		helper.succeed();
	}

	static List<AbstractCubeMob> piecesNear(GameTestHelper helper, AbstractCubeMob cube) {
		return helper.getLevel().getEntitiesOfClass(AbstractCubeMob.class,
				new AABB(cube.blockPosition()).inflate(4.0), other -> other != cube && other.isAlive());
	}

	/**
	 * Adds a cube of the size with the genes, with no AI. A cube with no AI does not hop, so it stays at its place.
	 */
	static AbstractCubeMob cubeWith(GameTestHelper helper, EntityType<? extends AbstractCubeMob> type, BlockPos pos, int size,
			String... genes) {
		AbstractCubeMob cube = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		cube.setSize(size, true);
		cube.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		cube.setNoAi(true);
		cube.setPersistenceRequired();
		cube.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(cube);
		return cube;
	}
}
