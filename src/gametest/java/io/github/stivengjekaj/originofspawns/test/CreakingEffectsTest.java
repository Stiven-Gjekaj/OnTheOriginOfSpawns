package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

/**
 * Checks the genes of a creaking: the pressures of its heart, its twitch while a player looks at it, its heart, its
 * wake, and the reused effects.
 */
public class CreakingEffectsTest {
	static final Identifier CREAKING = Identifier.withDefaultNamespace("creaking");

	/**
	 * A creaking heart at the place, and the creaking that it protects.
	 */
	record Bound(CreakingHeartBlockEntity heart, Creaking creaking) {
	}

	/**
	 * Puts an awake creaking heart at the heart place, and a creaking with the test genes that the heart protects at the
	 * creaking place.
	 */
	static Bound bound(GameTestHelper helper, BlockPos heartPos, BlockPos creakingPos, String... genes) {
		helper.setBlock(heartPos, Blocks.CREAKING_HEART.defaultBlockState()
				.setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE));
		CreakingHeartBlockEntity heart = (CreakingHeartBlockEntity) helper.getBlockEntity(heartPos, CreakingHeartBlockEntity.class);
		Creaking creaking = MountEffectsTest.mob(helper, EntityTypes.CREAKING, creakingPos, genes);
		creaking.setTransient(helper.absolutePos(heartPos));
		heart.setCreakingInfo(creaking);
		return new Bound(heart, creaking);
	}

	static float frequency(GameTestHelper helper, Creaking creaking, String gene) {
		return GenePools.get(helper.getLevel().getServer()).frequency(CREAKING,
				BiomeCategory.of(helper.getLevel().getBiome(creaking.blockPosition())),
				Identifier.fromNamespaceAndPath("originofspawns_test", gene));
	}

	@GameTest
	public void aBlastThatBreaksTheHeartKillsTheCreakingAndCountsForTheBlastPressure(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4));
		float before = frequency(helper, bound.creaking(), "test_heart_blast");
		float melee = frequency(helper, bound.creaking(), "test_heart_player");
		bound.heart().removeProtector(helper.getLevel().damageSources().explosion(null, null));
		helper.assertTrue(bound.creaking().isDeadOrDying(), "the creaking dies when its heart breaks");
		helper.assertTrue(frequency(helper, bound.creaking(), "test_heart_blast") > before, "the blast counts for the blast pressure");
		helper.assertValueEqual(frequency(helper, bound.creaking(), "test_heart_player"), melee, "a blast is not a hit of a player");
		helper.succeed();
	}

	@GameTest
	public void aPlayerThatBreaksTheHeartIsTheKillerOfTheCreaking(GameTestHelper helper) {
		Bound bound = bound(helper, new BlockPos(1, 1, 1), new BlockPos(4, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		// A test player stays in its test area, so that it does not stand in the way of the other tests.
		player.setNoGravity(true);
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 4))));
		float before = frequency(helper, bound.creaking(), "test_heart_player");
		bound.heart().removeProtector(player.damageSources().playerAttack(player));
		helper.assertTrue(bound.creaking().isDeadOrDying(), "the creaking dies when its heart breaks");
		helper.assertTrue(frequency(helper, bound.creaking(), "test_heart_player") > before,
				"the break counts as a hit of the player");
		helper.assertValueEqual(bound.creaking().getKillCredit(), player, "the player is the killer");
		helper.succeed();
	}
}
