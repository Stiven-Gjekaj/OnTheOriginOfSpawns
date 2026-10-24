package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneTeleports;
import io.github.stivengjekaj.originofspawns.mixin.EndermiteAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the endermite genes use: the swarm, the ender pearl, the life, and the call to the
 * endermen. It also tests that the teleports and the reused effects work on an endermite.
 */
public class EndermiteEffectsTest {
	@GameTest
	public void aSwarmSpawnsMoreEndermitesWithGenesFromThePool(GameTestHelper helper) {
		// The gene of the test is an enderman gene, so the pool of the endermite never gives it to the new endermites.
		Endermite mite = endermiteWith(helper, new BlockPos(4, 1, 4), "test_swarm_pool");
		GeneBehaviors.onSpawn(mite, helper.getLevel(), EntitySpawnReason.TRIGGERED);
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_swarm_pool");
		List<Endermite> extras = helper.getLevel().getEntitiesOfClass(Endermite.class, mite.getBoundingBox().inflate(3.0),
				other -> other != mite);
		helper.assertValueEqual(extras.size(), 2, "2 more endermites spawn");
		for (Endermite extra : extras) {
			CarriedGenes genes = extra.getAttached(ModAttachments.GENES);
			helper.assertFalse(genes != null && genes.genes().contains(gene), "a new endermite has genes from the pool");
		}
		helper.succeed();
	}

