package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeDoorGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.Optional;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the vindicator genes use: the knockback, the villagers and the golems, the siege, and the
 * axe. It also tests that the reused effects work on a vindicator.
 */
public class VindicatorEffectsTest {
	@GameTest
	public void villageBaneHurtsVillagersAndTradersMore(GameTestHelper helper) {
		Vindicator bane = vindicatorWith(helper, new BlockPos(1, 1, 1), "test_village_bane");
		var hit = helper.getLevel().damageSources().mobAttack(bane);
		helper.assertValueEqual(damageTo(helper, EntityTypes.VILLAGER, hit), 8.0F, "a villager takes two times the damage");
		helper.assertValueEqual(damageTo(helper, EntityTypes.WANDERING_TRADER, hit), 8.0F, "a wandering trader too");
		helper.assertValueEqual(damageTo(helper, EntityTypes.IRON_GOLEM, hit), 4.0F, "a golem is not a villager");
		helper.succeed();
	}

	@GameTest
	public void golemSmasherHurtsIronAndSnowGolemsMore(GameTestHelper helper) {
		Vindicator smasher = vindicatorWith(helper, new BlockPos(1, 1, 1), "test_golem_smasher");
		var hit = helper.getLevel().damageSources().mobAttack(smasher);
		helper.assertValueEqual(damageTo(helper, EntityTypes.IRON_GOLEM, hit), 8.0F, "an iron golem takes two times the damage");
		helper.assertValueEqual(damageTo(helper, EntityTypes.SNOW_GOLEM, hit), 8.0F, "a snow golem too");
		helper.assertValueEqual(damageTo(helper, EntityTypes.VILLAGER, hit), 4.0F, "a villager is not a golem");
		helper.succeed();
	}

	@GameTest
	public void chopKnocksTheTargetBackFarther(GameTestHelper helper) {
		Vindicator plain = vindicatorWith(helper, new BlockPos(1, 1, 1));
		Vindicator chopper = vindicatorWith(helper, new BlockPos(1, 1, 4), "test_chop");
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		Pig shot = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		GeneCombat.onHit(plain, plainTarget);
		GeneCombat.onHit(chopper, target);
		GeneCombat.onProjectileHit(chopper, shot);
		helper.assertValueEqual(plainTarget.getDeltaMovement().x, 0.0, "a plain hit adds no push");
		helper.assertTrue(Math.abs(target.getDeltaMovement().x - 0.4) < 0.001, "a Chop hit adds one more push away, was "
				+ target.getDeltaMovement().x);
		helper.assertValueEqual(shot.getDeltaMovement().x, 0.0, "a projectile is not a melee hit");
		helper.succeed();
	}

