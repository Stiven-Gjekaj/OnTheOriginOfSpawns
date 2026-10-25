package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.ZombifiedPiglinAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the zombified piglin: the anger, the gold, the rot, the sword, and the chase. It also tests that
 * the zombie code of the zombified piglin does not give it the zombie genes or the zombie rules.
 */
public class ZombifiedPiglinEffectsTest {
	@GameTest
	public void aZombifiedPiglinIsNotAZombieForTheGenes(GameTestHelper helper) {
		var genes = helper.getLevel().registryAccess().lookupOrThrow(ModRegistries.GENE);
		Gene zombieGene = genes.getValue(Identifier.fromNamespaceAndPath("originofspawns_test", "test_climb"));
		helper.assertTrue(zombieGene.carriedBy(EntityTypes.ZOMBIE), "the test gene is a zombie gene");
		long shared = genes.stream().filter(gene -> gene.carriedBy(EntityTypes.ZOMBIE)
				&& gene.carriedBy(EntityTypes.ZOMBIFIED_PIGLIN)).count();
		helper.assertValueEqual(shared, 0L, "no zombie gene goes to a zombified piglin");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aZombifiedPiglinSpawnsWithAPlainGoldWeaponAndNoBaby(GameTestHelper helper) {
		// A high difficulty gives a vanilla zombified piglin a random enchantment often.
		DifficultyInstance difficulty = new DifficultyInstance(Difficulty.HARD, 10_000_000L, 10_000_000L, 1.0F);
		int babies = 0;
		int enchanted = 0;
		int plainWeapons = 0;
		for (int i = 0; i < 200; i++) {
			ZombifiedPiglin piglin = EntityTypes.ZOMBIFIED_PIGLIN.create(helper.getLevel(), EntitySpawnReason.NATURAL);
			piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))), 0.0F, 0.0F);
			piglin.finalizeSpawn(helper.getLevel(), difficulty, EntitySpawnReason.NATURAL, null);
			ItemStack weapon = piglin.getItemBySlot(EquipmentSlot.MAINHAND);
			if (weapon.is(Items.GOLDEN_SWORD) || weapon.is(Items.GOLDEN_SPEAR)) {
				plainWeapons++;
			}
			if (weapon.isEnchanted()) {
				enchanted++;
			}
			if (piglin.isBaby()) {
				babies++;
			}
			// A chicken jockey adds its chicken to the level. The test removes all of them.
			if (piglin.getVehicle() != null) {
				piglin.getVehicle().discard();
			}
			piglin.discard();
		}
		helper.assertValueEqual(plainWeapons, 200, "each zombified piglin keeps its gold weapon");
		helper.assertValueEqual(enchanted, 0, "no weapon gets a random enchantment");
		helper.assertValueEqual(babies, 0, "no zombified piglin spawns as a baby in a vanilla way");
		helper.succeed();
	}

	@GameTest
	public void vengefulAngerReachesTwoTimesAsFar(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		ZombifiedPiglin other = piglinWith(helper, new BlockPos(6, 1, 4));
		// The test makes the follow range 2 blocks, so the call stays in the test area. The other piglin is 5 blocks
		// away: out of the vanilla call, and in the call that reaches two times as far.
		for (String gene : new String[] {null, "test_vengeful"}) {
			ZombifiedPiglin caller = gene == null ? piglinWith(helper, new BlockPos(1, 1, 4))
					: piglinWith(helper, new BlockPos(1, 1, 4), gene);
			caller.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2.0);
			caller.setLastHurtByMob(pig);
			new HurtByTargetGoal(caller).setAlertOthers().start();
			helper.assertValueEqual(other.getTarget() == pig, gene != null, "the hurt call reaches the other piglin");
			other.setTarget(null);
			caller.setTarget(pig);
			((ZombifiedPiglinAccessor) caller).originofspawns$alertOthers();
			helper.assertValueEqual(other.getTarget() == pig, gene != null, "the target call reaches the other piglin");
			other.setTarget(null);
			caller.discard();
		}
		helper.succeed();
	}

	@GameTest
	public void bloodFeudMakesTheAngerLastTwoTimesAsLong(GameTestHelper helper) {
		ZombifiedPiglin plain = piglinWith(helper, new BlockPos(1, 1, 1));
		ZombifiedPiglin feud = piglinWith(helper, new BlockPos(5, 1, 5), "test_blood_feud");
		long now = helper.getLevel().getGameTime();
		// The vanilla anger lasts 20 to 39 seconds.
		for (int i = 0; i < 20; i++) {
			plain.startPersistentAngerTimer();
			feud.startPersistentAngerTimer();
			long plainTicks = plain.getPersistentAngerEndTime() - now;
			long feudTicks = feud.getPersistentAngerEndTime() - now;
			helper.assertTrue(plainTicks >= 400 && plainTicks <= 780, "the vanilla anger time, not " + plainTicks);
			helper.assertTrue(feudTicks >= 800 && feudTicks <= 1560, "two times the anger time, not " + feudTicks);
		}
		helper.succeed();
	}

	@GameTest
	public void longMemoryStaysAngryAtAPlayerThatDied(GameTestHelper helper) {
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		ZombifiedPiglin plain = piglinWith(helper, new BlockPos(1, 1, 1));
		ZombifiedPiglin memory = piglinWith(helper, new BlockPos(5, 1, 5), "test_long_memory");
		for (ZombifiedPiglin piglin : List.of(plain, memory)) {
			piglin.setPersistentAngerTarget(EntityReference.of(player));
			piglin.setTimeToRemainAngry(100L);
			piglin.setTarget(player);
			piglin.playerDied(helper.getLevel(), player);
		}
		helper.assertFalse(plain.isAngry(), "a plain zombified piglin forgives a dead player");
		helper.assertTrue(memory.isAngry(), "Long Memory stays angry");
		helper.assertTrue(memory.getPersistentAngerTarget().matches(player), "it stays angry at the same player");
		helper.assertValueEqual(memory.getPersistentAngerEndTime() - helper.getLevel().getGameTime(), 6000L,
				"it stays angry for 5 minutes");
		helper.assertTrue(memory.getTarget() == null, "it does not chase the dead body");
		helper.succeed();
	}

	@GameTest
	public void warSquealCountsOnlyTheAngryPiglinsNearIt(GameTestHelper helper) {
		ZombifiedPiglin squealer = piglinWith(helper, new BlockPos(4, 1, 4), "test_war_squeal");
		ZombifiedPiglin angry = piglinWith(helper, new BlockPos(2, 1, 4));
		piglinWith(helper, new BlockPos(6, 1, 4));
		angry.setTimeToRemainAngry(200L);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		float damage = GeneCombat.modifyDamage(pig, helper.getLevel().damageSources().mobAttack(squealer), 10.0F);
		helper.assertTrue(Math.abs(damage - 10.5F) < 0.001F, "5% more damage for the one angry piglin, not " + damage);
		helper.succeed();
	}

	@GameTest
	public void avengeAndRotSprintGiveTheEffectToThePiglinItself(GameTestHelper helper) {
		ZombifiedPiglin piglin = piglinWith(helper, new BlockPos(4, 1, 4), "test_avenge", "test_rot_sprint");
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1));
		GeneCombat.onHurt(piglin, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertFalse(piglin.hasEffect(MobEffects.STRENGTH), "a melee hit gives no Strength");
		GeneCombat.onHurt(piglin, helper.getLevel().damageSources().arrow(
				new Arrow(helper.getLevel(), pig, new ItemStack(Items.ARROW), null), pig));
		helper.assertTrue(piglin.hasEffect(MobEffects.STRENGTH), "a projectile gives Strength to the piglin");
		helper.assertFalse(pig.hasEffect(MobEffects.STRENGTH), "the attacker gets no Strength");
		GeneCombat.onHit(piglin, pig);
		helper.assertTrue(piglin.hasEffect(MobEffects.SPEED), "a hit gives Speed to the piglin");
		helper.assertFalse(pig.hasEffect(MobEffects.SPEED), "the target gets no Speed");
		helper.succeed();
	}

	@GameTest
	public void glintPutsSharpnessOnTheSwordAtTheSpawn(GameTestHelper helper) {
		ZombifiedPiglin piglin = piglinWith(helper, new BlockPos(4, 1, 4), "test_glint");
		piglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		GeneBehaviors.onSpawn(piglin, helper.getLevel(), EntitySpawnReason.NATURAL);
		var sharpness = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.SHARPNESS);
		helper.assertValueEqual(piglin.getMainHandItem().getEnchantments().getLevel(sharpness), 1, "Sharpness I");
		helper.assertTrue(piglin.getMainHandItem().is(Items.GOLDEN_SWORD), "the sword stays");
		helper.succeed();
	}

	@GameTest
	public void theStateTargetPlayerIsTrueOnlyWhileTheTargetIsAPlayer(GameTestHelper helper) {
		ZombifiedPiglin piglin = piglinWith(helper, new BlockPos(4, 1, 4));
		helper.assertFalse(GeneCombat.test(piglin, GeneEffect.When.TARGET_PLAYER), "no target");
		piglin.setTarget(CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 1)));
		helper.assertFalse(GeneCombat.test(piglin, GeneEffect.When.TARGET_PLAYER), "a pig is not a player");
		piglin.setTarget(CreeperEffectsTest.survivalPlayer(helper).player());
		helper.assertTrue(GeneCombat.test(piglin, GeneEffect.When.TARGET_PLAYER), "a player target");
		helper.succeed();
	}

	@GameTest(environment = CreeperEffectsTest.BLASTS)
	public void theReusedHitEffectsWorkOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.armorBreak(helper, ZombifiedPiglinEffectsTest::piglinWith);
		ReusedEffects.cleave(helper, ZombifiedPiglinEffectsTest::piglinWith);
		ReusedEffects.firstStrike(helper, ZombifiedPiglinEffectsTest::piglinWith);
		ReusedEffects.meleeDisarm(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDamageEffectsWorkOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.damageToGolems(helper, ZombifiedPiglinEffectsTest::piglinWith);
		ReusedEffects.damageTaken(helper, ZombifiedPiglinEffectsTest::piglinWith);
		ReusedEffects.parry(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedGroupEffectsWorkOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.allyAura(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedCallWorksOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.callOnHurt(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedDeathCloudWorksOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.deathCloud(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedMovesWorkOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.leap(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest
	public void theReusedSidestepWorksOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.sidestep(helper, ZombifiedPiglinEffectsTest::piglinWith);
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theReusedSpeedWhenWorksOnAZombifiedPiglin(GameTestHelper helper) {
		ReusedEffects.speedWhenWounded(helper, ZombifiedPiglinEffectsTest::piglinWith);
	}

	@GameTest
	public void shrunkenMakesAZombifiedPiglinABaby(GameTestHelper helper) {
		ZombifiedPiglin piglin = piglinWith(helper, new BlockPos(4, 1, 4), "test_runt");
		GeneBehaviors.onSpawn(piglin, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(piglin.isBaby(), "the baby gene makes a baby");
		helper.succeed();
	}

	/**
	 * Adds a zombified piglin with the test genes and no AI. The genes go on before it enters the level, so it gets
	 * no genes from the pool.
	 */
	static ZombifiedPiglin piglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		ZombifiedPiglin piglin = EntityTypes.ZOMBIFIED_PIGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		piglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		piglin.setNoAi(true);
		piglin.setPersistenceRequired();
		piglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(piglin);
		return piglin;
	}
}
