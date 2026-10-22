package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the pillager genes use: the crossbow, the rockets, the patrol, and the raid. It also tests
 * that the reused effects work on a pillager.
 */
public class PillagerEffectsTest {
	@GameTest
	public void aDeathInARaidCountsForTheRaidPressure(GameTestHelper helper) {
		Pillager raider = pillagerWith(helper, new BlockPos(1, 1, 1));
		Pillager patroller = pillagerWith(helper, new BlockPos(4, 1, 1));
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.NORMAL);
		raid.joinRaid(helper.getLevel(), 1, raider, null, true);
		var source = helper.getLevel().damageSources().generic();
		raider.hurtServer(helper.getLevel(), source, 100.0F);
		patroller.hurtServer(helper.getLevel(), source, 100.0F);
		// The raid lets the raider go when it dies, so only the record of the death can tell.
		helper.assertTrue(raider.getCurrentRaid() == null, "the raid let the raider go");
		helper.assertTrue(Evolution.facts(raider, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death in a raid");
		helper.assertFalse(Evolution.facts(patroller, source, helper.getLevel()).contains(DeathCondition.RAID),
				"a death out of a raid");
		helper.succeed();
	}

	static Pillager pillagerWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Pillager pillager = EntityTypes.PILLAGER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		pillager.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
		pillager.setNoAi(true);
		pillager.setPersistenceRequired();
		pillager.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(pillager);
		return pillager;
	}
}
