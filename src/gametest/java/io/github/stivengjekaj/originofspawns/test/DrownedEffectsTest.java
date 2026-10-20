package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the drowned genes use: water, tridents, and the deep.
 */
public class DrownedEffectsTest {
	@GameTest(maxTicks = 40)
	public void landFollowsTheWater(GameTestHelper helper) {
		BlockPos wet = new BlockPos(1, 1, 1);
		helper.setBlock(wet, Blocks.WATER);
		helper.setBlock(wet.above(), Blocks.WATER);
		Drowned swimmer = drownedWith(helper, wet);
		Drowned walker = drownedWith(helper, new BlockPos(4, 1, 4));
		helper.runAfterDelay(5, () -> {
			helper.assertFalse(GeneCombat.test(swimmer, When.ON_LAND), "a drowned in water");
			helper.assertTrue(GeneCombat.test(walker, When.ON_LAND), "a drowned on land");
			helper.succeed();
		});
	}

	@GameTest
	public void seaLevelStatesFollowTheHeight(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1));
		int sea = helper.getLevel().getSeaLevel();
		drowned.setPos(drowned.getX(), sea - 10, drowned.getZ());
		helper.assertTrue(GeneCombat.test(drowned, When.DEEP), "10 blocks under the sea level");
		helper.assertFalse(GeneCombat.test(drowned, When.ABOVE_SEA_LEVEL), "under the sea level");
		drowned.setPos(drowned.getX(), sea - 9, drowned.getZ());
		helper.assertFalse(GeneCombat.test(drowned, When.DEEP), "9 blocks under the sea level");
		drowned.setPos(drowned.getX(), sea, drowned.getZ());
		helper.assertTrue(GeneCombat.test(drowned, When.ABOVE_SEA_LEVEL), "at the sea level");
		helper.succeed();
	}

	@GameTest
	public void stormFollowsTheWeather(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1));
		var level = helper.getLevel();
		float rain = level.getRainLevel(1.0F);
		float thunder = level.getThunderLevel(1.0F);
		level.setRainLevel(1.0F);
		level.setThunderLevel(1.0F);
		boolean storm = GeneCombat.test(drowned, When.STORM);
		level.setRainLevel(0.0F);
		level.setThunderLevel(0.0F);
		boolean clear = GeneCombat.test(drowned, When.STORM);
		level.setRainLevel(rain);
		level.setThunderLevel(thunder);
		helper.assertTrue(storm, "in a storm");
		helper.assertFalse(clear, "in clear weather");
		helper.succeed();
	}

	@GameTest(maxTicks = 160)
	public void waterAuraReachesOnlyPlayersInWater(GameTestHelper helper) {
		BlockPos wet = new BlockPos(3, 1, 1);
		helper.setBlock(wet, Blocks.WATER);
		helper.setBlock(wet.above(), Blocks.WATER);
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_water_aura");
		ServerPlayer swimmer = helper.makeMockServerPlayerInLevel();
		ServerPlayer walker = helper.makeMockServerPlayerInLevel();
		swimmer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		walker.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		helper.runAfterDelay(70, () -> {
			swimmer.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(wet)));
			walker.setPos(drowned.position().add(0.0, 0.0, 2.0));
			// A mock player has no connection, so the server does not tick it. One tick finds the water.
			swimmer.doTick();
			walker.doTick();
		});
		helper.runAfterDelay(120, () -> {
			helper.assertTrue(swimmer.hasEffect(MobEffects.SLOWNESS), "the player in water");
			helper.assertFalse(walker.hasEffect(MobEffects.SLOWNESS), "the player on land");
			helper.succeed();
		});
	}

	@GameTest
	public void deathEffectCanReachTheAllies(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_death_allies");
		Drowned ally = drownedWith(helper, new BlockPos(3, 1, 1));
		Zombie other = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 3));
		GeneCombat.onDeath(drowned, helper.getLevel());
		helper.assertTrue(ally.hasEffect(MobEffects.RESISTANCE), "the ally");
		helper.assertFalse(other.hasEffect(MobEffects.RESISTANCE), "a mob of a different species");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void glowingFollowsItsState(GameTestHelper helper) {
		BlockPos wet = new BlockPos(1, 1, 1);
		helper.setBlock(wet, Blocks.WATER);
		helper.setBlock(wet.above(), Blocks.WATER);
		Drowned swimmer = drownedWith(helper, wet, "test_wet_glow");
		Drowned walker = drownedWith(helper, new BlockPos(4, 1, 4), "test_wet_glow");
		GeneBehaviors.installGoals(walker);
		helper.assertFalse(walker.hasGlowingTag(), "a glow in a state does not start at once");
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(swimmer.hasGlowingTag(), "in water");
			helper.assertFalse(walker.hasGlowingTag(), "on land");
			helper.succeed();
		});
	}

	@GameTest
	public void translucentCanNeedItsBlocks(GameTestHelper helper) {
		BlockPos hidden = new BlockPos(1, 1, 1);
		helper.setBlock(hidden, Blocks.CARPET.asList().getFirst());
		Drowned inside = drownedWith(helper, hidden, "test_carpet_hide");
		Drowned outside = drownedWith(helper, new BlockPos(4, 1, 4), "test_carpet_hide");
		helper.assertValueEqual(GeneEffects.translucentOpacity(inside).orElse(1.0F), 0.3F, "in the blocks");
		helper.assertTrue(GeneEffects.translucentOpacity(outside).isEmpty(), "out of the blocks");
		helper.succeed();
	}

	@GameTest
	public void projectileHitsGiveTheirEffect(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_barbed");
		Pig hand = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 1));
		Pig spear = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 1));
		GeneCombat.onHit(drowned, hand);
		helper.assertFalse(hand.hasEffect(MobEffects.SLOWNESS), "a hit of the hand");
		ThrownTrident trident = new ThrownTrident(helper.getLevel(), drowned, new ItemStack(Items.TRIDENT));
		spear.hurtServer(helper.getLevel(), helper.getLevel().damageSources().trident(trident, drowned), 1.0F);
		helper.assertTrue(spear.hasEffect(MobEffects.SLOWNESS), "a hit of the trident");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void waterPullMovesOnlySwimmers(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1));
		ServerPlayer swimmer = swimmer(helper, new BlockPos(4, 1, 1));
		ServerPlayer walker = helper.makeMockServerPlayerInLevel();
		walker.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 4))));
		walker.doTick();
		swimmer.setDeltaMovement(Vec3.ZERO);
		walker.setDeltaMovement(Vec3.ZERO);
		GeneBehaviors.waterPull(drowned, helper.getLevel(), new GeneEffect.WaterPull(8.0F, 0.1F));
		helper.assertTrue(swimmer.getDeltaMovement().x < -0.05, "the swimmer moves to the drowned");
		helper.assertValueEqual(walker.getDeltaMovement(), Vec3.ZERO, "the player on land");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void undertowPullsTheSwimmerDown(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_undertow");
		ServerPlayer swimmer = swimmer(helper, new BlockPos(3, 3, 3));
		double before = swimmer.getY();
		GeneCombat.onHit(drowned, swimmer);
		helper.assertValueEqual(before - swimmer.getY(), 2.0, "the blocks down");
		GeneCombat.onHit(drowned, swimmer);
		helper.assertValueEqual(before - swimmer.getY(), 2.0, "the floor stops the pull");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void dragUnderHoldsUntilAHit(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_drag");
		ServerPlayer swimmer = swimmer(helper, new BlockPos(3, 2, 3));
		GeneCombat.onHit(drowned, swimmer);
		helper.assertValueEqual(drowned.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE).dragTarget(),
				swimmer.getId(), "the drowned holds the player");
		GeneCombat.onHurt(drowned, helper.getLevel().damageSources().playerAttack(swimmer));
		helper.assertValueEqual(drowned.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE).dragTarget(), -1,
				"the player breaks free");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void swimVerticalChangesTheSinkAndTheRise(GameTestHelper helper) {
		BlockPos wet = new BlockPos(1, 1, 1);
		fill(helper, wet, 2);
		Drowned drowned = drownedWith(helper, wet);
		helper.runAfterDelay(5, () -> {
			var swim = new GeneEffect.SwimVertical(3.0F, 0.5F);
			drowned.setDeltaMovement(0.0, 0.2, 0.0);
			GeneBehaviors.swimVertical(drowned, swim);
			helper.assertTrue(Math.abs(drowned.getDeltaMovement().y - 0.1) < 1.0E-6, "a slower rise");
			drowned.setDeltaMovement(0.0, -0.1, 0.0);
			GeneBehaviors.swimVertical(drowned, swim);
			helper.assertTrue(Math.abs(drowned.getDeltaMovement().y + 0.12) < 1.0E-6, "a faster sink");
			helper.succeed();
		});
	}

	@GameTest
	public void tridentChanceFillsOnlyAnEmptyHand(GameTestHelper helper) {
		Drowned empty = drownedWith(helper, new BlockPos(1, 1, 1), "test_trident_chance");
		Drowned holding = drownedWith(helper, new BlockPos(3, 1, 3), "test_trident_chance");
		empty.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		holding.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
		GeneBehaviors.onSpawn(empty, helper.getLevel(), EntitySpawnReason.NATURAL);
		GeneBehaviors.onSpawn(holding, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(empty.getMainHandItem().is(Items.TRIDENT), "a trident in the empty hand");
		helper.assertTrue(holding.getMainHandItem().is(Items.FISHING_ROD), "the rod stays");
		helper.succeed();
	}

	@GameTest
	public void throwRateShortensTheInterval(GameTestHelper helper) {
		Drowned volley = drownedWith(helper, new BlockPos(1, 1, 1), "test_volley");
		Drowned plain = drownedWith(helper, new BlockPos(3, 1, 3));
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(volley, 40), 20, "two times as often");
		helper.assertValueEqual(GeneBehaviors.scaleThrowInterval(plain, 40), 40, "no throw rate");
		helper.succeed();
	}

	@GameTest
	public void throwRangeFollowsTheTarget(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_spearfisher");
		Pig walker = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 1));
		Pig rower = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		var boat = helper.spawn(EntityTypes.OAK_BOAT, new BlockPos(3, 1, 3));
		rower.startRiding(boat);
		helper.assertValueEqual(GeneBehaviors.throwRangeMultiplier(drowned, walker), 0.7F, "a target on land");
		helper.assertValueEqual(GeneBehaviors.throwRangeMultiplier(drowned, rower), 1.3F, "a target in a boat");
		helper.succeed();
	}

	@GameTest
	public void scavengerKeepsAPlayerTrident(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_scavenger");
		drowned.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ThrownTrident trident = new ThrownTrident(helper.getLevel(), player, new ItemStack(Items.TRIDENT));
		trident.setPos(drowned.position().add(1.0, 0.0, 0.0));
		helper.getLevel().addFreshEntity(trident);
		helper.assertTrue(GeneBehaviors.scavengeTrident(drowned, helper.getLevel(),
				new GeneEffect.ScavengeTridents(2.0F)), "the drowned takes the trident");
		helper.assertTrue(drowned.getMainHandItem().is(Items.TRIDENT), "the trident in its hand");
		helper.assertTrue(trident.isRemoved(), "the thrown trident is gone");
		helper.assertValueEqual(drowned.getDropChances().byEquipment(EquipmentSlot.MAINHAND), 0.0F, "no drop");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void lightningFollowsTheTridentHit(GameTestHelper helper) {
		Drowned drowned = drownedWith(helper, new BlockPos(1, 1, 1), "test_storm_spear");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		ThrownTrident trident = new ThrownTrident(helper.getLevel(), drowned, new ItemStack(Items.TRIDENT));
		pig.hurtServer(helper.getLevel(), helper.getLevel().damageSources().trident(trident, drowned), 1.0F);
		helper.assertFalse(helper.getLevel().getEntitiesOfClass(LightningBolt.class, pig.getBoundingBox().inflate(2.0))
				.isEmpty(), "a lightning bolt at the pig");
		helper.succeed();
	}

	/**
	 * Fills a column of water from the position up.
	 */
	private static void fill(GameTestHelper helper, BlockPos bottom, int height) {
		for (int y = 0; y < height; y++) {
			helper.setBlock(bottom.above(y), Blocks.WATER);
		}
	}

	/**
	 * Makes a player in a column of water from the floor to the position and one block above it.
	 */
	private static ServerPlayer swimmer(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos.atY(0), Blocks.STONE);
		fill(helper, pos.atY(1), pos.getY() + 1);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(pos)));
		// A mock player has no connection, so the server does not tick it. One tick finds the water.
		player.doTick();
		return player;
	}

	/**
	 * Makes a drowned with test genes and no AI. The genes go on before the drowned enters the level, so it gets
	 * nothing from the pool.
	 */
	static Drowned drownedWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Drowned drowned = EntityTypes.DROWNED.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		drowned.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		drowned.setNoAi(true);
		drowned.setPersistenceRequired();
		drowned.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(drowned);
		return drowned;
	}
}
