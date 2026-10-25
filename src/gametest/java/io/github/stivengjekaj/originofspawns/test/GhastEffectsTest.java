package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.ProjectileAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a ghast: its fireballs, its flight, and the reused effects.
 */
public class GhastEffectsTest {
	/**
	 * The place where the test fireballs explode. The ghast floats in the far corner, out of the reach of the blast.
	 */
	static final BlockPos BLAST = new BlockPos(1, 2, 1);

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void greatBlastMakesTheBlastOfTheFireballLarger(GameTestHelper helper) {
		// A blast of power 1 hurts to 2 blocks, and a blast of power 2 hurts to 4 blocks. The pig is 3 blocks away.
		Ghast plain = ghastWith(helper, new BlockPos(5, 1, 5));
		Pig plainPig = CreeperEffectsTest.pigAt(helper, BLAST.east(3));
		explodeFireball(helper, plain, BLAST);
		helper.assertValueEqual(plainPig.getHealth(), plainPig.getMaxHealth(), "the plain blast does not reach 3 blocks");
		plainPig.discard();
		Ghast bomber = ghastWith(helper, new BlockPos(5, 1, 5), "test_great_blast");
		plain.discard();
		Pig pig = CreeperEffectsTest.pigAt(helper, BLAST.east(3));
		explodeFireball(helper, bomber, BLAST);
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the larger blast reaches 3 blocks");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void theBlastEffectsActOnTheBlastOfAFireball(GameTestHelper helper) {
		Ghast ghast = ghastWith(helper, new BlockPos(5, 1, 5), "test_concussion");
		Pig pig = CreeperEffectsTest.pigAt(helper, BLAST.east(1));
		explodeFireball(helper, ghast, BLAST);
		helper.assertTrue(pig.hasEffect(MobEffects.NAUSEA), "the blast of the fireball gives Nausea");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void aFireballThatAPlayerHitsBackHasNoGenes(GameTestHelper helper) {
		Ghast ghast = ghastWith(helper, new BlockPos(5, 1, 5), "test_concussion");
		Pig pig = CreeperEffectsTest.pigAt(helper, BLAST.east(1));
		LargeFireball fireball = fireball(helper, ghast, BLAST);
		fireball.setOwner(CreeperEffectsTest.survivalPlayer(helper).player());
		((ProjectileAccessor) fireball).originofspawns$onHit(hitAt(fireball));
		helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the blast hurts the pig");
		helper.assertFalse(pig.hasEffect(MobEffects.NAUSEA), "the blast of a fireball that came back gives no effect");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void shieldBurstStopsTheShieldsInTheBlast(GameTestHelper helper) {
		ServerPlayer plainTarget = shieldedPlayerAt(helper, BLAST.east(1));
		explodeFireball(helper, ghastWith(helper, new BlockPos(5, 1, 5)), BLAST);
		helper.assertFalse(plainTarget.getCooldowns().isOnCooldown(plainTarget.getOffhandItem()),
				"a plain blast does not stop the shield");
		ServerPlayer target = shieldedPlayerAt(helper, BLAST.east(1));
		explodeFireball(helper, ghastWith(helper, new BlockPos(5, 1, 5), "test_shield_burst"), BLAST);
		helper.assertTrue(target.getCooldowns().isOnCooldown(target.getOffhandItem()), "the blast stops the shield");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void blastCryReachesThePlayersInItsRadius(GameTestHelper helper) {
		// The blast of power 1 hurts to 2 blocks. The radius of the effect is 5 blocks, and the player is 4 blocks away.
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(BLAST.east(4))));
		Pig pig = CreeperEffectsTest.pigAt(helper, BLAST.south(1));
		explodeFireball(helper, ghastWith(helper, new BlockPos(5, 1, 5), "test_blast_cry"), BLAST);
		helper.assertValueEqual(player.getHealth(), player.getMaxHealth(), "the blast does not hurt the player");
		helper.assertValueEqual(player.getEffect(MobEffects.SLOWNESS).getDuration(), 60, "Slowness for 3 seconds");
		helper.assertFalse(pig.hasEffect(MobEffects.SLOWNESS), "only the players get the effect");
		helper.succeed();
	}

	@GameTest
	public void fireRainSetsBlocksOnFireAfterTheBlast(GameTestHelper helper) {
		BlockPos center = new BlockPos(3, 1, 3);
		stoneFloor(helper, center);
		GeneBlasts.afterBlast(ghastWith(helper, new BlockPos(5, 3, 5)), helper.getLevel(),
				helper.absoluteVec(Vec3.atBottomCenterOf(center)), 1.0F);
		helper.assertValueEqual(fires(helper, center), 0, "a plain blast leaves no more fire");
		GeneBlasts.afterBlast(ghastWith(helper, new BlockPos(5, 3, 5), "test_fire_rain"), helper.getLevel(),
				helper.absoluteVec(Vec3.atBottomCenterOf(center)), 1.0F);
		int fires = fires(helper, center);
		helper.assertTrue(fires >= 4 && fires <= 8, "the blast leaves up to 8 fires, not " + fires);
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void fireRainObeysMobGriefing(GameTestHelper helper) {
		BlockPos center = new BlockPos(3, 1, 3);
		stoneFloor(helper, center);
		GeneBlasts.afterBlast(ghastWith(helper, new BlockPos(5, 3, 5), "test_fire_rain"), helper.getLevel(),
				helper.absoluteVec(Vec3.atBottomCenterOf(center)), 1.0F);
		helper.assertValueEqual(fires(helper, center), 0, "no fire with mobGriefing off");
		helper.succeed();
	}

	/**
	 * Puts stone under the blast, as far as the fire reaches: the radius and one more block.
	 */
	static void stoneFloor(GameTestHelper helper, BlockPos center) {
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				helper.setBlock(center.offset(x, -1, z), Blocks.STONE);
			}
		}
	}

