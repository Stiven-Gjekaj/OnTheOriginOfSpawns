package io.github.stivengjekaj.originofspawns.test;

import static io.github.stivengjekaj.originofspawns.test.SlimeEffectsTest.cubeWith;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
}
