package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.SensorAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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

	@GameTest
	public void goldWiseSeesAPlayerInGoldArmorAsATarget(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 1.5)));
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
		Piglin plain = piglinWith(helper, new BlockPos(1, 1, 4));
		Piglin wise = piglinWith(helper, new BlockPos(6, 1, 4), "test_gold_wise");
		for (Piglin piglin : new Piglin[] {plain, wise}) {
			sense(helper, piglin, player);
		}
		helper.assertFalse(plain.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD),
				"a plain piglin leaves a player in gold armor alone");
		helper.assertTrue(wise.getBrain().getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD)
				.filter(seen -> seen == player).isPresent(), "Gold Wise sees the player in gold armor as a target");
		helper.succeed();
	}

	@GameTest
	public void soulBraveDoesNotFearSoulFireOrZombifiedPiglins(GameTestHelper helper) {
		helper.setBlock(new BlockPos(4, 1, 1), Blocks.SOUL_LANTERN);
		ZombifiedPiglin zombified = ZombifiedPiglinEffectsTest.piglinWith(helper, new BlockPos(4, 1, 6));
		Piglin plain = piglinWith(helper, new BlockPos(2, 1, 3));
		Piglin brave = piglinWith(helper, new BlockPos(6, 1, 3), "test_soul_brave");
		for (Piglin piglin : new Piglin[] {plain, brave}) {
			sense(helper, piglin, zombified);
		}
		helper.assertTrue(plain.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT), "a plain piglin fears the soul lantern");
		helper.assertTrue(plain.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED),
				"a plain piglin sees the zombified piglin as a danger");
		helper.assertFalse(brave.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT), "Soul Brave does not fear soul fire");
		helper.assertFalse(brave.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED),
				"Soul Brave does not fear the zombified piglin");
		helper.succeed();
	}

	@GameTest
	public void theStateTargetGoldArmorIsTrueForAPlayerInGold(GameTestHelper helper) {
		Piglin piglin = piglinWith(helper, new BlockPos(4, 1, 4));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		// A piglin keeps its target in its brain.
		piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
		helper.assertTrue(piglin.getTarget() == player, "the piglin targets the player");
		helper.assertFalse(GeneCombat.test(piglin, GeneEffect.When.TARGET_GOLD_ARMOR), "no gold armor");
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
		helper.assertTrue(GeneCombat.test(piglin, GeneEffect.When.TARGET_GOLD_ARMOR), "gold boots");
		helper.succeed();
	}

	@GameTest
	public void aGeneTargetReachesTheBrainOfThePiglinKin(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		Piglin piglin = piglinWith(helper, new BlockPos(1, 1, 4));
		Mob brute = helper.spawnWithNoFreeWill(EntityTypes.PIGLIN_BRUTE, new BlockPos(3, 1, 4));
		Mob hoglin = helper.spawnWithNoFreeWill(EntityTypes.HOGLIN, new BlockPos(5, 1, 4));
		Mob zoglin = helper.spawnWithNoFreeWill(EntityTypes.ZOGLIN, new BlockPos(7, 1, 4));
		for (Mob mob : new Mob[] {piglin, brute, hoglin, zoglin}) {
			mob.setTarget(pig);
			helper.assertTrue(mob.getTarget() == pig, "the target goes in the brain of " + mob.getType());
			mob.setTarget(null);
			helper.assertTrue(mob.getTarget() == null, "the brain forgets the target of " + mob.getType());
		}
		helper.succeed();
	}

	/**
	 * Runs the piglin sensor one time. The piglin sees the entities of the list.
	 */
	static void sense(GameTestHelper helper, Piglin piglin, LivingEntity... seen) {
		piglin.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				new NearestVisibleLivingEntities(helper.getLevel(), piglin, java.util.List.of(seen)));
		((SensorAccessor) new PiglinSpecificSensor()).originofspawns$doTick(helper.getLevel(), piglin);
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
