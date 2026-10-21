package io.github.stivengjekaj.originofspawns.test;

import java.util.Set;

import io.github.stivengjekaj.originofspawns.definition.DeathCondition;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import io.github.stivengjekaj.originofspawns.mixin.AbstractSkeletonAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.PowderSnowBlock;


import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the stray genes use: snow, ice, cold, and frost.
 */
public class StrayEffectsTest {
	@GameTest
	public void snowAndIceUnderTheMobAreStates(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.SNOW_BLOCK);
		helper.setBlock(new BlockPos(3, 0, 1), Blocks.PACKED_ICE);
		helper.setBlock(new BlockPos(5, 0, 1), Blocks.STONE);
		Stray onSnow = strayWith(helper, new BlockPos(1, 1, 1));
		Stray onIce = strayWith(helper, new BlockPos(3, 1, 1));
		Stray onStone = strayWith(helper, new BlockPos(5, 1, 1));
		helper.assertTrue(GeneCombat.test(onSnow, When.ON_SNOW), "a snow block counts as snow");
		helper.assertFalse(GeneCombat.test(onSnow, When.ON_ICE), "a snow block is not ice");
		helper.assertTrue(GeneCombat.test(onIce, When.ON_SNOW), "ice counts as snow and ice");
		helper.assertTrue(GeneCombat.test(onIce, When.ON_ICE), "packed ice is ice");
		helper.assertTrue(GeneCombat.test(onStone, When.OFF_SNOW), "stone is not snow");
		helper.succeed();
	}

	@GameTest
	public void aSnowLayerAtTheFeetCounts(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.SNOW);
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1));
		helper.assertTrue(GeneCombat.test(stray, When.ON_SNOW), "a snow layer at the feet");
		helper.succeed();
	}

	@GameTest
	public void snowAndColdFollowTheBiome(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(2, 1, 2));
		biome(helper, "minecraft:plains");
		BlockPos head = helper.absolutePos(new BlockPos(2, 2, 2));
		helper.assertFalse(GeneCombat.snowsAt(helper.getLevel(), head), "a storm in the plains is rain");
		helper.assertFalse(GeneCombat.test(stray, When.COLD_BIOME), "the plains are not cold");
		Set<DeathCondition> warm = Evolution.facts(stray, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(warm.contains(DeathCondition.WARM), "a death in the plains is warm");

		biome(helper, "minecraft:snowy_plains");
		helper.assertTrue(GeneCombat.snowsAt(helper.getLevel(), head), "a storm in the snowy plains is snow");
		helper.assertTrue(GeneCombat.test(stray, When.COLD_BIOME), "the snowy plains are cold");
		Set<DeathCondition> cold = Evolution.facts(stray, helper.getLevel().damageSources().generic(), helper.getLevel());
		helper.assertTrue(cold.contains(DeathCondition.COLD), "a death in the snowy plains is cold");
		helper.assertFalse(cold.contains(DeathCondition.WARM), "a death in the snowy plains is not warm");
		helper.assertValueEqual(cold.contains(DeathCondition.SNOW), helper.getLevel().isRaining(), "snow needs a storm");
		helper.assertValueEqual(GeneCombat.test(stray, When.NOT_SNOWING), !helper.getLevel().isRaining(),
				"no storm is no snow");
		helper.succeed();
	}

	@GameTest
	public void aFreezingTargetIsAState(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		stray.setTarget(pig);
		helper.assertFalse(GeneCombat.test(stray, When.TARGET_FROZEN), "a warm target");
		pig.setTicksFrozen(40);
		helper.assertTrue(GeneCombat.test(stray, When.TARGET_FROZEN), "a target that freezes");
		helper.succeed();
	}

	@GameTest
	public void frostArrowsFreezeTheTarget(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_frost_arrows");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 1));
		GeneCombat.onHit(stray, pig);
		helper.assertValueEqual(pig.getTicksFrozen(), 0, "a melee hit does not freeze");
		GeneCombat.onProjectileHit(stray, pig);
		helper.assertTrue(pig.isFullyFrozen(), "an arrow hit freezes the pig");
		helper.assertValueEqual(pig.getTicksFrozen(), pig.getTicksRequiredToFreeze() + 120, "full for 3 seconds");
		helper.succeed();
	}

	@GameTest
	public void leatherArmorStopsTheFreeze(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_frost_arrows");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
		GeneCombat.onProjectileHit(stray, player);
		helper.assertValueEqual(player.getTicksFrozen(), 0, "leather boots stop the freeze");
		helper.succeed();
	}

	@GameTest
	public void aMeleeHitOnRimeFreezesTheAttacker(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_rime_shell");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 1));
		GeneCombat.onHurt(stray, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertTrue(pig.isFullyFrozen(), "the attacker freezes");
		helper.succeed();
	}

	@GameTest
	public void aFrostWindFreezesThePlayersNear(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_frost_wind");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.snapTo(helper.absoluteVec(new Vec3(3.5, 1.0, 1.5)));
		stray.tickCount = 20;
		GeneBehaviors.tick(stray, helper.getLevel());
		helper.assertValueEqual(player.getTicksFrozen(), 60, "the freeze grows by the rate");
		for (int i = 0; i < 10; i++) {
			GeneBehaviors.tick(stray, helper.getLevel());
		}
		helper.assertValueEqual(player.getTicksFrozen(), player.getTicksRequiredToFreeze() + 40,
				"the freeze stops 1 second past full");
		helper.succeed();
	}

	@GameTest
	public void statesLimitTheRangeAndTheTranslucency(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.SNOW_BLOCK);
		helper.setBlock(new BlockPos(4, 0, 1), Blocks.STONE);
		Stray onSnow = strayWith(helper, new BlockPos(1, 1, 1), "test_snow_range", "test_snow_shade");
		Stray onStone = strayWith(helper, new BlockPos(4, 1, 1), "test_snow_range", "test_snow_shade");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 4));
		helper.assertValueEqual(GeneBehaviors.throwRangeMultiplier(onSnow, pig), 1.5F, "the range on snow");
		helper.assertValueEqual(GeneBehaviors.throwRangeMultiplier(onStone, pig), 1.0F, "the range on stone");
		helper.assertTrue(GeneEffects.translucentOpacity(onSnow).isPresent(), "see-through on snow");
		helper.assertTrue(GeneEffects.translucentOpacity(onStone).isEmpty(), "solid on stone");
		helper.succeed();
	}

	@GameTest
	public void anAllyAuraGoesToTheSameSpecies(GameTestHelper helper) {
		Stray leader = strayWith(helper, new BlockPos(1, 1, 1), "test_ally_speed");
		Stray ally = strayWith(helper, new BlockPos(3, 1, 1));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.snapTo(helper.absoluteVec(new Vec3(1.5, 1.0, 3.5)));
		leader.tickCount = 20;
		GeneBehaviors.tick(leader, helper.getLevel());
		helper.assertTrue(ally.hasEffect(MobEffects.SPEED), "the ally gets Speed");
		helper.assertFalse(player.hasEffect(MobEffects.SPEED), "the player does not");
		helper.succeed();
	}

	@GameTest
	public void aFanShotAddsArrowsOnceEachCooldown(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_fan_shot");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 1, 6));
		stray.performRangedAttack(pig, 1.0F);
		helper.assertValueEqual(arrows(helper), 3, "the first shot is a fan of 3");
		stray.performRangedAttack(pig, 1.0F);
		helper.assertValueEqual(arrows(helper), 4, "the next shot in the cooldown is 1 arrow");
		helper.succeed();
	}

	@GameTest
	public void galeArrowsPushTheTargetBack(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_gale");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 1, 4));
		pig.setDeltaMovement(Vec3.ZERO);
		GeneCombat.onProjectileHit(stray, pig);
		helper.assertTrue(pig.getDeltaMovement().z > 0.3, "the pig moves away from the stray: " + pig.getDeltaMovement());
		helper.succeed();
	}

	@GameTest
	public void snowshoesWalkOnPowderSnow(GameTestHelper helper) {
		Stray walker = strayWith(helper, new BlockPos(1, 1, 1), "test_snowshoe");
		Stray plain = strayWith(helper, new BlockPos(3, 1, 1));
		helper.assertTrue(PowderSnowBlock.canEntityWalkOnPowderSnow(walker), "the gene walks on powder snow");
		helper.assertFalse(PowderSnowBlock.canEntityWalkOnPowderSnow(plain), "a plain stray sinks");
		helper.succeed();
	}

	@GameTest
	public void arrowEffectsGoOnTheArrows(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_coldsnap");
		AbstractArrow arrow = ((AbstractSkeletonAccessor) stray).originofspawns$getArrow(new ItemStack(Items.ARROW), 1.0F,
				new ItemStack(Items.BOW));
		PotionContents contents = arrow.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS,
				PotionContents.EMPTY);
		helper.assertTrue(contents.customEffects().stream().anyMatch(effect -> effect.is(MobEffects.SLOWNESS)
				&& effect.getAmplifier() == 1 && effect.getDuration() == 1200), "a Slowness II of 60 seconds: " + contents);
		helper.succeed();
	}

	@GameTest
	public void rimefallLeavesPowderSnow(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_rimefall");
		GeneCombat.onDeath(stray, helper.getLevel());
		helper.assertBlockPresent(Blocks.POWDER_SNOW, new BlockPos(1, 1, 1));
		Stray blocked = strayWith(helper, new BlockPos(4, 1, 1), "test_rimefall");
		helper.setBlock(new BlockPos(4, 1, 1), Blocks.OAK_FENCE);
		GeneCombat.onDeath(blocked, helper.getLevel());
		helper.assertBlockPresent(Blocks.OAK_FENCE, new BlockPos(4, 1, 1));
		helper.succeed();
	}

	@GameTest
	public void brittleShotsBreakTheArmorOfAFrozenPlayer(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(1, 1, 1), "test_brittle");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		GeneCombat.breakArmor(stray, player, 8.0F);
		helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 0, "a warm player");
		player.setTicksFrozen(100);
		GeneCombat.breakArmor(stray, player, 8.0F);
		helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 2,
				"2 more damage for a hit of 8");
		helper.succeed();
	}

	@GameTest
	public void aPlayerThatLooksAtTheGazeIsSlowed(GameTestHelper helper) {
		Stray stray = strayWith(helper, new BlockPos(2, 1, 1), "test_gaze");
		ServerPlayer looker = helper.makeMockServerPlayerInLevel();
		looker.setGameMode(GameType.SURVIVAL);
		Vec3 eyes = helper.absoluteVec(new Vec3(2.5, 1.0, 6.5));
		// The player looks north, at the stray. The eye height of the player and the stray is near the same.
		looker.snapTo(eyes.x, eyes.y, eyes.z, 180.0F, 0.0F);
		looker.setYHeadRot(180.0F);
		ServerPlayer away = helper.makeMockServerPlayerInLevel();
		away.setGameMode(GameType.SURVIVAL);
		away.snapTo(eyes.x + 1.0, eyes.y, eyes.z, 0.0F, 0.0F);
		stray.tickCount = 20;
		GeneBehaviors.tick(stray, helper.getLevel());
		helper.assertTrue(looker.hasEffect(MobEffects.SLOWNESS), "the player that looks gets Slowness");
		helper.assertFalse(away.hasEffect(MobEffects.SLOWNESS), "the player that looks away does not");
		helper.succeed();
	}

	@GameTest
	public void hollowCryCallsAndMournerHeals(GameTestHelper helper) {
		Stray crier = strayWith(helper, new BlockPos(1, 1, 1), "test_hollow_cry");
		Stray mourner = strayWith(helper, new BlockPos(4, 1, 1), "test_mourner");
		Stray far = strayWith(helper, new BlockPos(7, 1, 7), "test_mourner");
		mourner.setHealth(10.0F);
		far.setHealth(10.0F);
		far.snapTo(far.position().add(0.0, 0.0, 20.0));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		GeneCombat.onDeath(crier, helper.getLevel(), helper.getLevel().damageSources().playerAttack(player));
		helper.assertTrue(mourner.getTarget() == player, "the ally targets the killer");
		helper.assertValueEqual(mourner.getHealth(), 14.0F, "the mourner heals 4");
		helper.assertValueEqual(far.getHealth(), 10.0F, "a mourner out of the radius does not heal");
		helper.succeed();
	}

	private static int arrows(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(AbstractArrow.class,
				new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0)).size();
	}

	private static void biome(GameTestHelper helper, String biome) {
		BlockPos from = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos to = helper.absolutePos(new BlockPos(5, 4, 5));
		helper.getLevel().getServer().getCommands().performPrefixedCommand(
				helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(),
				"fillbiome " + from.getX() + " " + from.getY() + " " + from.getZ() + " "
						+ to.getX() + " " + to.getY() + " " + to.getZ() + " " + biome);
	}

	static Stray strayWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Stray stray = EntityTypes.STRAY.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		stray.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		stray.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		stray.setNoAi(true);
		stray.setPersistenceRequired();
		stray.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(stray);
		return stray;
	}
}
