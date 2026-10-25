package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.goal.CreeperFleeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.ZoglinAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the zoglin: the gore, the rush, the rot, the bones, and the madness.
 */
public class ZoglinEffectsTest {
	@GameTest
	public void playerHateTargetsAPlayerBeforeANearerMob(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 1, 4))));
		Zoglin plain = zoglinWith(helper, new BlockPos(1, 1, 4));
		Zoglin hate = zoglinWith(helper, new BlockPos(1, 1, 3), "test_player_hate");
		for (Zoglin zoglin : new Zoglin[] {plain, hate}) {
			zoglin.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
					new NearestVisibleLivingEntities(helper.getLevel(), zoglin, List.of(pig, player)));
		}
		helper.assertTrue(ZoglinAccessor.originofspawns$findNearestValidAttackTarget(helper.getLevel(), plain)
				.filter(target -> target == pig).isPresent(), "a plain zoglin targets the nearest mob");
		helper.assertTrue(ZoglinAccessor.originofspawns$findNearestValidAttackTarget(helper.getLevel(), hate)
				.filter(target -> target == player).isPresent(), "Player Hate targets the player first");
		helper.succeed();
	}

	@GameTest
	public void creeperHunterAttacksCreepersAndCreepersRunFromIt(GameTestHelper helper) {
		// A mob with no free will has no goals, so the creeper of the test only has no AI.
		Creeper creeper = EntityTypes.CREEPER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		creeper.setNoAi(true);
		creeper.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 1, 4))), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(creeper);
		Zoglin plain = zoglinWith(helper, new BlockPos(5, 1, 4));
		// The goal also needs a path away, so the test checks that the creeper has the goal, and whom it runs from.
		helper.assertTrue(((MobAccessor) creeper).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).anyMatch(CreeperFleeGoal.class::isInstance), "a creeper has the run goal");
		helper.assertFalse(CreeperFleeGoal.huntsCreepers(plain), "a creeper does not run from a plain zoglin");
		plain.discard();
		Zoglin hunter = zoglinWith(helper, new BlockPos(5, 1, 4), "test_creeper_hunter");
		helper.assertTrue(CreeperFleeGoal.huntsCreepers(hunter), "a creeper runs from Creeper Hunter");
		helper.assertTrue(GeneBehaviors.huntMobs(hunter, helper.getLevel(),
				new GeneEffect.HuntMobs(HolderSet.direct(EntityTypes.CREEPER.builtInRegistryHolder()))), "it finds the creeper");
		helper.assertTrue(hunter.getTarget() == creeper, "it attacks the creeper");
		helper.succeed();
	}

	@GameTest
	public void holyBaneHalvesTheExtraDamageOfSmite(GameTestHelper helper) {
		Zoglin plain = zoglinWith(helper, new BlockPos(1, 1, 1));
		Zoglin bane = zoglinWith(helper, new BlockPos(5, 1, 5), "test_holy_bane");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		sword.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SMITE), 2);
		player.setItemSlot(EquipmentSlot.MAINHAND, sword);
		var hit = helper.getLevel().damageSources().playerAttack(player);
		helper.assertValueEqual(GeneCombat.modifyDamage(plain, hit, 11.0F), 11.0F, "a plain zoglin takes the full damage");
		// Smite II adds 5 damage. Holy Bane takes away half of it.
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(bane, hit, 11.0F) - 8.5F) < 0.001F, "half the extra damage");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		helper.assertValueEqual(GeneCombat.modifyDamage(bane, hit, 6.0F), 6.0F, "no change with no Smite");
		helper.succeed();
	}

	@GameTest
	public void theReusedHitEffectsWorkOnAZoglin(GameTestHelper helper) {
		ReusedEffects.healOnHit(helper, ZoglinEffectsTest::zoglinWith);
		ReusedEffects.brainCooldown(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDamageEffectsWorkOnAZoglin(GameTestHelper helper) {
		ReusedEffects.damageToGolems(helper, ZoglinEffectsTest::zoglinWith);
		ReusedEffects.damageTaken(helper, ZoglinEffectsTest::zoglinWith);
		ReusedEffects.thorns(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedGroupDamageWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.groupedAndAlone(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedAuraWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.playerAura(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDeathCloudWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.deathCloud(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedCallWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.callOnHurt(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedMovesWorkOnAZoglin(GameTestHelper helper) {
		ReusedEffects.leap(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedSidestepWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.sidestep(helper, ZoglinEffectsTest::zoglinWith);
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theReusedAttributeWhenWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.armorWhenWounded(helper, ZoglinEffectsTest::zoglinWith);
	}

	@GameTest(maxTicks = 40)
	public void theReusedSpeedWhenWorksOnAZoglin(GameTestHelper helper) {
		ReusedEffects.speedWhenWounded(helper, ZoglinEffectsTest::zoglinWith);
	}

	/**
	 * Adds an adult zoglin with the test genes and no AI. The genes go on before it enters the level, so it gets no
	 * genes from the pool.
	 */
	static Zoglin zoglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Zoglin zoglin = EntityTypes.ZOGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zoglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		zoglin.setNoAi(true);
		zoglin.setPersistenceRequired();
		zoglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zoglin);
		return zoglin;
	}
}
