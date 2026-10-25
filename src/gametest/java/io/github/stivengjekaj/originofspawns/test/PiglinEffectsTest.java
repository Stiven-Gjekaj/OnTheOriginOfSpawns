package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the piglin: the crossbow, the gold, the hunt, the pack, and the heat. Each piglin, bolt, and
 * call stays inside the test area.
 */
public class PiglinEffectsTest {
	@GameTest(maxTicks = 40)
	public void aPiglinSpawnsWithItsWeaponAndNoRandomArmorAndNoBaby(GameTestHelper helper) {
		// A high difficulty gives a vanilla piglin a random enchantment often.
		DifficultyInstance difficulty = new DifficultyInstance(Difficulty.HARD, 10_000_000L, 10_000_000L, 1.0F);
		int babies = 0;
		int armor = 0;
		int enchanted = 0;
		int weapons = 0;
		for (int i = 0; i < 200; i++) {
			Piglin piglin = EntityTypes.PIGLIN.create(helper.getLevel(), EntitySpawnReason.NATURAL);
			piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))), 0.0F, 0.0F);
			piglin.finalizeSpawn(helper.getLevel(), difficulty, EntitySpawnReason.NATURAL, null);
			ItemStack weapon = piglin.getMainHandItem();
			if (weapon.is(Items.CROSSBOW) || weapon.is(Items.GOLDEN_SWORD) || weapon.is(Items.GOLDEN_SPEAR)) {
				weapons++;
			}
			if (weapon.isEnchanted()) {
				enchanted++;
			}
			for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
				if (!piglin.getItemBySlot(slot).isEmpty()) {
					armor++;
				}
			}
			if (piglin.isBaby()) {
				babies++;
			}
			piglin.discard();
		}
		helper.assertValueEqual(weapons, 200, "each piglin gets its weapon");
		helper.assertValueEqual(enchanted, 0, "no weapon gets a random enchantment");
		helper.assertValueEqual(armor, 0, "no piglin gets random gold armor");
		helper.assertValueEqual(babies, 0, "no piglin spawns as a baby in a vanilla way");
		helper.succeed();
	}

	@GameTest
	public void pigletMakesAPiglinABabyWithNoChicken(GameTestHelper helper) {
		Piglin piglin = piglinWith(helper, new BlockPos(4, 1, 4), "test_runt");
		GeneBehaviors.onSpawn(piglin, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(piglin.isBaby(), "the baby gene makes a baby");
		helper.assertTrue(piglin.getVehicle() == null, "a baby piglin rides no chicken");
		helper.succeed();
	}

	@GameTest
	public void overworldLungsMakesThePiglinTurnLater(GameTestHelper helper) {
		// The test sets the time in the Overworld, and runs one AI step. A piglin turns after more than its time.
		Piglin plain = piglinWith(helper, new BlockPos(1, 1, 1));
		Piglin lungs = piglinWith(helper, new BlockPos(5, 1, 5), "test_overworld_lungs");
		for (Piglin piglin : new Piglin[] {plain, lungs}) {
			piglin.setNoAi(false);
			piglin.setTimeInOverworld(300);
			((MobAccessor) piglin).originofspawns$customServerAiStep(helper.getLevel());
		}
		helper.assertTrue(plain.isRemoved(), "a plain piglin turns after 15 seconds");
		helper.assertFalse(lungs.isRemoved(), "Overworld Lungs does not turn after 15 seconds");
		lungs.setTimeInOverworld(1199);
		((MobAccessor) lungs).originofspawns$customServerAiStep(helper.getLevel());
		helper.assertFalse(lungs.isRemoved(), "Overworld Lungs does not turn at 60 seconds");
		((MobAccessor) lungs).originofspawns$customServerAiStep(helper.getLevel());
		helper.assertTrue(lungs.isRemoved(), "Overworld Lungs turns after 60 seconds");
		helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.ZombifiedPiglin.class,
				new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(0, 0, 0))).inflate(10.0))
				.forEach(net.minecraft.world.entity.Entity::discard);
		helper.succeed();
	}

	/**
	 * Adds a piglin with the test genes and no AI. The genes go on before it enters the level, so it gets no genes
	 * from the pool. It holds a gold sword.
	 */
	static Piglin piglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Piglin piglin = EntityTypes.PIGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		piglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		piglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		piglin.setNoAi(true);
		piglin.setPersistenceRequired();
		piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(piglin);
		return piglin;
	}
}
