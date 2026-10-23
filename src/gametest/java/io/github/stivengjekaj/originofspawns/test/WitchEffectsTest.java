package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.mixin.ProjectileAccessor;
import io.github.stivengjekaj.originofspawns.mixin.WitchAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the witch genes use: the thrown potions, the drinks, and the heal throw. It also tests that
 * the reused effects work on a witch.
 */
public class WitchEffectsTest {
	@GameTest
	public void longBrewMakesTheThrownEffectsLastLonger(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(2, 1, 2));
		Witch witch = witchWith(helper, new BlockPos(2, 1, 5), "test_long_brew");
		ItemStack poison = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON);
		helper.assertValueEqual(GeneBehaviors.brew(plain, poison).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
				1.0F, "a plain witch does not change the time");
		helper.assertValueEqual(GeneBehaviors.brew(witch, poison).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
				2.0F, "the effects last two times as long");
		helper.succeed();
	}

	@GameTest
	public void strongBrewMakesTheEffectsWithATimeOneLevelStronger(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_strong_brew");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).getFirst().getAmplifier(), 1,
				"Poison I becomes Poison II");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.HARMING))).getFirst().getAmplifier(), 0,
				"Harming does not change");
		helper.succeed();
	}

	@GameTest
	public void blindingBrewThrowsBlindnessInPlaceOfSlowness(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_blinding_brew");
		List<MobEffectInstance> effects = effectsOf(GeneBehaviors.brew(witch, splash(Potions.SLOWNESS)));
		helper.assertValueEqual(effects.size(), 1, "one effect");
		helper.assertTrue(is(effects.getFirst(), MobEffects.BLINDNESS), "Blindness takes the place of Slowness");
		helper.assertValueEqual(effects.getFirst().getDuration(), 100, "for 5 seconds");
		helper.assertTrue(is(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).getFirst(), MobEffects.POISON),
				"Poison does not change");
		helper.succeed();
	}

	@GameTest
	public void witherBrewAddsWitherToHarming(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_wither_brew");
		List<MobEffectInstance> effects = effectsOf(GeneBehaviors.brew(witch, splash(Potions.HARMING)));
		helper.assertValueEqual(effects.size(), 2, "Harming and Wither");
		helper.assertTrue(is(effects.getFirst(), MobEffects.INSTANT_DAMAGE), "the potion still harms");
		helper.assertTrue(is(effects.get(1), MobEffects.WITHER) && effects.get(1).getDuration() == 60,
				"Wither for 3 seconds");
		helper.assertValueEqual(effectsOf(GeneBehaviors.brew(witch, splash(Potions.POISON))).size(), 1,
				"Poison gets no Wither");
		helper.succeed();
	}

	@GameTest
	public void lingeringBrewThrowsALingeringPotionButNotAtARaider(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(2, 1, 1));
		Witch witch = witchWith(helper, new BlockPos(2, 1, 4), "test_lingering_brew");
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1));
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 4));
		plain.performRangedAttack(plainTarget, 1.0F);
		AbstractThrownPotion plainPotion = potionOf(helper, plain);
		helper.assertFalse(plainPotion instanceof ThrownLingeringPotion, "a plain witch throws a splash potion");
		helper.assertFalse(plainPotion.hasAttached(ModAttachments.GENE_POTION), "a plain witch does not mark it");
		plainPotion.discard();
		witch.performRangedAttack(target, 1.0F);
		AbstractThrownPotion potion = potionOf(helper, witch);
		helper.assertTrue(potion instanceof ThrownLingeringPotion, "the witch throws a lingering potion");
		helper.assertTrue(potion.hasAttached(ModAttachments.GENE_POTION), "the potion carries the mark of the genes");
		potion.discard();
		Pillager raider = PillagerEffectsTest.pillagerWith(helper, new BlockPos(5, 1, 6));
		witch.performRangedAttack(raider, 1.0F);
		AbstractThrownPotion heal = potionOf(helper, witch);
		helper.assertFalse(heal instanceof ThrownLingeringPotion, "the heal of a raider stays a splash potion");
		helper.assertFalse(heal.hasAttached(ModAttachments.GENE_POTION), "the heal has no mark");
		heal.discard();
		helper.succeed();
	}

	@GameTest
	public void doubleThrowThrowsTwoPotionsALittleApart(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 3), "test_double_throw");
		// With no aim spread, only the angle of the gene parts the potions.
		GeneBehaviors.throwPotion(witch, helper.getLevel(), splash(Potions.POISON), 1.0, 0.2, 0.0, 0.75F, 0.0F);
		List<AbstractThrownPotion> potions = helper.getLevel().getEntitiesOfClass(AbstractThrownPotion.class,
				witch.getBoundingBox().inflate(4.0), potion -> potion.getOwner() == witch);
		helper.assertValueEqual(potions.size(), 2, "the witch throws two potions");
		Vec3 one = potions.get(0).getDeltaMovement();
		Vec3 two = potions.get(1).getDeltaMovement();
		double apart = Math.toDegrees(Math.abs(Math.atan2(one.z, one.x) - Math.atan2(two.z, two.x)));
		potions.forEach(AbstractThrownPotion::discard);
		helper.assertTrue(Math.abs(apart - 10.0) < 0.5, "the potions fly 10 degrees apart, " + apart);
		helper.succeed();
	}

	@GameTest
	public void wideSplashReachesFartherAndMakesAWiderCloud(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(1, 1, 1));
		Witch witch = witchWith(helper, new BlockPos(1, 1, 4), "test_wide_splash");
		// The two cases use one place, one after the other, so that the wide splash does not reach the first pig.
		Pig plainTarget = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		splashAt(helper, plain, new BlockPos(1, 1, 1), Items.SPLASH_POTION);
		helper.assertFalse(plainTarget.hasEffect(MobEffects.POISON), "a plain splash does not reach 5 blocks");
		plainTarget.discard();
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 1));
		splashAt(helper, witch, new BlockPos(1, 1, 1), Items.SPLASH_POTION);
		helper.assertTrue(target.hasEffect(MobEffects.POISON), "a wide splash reaches 5 blocks");
		target.discard();
		splashAt(helper, plain, new BlockPos(2, 1, 2), Items.LINGERING_POTION);
		splashAt(helper, witch, new BlockPos(2, 1, 5), Items.LINGERING_POTION);
		List<AreaEffectCloud> clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0));
		helper.assertValueEqual(clouds.stream().filter(cloud -> cloud.getOwner() == plain).findFirst().orElseThrow()
				.getRadius(), 3.0F, "a plain cloud starts at 3 blocks");
		helper.assertValueEqual(clouds.stream().filter(cloud -> cloud.getOwner() == witch).findFirst().orElseThrow()
				.getRadius(), 4.5F, "a wide cloud starts at 4.5 blocks");
		clouds.forEach(AreaEffectCloud::discard);
		helper.succeed();
	}

	@GameTest
	public void rustBrewHurtsOnlyTheGolemsMore(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(1, 1, 1), "test_rust_brew");
		AbstractThrownPotion potion = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(
				helper.getLevel(), witch, splash(Potions.HARMING));
		DamageSource harm = helper.getLevel().damageSources().indirectMagic(potion, witch);
		DamageSource hit = helper.getLevel().damageSources().mobAttack(witch);
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.IRON_GOLEM, harm), 8.0F,
				"an iron golem takes two times the damage");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.SNOW_GOLEM, harm), 8.0F,
				"a snow golem too");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.PIG, harm), 4.0F, "a pig is not a golem");
		helper.assertValueEqual(VindicatorEffectsTest.damageTo(helper, EntityTypes.IRON_GOLEM, hit), 4.0F,
				"a hit that is not a potion does not change");
		helper.succeed();
	}

	@GameTest
	public void aPotionThatBreaksOnATargetCutsItAndHealsTheWitch(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2), "test_glass_shards", "test_leech_jar");
		witch.setHealth(10.0F);
		Pig target = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 2));
		AbstractThrownPotion potion = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(
				helper.getLevel(), witch, splash(Potions.WEAKNESS));
		potion.setAttached(ModAttachments.GENE_POTION, true);
		potion.setPos(target.position());
		((ProjectileAccessor) potion).originofspawns$onHit(new EntityHitResult(target));
		helper.assertValueEqual(target.getHealth(), 8.0F, "the glass does 2 damage");
		helper.assertValueEqual(witch.getHealth(), 11.0F, "the hit heals the witch 1 health");
		Pig ally = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		AbstractThrownPotion heal = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(
				helper.getLevel(), witch, splash(Potions.WEAKNESS));
		heal.setPos(ally.position());
		((ProjectileAccessor) heal).originofspawns$onHit(new EntityHitResult(ally));
		helper.assertValueEqual(ally.getHealth(), ally.getMaxHealth(), "a potion with no mark does no damage");
		helper.assertValueEqual(witch.getHealth(), 11.0F, "and it does not heal the witch");
		helper.succeed();
	}

	@GameTest
	public void throwRateMakesTheWitchThrowMoreOften(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(2, 1, 1));
		Witch quick = witchWith(helper, new BlockPos(2, 1, 5), "test_quick_hand");
		RangedAttackGoal plainGoal = rangedGoal(plain, CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 1)));
		RangedAttackGoal quickGoal = rangedGoal(quick, CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5)));
		// The vanilla witch throws each 60 ticks. Two times as often is each 30 ticks.
		for (int i = 0; i < 35; i++) {
			plainGoal.tick();
			quickGoal.tick();
		}
		List<AbstractThrownPotion> potions = helper.getLevel().getEntitiesOfClass(AbstractThrownPotion.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0));
		long plainThrows = potions.stream().filter(potion -> potion.getOwner() == plain).count();
		long quickThrows = potions.stream().filter(potion -> potion.getOwner() == quick).count();
		potions.forEach(AbstractThrownPotion::discard);
		helper.assertValueEqual(plainThrows, 0L, "a plain witch does not throw in 35 ticks");
		helper.assertValueEqual(quickThrows, 1L, "the quick witch throws in 35 ticks");
		helper.succeed();
	}

	@GameTest
	public void throwRangeChangesTheRangeOfTheWitch(GameTestHelper helper) {
		// A shorter range tests the same code as the longer range of Long Arm, and it stays in the test area.
		Witch plain = witchWith(helper, new BlockPos(2, 1, 1));
		Witch near = witchWith(helper, new BlockPos(2, 1, 5), "test_short_arm");
		// A mob with no AI is not on the ground, and a mob in the air makes no path.
		plain.setOnGround(true);
		near.setOnGround(true);
		RangedAttackGoal plainGoal = rangedGoal(plain, CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 1)));
		RangedAttackGoal nearGoal = rangedGoal(near, CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 5)));
		for (int i = 0; i < 6; i++) {
			plainGoal.tick();
			nearGoal.tick();
		}
		helper.assertTrue(plain.getNavigation().isDone(), "a plain witch stays, the target is in 10 blocks");
		helper.assertFalse(near.getNavigation().isDone(), "the witch walks, the target is out of 3 blocks");
		helper.succeed();
	}

	@GameTest
	public void sureAimThrowsStraight(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 3), "test_sure_aim");
		witch.performRangedAttack(CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 3)), 1.0F);
		AbstractThrownPotion potion = potionOf(helper, witch);
		Vec3 flight = potion.getDeltaMovement();
		potion.discard();
		helper.assertTrue(Math.abs(flight.z) < 1.0E-6, "the potion flies straight at the target, z was " + flight.z);
		helper.succeed();
	}

	@GameTest
	public void theNewStatesOfTheDrinks(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(2, 1, 2));
		helper.assertFalse(GeneCombat.test(witch, GeneEffect.When.ON_FIRE), "the witch does not burn");
		witch.setRemainingFireTicks(100);
		helper.assertTrue(GeneCombat.test(witch, GeneEffect.When.ON_FIRE), "the witch burns");
		witch.clearFire();
		// The chunk column of the test is loaded at each height, so a target high up stays in the test area.
		Pig high = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 14, 2));
		Pig near = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 6));
		witch.setTarget(near);
		helper.assertFalse(GeneCombat.test(witch, GeneEffect.When.TARGET_FAR), "a target 4 blocks away is near");
		witch.setTarget(high);
		helper.assertTrue(GeneCombat.test(witch, GeneEffect.When.TARGET_FAR), "a target 13 blocks away is far");
		witch.setOnGround(false);
		witch.fallDistance = 1.0;
		helper.assertFalse(GeneCombat.test(witch, GeneEffect.When.FALLING), "a short drop is not a fall");
		witch.fallDistance = 4.0;
		helper.assertTrue(GeneCombat.test(witch, GeneEffect.When.FALLING), "the witch falls");
		witch.setOnGround(true);
		helper.assertFalse(GeneCombat.test(witch, GeneEffect.When.FALLING), "a witch on the ground does not fall");
		helper.assertFalse(GeneCombat.test(witch, GeneEffect.When.SEES_PLAYER), "a pig is not a player");
		Player player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 2))));
		witch.setTarget(player);
		helper.assertTrue(GeneCombat.test(witch, GeneEffect.When.SEES_PLAYER), "the witch sees the player");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void shadeDraughtDrinksInvisibilityAndSharesItWithTheMonstersNear(GameTestHelper helper) {
		Witch plain = witchWith(helper, new BlockPos(1, 1, 1));
		Witch witch = witchWith(helper, new BlockPos(3, 1, 3), "test_shade_draught", "test_shared_draught");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(5, 1, 3));
		Zombie far = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 7));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 5));
		Player player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(6, 1, 1))));
		plain.setTarget(player);
		witch.setTarget(player);
		GeneEffect.Drink shade = new GeneEffect.Drink(Potions.INVISIBILITY, GeneEffect.When.SEES_PLAYER, 60.0F,
				java.util.Optional.of(10.0F));
		helper.assertFalse(GeneBehaviors.drink(plain, helper.getLevel(), new GeneEffect.Drink(Potions.INVISIBILITY,
				GeneEffect.When.ON_FIRE, 0.0F, java.util.Optional.empty())), "a witch that does not burn does not drink");
		helper.assertTrue(GeneBehaviors.drink(witch, helper.getLevel(), shade), "the witch starts to drink");
		helper.assertTrue(witch.isDrinkingPotion(), "the witch drinks");
		helper.assertFalse(GeneBehaviors.drink(witch, helper.getLevel(), shade), "one drink at a time");
		helper.succeedWhen(() -> {
			helper.assertFalse(witch.isDrinkingPotion(), "the drink ends");
			helper.assertTrue(witch.hasEffect(MobEffects.INVISIBILITY), "the witch is invisible");
			helper.assertTrue(witch.getEffect(MobEffects.INVISIBILITY).getDuration() <= 200, "for 10 seconds");
			helper.assertTrue(ally.hasEffect(MobEffects.INVISIBILITY), "a zombie in 3 blocks shares the drink");
			helper.assertFalse(far.hasEffect(MobEffects.INVISIBILITY), "a zombie 4 blocks away does not");
			helper.assertFalse(pig.hasEffect(MobEffects.INVISIBILITY), "a pig is not a monster");
			witch.removeAllEffects();
			helper.assertFalse(GeneBehaviors.drink(witch, helper.getLevel(), shade), "the drink waits for its cooldown");
		});
	}

	@GameTest
	public void mendingHandThrowsHealingAtAHurtMonsterButNotAtItsTarget(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(3, 1, 3));
		GeneEffect.HealThrow mending = new GeneEffect.HealThrow(5.0F, 15.0F);
		Creeper full = helper.spawnWithNoFreeWill(EntityTypes.CREEPER, new BlockPos(5, 1, 3));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 5));
		pig.setHealth(5.0F);
		helper.assertTrue(GeneBehaviors.healThrow(witch, helper.getLevel(), mending) == null,
				"no throw at a monster with full health or at a pig");
		Creeper target = helper.spawnWithNoFreeWill(EntityTypes.CREEPER, new BlockPos(1, 1, 3));
		target.setHealth(5.0F);
		witch.setTarget(target);
		helper.assertTrue(GeneBehaviors.healThrow(witch, helper.getLevel(), mending) == null, "no throw at its target");
		full.setHealth(5.0F);
		helper.assertTrue(GeneBehaviors.healThrow(witch, helper.getLevel(), mending) == full,
				"the witch throws at the hurt creeper");
		AbstractThrownPotion potion = potionOf(helper, witch);
		helper.assertTrue(potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
				.is(Potions.HEALING), "the potion is Healing");
		helper.assertFalse(potion.hasAttached(ModAttachments.GENE_POTION), "a heal has no mark of an attack");
		potion.discard();
		helper.assertTrue(GeneBehaviors.healThrow(witch, helper.getLevel(), mending) == null,
				"the throw waits for its cooldown");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void aWitchWithMendingHandHealsAZombieWithHarming(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(3, 1, 3), "test_mending_hand");
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(5, 1, 3));
		zombie.setHealth(5.0F);
		// A zombie burns in the sun, and the fire would hide the heal.
		zombie.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
		// The behavior tick of the witch throws the potion, and the potion flies 2 blocks to the zombie.
		helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() > 5.0F, "health was " + zombie.getHealth()));
	}

	// The genes of these tests reach only 4 blocks, so they do not reach the witches of other tests.
	@GameTest(maxTicks = 60)
	public void covenCallCallsTheWitchesWhenItSeesAPlayer(GameTestHelper helper) {
		Witch caller = witchWith(helper, new BlockPos(1, 1, 1), "test_coven_call");
		Witch ally = witchWith(helper, new BlockPos(3, 1, 1));
		Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
		caller.setTarget(player);
		helper.succeedWhen(() -> helper.assertTrue(ally.getTarget() == player, "the witch targets the player"));
	}

	@GameTest(maxTicks = 80)
	public void covenWardGivesResistanceToWitchesOnly(GameTestHelper helper) {
		witchWith(helper, new BlockPos(1, 1, 1), "test_coven_ward");
		Witch ally = witchWith(helper, new BlockPos(4, 1, 1));
		Pillager pillager = PillagerEffectsTest.pillagerWith(helper, new BlockPos(1, 1, 3));
		helper.succeedWhen(() -> {
			helper.assertTrue(ally.hasEffect(MobEffects.RESISTANCE), "the witch gets Resistance");
			helper.assertFalse(pillager.hasEffect(MobEffects.RESISTANCE), "the pillager is not a witch");
		});
	}

	@GameTest
	public void bloodPactSharesTheDamageWithAWitch(GameTestHelper helper) {
		Witch pact = witchWith(helper, new BlockPos(1, 1, 1), "test_blood_pact");
		Witch ally = witchWith(helper, new BlockPos(3, 1, 1));
		Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
		float full = pact.getHealth();
		pact.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 10.0F);
		helper.assertValueEqual(pact.getHealth(), full - 5.0F, "the witch takes half");
		helper.assertValueEqual(ally.getHealth(), full - 5.0F, "the ally takes the other half");
		helper.succeed();
	}

	@GameTest
	public void lastRitesHealsWhenAWitchDiesNear(GameTestHelper helper) {
		Witch dead = witchWith(helper, new BlockPos(2, 1, 2));
		Witch mourner = witchWith(helper, new BlockPos(4, 1, 2), "test_last_rites");
		mourner.setHealth(10.0F);
		GeneCombat.onDeath(dead, helper.getLevel(), helper.getLevel().damageSources().generic());
		helper.assertValueEqual(mourner.getHealth(), 16.0F, "the witch heals 6");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void rainDanceHealsOnlyInAStorm(GameTestHelper helper) {
		// The test does not start a storm, because the weather of the level reaches each test that runs now.
		Witch dancer = witchWith(helper, new BlockPos(1, 1, 1), "test_rain_dance");
		Witch healer = witchWith(helper, new BlockPos(1, 1, 4), "test_witch_regen");
		dancer.setHealth(10.0F);
		healer.setHealth(10.0F);
		holdVanillaDrinks(dancer);
		holdVanillaDrinks(healer);
		helper.runAfterDelay(50, () -> {
			if (!helper.getLevel().isThundering()) {
				helper.assertValueEqual(dancer.getHealth(), 10.0F, "no heal out of a storm");
			}
			helper.assertTrue(healer.getHealth() > 10.0F, "the regeneration heals a witch");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 60)
	public void swampStrideIsFasterInWater(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, Blocks.WATER);
		Witch wet = witchWith(helper, pos, "test_swamp_stride");
		Witch dry = witchWith(helper, new BlockPos(1, 1, 4), "test_swamp_stride");
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(wet.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(OriginOfSpawns.id("speed_when/0")),
					"the water modifier is on");
			helper.assertFalse(dry.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(OriginOfSpawns.id("speed_when/0")),
					"no modifier on land");
			helper.succeed();
		});
	}

	@GameTest
	public void anAttributeGeneChangesTheHealthOfTheWitch(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(1, 1, 1), "test_witch_health");
		GeneEffects.applyAttributes(witch, GeneEffects.sources(helper.getLevel().registryAccess(),
				witch.getAttached(ModAttachments.GENES)));
		helper.assertTrue(Math.abs(witch.getMaxHealth() - 23.4F) < 0.001F, "10% less of 26 health, was " + witch.getMaxHealth());
		helper.succeed();
	}

	@GameTest
	public void theWardsChangeTheDamageThatTheWitchTakes(GameTestHelper helper) {
		Witch witch = witchWith(helper, new BlockPos(1, 1, 1), "test_witch_wards");
		var sources = helper.getLevel().damageSources();
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(witch, sources.onFire(), 10.0F) - 13.75F) < 0.001F,
				"25% more fire damage, then 10% more");
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(witch, sources.explosion(null, null), 10.0F) - 7.7F) < 0.001F,
				"30% less explosion damage, then 10% more");
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(witch, sources.generic(), 10.0F) - 11.0F) < 0.001F,
				"10% more damage from the rest");
		helper.succeed();
	}

	@GameTest
	public void aWitchCannotLeadARaidWave(GameTestHelper helper) {
		Raid raid = new Raid(helper.absolutePos(new BlockPos(3, 1, 3)), Difficulty.NORMAL);
		// The game makes the first raider of the wave the leader, before the genes exist.
		Witch first = PillagerEffectsTest.raidMember(helper, raid,
				EntityTypes.WITCH.create(helper.getLevel(), EntitySpawnReason.EVENT), new BlockPos(1, 1, 1), true,
				"test_long_brew");
		helper.assertTrue(raid.getLeader(1) == null, "the witch does not lead the wave");
		helper.assertFalse(first.isPatrolLeader(), "the witch is not a captain");
		helper.assertTrue(first.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "the witch has no banner");
		helper.succeed();
	}

	@GameTest
	public void aVillagerThatLightningHitsBecomesAWitchWithGenes(GameTestHelper helper) {
		Villager villager = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new BlockPos(3, 1, 3));
		helper.assertFalse(villager.hasAttached(ModAttachments.GENES), "a villager has no genes");
		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		bolt.snapTo(villager.position());
		villager.thunderHit(helper.getLevel(), bolt);
		List<Witch> witches = helper.getLevel().getEntitiesOfClass(Witch.class, villager.getBoundingBox().inflate(1.0));
		helper.assertValueEqual(witches.size(), 1, "the villager becomes a witch");
		helper.assertTrue(witches.getFirst().hasAttached(ModAttachments.GENES), "the new witch gets genes");
		helper.succeed();
	}

	/**
	 * Makes the witch drink an empty hand for a long time. A vanilla witch that is hurt drinks Healing at random, and
	 * a witch that drinks starts no other drink.
	 */
	static void holdVanillaDrinks(Witch witch) {
		((WitchAccessor) witch).originofspawns$setUsingTime(10_000);
		witch.setUsingItem(true);
	}

	static RangedAttackGoal rangedGoal(Witch witch, Pig target) {
		witch.setTarget(target);
		RangedAttackGoal goal = ((MobAccessor) witch).originofspawns$goalSelector().getAvailableGoals().stream()
				.map(WrappedGoal::getGoal).filter(RangedAttackGoal.class::isInstance).map(RangedAttackGoal.class::cast)
				.findFirst().orElseThrow();
		goal.canUse();
		goal.start();
		return goal;
	}

	/**
	 * Breaks a potion of Poison of the witch on the ground at the place.
	 */
	static void splashAt(GameTestHelper helper, Witch witch, BlockPos pos, net.minecraft.world.item.Item item) {
		ItemStack stack = PotionContents.createItemStack(item, Potions.POISON);
		AbstractThrownPotion potion = item == Items.LINGERING_POTION
				? new ThrownLingeringPotion(helper.getLevel(), witch, stack)
				: new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(helper.getLevel(),
						witch, stack);
		Vec3 place = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
		potion.setPos(place);
		((ProjectileAccessor) potion).originofspawns$onHit(
				new BlockHitResult(place, Direction.UP, helper.absolutePos(pos.below()), false));
	}

	static ItemStack splash(Holder<net.minecraft.world.item.alchemy.Potion> potion) {
		return PotionContents.createItemStack(Items.SPLASH_POTION, potion);
	}

	static List<MobEffectInstance> effectsOf(ItemStack stack) {
		List<MobEffectInstance> effects = new java.util.ArrayList<>();
		stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effects::add);
		return effects;
	}

	static boolean is(MobEffectInstance instance, Holder<MobEffect> effect) {
		return instance.getEffect().value() == effect.value();
	}

	/**
	 * Returns the one potion that the witch threw. The test discards it before it flies out of the test area.
	 */
	static AbstractThrownPotion potionOf(GameTestHelper helper, Witch witch) {
		List<AbstractThrownPotion> potions = helper.getLevel().getEntitiesOfClass(AbstractThrownPotion.class,
				witch.getBoundingBox().inflate(4.0), potion -> potion.getOwner() == witch && !potion.isRemoved());
		helper.assertValueEqual(potions.size(), 1, "the witch threw one potion");
		return potions.getFirst();
	}

	static Witch witchWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Witch witch = EntityTypes.WITCH.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		witch.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		witch.setNoAi(true);
		witch.setPersistenceRequired();
		witch.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(witch);
		return witch;
	}
}
