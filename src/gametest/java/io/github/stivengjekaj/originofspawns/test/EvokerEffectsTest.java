package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.SpellcasterUseSpellGoalAccessor;
import io.github.stivengjekaj.originofspawns.mixin.VexAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.Blocks;
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

	@GameTest
	public void theVexSpellSummonsMoreAndStrongerVexes(GameTestHelper helper) {
		Evoker plain = evokerWith(helper, new BlockPos(1, 1, 1));
		plain.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 7)));
		castVexes(plain);
		java.util.List<Vex> plainVexes = vexesOf(helper, plain);
		helper.assertValueEqual(plainVexes.size(), 3, "a plain evoker summons 3 vexes");
		helper.assertValueEqual(plainVexes.getFirst().getMaxHealth(), 14.0F, "a plain vex has 14 health");
		helper.assertValueEqual(plainVexes.getFirst().getAttributeValue(Attributes.ATTACK_DAMAGE), 4.0,
				"a plain vex does 4 damage");
		Evoker swarm = evokerWith(helper, new BlockPos(5, 1, 1), "test_vex_swarm");
		swarm.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 7)));
		castVexes(swarm);
		java.util.List<Vex> vexes = vexesOf(helper, swarm);
		helper.assertValueEqual(vexes.size(), 4, "the evoker summons 4 vexes");
		for (Vex vex : vexes) {
			helper.assertValueEqual(vex.getMaxHealth(), 28.0F, "a vex has two times the health");
			helper.assertValueEqual(vex.getHealth(), 28.0F, "a vex starts with full health");
			helper.assertValueEqual(vex.getAttributeValue(Attributes.ATTACK_DAMAGE), 6.0, "a vex does 1.5 times the damage");
		}
		helper.succeed();
	}

	@GameTest
	public void longBondMakesTheVexesLiveLonger(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_long_bond");
		Vex vex = helper.spawnWithNoFreeWill(EntityTypes.VEX, new BlockPos(2, 2, 1));
		vex.setLimitedLife(600);
		GeneBehaviors.shapeVex(evoker, vex);
		helper.assertValueEqual(((VexAccessor) vex).originofspawns$limitedLifeTicks(), 1200, "the vex lives two times as long");
		Vex free = helper.spawnWithNoFreeWill(EntityTypes.VEX, new BlockPos(2, 2, 3));
		GeneBehaviors.shapeVex(evoker, free);
		helper.assertFalse(((VexAccessor) free).originofspawns$hasLimitedLife(), "a vex with no limit gets no limit");
		helper.succeed();
	}

	@GameTest
	public void theHitsOfTheVexesOfAnEvokerPoisonAndHealIt(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1), "test_venom_vexes");
		evoker.setHealth(10.0F);
		Vex vex = helper.spawnWithNoFreeWill(EntityTypes.VEX, new BlockPos(2, 2, 1));
		vex.setOwner(evoker);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		GeneCombat.onHit(vex, pig);
		helper.assertTrue(pig.hasEffect(MobEffects.POISON), "the vex hit gives Poison");
		helper.assertValueEqual(pig.getEffect(MobEffects.POISON).getDuration(), 60, "the Poison lasts 3 seconds");
		helper.assertValueEqual(evoker.getHealth(), 11.0F, "the vex hit heals the evoker 1 health");
		Vex wild = helper.spawnWithNoFreeWill(EntityTypes.VEX, new BlockPos(2, 2, 4));
		Pig other = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 4));
		GeneCombat.onHit(wild, other);
		helper.assertFalse(other.hasEffect(MobEffects.POISON), "a vex with no owner gives nothing");
		helper.succeed();
	}

	@GameTest
	public void aHexGivesTheEffectInRangeAndSightOneTimeInEachCooldown(GameTestHelper helper) {
		GeneEffect.Hex wither = new GeneEffect.Hex(MobEffects.WITHER, 4.0F, 0, 16.0F, 15.0F);
		Evoker evoker = evokerWith(helper, new BlockPos(1, 1, 1));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		evoker.setTarget(pig);
		helper.assertTrue(GeneBehaviors.hex(evoker, helper.getLevel(), wither), "the evoker casts the hex");
		helper.assertTrue(pig.hasEffect(MobEffects.WITHER), "the target gets Wither");
		helper.assertValueEqual(pig.getEffect(MobEffects.WITHER).getDuration(), 80, "the Wither lasts 4 seconds");
		pig.removeAllEffects();
		helper.assertFalse(GeneBehaviors.hex(evoker, helper.getLevel(), wither), "the hex waits for its cooldown");
		helper.assertFalse(pig.hasEffect(MobEffects.WITHER), "no effect in the cooldown");
		GeneEffect.Hex blind = new GeneEffect.Hex(MobEffects.BLINDNESS, 3.0F, 0, 16.0F, 15.0F);
		helper.assertTrue(GeneBehaviors.hex(evoker, helper.getLevel(), blind), "each hex has its own cooldown");
		GeneEffect.Hex near = new GeneEffect.Hex(MobEffects.HUNGER, 10.0F, 0, 3.0F, 15.0F);
		helper.assertFalse(GeneBehaviors.hex(evoker, helper.getLevel(), near), "the target is out of the range");
		Evoker blocked = evokerWith(helper, new BlockPos(1, 1, 4));
		blocked.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4)));
		helper.setBlock(new BlockPos(3, 1, 4), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 2, 4), Blocks.STONE);
		helper.assertFalse(GeneBehaviors.hex(blocked, helper.getLevel(), wither), "a hex needs a line of sight");
		helper.succeed();
	}

	@GameTest
	public void decoySummonsAVexAtTheAttackerOneTimeInEachCooldown(GameTestHelper helper) {
		Evoker evoker = evokerWith(helper, new BlockPos(2, 1, 2), "test_decoy");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 2));
		DamageSource hit = helper.getLevel().damageSources().mobAttack(pig);
		GeneCombat.onHurt(evoker, hit);
		java.util.List<Vex> vexes = vexesOf(helper, evoker);
		helper.assertValueEqual(vexes.size(), 1, "the evoker summons 1 vex");
		helper.assertTrue(vexes.getFirst().getTarget() == pig, "the vex attacks the attacker");
		helper.assertTrue(((VexAccessor) vexes.getFirst()).originofspawns$hasLimitedLife(), "the vex has a limited life");
		GeneCombat.onHurt(evoker, hit);
		helper.assertValueEqual(vexesOf(helper, evoker).size(), 1, "the decoy waits for its cooldown");
		Evoker plain = evokerWith(helper, new BlockPos(2, 1, 6));
		GeneCombat.onHurt(plain, hit);
		helper.assertValueEqual(vexesOf(helper, plain).size(), 0, "a plain evoker summons no vex");
		helper.succeed();
	}

	static void castVexes(Evoker evoker) {
		((MobAccessor) evoker).originofspawns$goalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal)
				.filter(each -> each.getClass().getSimpleName().equals("EvokerSummonSpellGoal"))
				.forEach(each -> ((SpellcasterUseSpellGoalAccessor) each).originofspawns$performSpellCasting());
	}

	static java.util.List<Vex> vexesOf(GameTestHelper helper, Evoker evoker) {
		return helper.getLevel().getEntitiesOfClass(Vex.class, evoker.getBoundingBox().inflate(8.0),
				vex -> vex.getOwner() == evoker);
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
