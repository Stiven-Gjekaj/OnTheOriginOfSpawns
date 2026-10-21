package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the parched genes use: the day, the health of the target, Weakness, and the disarm.
 */
public class ParchedEffectsTest {
	@GameTest
	public void dayAndHealthAreStates(GameTestHelper helper) {
		Parched parched = parchedWith(helper, new BlockPos(1, 1, 1));
		helper.assertValueEqual(GeneCombat.test(parched, When.DAY), !helper.getLevel().isDarkOutside(), "day");
		helper.assertTrue(GeneCombat.test(parched, When.HEALTHY), "full health is healthy");
		parched.setHealth(parched.getMaxHealth() / 4.0F);
		helper.assertFalse(GeneCombat.test(parched, When.HEALTHY), "a quarter of health is not healthy");
		helper.assertValueEqual(Evolution.facts(parched, helper.getLevel().damageSources().generic(), helper.getLevel())
				.contains(DeathCondition.DAY), !helper.getLevel().isDarkOutside(), "a death in the day");
		helper.succeed();
	}

	@GameTest
	public void theTargetPlayerHealthAndWeaknessAreStates(GameTestHelper helper) {
		Parched parched = parchedWith(helper, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		parched.setTarget(player);
		helper.assertTrue(GeneCombat.test(parched, When.HUNTING_HEALTHY), "a healthy player");
		helper.assertFalse(GeneCombat.test(parched, When.TARGET_WEAKENED), "no Weakness yet");
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
		helper.assertTrue(GeneCombat.test(parched, When.TARGET_WEAKENED), "a weakened target");
		player.setHealth(4.0F);
		helper.assertFalse(GeneCombat.test(parched, When.HUNTING_HEALTHY), "a wounded player is not healthy");
		helper.assertTrue(GeneCombat.test(parched, When.HUNTING_WOUNDED), "a wounded player");
		helper.succeed();
	}

	@GameTest
	public void fumbleDropsTheHeldItemOnceEachCooldown(GameTestHelper helper) {
		Parched parched = parchedWith(helper, new BlockPos(1, 1, 1), "test_fumble");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		GeneCombat.onHit(parched, player);
		helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "a melee hit does not disarm");
		GeneCombat.onProjectileHit(parched, player);
		helper.assertTrue(player.getMainHandItem().isEmpty(), "an arrow hit disarms");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		GeneCombat.onProjectileHit(parched, player);
		helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "no second disarm in the cooldown");
		player.removeAttached(ModAttachments.DISARM_TIME);
		ItemStack bound = new ItemStack(Items.IRON_SWORD);
		bound.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.BINDING_CURSE), 1);
		player.setItemSlot(EquipmentSlot.MAINHAND, bound);
		GeneCombat.onProjectileHit(parched, player);
		helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD), "a bound item does not drop");
		helper.succeed();
	}

	@GameTest
	public void sandHideNeedsSandAndTheDay(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.SAND);
		helper.setBlock(new BlockPos(4, 0, 1), Blocks.STONE);
		Parched onSand = parchedWith(helper, new BlockPos(1, 1, 1), "test_sand_hide");
		Parched onStone = parchedWith(helper, new BlockPos(4, 1, 1), "test_sand_hide");
		helper.assertValueEqual(GeneEffects.translucentOpacity(onSand).isPresent(), !helper.getLevel().isDarkOutside(),
				"see-through on sand only in the day");
		helper.assertTrue(GeneEffects.translucentOpacity(onStone).isEmpty(), "solid on stone");
		helper.succeed();
	}

	static Parched parchedWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Parched parched = EntityTypes.PARCHED.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		parched.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		parched.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		parched.setNoAi(true);
		parched.setPersistenceRequired();
		parched.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(parched);
		return parched;
	}
}
