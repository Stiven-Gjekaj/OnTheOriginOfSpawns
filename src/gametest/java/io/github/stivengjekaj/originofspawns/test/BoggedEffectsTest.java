package io.github.stivengjekaj.originofspawns.test;

import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the bogged genes use: mud, rot, water, and mushrooms.
 */
public class BoggedEffectsTest {
	@GameTest
	public void mudAndRotGroundAreStates(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.MUD);
		helper.setBlock(new BlockPos(3, 0, 1), Blocks.MOSS_BLOCK);
		helper.setBlock(new BlockPos(5, 0, 1), Blocks.STONE);
		Bogged onMud = boggedWith(helper, new BlockPos(1, 1, 1));
		Bogged onMoss = boggedWith(helper, new BlockPos(3, 1, 1));
		Bogged onStone = boggedWith(helper, new BlockPos(5, 1, 1));
		helper.assertTrue(GeneCombat.test(onMud, When.WET_GROUND), "mud is wet ground");
		helper.assertTrue(GeneCombat.test(onMud, When.ON_ROT_GROUND), "mud is rot ground");
		helper.assertTrue(GeneCombat.test(onMoss, When.DRY_GROUND), "moss is dry ground");
		helper.assertTrue(GeneCombat.test(onMoss, When.ON_ROT_GROUND), "moss is rot ground");
		helper.assertTrue(GeneCombat.test(onStone, When.DRY_GROUND), "stone is dry ground");
		helper.assertFalse(GeneCombat.test(onStone, When.ON_ROT_GROUND), "stone is not rot ground");
		helper.succeed();
	}

	@GameTest
	public void aTargetInWaterIsAState(GameTestHelper helper) {
		helper.setBlock(new BlockPos(4, 1, 1), Blocks.WATER);
		Bogged bogged = boggedWith(helper, new BlockPos(1, 1, 1));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 1, 4));
		bogged.setTarget(pig);
		helper.assertFalse(GeneCombat.test(bogged, When.TARGET_IN_WATER), "a target on land");
		Pig swimmer = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		bogged.setTarget(swimmer);
		helper.succeedWhen(() -> helper.assertTrue(GeneCombat.test(bogged, When.TARGET_IN_WATER), "a target in water"));
	}

	@GameTest
	public void leechboneHealsOnHitsOnlyOnWetGround(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.MUD);
		helper.setBlock(new BlockPos(4, 0, 1), Blocks.STONE);
		Bogged wet = boggedWith(helper, new BlockPos(1, 1, 1), "test_leechbone");
		Bogged dry = boggedWith(helper, new BlockPos(4, 1, 1), "test_leechbone");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 4));
		wet.setHealth(5.0F);
		dry.setHealth(5.0F);
		GeneCombat.onProjectileHit(wet, pig);
		GeneCombat.onProjectileHit(dry, pig);
		helper.assertValueEqual(wet.getHealth(), 6.0F, "a hit heals 1 on mud");
		helper.assertValueEqual(dry.getHealth(), 5.0F, "no heal on stone");
		helper.succeed();
	}

	@GameTest
	public void shearedMushroomsGrowBack(GameTestHelper helper) {
		Bogged bogged = boggedWith(helper, new BlockPos(1, 1, 1), "test_regrow");
		bogged.setHealth(4.0F);
		bogged.shear(helper.getLevel(), SoundSource.PLAYERS, new ItemStack(Items.SHEARS));
		helper.assertTrue(bogged.isSheared(), "the shear works");
		long sheared = bogged.getAttached(ModAttachments.SHEAR_TIME);
		Set<DeathCondition> facts = Evolution.facts(bogged, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(facts.contains(DeathCondition.SHEARS), "a sheared death counts for the shears pressure");
		bogged.tickCount = 20;
		GeneBehaviors.tick(bogged, helper.getLevel());
		helper.assertTrue(bogged.isSheared(), "the mushrooms do not grow back at once");
		bogged.setAttached(ModAttachments.SHEAR_TIME, sheared - 600L);
		GeneBehaviors.tick(bogged, helper.getLevel());
		helper.assertFalse(bogged.isSheared(), "the mushrooms grow back after 30 seconds");
		helper.assertValueEqual(bogged.getHealth(), 10.0F, "it heals 6");
		helper.succeed();
	}

	@GameTest
	public void dryFollowsTheBiome(GameTestHelper helper) {
		Bogged bogged = boggedWith(helper, new BlockPos(2, 1, 2));
		biome(helper, "minecraft:plains");
		helper.assertTrue(Evolution.facts(bogged, helper.getLevel().damageSources().generic(), helper.getLevel())
				.contains(DeathCondition.DRY), "a death in the plains is dry");
		biome(helper, "minecraft:swamp");
		helper.assertFalse(Evolution.facts(bogged, helper.getLevel().damageSources().generic(), helper.getLevel())
				.contains(DeathCondition.DRY), "a death in the swamp is not dry");
		helper.succeed();
	}

	private static void biome(GameTestHelper helper, String biome) {
		BlockPos from = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos to = helper.absolutePos(new BlockPos(5, 4, 5));
		helper.getLevel().getServer().getCommands().performPrefixedCommand(
				helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(),
				"fillbiome " + from.getX() + " " + from.getY() + " " + from.getZ() + " "
						+ to.getX() + " " + to.getY() + " " + to.getZ() + " " + biome);
	}

	static Bogged boggedWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Bogged bogged = EntityTypes.BOGGED.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		bogged.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		bogged.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		bogged.setNoAi(true);
		bogged.setPersistenceRequired();
		bogged.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(bogged);
		return bogged;
	}
}
