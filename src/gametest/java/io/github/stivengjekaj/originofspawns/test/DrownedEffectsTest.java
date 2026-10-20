package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
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
