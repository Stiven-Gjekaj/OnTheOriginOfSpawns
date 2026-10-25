package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;
import io.github.stivengjekaj.originofspawns.mixin.AbstractCubeMobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the cube parts of the genes on a slime: the split, the hop, the landing, the growth, and the touch attack. It
 * also tests that the reused effects work on a slime at each size. Each cube stays inside the test area.
 */
public class SlimeEffectsTest {
	@GameTest(maxTicks = 60)
	public void thePiecesOfASplitKeepTheGenesOfTheSlime(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 4, "test_sticky_hit");
		CarriedGenes genes = slime.getAttached(ModAttachments.GENES);
		slime.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		// A dead mob leaves the level after its death time, and the split comes then.
		helper.succeedWhen(() -> {
			List<AbstractCubeMob> pieces = piecesNear(helper, slime);
			helper.assertTrue(pieces.size() >= 2 && pieces.size() <= 4, "the slime splits into 2 to 4 pieces, " + pieces.size());
			for (AbstractCubeMob piece : pieces) {
				helper.assertValueEqual(piece.getSize(), 2, "each piece has half the size");
				helper.assertTrue(genes.equals(piece.getAttached(ModAttachments.GENES)), "each piece has the genes of the slime");
			}
		});
	}

	@GameTest
	public void aTouchHitOfASlimeRunsItsHitGenes(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 2, "test_sticky_hit");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		((AbstractCubeMobAccessor) slime).originofspawns$dealDamage(pig);
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the touch hurts the pig");
		helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "the hit gives Slowness");
		helper.succeed();
	}

	@GameTest
	public void greatBlobSpawnsAtSizeFourWithItsChance(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 1, "test_great_blob");
		GeneBehaviors.onSpawn(slime, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertValueEqual(slime.getSize(), 4, "a chance of 1 gives size 4");
		var half = new GeneEffect.SpawnSize(4, 0.5F);
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.49F, 0.9F), 4, "a roll under the chance gives size 4");
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.5F, 0.0F), 1, "the other sizes share the rest");
		helper.assertValueEqual(GeneCubes.spawnSize(half, 0.5F, 0.99F), 2, "the other sizes share the rest");
		// Many rolls give the sizes in about the chances of the gene.
		var random = net.minecraft.util.RandomSource.create(1L);
		int[] count = new int[5];
		for (int i = 0; i < 1000; i++) {
			count[GeneCubes.spawnSize(half, random.nextFloat(), random.nextFloat())]++;
		}
		helper.assertTrue(count[4] > 430 && count[4] < 570, "about half the cubes are size 4, " + count[4]);
		helper.assertTrue(count[1] > 180 && count[2] > 180 && count[3] == 0, "sizes 1 and 2 share the rest");
		helper.succeed();
	}

	@GameTest
	public void theHopGeneChangesTheWaitTheDistanceAndTheHeight(GameTestHelper helper) {
		AbstractCubeMob plain = cubeWith(helper, EntityTypes.SLIME, new BlockPos(2, 1, 2), 2);
		AbstractCubeMob bouncer = cubeWith(helper, EntityTypes.SLIME, new BlockPos(5, 1, 5), 2, "test_bouncer");
		// The vanilla wait is 10 to 29 ticks. Two times the wait is 20 to 58 ticks, so the counts show the change.
		int plainShort = 0;
		int bouncerShort = 0;
		for (int i = 0; i < 100; i++) {
			plainShort += ((AbstractCubeMobAccessor) plain).originofspawns$jumpDelay() < 20 ? 1 : 0;
			bouncerShort += ((AbstractCubeMobAccessor) bouncer).originofspawns$jumpDelay() < 20 ? 1 : 0;
		}
		helper.assertTrue(plainShort > 20, "a plain slime often waits less than 20 ticks, " + plainShort);
		helper.assertValueEqual(bouncerShort, 0, "the bouncer never waits less than 20 ticks");
		plain.setDeltaMovement(Vec3.ZERO);
		bouncer.setDeltaMovement(Vec3.ZERO);
		plain.jumpFromGround();
		bouncer.jumpFromGround();
		helper.assertTrue(Math.abs(bouncer.getDeltaMovement().y - 2.0 * plain.getDeltaMovement().y) < 1.0E-4,
				"the bouncer hops two times as high");
		// In the air, the move control sets the speed of the hop.
		plain.setOnGround(false);
		bouncer.setOnGround(false);
		plain.getMoveControl().setWantedPosition(plain.getX() + 3.0, plain.getY(), plain.getZ(), 1.0);
		bouncer.getMoveControl().setWantedPosition(bouncer.getX() + 3.0, bouncer.getY(), bouncer.getZ(), 1.0);
		plain.getMoveControl().tick();
		bouncer.getMoveControl().tick();
		helper.assertTrue(Math.abs(bouncer.getSpeed() - 1.5F * plain.getSpeed()) < 1.0E-4F,
				"the bouncer moves 50% faster in the air, so it hops farther");
		helper.succeed();
	}

	// The players of the test take the normal damage of the difficulty normal.
	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void theLandingGenesHurtSlowAndDamageTheArmorOfPlayersNear(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 2, "test_slam");
		ServerPlayer near = CreeperEffectsTest.survivalPlayer(helper).player();
		near.setPos(slime.position().add(2.0, 0.0, 0.0));
		near.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		ServerPlayer far = CreeperEffectsTest.survivalPlayer(helper).player();
		far.setPos(slime.position().add(0.0, 0.0, -4.0));
		// The test lands the slime: it is in the air for one tick, and then on the ground.
		slime.setOnGround(false);
		slime.tick();
		helper.assertValueEqual(near.getHealth(), near.getMaxHealth(), "no effect while the slime is in the air");
		slime.setOnGround(true);
		slime.tick();
		helper.assertValueEqual(near.getHealth(), near.getMaxHealth() - 2.0F, "the landing does 2 damage");
		helper.assertTrue(near.hasEffect(MobEffects.SLOWNESS), "the landing gives Slowness");
		// The landing damage also wears the armor by 1, as each hit does.
		helper.assertValueEqual(near.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 6, "the landing damages the armor by 5");
		helper.assertValueEqual(far.getHealth(), far.getMaxHealth(), "a player 4 blocks away is safe");
		slime.tick();
		helper.assertValueEqual(near.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 6, "a slime on the ground does not land again");
		helper.succeed();
	}

	@GameTest
	public void shieldSlamStopsTheShieldOfThePlayerThatItLandsOn(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 1), 2, "test_shield_slam");
		slime.snapTo(player.position().add(-0.9, 0.0, 0.0), 0.0F, 0.0F);
		GeneCubes.onLand(slime, helper.getLevel());
		helper.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(Items.SHIELD)), "the shield stops for a time");
		helper.succeed();
	}

	@GameTest
	public void theSplitCountGenesChangeTheNumberOfPieces(GameTestHelper helper) {
		AbstractCubeMob plain = cubeWith(helper, EntityTypes.SLIME, new BlockPos(1, 1, 1), 2);
		AbstractCubeMob many = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 1), 2, "test_many_pieces");
		AbstractCubeMob splitter = cubeWith(helper, EntityTypes.SLIME, new BlockPos(1, 1, 5), 2, "test_splitter");
		// The split count is random, so the test asks each count many times and checks the range.
		java.util.Set<Integer> plainCounts = new java.util.TreeSet<>();
		java.util.Set<Integer> manyCounts = new java.util.TreeSet<>();
		java.util.Set<Integer> splitterCounts = new java.util.TreeSet<>();
		for (int i = 0; i < 100; i++) {
			plainCounts.add(((AbstractCubeMobAccessor) plain).originofspawns$splitCount());
			manyCounts.add(((AbstractCubeMobAccessor) many).originofspawns$splitCount());
			splitterCounts.add(((AbstractCubeMobAccessor) splitter).originofspawns$splitCount());
		}
		helper.assertValueEqual(plainCounts, java.util.Set.of(2, 3, 4), "a plain slime makes 2 to 4 pieces");
		helper.assertValueEqual(manyCounts, java.util.Set.of(4), "Many Pieces always makes 4");
		helper.assertValueEqual(splitterCounts, java.util.Set.of(4, 5, 6), "2 more pieces make 4 to 6");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void thePiecesGetTheEffectAndJumpApart(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 2, "test_quick_split");
		slime.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		helper.succeedWhen(() -> {
			List<AbstractCubeMob> pieces = piecesNear(helper, slime);
			helper.assertFalse(pieces.isEmpty(), "the slime splits");
			for (AbstractCubeMob piece : pieces) {
				helper.assertTrue(piece.hasEffect(MobEffects.SPEED), "each piece gets Speed");
				Vec3 away = piece.position().subtract(slime.position());
				Vec3 jump = piece.getDeltaMovement();
				helper.assertTrue(jump.y > 0.3, "each piece jumps up");
				helper.assertTrue(jump.x * away.x + jump.z * away.z > 0.0, "each piece jumps away from the slime");
			}
		});
	}

	@GameTest(maxTicks = 60)
	public void scatterShotSendsThePiecesTowardTheKiller(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(2, 1, 4), 2, "test_scatter_shot");
		Pig killer = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 4));
		slime.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(killer), 1000.0F);
		helper.succeedWhen(() -> {
			List<AbstractCubeMob> pieces = piecesNear(helper, slime);
			helper.assertFalse(pieces.isEmpty(), "the slime splits");
			for (AbstractCubeMob piece : pieces) {
				helper.assertTrue(piece.getDeltaMovement().x > 0.0, "each piece jumps toward the pig");
			}
		});
	}

	@GameTest(maxTicks = 60)
	public void gelTrapSlowsAndBurnsThePlayersNearASplit(GameTestHelper helper) {
		AbstractCubeMob slime = cubeWith(helper, EntityTypes.SLIME, new BlockPos(4, 1, 4), 2, "test_gel_trap");
		ServerPlayer near = CreeperEffectsTest.survivalPlayer(helper).player();
		near.setPos(slime.position().add(2.0, 0.0, 0.0));
		slime.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
		helper.assertFalse(near.hasEffect(MobEffects.SLOWNESS), "no effect before the split");
		helper.succeedWhen(() -> {
			helper.assertTrue(near.hasEffect(MobEffects.SLOWNESS), "the split gives Slowness");
			helper.assertTrue(near.getRemainingFireTicks() > 0, "the split sets the player on fire");
		});
	}

	@GameTest
	public void tinyTeethLetsASlimeOfSizeOneDoDamage(GameTestHelper helper) {
		AbstractCubeMob plain = cubeWith(helper, EntityTypes.SLIME, new BlockPos(2, 1, 2), 1);
		AbstractCubeMob teeth = cubeWith(helper, EntityTypes.SLIME, new BlockPos(5, 1, 5), 1, "test_tiny_teeth");
		// Only a cube with AI does damage. The test turns the AI on for the question, so the cubes do not move.
		plain.setNoAi(false);
		teeth.setNoAi(false);
		boolean plainDeals = ((AbstractCubeMobAccessor) plain).originofspawns$canDealDamage();
		boolean teethDeals = ((AbstractCubeMobAccessor) teeth).originofspawns$canDealDamage();
		plain.setNoAi(true);
		teeth.setNoAi(true);
		helper.assertFalse(plainDeals, "a plain slime of size 1 does no damage");
		helper.assertTrue(teethDeals, "a slime of size 1 with Tiny Teeth does damage");
		helper.succeed();
	}

	static List<AbstractCubeMob> piecesNear(GameTestHelper helper, AbstractCubeMob cube) {
		return helper.getLevel().getEntitiesOfClass(AbstractCubeMob.class,
				new AABB(cube.blockPosition()).inflate(4.0), other -> other != cube && other.isAlive());
	}

	/**
	 * Adds a cube of the size with the genes, with no AI. A cube with no AI does not hop, so it stays at its place.
	 */
	static AbstractCubeMob cubeWith(GameTestHelper helper, EntityType<? extends AbstractCubeMob> type, BlockPos pos, int size,
			String... genes) {
		AbstractCubeMob cube = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		cube.setSize(size, true);
		cube.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		cube.setNoAi(true);
		cube.setPersistenceRequired();
		cube.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(cube);
		return cube;
	}
}
