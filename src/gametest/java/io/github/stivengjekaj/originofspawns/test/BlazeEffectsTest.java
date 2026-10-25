package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a blaze: its bursts of fireballs, its flight, and the reused effects.
 */
public class BlazeEffectsTest {
	/**
	 * The ticks from the last fireball of a vanilla burst to the first fireball of the next burst: 6 ticks to the end
	 * of the burst, the rest of 100 ticks, and the charge of 60 ticks.
	 */
	static final long VANILLA_GAP = 166L;

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void aPlainBlazeShootsBurstsOfThree(GameTestHelper helper) {
		Shots shots = shooter(helper);
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(0).size(), 3, "fireballs in a burst");
			helper.assertValueEqual(bursts.get(1).getFirst() - bursts.get(0).getLast(), VANILLA_GAP, "ticks between bursts");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void longBurstShootsMoreFireballsInEachBurst(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_long_burst");
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(0).size(), 5, "fireballs in a burst");
		});
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS, maxTicks = 300)
	public void quickBurstMakesTheRestBetweenBurstsShorter(GameTestHelper helper) {
		Shots shots = shooter(helper, "test_quick_burst");
		helper.succeedWhen(() -> {
			var bursts = shots.bursts();
			helper.assertTrue(bursts.size() >= 2, "the blaze shoots two bursts");
			helper.assertValueEqual(bursts.get(1).getFirst() - bursts.get(0).getLast(), VANILLA_GAP - 50L,
					"half of the rest of 100 ticks");
		});
	}

	/**
	 * The shots of a test blaze: the game time and the projectiles of each fireball.
	 */
	static final class Shots {
		final java.util.List<Long> times = new java.util.ArrayList<>();
		final java.util.List<Projectile> projectiles = new java.util.ArrayList<>();

		/**
		 * Returns the times of the shots, in groups. A group ends at a gap of more than 20 ticks.
		 */
		java.util.List<java.util.List<Long>> bursts() {
			java.util.List<java.util.List<Long>> bursts = new java.util.ArrayList<>();
			for (long time : times) {
				if (bursts.isEmpty() || time - bursts.getLast().getLast() > 20L) {
					bursts.add(new java.util.ArrayList<>());
				}
				bursts.getLast().add(time);
			}
			return bursts;
		}
	}

	/**
	 * Makes a blaze with its AI that shoots at a pig. The blaze stays in its place, and has no target goals, so it
	 * shoots only at the pig. Each tick, the test records the new projectiles of the blaze and removes them, so that
	 * no fireball leaves the test area. The blaze can shoot a burst only when the last burst is complete, so the test
	 * waits for the second burst to start before it counts the first one.
	 */
	static Shots shooter(GameTestHelper helper, String... genes) {
		Blaze blaze = blazeWith(helper, new BlockPos(1, 2, 4), genes);
		blaze.setNoAi(false);
		((MobAccessor) blaze).originofspawns$targetSelector().removeAllGoals(goal -> true);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 2, 4));
		blaze.setTarget(pig);
		Vec3 home = blaze.position();
		Shots shots = new Shots();
		helper.onEachTick(() -> {
			blaze.setPos(home);
			blaze.setDeltaMovement(Vec3.ZERO);
			for (Projectile shot : helper.getLevel().getEntitiesOfClass(Projectile.class, helper.getBounds().inflate(8.0),
					projectile -> projectile.getOwner() == blaze && projectile.isAlive())) {
				shots.times.add(helper.getLevel().getGameTime());
				shots.projectiles.add(shot);
				shot.discard();
			}
		});
		return shots;
	}

	static Blaze blazeWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Blaze blaze = EntityTypes.BLAZE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		blaze.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		blaze.setNoAi(true);
		blaze.setPersistenceRequired();
		blaze.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(blaze);
		return blaze;
	}
}
