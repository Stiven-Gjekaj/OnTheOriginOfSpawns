package io.github.stivengjekaj.originofspawns.test;

import static io.github.stivengjekaj.originofspawns.test.SlimeEffectsTest.cubeWith;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.DeathTrace;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.TemporaryBlocks;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.mixin.AbstractCubeMobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes that only a magma cube has: the fire, the lava, and the explosions. It also tests that the reused
 * effects work on a magma cube at each size. Each cube, fireball, and fire stays inside the test area.
 */
public class MagmaCubeEffectsTest {
	@GameTest
	public void ironPounceTakesHalfTheProjectileDamageInTheAir(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 2, "test_iron_pounce");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		Arrow arrow = new Arrow(helper.getLevel(), pig, new ItemStack(Items.ARROW), null);
		DamageSource shot = helper.getLevel().damageSources().arrow(arrow, pig);
		DamageSource melee = helper.getLevel().damageSources().mobAttack(pig);
		cube.setOnGround(true);
		helper.assertValueEqual(GeneCombat.modifyDamage(cube, shot, 4.0F), 4.0F, "full damage on the ground");
		cube.setOnGround(false);
		helper.assertValueEqual(GeneCombat.modifyDamage(cube, shot, 4.0F), 2.0F, "half the damage in the air");
		helper.assertValueEqual(GeneCombat.modifyDamage(cube, melee, 4.0F), 4.0F, "a melee hit in the air does full damage");
		helper.succeed();
	}
	@GameTest(maxTicks = 40)
	public void waterHurtsAMagmaCubeWithTheGeneUntilItHasFireResistance(GameTestHelper helper) {
		AbstractCubeMob wet = cubeInWater(helper, new BlockPos(1, 1, 1), "test_flame_touch_water");
		AbstractCubeMob resists = cubeInWater(helper, new BlockPos(4, 1, 1), "test_flame_touch_water");
		AbstractCubeMob steam = cubeInWater(helper, new BlockPos(1, 1, 5), "test_flame_touch_water", "test_steam_body");
		AbstractCubeMob plain = cubeInWater(helper, new BlockPos(4, 1, 5));
		resists.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600));
		// The cubes find the water on their first tick.
		helper.runAfterDelay(2, () -> {
			for (AbstractCubeMob cube : List.of(wet, resists, steam, plain)) {
				helper.assertTrue(cube.isInWater(), "each cube is in water");
				cube.tickCount = 40;
				GeneEffects.tick(cube, helper.getLevel());
			}
			helper.assertTrue(wet.getHealth() < wet.getMaxHealth(), "water hurts the magma cube with the gene");
			helper.assertValueEqual(resists.getHealth(), resists.getMaxHealth(), "Fire Resistance stops the water damage");
			helper.assertValueEqual(steam.getHealth(), steam.getMaxHealth(), "Steam Body stops the water damage");
			helper.assertValueEqual(plain.getHealth(), plain.getMaxHealth(), "water does not hurt a plain magma cube");
			helper.succeed();
		});
	}

	@GameTest
	public void magmaBloodBurnsAnAttackerInMelee(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 2, "test_magma_blood");
		Pig archer = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		Pig fighter = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		GeneCombat.onHurt(cube, helper.getLevel().damageSources().arrow(
				new Arrow(helper.getLevel(), archer, new ItemStack(Items.ARROW), null), archer));
		helper.assertValueEqual(archer.getRemainingFireTicks(), 0, "an arrow does not burn the archer");
		GeneCombat.onHurt(cube, helper.getLevel().damageSources().mobAttack(fighter));
		helper.assertValueEqual(fighter.getRemainingFireTicks(), 60, "a melee hit burns the attacker for 3 seconds");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void blazeHeartBurnsTheTargetForTenSecondsAfterLava(GameTestHelper helper) {
		AbstractCubeMob dry = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(5, 1, 5), 1, "test_blaze_heart");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		GeneCombat.onHit(dry, pig);
		helper.assertValueEqual(pig.getRemainingFireTicks(), 0, "a cube that was not in lava does not burn the pig");
		AbstractCubeMob bathed = cubeInFluid(helper, new BlockPos(1, 1, 1), Blocks.LAVA, "test_blaze_heart");
		helper.runAfterDelay(2, () -> {
			long now = helper.getLevel().getGameTime();
			helper.assertValueEqual(bathed.getAttached(ModAttachments.LAVA_TIME), now, "the cube keeps the time in lava");
			dry.setAttached(ModAttachments.LAVA_TIME, now - 200L);
			GeneCombat.onHit(dry, pig);
			helper.assertValueEqual(pig.getRemainingFireTicks(), 160, "10 seconds after lava, the hit burns for 8 seconds");
			pig.clearFire();
			dry.setAttached(ModAttachments.LAVA_TIME, now - 201L);
			GeneCombat.onHit(dry, pig);
			helper.assertValueEqual(pig.getRemainingFireTicks(), 0, "after 10 seconds, the hit does not burn");
			helper.succeed();
		});
	}

	@GameTest
	public void heatWaveSetsThePlayersNearOnFire(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(2, 1, 4), 2, "test_heat_wave");
		ServerPlayer near = CreeperEffectsTest.survivalPlayer(helper).player();
		near.setPos(cube.position().add(2.0, 0.0, 0.0));
		ServerPlayer far = CreeperEffectsTest.survivalPlayer(helper).player();
		far.setPos(cube.position().add(4.0, 0.0, 0.0));
		helper.assertValueEqual(GeneBehaviors.fireAura(cube, helper.getLevel(),
				new GeneEffect.FireAura(2.0F, 2.0F, 5.0F)), 1, "one player is in 2 blocks");
		helper.assertValueEqual(near.getRemainingFireTicks(), 40, "the near player burns for 2 seconds");
		helper.assertValueEqual(far.getRemainingFireTicks(), 0, "the far player does not burn");
		helper.succeed();
	}

	@GameTest
	public void spitFireShootsASmallFireballAtAFarTargetOneTimeInEachCooldown(GameTestHelper helper) {
		// The test gene has a minimum range of 4 blocks, so the target and the fireball stay in the test area.
		var shot = new GeneEffect.FireballShot(10.0F, 4.0F, 16.0F);
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(1, 1, 4), 2, "test_spit_fire");
		Pig near = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		cube.setTarget(near);
		helper.assertFalse(GeneBehaviors.fireballShot(cube, helper.getLevel(), shot), "no shot at a near target");
		Pig far = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		cube.setTarget(far);
		helper.assertTrue(GeneBehaviors.fireballShot(cube, helper.getLevel(), shot), "a shot at a far target");
		List<SmallFireball> fireballs = helper.getLevel().getEntitiesOfClass(SmallFireball.class,
				cube.getBoundingBox().inflate(3.0));
		helper.assertValueEqual(fireballs.size(), 1, "one small fireball");
		SmallFireball fireball = fireballs.get(0);
		helper.assertTrue(fireball.getOwner() == cube, "the cube shot the fireball");
		Vec3 toFar = far.position().subtract(cube.position()).normalize();
		helper.assertTrue(fireball.getDeltaMovement().normalize().dot(toFar) > 0.9, "the fireball flies at the target");
		fireball.discard();
		helper.assertFalse(GeneBehaviors.fireballShot(cube, helper.getLevel(), shot), "the next shot waits for the cooldown");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void blastCoreHurtsAndBurnsThePlayersNearItsDeath(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 2, "test_blast_core");
		ServerPlayer near = CreeperEffectsTest.survivalPlayer(helper).player();
		near.setPos(cube.position().add(2.0, 0.0, 0.0));
		float health = near.getHealth();
		GeneCombat.onDeath(cube, helper.getLevel());
		helper.assertTrue(near.getHealth() < health, "the burst hurts the player");
		helper.assertValueEqual(near.getRemainingFireTicks(), 100, "the burst sets the player on fire for 5 seconds");
		helper.succeed();
	}

	@GameTest
	public void moltenCoreHealsTheCubeForHalfTheDamageOfAnExplosion(GameTestHelper helper) {
		AbstractCubeMob plain = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(1, 1, 1), 4);
		AbstractCubeMob molten = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(5, 1, 5), 4, "test_molten_core");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 6));
		DamageSource blast = helper.getLevel().damageSources().explosion(pig, pig);
		for (AbstractCubeMob cube : List.of(plain, molten)) {
			cube.setHealth(cube.getMaxHealth() - 8.0F);
			cube.hurtServer(helper.getLevel(), blast, 4.0F);
		}
		helper.assertTrue(plain.getHealth() < plain.getMaxHealth() - 8.0F, "an explosion hurts a plain magma cube");
		helper.assertValueEqual(molten.getHealth(), molten.getMaxHealth() - 6.0F, "the explosion heals 2 health");
		molten.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 4.0F);
		helper.assertTrue(molten.getHealth() < molten.getMaxHealth() - 6.0F, "a melee hit still hurts it");
		helper.succeed();
	}

	@GameTest
	public void emberTrailLeavesFireThatDoesNotSpread(GameTestHelper helper) {
		var trail = new GeneEffect.FireTrail(3.0F, 4.0F);
		// The fire needs a solid block under it.
		helper.setBlock(new BlockPos(5, 0, 5), Blocks.STONE);
		helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(5, 1, 5), 1, "test_ember_trail");
		cube.setOnGround(true);
		helper.assertTrue(GeneBehaviors.fireTrail(cube, helper.getLevel(), trail), "the cube leaves fire");
		BlockPos geneFire = new BlockPos(5, 1, 5);
		helper.assertBlockPresent(Blocks.FIRE, geneFire);
		helper.assertFalse(GeneBehaviors.fireTrail(cube, helper.getLevel(), trail), "the next fire waits 3 seconds");
		// A plain fire with hay next to it shows that the fire of the test world can spread.
		BlockPos plainFire = new BlockPos(1, 1, 1);
		helper.setBlock(plainFire, Blocks.FIRE);
		cube.discard();
		for (BlockPos fire : List.of(plainFire, geneFire)) {
			for (Direction side : Direction.Plane.HORIZONTAL) {
				helper.setBlock(fire.relative(side), Blocks.HAY_BLOCK);
			}
		}
		var rules = helper.getLevel().getGameRules();
		int radius = rules.get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER);
		rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, -1, helper.getLevel().getServer());
		for (int i = 0; i < 60; i++) {
			for (BlockPos fire : List.of(plainFire, geneFire)) {
				BlockPos pos = helper.absolutePos(fire);
				BlockState state = helper.getLevel().getBlockState(pos);
				if (state.is(Blocks.FIRE)) {
					state.tick(helper.getLevel(), pos, helper.getLevel().getRandom());
				}
			}
		}
		rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, radius, helper.getLevel().getServer());
		helper.assertFalse(Direction.Plane.HORIZONTAL.stream().allMatch(side -> helper.getBlockState(plainFire.relative(side))
				.is(Blocks.HAY_BLOCK)), "a plain fire burns the hay");
		helper.assertTrue(Direction.Plane.HORIZONTAL.stream().allMatch(side -> helper.getBlockState(geneFire.relative(side))
				.is(Blocks.HAY_BLOCK)), "the gene fire does not burn the hay");
		helper.assertBlockPresent(Blocks.FIRE, geneFire);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void emberTrailObeysMobGriefing(GameTestHelper helper) {
		helper.setBlock(new BlockPos(4, 0, 4), Blocks.STONE);
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 1, "test_ember_trail");
		cube.setOnGround(true);
		helper.assertFalse(GeneBehaviors.fireTrail(cube, helper.getLevel(), new GeneEffect.FireTrail(3.0F, 4.0F)),
				"no fire with mobGriefing off");
		helper.assertBlockNotPresent(Blocks.FIRE, new BlockPos(4, 1, 4));
		helper.succeed();
	}

	@GameTest
	public void lavaPoolLeavesLavaThatDoesNotFlow(GameTestHelper helper) {
		// The lava needs a solid block under it, else it flows down.
		BlockPos genePool = new BlockPos(5, 1, 5);
		BlockPos plainPool = new BlockPos(1, 1, 1);
		helper.setBlock(genePool.below(), Blocks.STONE);
		helper.setBlock(plainPool.below(), Blocks.STONE);
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, genePool, 1, "test_lava_pool");
		GeneCombat.onDeath(cube, helper.getLevel());
		cube.discard();
		helper.assertBlockPresent(Blocks.LAVA, genePool);
		helper.assertTrue(TemporaryBlocks.isTemporary(helper.getLevel(), helper.absolutePos(genePool)),
				"the lava goes away at its time");
		// A plain lava source shows that the lava of the test world can flow.
		helper.setBlock(plainPool, Blocks.LAVA);
		for (int i = 0; i < 4; i++) {
			for (BlockPos pool : List.of(plainPool, genePool)) {
				for (BlockPos pos : BlockPos.betweenClosed(pool.offset(-1, 0, -1), pool.offset(1, 0, 1))) {
					BlockPos at = helper.absolutePos(pos);
					FluidState fluid = helper.getLevel().getFluidState(at);
					if (!fluid.isEmpty()) {
						fluid.tick(helper.getLevel(), at, helper.getLevel().getBlockState(at));
					}
				}
			}
		}
		helper.assertTrue(Direction.Plane.HORIZONTAL.stream().anyMatch(side -> helper.getBlockState(plainPool.relative(side))
				.is(Blocks.LAVA)), "a plain lava source flows");
		helper.assertTrue(Direction.Plane.HORIZONTAL.stream().allMatch(side -> helper.getBlockState(genePool.relative(side))
				.isAir()), "the lava of the gene does not flow");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void lavaPoolObeysMobGriefing(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 1, "test_lava_pool");
		GeneCombat.onDeath(cube, helper.getLevel());
		helper.assertBlockNotPresent(Blocks.LAVA, new BlockPos(4, 1, 4));
		helper.succeed();
	}

	@GameTest
	public void flameTouchSetsTheTargetOnFireAtEachSizeThatHits(GameTestHelper helper) {
		for (int size : new int[] {1, 2, 4}) {
			AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), size, "test_flame_touch");
			Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
			((AbstractCubeMobAccessor) cube).originofspawns$dealDamage(pig);
			helper.assertValueEqual(pig.getRemainingFireTicks(), 100, "the hit burns the pig for 5 seconds at size " + size);
			pig.discard();
			cube.discard();
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void basaltSkinAddsToTheArmorOfEachSize(GameTestHelper helper) {
		AbstractCubeMob plain = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(1, 1, 1), 1);
		AbstractCubeMob skin = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(5, 1, 5), 1, "test_basalt_skin");
		// The game puts the attribute genes on a new mob when it rolls the genes. The test cube gets its genes direct.
		GeneEffects.applyAttributes(skin, GeneEffects.sources(helper.getLevel().registryAccess(),
				skin.getAttached(ModAttachments.GENES)));
		for (int size : SlimeEffectsTest.SIZES) {
			plain.setSize(size, true);
			skin.setSize(size, true);
			helper.assertValueEqual(plain.getArmorValue(), 3 * size, "a plain magma cube has 3 armor for each size");
			helper.assertValueEqual(skin.getArmorValue(), 3 * size + 4, "Basalt Skin adds 4 armor at size " + size);
		}
		plain.discard();
		skin.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		// A dead mob leaves the level after its death time, and the split comes then.
		helper.succeedWhen(() -> {
			List<AbstractCubeMob> pieces = SlimeEffectsTest.piecesNear(helper, skin);
			helper.assertFalse(pieces.isEmpty(), "the cube splits");
			for (AbstractCubeMob piece : pieces) {
				helper.assertValueEqual(piece.getArmorValue(), 3 * 2 + 4, "each piece of size 2 has the extra armor");
			}
		});
	}

	@GameTest(maxTicks = 40)
	public void crustAddsArmorWhileTheCubeIsWoundedAtEachSize(GameTestHelper helper) {
		AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(4, 1, 4), 1, "test_crust");
		int[] index = {0};
		helper.onEachTick(() -> {
			if (index[0] < SlimeEffectsTest.SIZES.length && cube.getSize() != SlimeEffectsTest.SIZES[index[0]]) {
				cube.setSize(SlimeEffectsTest.SIZES[index[0]], true);
			}
			cube.setHealth(cube.getMaxHealth() * 0.3F);
		});
		helper.succeedWhen(() -> {
			helper.assertValueEqual(cube.getArmorValue(), 3 * cube.getSize() + 6,
					"a wounded cube has 6 more armor at size " + cube.getSize());
			if (++index[0] < SlimeEffectsTest.SIZES.length) {
				helper.fail("the next size");
			}
		});
	}

	@GameTest
	public void lavaLeapLeapsAtATargetAtEachSize(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 4));
		for (int size : SlimeEffectsTest.SIZES) {
			AbstractCubeMob cube = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(1, 1, 4), size);
			cube.setTarget(pig);
			cube.setOnGround(true);
			LeapGoal goal = new LeapGoal(cube, new GeneEffect.Leap(4.0F, 10.0F, 5.0F, 1.0F));
			helper.assertTrue(goal.canUse(), "the cube can leap at the pig at size " + size);
			goal.start();
			helper.assertTrue(cube.getDeltaMovement().x > 0.5 && cube.getDeltaMovement().y > 0.0,
					"the cube leaps at the pig at size " + size);
			cube.discard();
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void lavaHealAndFireDrinkHealTheCubeAlsoInCombat(GameTestHelper helper) {
		AbstractCubeMob inLava = cubeInFluid(helper, new BlockPos(1, 1, 1), Blocks.LAVA, "test_lava_heal");
		AbstractCubeMob dry = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(1, 1, 5), 1, "test_lava_heal");
		helper.setBlock(new BlockPos(5, 0, 5), Blocks.STONE);
		helper.setBlock(new BlockPos(5, 1, 5), Blocks.FIRE);
		AbstractCubeMob inFire = cubeWith(helper, EntityTypes.MAGMA_CUBE, new BlockPos(5, 1, 5), 1, "test_fire_drink");
		long now = helper.getLevel().getGameTime();
		for (AbstractCubeMob cube : List.of(inLava, dry, inFire)) {
			cube.setHealth(0.5F);
			// The cubes are in combat now.
			cube.setAttached(ModAttachments.TRACE, DeathTrace.NONE.withCombat(now));
		}
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(inLava.getHealth() > 0.5F, "Lava Heal heals the cube in lava in combat");
			helper.assertTrue(inFire.getHealth() > 0.5F, "Fire Drink heals the cube in fire in combat");
			helper.assertValueEqual(dry.getHealth(), 0.5F, "Lava Heal does not heal out of lava");
			helper.succeed();
		});
	}

	@GameTest
	public void theHurtAndDeathGenesOfAMagmaCubeWorkAtEachSize(GameTestHelper helper) {
		SlimeEffectsTest.hurtAndDeathGenesWorkAtEachSize(helper, EntityTypes.MAGMA_CUBE);
	}

	@GameTest
	public void theDamageGenesOfAMagmaCubeWorkAtEachSize(GameTestHelper helper) {
		SlimeEffectsTest.damageGenesWorkAtEachSize(helper, EntityTypes.MAGMA_CUBE);
	}

	@GameTest(maxTicks = 40)
	public void speedWhenWorksOnAMagmaCubeAfterEachChangeOfSize(GameTestHelper helper) {
		SlimeEffectsTest.speedWhenWorksAfterEachChangeOfSize(helper, EntityTypes.MAGMA_CUBE);
	}

	static AbstractCubeMob cubeInWater(GameTestHelper helper, BlockPos pos, String... genes) {
		return cubeInFluid(helper, pos, Blocks.WATER, genes);
	}

	/**
	 * Adds a magma cube of size 1 in a source of the fluid. Glass on the four sides keeps the fluid in its place.
	 */
	static AbstractCubeMob cubeInFluid(GameTestHelper helper, BlockPos pos, Block fluid, String... genes) {
		for (Direction side : Direction.Plane.HORIZONTAL) {
			helper.setBlock(pos.relative(side), Blocks.GLASS);
		}
		helper.setBlock(pos, fluid);
		return cubeWith(helper, EntityTypes.MAGMA_CUBE, pos, 1, genes);
	}
}