	static int fires(GameTestHelper helper, BlockPos center) {
		int fires = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 3, 3))) {
			if (helper.getBlockState(pos).is(Blocks.FIRE)) {
				fires++;
			}
		}
		return fires;
	}

	static ServerPlayer shieldedPlayerAt(GameTestHelper helper, BlockPos pos) {
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(pos)));
		return player;
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void aPlainGhastShootsEachThreeSeconds(GameTestHelper helper) {
		Shots shots = shooter(helper);
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 2, "the ghast shoots two times");
			helper.assertValueEqual(shots.times.get(1) - shots.times.get(0), 60L, "ticks from one shot to the next");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void quickFireMakesTheGhastShootMoreOften(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_quick_fire");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 2, "the ghast shoots two times");
			helper.assertValueEqual(shots.times.get(1) - shots.times.get(0), 40L, "ticks from one shot to the next");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void twinShotShootsTwoFireballsAtOnce(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_twin_shot");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 2, "the ghast shoots two times");
			for (var shot : shots.projectiles) {
				helper.assertValueEqual(shot.size(), 2, "fireballs in each shot");
				helper.assertTrue(shot.stream().allMatch(LargeFireball.class::isInstance), "both are large fireballs");
				// The fan turns about the up axis, so the test compares the directions on the ground.
				Vec3 first = shot.get(0).getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
				Vec3 second = shot.get(1).getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
				double degrees = Math.toDegrees(Math.acos(Math.min(1.0, first.dot(second))));
				helper.assertTrue(Math.abs(degrees - 10.0) < 1.0, "the fireballs are 10 degrees apart, not " + degrees);
			}
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void spreadFireShootsSmallFireballsWithTheLargeOne(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_spread_fire");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 1, "the ghast shoots");
			var shot = shots.projectiles.get(0);
			helper.assertValueEqual(shot.stream().filter(LargeFireball.class::isInstance).count(), 1L, "large fireballs");
			helper.assertValueEqual(shot.stream().filter(SmallFireball.class::isInstance).count(), 3L, "small fireballs");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void aPlayerCannotHitAHardFireballBack(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(BLAST.east(2))));
		Ghast plain = ghastWith(helper, new BlockPos(5, 1, 5));
		LargeFireball plainFireball = fireball(helper, plain, BLAST.above());
		player.attack(plainFireball);
		helper.assertTrue(plainFireball.getOwner() == player, "the player hits a plain fireball back");
		plainFireball.discard();
		Ghast hard = ghastWith(helper, new BlockPos(5, 1, 5), "test_hard_fireball");
		LargeFireball fireball = fireball(helper, hard, BLAST.above());
		Vec3 motion = fireball.getDeltaMovement();
		player.attack(fireball);
		helper.assertTrue(fireball.getOwner() == hard, "the hard fireball stays the fireball of the ghast");
		helper.assertTrue(fireball.getDeltaMovement().equals(motion), "the hard fireball does not turn");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8)
	public void theFireballsOfOneMobDoNotHitEachOther(GameTestHelper helper) {
		Ghast ghast = ghastWith(helper, new BlockPos(5, 1, 5), "test_twin_shot");
		LargeFireball first = fireball(helper, ghast, BLAST);
		LargeFireball second = fireball(helper, ghast, BLAST);
		helper.assertFalse(((ProjectileAccessor) first).originofspawns$canHitEntity(second),
				"a fireball does not hit a fireball of the same ghast");
		LargeFireball plain = fireball(helper, ghastWith(helper, new BlockPos(5, 1, 5)), BLAST);
		helper.assertTrue(((ProjectileAccessor) plain).originofspawns$canHitEntity(second),
				"a fireball of a plain ghast still hits a fireball");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void aPlainGhastWarnsBeforeItShoots(GameTestHelper helper) {
		var plainListener = CreeperEffectsTest.listener(helper);
		Shots plain = shooter(helper);
		helper.succeedWhen(() -> {
			helper.assertTrue(plain.times.size() >= 1, "the plain ghast shoots");
			helper.assertTrue(levelEvents(plainListener, plain.ghast).contains(1015), "a plain ghast warns before it shoots");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, padding = 8, maxTicks = 200)
	public void ghostGlideMakesNoWarningSound(GameTestHelper helper) {
		var listener = CreeperEffectsTest.listener(helper);
		Shots shots = shooter(helper, "test_ghost_glide");
		helper.succeedWhen(() -> {
			helper.assertTrue(shots.times.size() >= 1, "the ghast shoots");
			var events = levelEvents(listener, shots.ghast);
			helper.assertTrue(events.contains(1016), "the ghast makes the sound of the shot");
			helper.assertFalse(events.contains(1015), "the ghast does not warn");
		});
	}

	/**
	 * Returns the types of the level events of the ghast that the server sent to the player. The ghasts of the other
	 * tests are near, so only the events at the place of this ghast count. The packets stay in the channel.
	 */
	static java.util.List<Integer> levelEvents(CreeperEffectsTest.Listener listener, Ghast ghast) {
		Object packet;
		java.util.List<Object> kept = new java.util.ArrayList<>();
		java.util.List<Integer> events = new java.util.ArrayList<>();
		while ((packet = listener.channel().readOutbound()) != null) {
			kept.add(packet);
			if (packet instanceof ClientboundLevelEventPacket event && event.getPos().equals(ghast.blockPosition())) {
				events.add(event.getType());
			}
		}
		kept.forEach(listener.channel()::writeOutbound);
		return events;
	}

	@GameTest
	public void swiftDriftMakesTheFlightFaster(GameTestHelper helper) {
		// The test gene gives 30% more flying speed.
		double plain = flightStep(helper, ghastWith(helper, new BlockPos(2, 2, 2)));
		double swift = flightStep(helper, ghastWith(helper, new BlockPos(2, 2, 2), "test_swift_wing"));
		helper.assertTrue(plain > 0.0, "the ghast flies");
		helper.assertTrue(Math.abs(swift / plain - 1.3) < 0.001, "30% more flying speed, not " + swift / plain);
		helper.succeed();
	}

	@GameTest
	public void highFloatKeepsTheGhastAboveItsTarget(GameTestHelper helper) {
		Ghast ghast = ghastWith(helper, new BlockPos(2, 1, 2), "test_high_float");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		var goal = new Ghast.RandomFloatAroundGoal(ghast);
		ghast.setTarget(pig);
		for (int i = 0; i < 10; i++) {
			goal.start();
			double above = ghast.getMoveControl().getWantedY() - pig.getY();
			helper.assertTrue(above >= 4.0 - 1.0E-6, "the ghast floats 4 blocks above its target, not " + above);
		}
		helper.succeed();
	}

	/**
	 * Returns the speed that one step of the move control gives the ghast toward a place 2 blocks away, and removes
	 * the ghast.
	 */
	static double flightStep(GameTestHelper helper, Ghast ghast) {
		ghast.setDeltaMovement(Vec3.ZERO);
		Vec3 wanted = ghast.position().add(2.0, 0.0, 0.0);
		ghast.getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, 1.0);
		ghast.getMoveControl().tick();
		double speed = ghast.getDeltaMovement().length();
		ghast.discard();
		return speed;
	}

	/**
	 * The shots of a test ghast: the game time of each shot, and the projectiles of each shot.
	 */
	static final class Shots {
		Ghast ghast;
		final java.util.List<Long> times = new java.util.ArrayList<>();
		final java.util.List<java.util.List<Projectile>> projectiles = new java.util.ArrayList<>();
	}

	/**
	 * Makes a ghast with its AI that shoots at a pig. The ghast stays in its place, and has no target goals, so it
	 * shoots only at the pig. Each tick, the test records the new projectiles of the ghast and removes them, so that
	 * no fireball leaves the test area.
	 */
	static Shots shooter(GameTestHelper helper, String... genes) {
		Ghast ghast = ghastWith(helper, new BlockPos(2, 2, 4), genes);
		ghast.setNoAi(false);
		((MobAccessor) ghast).originofspawns$targetSelector().removeAllGoals(goal -> true);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 3, 4));
		ghast.setTarget(pig);
		Vec3 home = ghast.position();
		Shots shots = new Shots();
		shots.ghast = ghast;
		helper.onEachTick(() -> {
			ghast.setPos(home);
			ghast.setDeltaMovement(Vec3.ZERO);
			var shot = helper.getLevel().getEntitiesOfClass(Projectile.class, helper.getBounds().inflate(8.0),
					projectile -> projectile.getOwner() == ghast && projectile.isAlive());
			if (!shot.isEmpty()) {
				shots.times.add(helper.getLevel().getGameTime());
				shots.projectiles.add(java.util.List.copyOf(shot));
				shot.forEach(Projectile::discard);
			}
		});
		return shots;
	}

	/**
	 * Makes a fireball of the ghast at the place and makes it explode there at once.
	 */
	static void explodeFireball(GameTestHelper helper, Ghast ghast, BlockPos pos) {
		LargeFireball fireball = fireball(helper, ghast, pos);
		((ProjectileAccessor) fireball).originofspawns$onHit(hitAt(fireball));
	}

	static LargeFireball fireball(GameTestHelper helper, Ghast ghast, BlockPos pos) {
		LargeFireball fireball = new LargeFireball(helper.getLevel(), ghast, new Vec3(0.0, -1.0, 0.0), ghast.getExplosionPower());
		fireball.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(pos)));
		helper.getLevel().addFreshEntity(fireball);
		return fireball;
	}

	static BlockHitResult hitAt(LargeFireball fireball) {
		return new BlockHitResult(fireball.position(), Direction.UP, fireball.blockPosition().below(), false);
	}

	static Ghast ghastWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Ghast ghast = EntityTypes.GHAST.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		ghast.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		ghast.setNoAi(true);
		ghast.setPersistenceRequired();
		ghast.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(ghast);
		return ghast;
	}
}