	@GameTest
	public void shattererAlsoBreaksFences(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Vindicator vindicator = vindicatorWith(helper, pos);
		helper.setBlock(pos.east(), Blocks.OAK_FENCE);
		Pig pig = CreeperEffectsTest.pigAt(helper, pos.east(4));
		vindicator.setTarget(pig);
		var shatterable = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("originofspawns", "shatterable"));
		helper.assertFalse(new SiegeGoal(vindicator, new GeneEffect.Siege(3.0F)).canUse(), "a plain siege leaves a fence");
		helper.assertTrue(new SiegeGoal(vindicator, new GeneEffect.Siege(3.0F, Optional.of(shatterable))).canUse(),
				"a siege that also breaks fences finds the fence");
		helper.succeed();
	}

	@GameTest
	public void aVindicatorGetsNoRandomAxeEnchantmentButTheRaidSharpnessStays(GameTestHelper helper) {
		Vindicator vindicator = vindicatorWith(helper, new BlockPos(1, 1, 1));
		// On Hard, in a world that is old, with a full moon, the vanilla axe gets an enchantment on 1 spawn in 4.
		DifficultyInstance hard = new DifficultyInstance(Difficulty.HARD, 10_000_000L, 10_000_000L, 1.0F);
		for (int i = 0; i < 40; i++) {
			vindicator.finalizeSpawn(helper.getLevel(), hard, EntitySpawnReason.NATURAL, null);
			helper.assertTrue(vindicator.getMainHandItem().is(Items.IRON_AXE), "the vindicator keeps its axe");
			helper.assertFalse(vindicator.getMainHandItem().isEnchanted(), "the axe has no random enchantment");
		}
		// A raid with the strongest omen enchants the axe on 3 raiders in 4.
		Raid raid = new Raid(helper.absolutePos(new BlockPos(1, 1, 1)), Difficulty.HARD);
		raid.setRaidOmenLevel(5);
		vindicator.setCurrentRaid(raid);
		boolean sharpened = false;
		for (int i = 0; i < 20 && !sharpened; i++) {
			vindicator.applyRaidBuffs(helper.getLevel(), 1, false);
			sharpened = vindicator.getMainHandItem().getEnchantments().keySet().stream()
					.anyMatch(enchantment -> enchantment.is(Enchantments.SHARPNESS));
		}
		helper.assertTrue(sharpened, "the raid still gives the axe Sharpness");
		helper.succeed();
	}

	@GameTest
	public void aDoorSplitterBreaksADoorFaster(GameTestHelper helper) {
		helper.assertValueEqual(doorTicks(helper, new BlockPos(1, 1, 1), "test_siege_plain"), 240,
				"a plain siege breaks a door in 12 seconds, as in vanilla");
		helper.assertValueEqual(doorTicks(helper, new BlockPos(1, 1, 4), "test_door_splitter"), 40,
				"a Door Splitter breaks a door in 2 seconds");
		helper.succeed();
	}

	/**
	 * Gives a vindicator with the genes its gene goals, and returns the ticks of its door goal.
	 */
	static int doorTicks(GameTestHelper helper, BlockPos pos, String... genes) {
		Vindicator vindicator = vindicatorWith(helper, pos, genes);
		GeneBehaviors.installGoals(vindicator);
		return ((MobAccessor) vindicator).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(SiegeDoorGoal.class::isInstance).map(SiegeDoorGoal.class::cast)
				.findFirst().orElseThrow().breakTicks();
	}

	@GameTest
	public void aLooterKeepsWhatItPicksUpAndDropsItWhenItDies(GameTestHelper helper) {
		Vindicator plain = vindicatorWith(helper, new BlockPos(1, 1, 1));
		Vindicator looter = vindicatorWith(helper, new BlockPos(1, 1, 4), "test_looting");
		GeneBehaviors.onSpawn(looter, helper.getLevel(), EntitySpawnReason.NATURAL);
		ItemEntity plainLoot = breadAt(helper, plain);
		ItemEntity loot = breadAt(helper, looter);
		((MobAccessor) plain).originofspawns$pickUpItem(helper.getLevel(), plainLoot);
		((MobAccessor) looter).originofspawns$pickUpItem(helper.getLevel(), loot);
		helper.assertFalse(plainLoot.isRemoved(), "a plain vindicator does not pick up the bread");
		helper.assertTrue(loot.isRemoved(), "the looter takes the bread");
		helper.assertTrue(looter.getMainHandItem().is(Items.IRON_AXE), "the looter keeps its axe");
		looter.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 100.0F);
		helper.assertFalse(helper.getLevel().getEntitiesOfClass(ItemEntity.class, looter.getBoundingBox().inflate(2.0),
				item -> item.getItem().is(Items.BREAD)).isEmpty(), "the looter drops the bread when it dies");
		helper.succeed();
	}

	static ItemEntity breadAt(GameTestHelper helper, Vindicator vindicator) {
		ItemEntity item = new ItemEntity(helper.getLevel(), vindicator.getX(), vindicator.getY(), vindicator.getZ(),
				new ItemStack(Items.BREAD, 3));
		helper.getLevel().addFreshEntity(item);
		return item;
	}

	/**
	 * Returns the damage that a hit of 4 does to a new mob of the type, after the genes.
	 */
	static float damageTo(GameTestHelper helper, EntityType<? extends Mob> type,
			net.minecraft.world.damagesource.DamageSource hit) {
		Mob target = helper.spawnWithNoFreeWill(type, new BlockPos(4, 1, 1));
		float damage = GeneCombat.modifyDamage(target, hit, 4.0F);
		target.discard();
		return damage;
	}

	static Vindicator vindicatorWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Vindicator vindicator = EntityTypes.VINDICATOR.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		vindicator.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		vindicator.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
		vindicator.setNoAi(true);
		vindicator.setPersistenceRequired();
		vindicator.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(vindicator);
		return vindicator;
	}
}
