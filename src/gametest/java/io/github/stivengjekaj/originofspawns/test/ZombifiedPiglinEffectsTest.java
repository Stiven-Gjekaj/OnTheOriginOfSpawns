package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the zombified piglin: the anger, the gold, the rot, the sword, and the chase. It also tests that
 * the zombie code of the zombified piglin does not give it the zombie genes or the zombie rules.
 */
public class ZombifiedPiglinEffectsTest {
	@GameTest
	public void aZombifiedPiglinIsNotAZombieForTheGenes(GameTestHelper helper) {
		var genes = helper.getLevel().registryAccess().lookupOrThrow(ModRegistries.GENE);
		Gene zombieGene = genes.getValue(Identifier.fromNamespaceAndPath("originofspawns_test", "test_climb"));
		helper.assertTrue(zombieGene.carriedBy(EntityTypes.ZOMBIE), "the test gene is a zombie gene");
		long shared = genes.stream().filter(gene -> gene.carriedBy(EntityTypes.ZOMBIE)
				&& gene.carriedBy(EntityTypes.ZOMBIFIED_PIGLIN)).count();
		helper.assertValueEqual(shared, 0L, "no zombie gene goes to a zombified piglin");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aZombifiedPiglinSpawnsWithAPlainGoldWeaponAndNoBaby(GameTestHelper helper) {
		// A high difficulty gives a vanilla zombified piglin a random enchantment often.
		DifficultyInstance difficulty = new DifficultyInstance(Difficulty.HARD, 10_000_000L, 10_000_000L, 1.0F);
		int babies = 0;
		int enchanted = 0;
		int plainWeapons = 0;
		for (int i = 0; i < 200; i++) {
			ZombifiedPiglin piglin = EntityTypes.ZOMBIFIED_PIGLIN.create(helper.getLevel(), EntitySpawnReason.NATURAL);
			piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))), 0.0F, 0.0F);
			piglin.finalizeSpawn(helper.getLevel(), difficulty, EntitySpawnReason.NATURAL, null);
			ItemStack weapon = piglin.getItemBySlot(EquipmentSlot.MAINHAND);
			if (weapon.is(Items.GOLDEN_SWORD) || weapon.is(Items.GOLDEN_SPEAR)) {
				plainWeapons++;
			}
			if (weapon.isEnchanted()) {
				enchanted++;
			}
			if (piglin.isBaby()) {
				babies++;
			}
			// A chicken jockey adds its chicken to the level. The test removes all of them.
			if (piglin.getVehicle() != null) {
				piglin.getVehicle().discard();
			}
			piglin.discard();
		}
		helper.assertValueEqual(plainWeapons, 200, "each zombified piglin keeps its gold weapon");
		helper.assertValueEqual(enchanted, 0, "no weapon gets a random enchantment");
		helper.assertValueEqual(babies, 0, "no zombified piglin spawns as a baby in a vanilla way");
		helper.succeed();
	}

	/**
	 * Adds a zombified piglin with the test genes and no AI. The genes go on before it enters the level, so it gets
	 * no genes from the pool.
	 */
	static ZombifiedPiglin piglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		ZombifiedPiglin piglin = EntityTypes.ZOMBIFIED_PIGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		piglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		piglin.setNoAi(true);
		piglin.setPersistenceRequired();
		piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(piglin);
		return piglin;
	}
}
