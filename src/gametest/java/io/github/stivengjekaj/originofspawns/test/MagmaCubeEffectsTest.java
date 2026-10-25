package io.github.stivengjekaj.originofspawns.test;

import static io.github.stivengjekaj.originofspawns.test.SlimeEffectsTest.cubeWith;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

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

	/**
	 * Adds a magma cube of size 1 in a water source. Glass on the four sides keeps the water in its place.
	 */
	static AbstractCubeMob cubeInWater(GameTestHelper helper, BlockPos pos, String... genes) {
		for (Direction side : Direction.Plane.HORIZONTAL) {
			helper.setBlock(pos.relative(side), Blocks.GLASS);
		}
		helper.setBlock(pos, Blocks.WATER);
		return cubeWith(helper, EntityTypes.MAGMA_CUBE, pos, 1, genes);
	}
}
