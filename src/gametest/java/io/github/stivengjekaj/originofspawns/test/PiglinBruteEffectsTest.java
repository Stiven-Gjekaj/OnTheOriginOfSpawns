package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the piglin brute: the axe, the heavy body, the rage, the gate, and the broken guard.
 */
public class PiglinBruteEffectsTest {
	@GameTest
	public void theHomeOfABruteIsThePlaceWhereItSpawned(GameTestHelper helper) {
		PiglinBrute brute = bruteWith(helper, new BlockPos(4, 1, 4));
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "no home before the spawn");
		GeneBehaviors.onSpawn(brute, helper.getLevel(), EntitySpawnReason.NATURAL);
		BlockPos home = brute.getAttached(ModAttachments.HOME);
		helper.assertTrue(home.equals(brute.blockPosition()), "the home is the spawn place");
		helper.assertTrue(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "near home at the spawn");
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "not far from home at the spawn");
		// The test moves the home, not the brute, so the brute stays in the test area.
		brute.setAttached(ModAttachments.HOME, brute.blockPosition().east(20));
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.NEAR_HOME), "20 blocks is not near");
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "20 blocks is not far");
		brute.setAttached(ModAttachments.HOME, brute.blockPosition().east(40));
		helper.assertTrue(GeneCombat.test(brute, GeneEffect.When.FAR_FROM_HOME), "40 blocks is far");
		GeneBehaviors.onSpawn(brute, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(brute.getAttached(ModAttachments.HOME).equals(brute.blockPosition().east(40)),
				"a later spawn call keeps the old home");
		helper.succeed();
	}

	@GameTest
	public void goldGuardAttacksAPlayerThatBreaksGold(GameTestHelper helper) {
		PiglinBrute plain = bruteWith(helper, new BlockPos(1, 1, 1));
		PiglinBrute guard = bruteWith(helper, new BlockPos(6, 1, 6), "test_gold_guard");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))));
		PiglinAi.angerNearbyPiglins(helper.getLevel(), player, false);
		helper.assertTrue(plain.getTarget() == null, "a plain brute does not guard gold");
		helper.assertTrue(guard.getTarget() == player, "Gold Guard attacks the player");
		helper.succeed();
	}

	@GameTest
	public void theStateTargetNoShieldIsFalseWhileThePlayerBlocks(GameTestHelper helper) {
		ServerPlayer shielded = CreeperEffectsTest.shieldedPlayer(helper);
		ServerPlayer open = CreeperEffectsTest.survivalPlayer(helper).player();
		PiglinBrute brute = bruteWith(helper, new BlockPos(1, 1, 4));
		brute.setTarget(open);
		helper.assertTrue(GeneCombat.test(brute, GeneEffect.When.TARGET_NO_SHIELD), "a player with no shield up");
		brute.setTarget(shielded);
		helper.assertFalse(GeneCombat.test(brute, GeneEffect.When.TARGET_NO_SHIELD), "a player with a shield up");
		helper.succeed();
	}

	@GameTest
	public void shatterBlowBreaksAWornArmorPieceAtLowDurability(GameTestHelper helper) {
		PiglinBrute brute = bruteWith(helper, new BlockPos(4, 1, 4), "test_shatter_blow");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		ItemStack worn = new ItemStack(Items.IRON_CHESTPLATE);
		worn.setDamageValue(Math.round(worn.getMaxDamage() * 0.8F));
		player.setItemSlot(EquipmentSlot.CHEST, worn);
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		GeneCombat.onHit(brute, player);
		helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), "the worn chestplate breaks");
		helper.assertFalse(player.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "the new helmet stays");
		helper.succeed();
	}

	@GameTest
	public void rustAxeGivesWeaknessOnlyToIronGolems(GameTestHelper helper) {
		PiglinBrute brute = bruteWith(helper, new BlockPos(4, 1, 4), "test_rust_axe");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(6, 1, 6));
		GeneCombat.onHit(brute, pig);
		GeneCombat.onHit(brute, golem);
		helper.assertFalse(pig.hasEffect(MobEffects.WEAKNESS), "a pig gets no Weakness");
		helper.assertTrue(golem.hasEffect(MobEffects.WEAKNESS), "an iron golem gets Weakness");
		helper.succeed();
	}

	@GameTest
	public void alarmHornCallsThePiglinsAndTheBrutes(GameTestHelper helper) {
		PiglinBrute brute = bruteWith(helper, new BlockPos(1, 1, 4), "test_alarm_horn");
		Piglin piglin = PiglinEffectsTest.piglinWith(helper, new BlockPos(4, 1, 6));
		PiglinBrute other = bruteWith(helper, new BlockPos(6, 1, 4));
		Mob hoglin = helper.spawnWithNoFreeWill(EntityTypes.HOGLIN, new BlockPos(4, 1, 1));
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 1, 3))));
		brute.setTarget(player);
		brute.tickCount = 20;
		GeneBehaviors.tick(brute, helper.getLevel());
		helper.assertTrue(piglin.getTarget() == player, "the piglin answers the call");
		helper.assertTrue(other.getTarget() == player, "the other brute answers the call");
		helper.assertTrue(hoglin.getTarget() == null, "the hoglin does not");
		helper.succeed();
	}

	@GameTest
	public void guardCrusherStopsTheShieldWithAMeleeHit(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		PiglinBrute brute = bruteWith(helper, new BlockPos(4, 1, 1), "test_guard_crusher");
		// A vanilla axe stops a shield too, so the brute of the test holds a sword.
		brute.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		brute.snapTo(player.position().add(-0.9, 0.0, 0.0), -90.0F, 0.0F);
		PiglinBrute plain = bruteWith(helper, new BlockPos(4, 1, 1));
		plain.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		plain.snapTo(player.position().add(-0.9, 0.0, 0.0), -90.0F, 0.0F);
		plain.doHurtTarget(helper.getLevel(), player);
		helper.assertFalse(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "a sword hit keeps the shield up");
		plain.discard();
		brute.doHurtTarget(helper.getLevel(), player);
		helper.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "the shield stops for a time");
		helper.succeed();
	}

	@GameTest
	public void theAttackIntervalGenesChangeTheBrainCooldownOfABrute(GameTestHelper helper) {
		PiglinBrute plain = bruteWith(helper, new BlockPos(1, 1, 1));
		PiglinBrute slow = bruteWith(helper, new BlockPos(6, 1, 6), "test_enderman_bruiser");
		for (PiglinBrute brute : new PiglinBrute[] {plain, slow}) {
			// The brain of a brute sets a cooldown of 20 ticks after each hit.
			brute.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 20L);
			GeneBehaviors.tick(brute, helper.getLevel());
		}
		helper.assertValueEqual(plain.getBrain().getTimeUntilExpiry(MemoryModuleType.ATTACK_COOLING_DOWN), 20L,
				"the vanilla cooldown");
		helper.assertValueEqual(slow.getBrain().getTimeUntilExpiry(MemoryModuleType.ATTACK_COOLING_DOWN), 23L,
				"15% more time between two attacks");
		GeneBehaviors.tick(slow, helper.getLevel());
		helper.assertValueEqual(slow.getBrain().getTimeUntilExpiry(MemoryModuleType.ATTACK_COOLING_DOWN), 23L,
				"the change comes one time for each cooldown");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void theReusedHitEffectsWorkOnABrute(GameTestHelper helper) {
		ReusedEffects.armorBreak(helper, PiglinBruteEffectsTest::bruteWith);
		ReusedEffects.cleave(helper, PiglinBruteEffectsTest::bruteWith);
		ReusedEffects.firstStrike(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedMeleeEffectsWorkOnABrute(GameTestHelper helper) {
		ReusedEffects.knockback(helper, PiglinBruteEffectsTest::bruteWith);
		ReusedEffects.healOnHit(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedThrowWorksOnABrute(GameTestHelper helper) {
		ReusedEffects.throwEffect(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedHealOnceWorksOnABrute(GameTestHelper helper) {
		ReusedEffects.healOnce(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest(maxTicks = 300)
	public void theReusedRegenerationWorksOnABrute(GameTestHelper helper) {
		ReusedEffects.regenerate(helper, PiglinBruteEffectsTest::bruteWith);
	}

	@GameTest
	public void theReusedDamageEffectsWorkOnABrute(GameTestHelper helper) {
		ReusedEffects.damageToGolems(helper, PiglinBruteEffectsTest::bruteWith);
		ReusedEffects.damageTaken(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedGroupDamageWorksOnABrute(GameTestHelper helper) {
		ReusedEffects.groupedAndAlone(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedAuraWorksOnABrute(GameTestHelper helper) {
		ReusedEffects.allyAura(helper, PiglinBruteEffectsTest::bruteWith);
		helper.succeed();
	}

	/**
	 * Adds a piglin brute with the test genes, a gold axe, and no AI. The genes go on before it enters the level, so
	 * it gets no genes from the pool.
	 */
	static PiglinBrute bruteWith(GameTestHelper helper, BlockPos pos, String... genes) {
		PiglinBrute brute = EntityTypes.PIGLIN_BRUTE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		brute.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		brute.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
		brute.setNoAi(true);
		brute.setPersistenceRequired();
		brute.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(brute);
		return brute;
	}
}