	@GameTest
	public void pearlBroodRaisesTheChanceOfAnEndermiteFromAPearl(GameTestHelper helper) {
		// Each endermite of the test is not in the level yet, as an endermite that a pearl may make.
		Endermite brood = loose(helper, new BlockPos(2, 1, 2), "test_pearl_brood");
		Endermite plain = loose(helper, new BlockPos(5, 1, 5));
		helper.assertFalse(GeneBehaviors.pearlSpawn(helper.getLevel(), plain, 0.0F), "a plain endermite keeps the vanilla chance");
		helper.assertTrue(GeneBehaviors.pearlSpawn(helper.getLevel(), brood, 0.99F), "a chance of 1 always makes the endermite");
		helper.assertTrue(helper.getLevel().getEntity(brood.getId()) == brood, "the endermite joins the level");
		helper.assertValueEqual(GeneBehaviors.pearlRoll(helper.getLevel(), plain, 0.01F), 0.01F,
				"a roll under 5% makes the vanilla endermite");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void theLifeGenesKeepTheEndermiteAlivePastTwoMinutes(GameTestHelper helper) {
		Endermite plain = aging(helper, new BlockPos(1, 1, 1), 2399);
		Endermite longLife = aging(helper, new BlockPos(4, 1, 1), 2399, "test_long_life");
		Endermite oldLongLife = aging(helper, new BlockPos(1, 1, 5), 4799, "test_long_life");
		Endermite ageless = aging(helper, new BlockPos(4, 1, 5), 1_000_000, "test_ageless");
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(plain.isRemoved(), "a plain endermite dies of age after 2 minutes");
			helper.assertFalse(longLife.isRemoved(), "a long life lasts past 2 minutes");
			helper.assertTrue(oldLongLife.isRemoved(), "a long life ends after 4 minutes");
			helper.assertFalse(ageless.isRemoved(), "an ageless endermite does not die of age");
			helper.succeed();
		});
	}

	@GameTest
	public void lifeDrainAddsTenSecondsOfLifeOnEachHit(GameTestHelper helper) {
		Endermite plain = endermiteWith(helper, new BlockPos(1, 1, 1));
		Endermite drain = endermiteWith(helper, new BlockPos(1, 1, 5), "test_life_drain");
		((EndermiteAccessor) plain).originofspawns$setLife(1000);
		((EndermiteAccessor) drain).originofspawns$setLife(1000);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 3));
		GeneCombat.onHit(plain, pig);
		GeneCombat.onHit(drain, pig);
		helper.assertValueEqual(((EndermiteAccessor) plain).originofspawns$life(), 1000, "a plain hit does not change the life");
		helper.assertValueEqual(((EndermiteAccessor) drain).originofspawns$life(), 800, "the hit gives 10 more seconds");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void oldKinLetsTwoEndermitesOutWhenItDiesOfAge(GameTestHelper helper) {
		Endermite kin = aging(helper, new BlockPos(4, 1, 4), 2399, "test_old_kin");
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(kin.isRemoved(), "the endermite dies of age");
			List<Endermite> young = helper.getLevel().getEntitiesOfClass(Endermite.class,
					new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(3.0));
			helper.assertValueEqual(young.size(), 2, "2 endermites come out");
			young.forEach(Endermite::discard);
			helper.succeed();
		});
	}

	@GameTest
	public void miteBlinkTeleportsAwayFromAProjectileOneTimeInEachCooldown(GameTestHelper helper) {
		// The range of the test gene is 3 blocks, so each place of the teleport is in the test area.
		Endermite mite = endermiteWith(helper, new BlockPos(4, 1, 4), "test_mite_blink");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(helper.getLevel(), pig, new ItemStack(Items.ARROW), null);
		DamageSource shot = helper.getLevel().damageSources().arrow(arrow, pig);
		Vec3 before = mite.position();
		GeneCombat.onHurt(mite, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertValueEqual(mite.position(), before, "a melee hit does not teleport");
		GeneCombat.onHurt(mite, shot);
		helper.assertTrue(mite.position().distanceTo(before) > 0.01, "the arrow makes the endermite teleport");
		Vec3 after = mite.position();
		GeneCombat.onHurt(mite, shot);
		helper.assertValueEqual(mite.position(), after, "the next arrow in the cooldown does not");
		helper.succeed();
	}

	@GameTest
	public void enderScentCallsOnlyTheEndermenWithNoTarget(GameTestHelper helper) {
		Endermite mite = endermiteWith(helper, new BlockPos(4, 1, 4), "test_ender_scent");
		Enderman free = EndermanEffectsTest.endermanWith(helper, new BlockPos(2, 1, 4));
		Enderman busy = EndermanEffectsTest.endermanWith(helper, new BlockPos(6, 1, 4));
		Pig other = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		busy.setTarget(other);
		Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
		GeneCombat.onHit(mite, player);
		helper.assertTrue(free.getTarget() == player, "a free enderman attacks the player");
		helper.assertTrue(busy.getTarget() == other, "a busy enderman keeps its target");
		helper.succeed();
	}

	@GameTest
	public void theEndermanTeleportsWorkOnAnEndermite(GameTestHelper helper) {
		Endermite mite = endermiteWith(helper, new BlockPos(1, 1, 1));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		mite.setTarget(pig);
		helper.assertTrue(GeneTeleports.toTarget(mite, helper.getLevel(),
				new GeneEffect.TeleportToTarget(8.0F, GeneEffect.When.ALWAYS, 3.0F)), "the endermite teleports to the pig");
		helper.assertTrue(mite.distanceTo(pig) < 3.0F, "the endermite is next to the pig");
		pig.setYRot(0.0F);
		helper.assertTrue(GeneTeleports.behind(mite, helper.getLevel(), pig), "the endermite teleports behind the pig");
		helper.assertTrue(mite.getZ() < pig.getZ(), "the endermite is behind the pig");
		helper.succeed();
	}

	@GameTest
	public void anEndermiteFromAnEventBurrowsNearAPlayer(GameTestHelper helper) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
				helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT);
			}
		}
		Endermite mite = endermiteWith(helper, new BlockPos(1, 3, 1), "test_stone_hide");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 3, 3))));
		double surface = mite.getY();
		// An ender pearl makes an endermite with the reason of an event.
		GeneBehaviors.onSpawn(mite, helper.getLevel(), EntitySpawnReason.TRIGGERED);
		helper.assertTrue(mite.getY() < surface, "the endermite goes under the ground");
		helper.succeed();
	}

	/**
	 * Adds an endermite that ages, as an endermite with no name that is not persistent.
	 */
	static Endermite aging(GameTestHelper helper, BlockPos pos, int age, String... genes) {
		Endermite mite = loose(helper, pos, genes);
		((EndermiteAccessor) mite).originofspawns$setLife(age);
		helper.getLevel().addFreshEntity(mite);
		return mite;
	}

	/**
	 * Makes an endermite with the genes at the place, but does not add it to the level.
	 */
	static Endermite loose(GameTestHelper helper, BlockPos pos, String... genes) {
		Endermite mite = EntityTypes.ENDERMITE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		mite.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		mite.setNoAi(true);
		mite.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		return mite;
	}

	static Endermite endermiteWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Endermite mite = EntityTypes.ENDERMITE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		mite.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		mite.setNoAi(true);
		mite.setPersistenceRequired();
		mite.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(mite);
		return mite;
	}
}
