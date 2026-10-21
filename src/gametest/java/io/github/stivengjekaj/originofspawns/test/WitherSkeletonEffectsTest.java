package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the wither skeleton genes use: the sword, the fire, the curse, the fortress, and the souls.
 */
public class WitherSkeletonEffectsTest {
	@GameTest
	public void lavaFireAndSoulGroundAreStates(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.SOUL_SAND);
		helper.setBlock(new BlockPos(4, 0, 1), Blocks.NETHERRACK);
		helper.setBlock(new BlockPos(4, 1, 1), Blocks.FIRE);
		WitherSkeleton onSoul = witherWith(helper, new BlockPos(1, 1, 1));
		WitherSkeleton inFire = witherWith(helper, new BlockPos(4, 1, 1));
		helper.assertTrue(GeneCombat.test(onSoul, When.ON_SOUL_GROUND), "soul sand is soul ground");
		helper.assertTrue(GeneCombat.test(inFire, When.OFF_SOUL_GROUND), "netherrack is not soul ground");
		helper.assertTrue(GeneCombat.test(inFire, When.IN_FIRE), "fire at the feet");
		helper.assertFalse(GeneCombat.test(onSoul, When.IN_FIRE), "no fire at the feet");
		helper.assertTrue(GeneCombat.test(inFire, When.OUT_OF_LAVA), "fire is not lava");
		helper.setBlock(new BlockPos(4, 1, 1), Blocks.LAVA);
		helper.assertTrue(GeneCombat.test(inFire, When.IN_LAVA), "lava at the feet");
		helper.assertTrue(GeneCombat.test(inFire, When.IN_FIRE), "lava counts as fire");
		helper.succeed();
	}

	@GameTest
	public void aSmiteKillCountsForTheSmitePressure(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		sword.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.SMITE), 1);
		var sources = helper.getLevel().damageSources();
		helper.assertFalse(Evolution.facts(wither, sources.playerAttack(player), helper.getLevel())
				.contains(DeathCondition.SMITE), "a bare hand is not Smite");
		player.setItemSlot(EquipmentSlot.MAINHAND, sword);
		helper.assertTrue(Evolution.facts(wither, sources.playerAttack(player), helper.getLevel())
				.contains(DeathCondition.SMITE), "a Smite sword counts");
		helper.succeed();
	}

	@GameTest
	public void parryBlocksOneMeleeHitEachCooldown(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_parry");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 1));
		var hit = helper.getLevel().damageSources().mobAttack(pig);
		helper.assertTrue(GeneCombat.parries(wither, hit), "the first hit is parried");
		helper.assertFalse(GeneCombat.parries(wither, hit), "the next hit in the cooldown is not");
		wither.setAttached(ModAttachments.PARRY_TIME, helper.getLevel().getGameTime() - 120L);
		helper.assertTrue(GeneCombat.parries(wither, hit), "a hit after the cooldown is parried");
		helper.assertFalse(GeneCombat.parries(witherWith(helper, new BlockPos(5, 1, 5)), hit), "no gene, no parry");
		helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void cleaveHurtsThePlayersNearTheTarget(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_cleave");
		ServerPlayer target = helper.makeMockServerPlayerInLevel();
		ServerPlayer near = helper.makeMockServerPlayerInLevel();
		target.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		near.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		// A player that just joined cannot take damage for 60 ticks.
		helper.runAfterDelay(80, () -> {
			target.snapTo(helper.absoluteVec(new Vec3(3.5, 1.0, 1.5)));
			near.snapTo(helper.absoluteVec(new Vec3(4.5, 1.0, 1.5)));
			float health = near.getHealth();
			GeneCombat.onHit(wither, target);
			helper.assertTrue(near.getHealth() < health, "the near player takes damage");
			helper.succeed();
		});
	}

	@GameTest
	public void meleeArmorBreakActsOnlyOnMeleeHits(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_black_rot");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		GeneCombat.breakArmor(wither, player, 8.0F, true);
		helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 0, "a projectile hit");
		GeneCombat.breakArmor(wither, player, 8.0F, false);
		helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 2, "a melee hit of 8");
		helper.succeed();
	}

	@GameTest
	public void skullShotFiresAtAFarTarget(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_skull_shot");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.snapTo(helper.absoluteVec(new Vec3(1.5, 1.0, 7.5)));
		wither.setTarget(player);
		helper.assertTrue(wither.getTarget() == player, "the target is set");
		helper.assertTrue(wither.hasLineOfSight(player), "the wither skeleton sees the player");
		helper.assertTrue(GeneCombat.has(wither, io.github.stivengjekaj.originofspawns.definition.GeneEffect.SkullShot.class),
				"the gene loads");
		wither.tickCount = 20;
		GeneBehaviors.tick(wither, helper.getLevel());
		helper.assertValueEqual(skulls(helper), 1, "one skull at 6 blocks");
		GeneBehaviors.tick(wither, helper.getLevel());
		helper.assertValueEqual(skulls(helper), 1, "no second skull in the cooldown");
		helper.succeed();
	}

	@GameTest
	public void aVengefulSpiritAttacksTheKiller(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_spirit");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		GeneCombat.onDeath(wither, helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig));
		var vexes = helper.getLevel().getEntitiesOfClass(Vex.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0));
		helper.assertValueEqual(vexes.size(), 1, "one vex");
		helper.assertTrue(vexes.getFirst().getTarget() == pig, "the vex targets the killer");
		helper.succeed();
	}

	@GameTest
	public void soulHungerGrowsWithEachPlayerHit(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_soul_hunger");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		var hit = helper.getLevel().damageSources().mobAttack(wither);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "no player hit yet");
		GeneCombat.onHit(wither, helper.makeMockPlayer(GameType.SURVIVAL));
		GeneCombat.onHit(wither, helper.makeMockPlayer(GameType.SURVIVAL));
		helper.assertValueEqual(GeneCombat.recentPlayers(wither, 10.0F), 2, "two players hit");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 14.0F, "40% more damage");
		for (int i = 0; i < 3; i++) {
			GeneCombat.onHit(wither, helper.makeMockPlayer(GameType.SURVIVAL));
		}
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 16.0F, "60% at the most");
		helper.succeed();
	}

	@GameTest
	public void damageToATagHitsGolemsHarder(GameTestHelper helper) {
		WitherSkeleton wither = witherWith(helper, new BlockPos(1, 1, 1), "test_golem_bane");
		var golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(4, 1, 1));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 1, 4));
		var hit = helper.getLevel().damageSources().mobAttack(wither);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, hit, 10.0F), 10.0F, "a pig is not in the tag");
		helper.assertValueEqual(GeneCombat.modifyDamage(golem, hit, 10.0F), 15.0F, "50% more to a golem");
		helper.assertTrue(golem.isOnFire(), "the golem burns");
		helper.succeed();
	}

	@GameTest
	public void snowballsAndWaterHurtTheWeak(GameTestHelper helper) {
		WitherSkeleton weak = witherWith(helper, new BlockPos(1, 1, 1), "test_snow_weak");
		WitherSkeleton plain = witherWith(helper, new BlockPos(4, 1, 1));
		helper.assertValueEqual(GeneCombat.snowballDamage(weak, 0.0F), 3.0F, "a snowball does 3");
		helper.assertValueEqual(GeneCombat.snowballDamage(plain, 0.0F), 0.0F, "a snowball does nothing to a plain one");
		helper.assertTrue(weak.isSensitiveToWater(), "water hurts the weak one");
		helper.assertFalse(plain.isSensitiveToWater(), "water does not hurt a plain one");
		helper.succeed();
	}

	private static int skulls(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(WitherSkull.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0)).size();
	}

	static WitherSkeleton witherWith(GameTestHelper helper, BlockPos pos, String... genes) {
		WitherSkeleton wither = EntityTypes.WITHER_SKELETON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		wither.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		wither.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		wither.setNoAi(true);
		wither.setPersistenceRequired();
		wither.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(wither);
		return wither;
	}
}
