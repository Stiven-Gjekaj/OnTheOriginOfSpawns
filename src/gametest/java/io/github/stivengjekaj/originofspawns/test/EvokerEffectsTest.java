package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.SpellcasterUseSpellGoalAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the evoker genes use: the fangs, the spells, the vexes, the hex, and the decoy. It also tests
 * that the reused effects work on an evoker.
 */
public class EvokerEffectsTest {
	@GameTest
	public void fangDamageMultipliesOnlyTheDamageOfTheFangs(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_fang_damage");
		EvokerFangs fangs = new EvokerFangs(helper.getLevel(), 0.0, 0.0, 0.0, 0.0F, 0, evoker);
		DamageSource fang = helper.getLevel().damageSources().indirectMagic(fangs, evoker);
		DamageSource melee = helper.getLevel().damageSources().mobAttack(evoker);
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.PIG, fang), 6.0F,
				"the fangs do 1.5 times the damage");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.PIG, melee), 4.0F,
				"a hit that is not a fang does not change");
		helper.succeed();
	}

	@GameTest
	public void golemJawsHurtsOnlyTheGolemsMore(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_golem_jaws");
		EvokerFangs fangs = new EvokerFangs(helper.getLevel(), 0.0, 0.0, 0.0, 0.0F, 0, evoker);
		DamageSource fang = helper.getLevel().damageSources().indirectMagic(fangs, evoker);
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.IRON_GOLEM, fang), 8.0F,
				"an iron golem takes two times the damage");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.SNOW_GOLEM, fang), 8.0F,
				"a snow golem too");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.PIG, fang), 4.0F,
				"a pig is not a golem");
		helper.succeed();
	}

	@GameTest
	public void theFangsOfAnEvokerGiveTheEffectAndHealIt(GameTestHelper helper) {
		Evoker plain = evokerWith(helper, new BlockPos(1, 1, 1));
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 4), "test_barbed_fangs", "test_drain_hex");
		plain.setHealth(10.0F);
		evoker.setHealth(10.0F);
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		plainTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(40.0);
		target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(40.0);
		plainTarget.setHealth(40.0F);
		target.setHealth(40.0F);
		helper.getLevel().addFreshEntity(new EvokerFangs(helper.getLevel(), plainTarget.getX(), plainTarget.getY(),
				plainTarget.getZ(), 0.0F, 0, plain));
		helper.getLevel().addFreshEntity(new EvokerFangs(helper.getLevel(), target.getX(), target.getY(), target.getZ(),
				0.0F, 0, evoker));
		helper.succeedWhen(() -> {
			helper.assertTrue(plainTarget.getHealth() < 40.0F && target.getHealth() < 40.0F, "the fangs hit");
			helper.assertFalse(plainTarget.hasEffect(MobEffects.SLOWNESS), "plain fangs give no effect");
			helper.assertValueEqual(plain.getHealth(), 10.0F, "plain fangs do not heal");
			helper.assertTrue(target.hasEffect(MobEffects.SLOWNESS)
					&& target.getEffect(MobEffects.SLOWNESS).getAmplifier() == 1, "the fangs give Slowness II");
			helper.assertValueEqual(evoker.getHealth(), 12.0F, "the hit heals the evoker 2 health");
		});
	}

	@GameTest
	public void upheavalThrowsTheTargetOneBlockUp(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_upheaval");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		GeneCombat.onFangHit(evoker, pig);
		double speed = pig.getDeltaMovement().y;
		helper.assertTrue(speed > 0.3 && speed < 0.42, "the speed lifts the pig a little less than a jump, was " + speed);
		// The jump of a player lifts it about 1.25 blocks, with the gravity and the drag of the game.
		double height = 0.0;
		for (double v = speed; v > 0.0; v = (v - 0.08) * 0.98) {
			height += v;
		}
		helper.assertTrue(height >= 1.0 && height < 1.1, "the pig goes 1 block up, was " + height);
		Pig other = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		GeneCombat.onFangHit(evokerWith(helper, new BlockPos(1, 1, 4)), other);
		helper.assertValueEqual(other.getDeltaMovement().y, 0.0, "plain fangs do not throw");
		helper.succeed();
	}

	@GameTest
	public void swiftCastShortensTheTimeBetweenTheFangAndVexSpells(GameTestHelper helper) {
		Evoker plain = evokerWith(helper, new BlockPos(1, 1, 1));
		Evoker swift = evokerWith(helper, new BlockPos(1, 1, 4), "test_swift_cast");
		helper.assertValueEqual(castingInterval(plain, "EvokerAttackSpellGoal"), 100, "the plain fang spell waits 100 ticks");
		helper.assertValueEqual(castingInterval(plain, "EvokerSummonSpellGoal"), 340, "the plain vex spell waits 340 ticks");
		helper.assertValueEqual(castingInterval(swift, "EvokerAttackSpellGoal"), 70, "the fang spell waits 30% less");
		helper.assertValueEqual(castingInterval(swift, "EvokerSummonSpellGoal"), 238, "the vex spell waits 30% less");
		helper.assertValueEqual(castingInterval(swift, "EvokerWololoSpellGoal"), 140, "the sheep spell does not change");
		helper.succeed();
	}

	@GameTest
	public void fangLineSetsTheNumberOfFangsInTheLine(GameTestHelper helper) {
		// A short line stays in the test area. The same constant makes a long line.
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_fang_line");
		evoker.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 1)));
		castFangs(evoker);
		helper.assertValueEqual(fangsOf(helper, evoker).size(), 4, "the line has 4 fangs, not 16");
		helper.succeed();
	}

	@GameTest
	public void fangRingCastsTheRingAtATargetInFiveBlocks(GameTestHelper helper) {
		Evoker plain = evokerWith(helper, new BlockPos(1, 1, 1));
		plain.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1)));
		castFangs(plain);
		helper.assertTrue(fangsOf(helper, plain).stream().anyMatch(fang -> across(fang, plain) > 3.0),
				"a plain evoker casts the line at a target 4 blocks away");
		Evoker ringer = evokerWith(helper, new BlockPos(1, 1, 4), "test_fang_ring");
		ringer.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4)));
		castFangs(ringer);
		java.util.List<EvokerFangs> ring = fangsOf(helper, ringer);
		helper.assertValueEqual(ring.size(), 13, "the ring has 13 fangs");
		helper.assertTrue(ring.stream().allMatch(fang -> across(fang, ringer) < 3.0), "the ring is near the evoker");
		helper.succeed();
	}

	/**
	 * Returns the distance in the flat plane. A fang can stand on a block that is higher than the evoker.
	 */
	static double across(EvokerFangs fang, Evoker evoker) {
		return Math.hypot(fang.getX() - evoker.getX(), fang.getZ() - evoker.getZ());
	}

	static void castFangs(Evoker evoker) {
		((MobAccessor) evoker).originofspawns$goalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal)
				.filter(each -> each.getClass().getSimpleName().equals("EvokerAttackSpellGoal"))
				.forEach(each -> ((SpellcasterUseSpellGoalAccessor) each).originofspawns$performSpellCasting());
	}

	static java.util.List<EvokerFangs> fangsOf(GameTestHelper helper, Evoker evoker) {
		return helper.getLevel().getEntitiesOfClass(EvokerFangs.class, evoker.getBoundingBox().inflate(32.0),
				fang -> fang.getOwner() == evoker);
	}

	static int castingInterval(Evoker evoker, String goal) {
		return ((MobAccessor) evoker).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(each -> each.getClass().getSimpleName().equals(goal))
				.map(each -> ((SpellcasterUseSpellGoalAccessor) each).originofspawns$castingInterval())
				.findFirst().orElseThrow();
	}

	static Evoker evokerWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Evoker evoker = EntityTypes.EVOKER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		evoker.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		evoker.setNoAi(true);
		evoker.setPersistenceRequired();
		evoker.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(evoker);
		return evoker;
	}
}
