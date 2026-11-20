package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Checks the genes of a zombie horse: its jump and its landing, and the reused effects.
 */
public class ZombieHorseEffectsTest {
	/**
	 * Lets the horse land now from the jump that it started: the jump was some ticks ago, and the horse is on the
	 * ground.
	 */
	static void land(GameTestHelper helper, Mob horse) {
		horse.setAttached(ModAttachments.JUMP_TIME, helper.getLevel().getGameTime() - 5L);
		horse.setOnGround(true);
		GeneBehaviors.tick(horse, helper.getLevel());
	}

	@GameTest
	public void leapStrikeAndShieldVaultActWhenTheHorseLandsFromAJump(GameTestHelper helper) {
		var level = helper.getLevel();
		ZombieHorse horse = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE_HORSE, new BlockPos(4, 1, 4), "test_horse_landing");
		Mob zombie = MountEffectsTest.mob(helper, EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		zombie.startRiding(horse, true, false);
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		var player = CreeperEffectsTest.shieldedPlayer(helper);
		player.setPos(horse.position().add(1.5, 0.0, 0.0));
		horse.setOnGround(true);
		GeneBehaviors.tick(horse, level);
		helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth(), "no landing with no jump");
		horse.handleStartJump(90);
		helper.assertTrue(horse.hasAttached(ModAttachments.JUMP_TIME), "the jump of a player counts");
		land(helper, horse);
		helper.assertValueEqual(pig.getMaxHealth() - pig.getHealth(), 3.0F, "the landing does 3 damage to the pig near it");
		helper.assertValueEqual(zombie.getHealth(), zombie.getMaxHealth(), "the rider is safe");
		helper.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "the landing stops the shield near it");
		horse.jumpFromGround();
		helper.assertTrue(horse.hasAttached(ModAttachments.JUMP_TIME), "the jump of a climb counts");
		helper.succeed();
	}
}
