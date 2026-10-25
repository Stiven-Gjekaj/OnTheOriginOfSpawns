package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.mixin.LargeFireballAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
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
		((LargeFireballAccessor) fireball).originofspawns$onHit(hitAt(fireball));
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

	static ServerPlayer shieldedPlayerAt(GameTestHelper helper, BlockPos pos) {
		ServerPlayer player = CreeperEffectsTest.shieldedPlayer(helper);
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(pos)));
		return player;
	}

	/**
	 * Makes a fireball of the ghast at the place and makes it explode there at once.
	 */
	static void explodeFireball(GameTestHelper helper, Ghast ghast, BlockPos pos) {
		LargeFireball fireball = fireball(helper, ghast, pos);
		((LargeFireballAccessor) fireball).originofspawns$onHit(hitAt(fireball));
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
