package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.evolution.LivingEvolution;
import io.github.stivengjekaj.originofspawns.evolution.NeutralMobs;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Checks the genes of a wolf: the pool, the tame rule, the babies, the anger, the calls, and the reused effects.
 */
public class WolfEffectsTest {
	static final Identifier WOLF = Identifier.withDefaultNamespace("wolf");
	static final Identifier PELT = Identifier.fromNamespaceAndPath("originofspawns_test", "test_wolf_pelt");

	static Wolf wolf(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.WOLF, pos, genes);
	}

	static BiomeCategory category(GameTestHelper helper, BlockPos pos) {
		return BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
	}

	@GameTest
	public void aWildWolfGetsGenesFromThePool(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		Wolf wolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, pos);
		pools.setFrequency(WOLF, category, PELT, before);
		CarriedGenes genes = wolf.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null && genes.genes().contains(PELT), "the wolf takes the gene from the pool");
		helper.succeed();
	}

	@GameTest
	public void eachDeathOfAWildWolfCountsAngryOrNot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		Wolf calm = wolf(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		calm.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(calm.isDeadOrDying(), "the calm wolf dies");
		float afterCalm = pools.frequency(WOLF, category, PELT);
		helper.assertTrue(afterCalm > before, "the death of a calm wolf raises the fire gene");
		Wolf angry = wolf(helper, new BlockPos(4, 1, 4));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		angry.setPersistentAngerTarget(EntityReference.of(player));
		angry.startPersistentAngerTimer();
		helper.assertTrue(angry.isAngry(), "the second wolf is angry");
		angry.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(pools.frequency(WOLF, category, PELT) > afterCalm, "the death of an angry wolf raises it too");
		helper.succeed();
	}

	static void tame(Wolf wolf, net.minecraft.world.entity.player.Player owner) {
		wolf.setTame(true, true);
		wolf.setOwnerReference(EntityReference.of(owner));
	}

	@GameTest
	public void aTameWolfKeepsItsGenesAndGetsNoNewGenes(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf wolf = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_pelt");
		wolf.tame(player);
		helper.assertValueEqual(wolf.getAttached(ModAttachments.GENES).genes(), java.util.List.of(PELT),
				"the tame wolf keeps its genes");
		helper.assertTrue(NeutralMobs.outsidePool(wolf), "the tame wolf is out of the pool");
		wolf.setAttached(ModAttachments.BORN, helper.getLevel().getGameTime() - 2L * LivingEvolution.MIN_AGE);
		helper.assertFalse(LivingEvolution.canChange(wolf, helper.getLevel()), "a tame wolf never changes its genes");

		BlockPos pos = new BlockPos(4, 1, 4);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		Wolf old = EntityTypes.WOLF.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		old.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		old.setNoAi(true);
		tame(old, player);
		helper.getLevel().addFreshEntity(old);
		pools.setFrequency(WOLF, category, PELT, before);
		helper.assertFalse(old.hasAttached(ModAttachments.GENES), "a tame wolf with no genes gets none when it loads");
		helper.succeed();
	}

	@GameTest
	public void theMaximumHealthOfATameWolfIsFortyBeforeTheGenes(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf wolf = wolf(helper, new BlockPos(2, 1, 2), "test_wolf_frail");
		// The game puts the attribute genes on a new mob when it rolls the genes. The test mob gets its genes direct.
		io.github.stivengjekaj.originofspawns.evolution.GeneEffects.applyAttributes(wolf,
				io.github.stivengjekaj.originofspawns.evolution.GeneEffects.sources(helper.getLevel().registryAccess(),
						wolf.getAttached(ModAttachments.GENES)));
		helper.assertTrue(Math.abs(wolf.getMaxHealth() - 7.2F) < 0.001F, "the wild wolf has 8 health less 10%");
		wolf.tame(player);
		helper.assertTrue(Math.abs(wolf.getMaxHealth() - 36.0F) < 0.001F, "the tame wolf has 40 health less 10%");
		helper.succeed();
	}

	@GameTest
	public void theDeathAndTheKillsOfATameWolfDoNotCount(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf hunter = wolf(helper, pos, "test_wolf_pelt");
		tame(hunter, owner);
		var victim = CreeperEffectsTest.survivalPlayer(helper).player();
		float before = pools.frequency(WOLF, category, PELT);
		victim.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(hunter), 1000.0F);
		helper.assertTrue(victim.isDeadOrDying(), "the tame wolf kills a player");
		helper.assertValueEqual(pools.frequency(WOLF, category, PELT), before, "the kill of a tame wolf does not count");
		Wolf pet = wolf(helper, new BlockPos(4, 1, 4), "test_wolf_pelt");
		tame(pet, owner);
		pet.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 1000.0F);
		helper.assertTrue(pet.isDeadOrDying(), "the tame wolf dies");
		helper.assertValueEqual(pools.frequency(WOLF, category, PELT), before, "the death of a tame wolf does not count");
		helper.succeed();
	}

	@GameTest
	public void aBredBabyTakesGenesFromThePoolAlsoWhenItIsTame(GameTestHelper helper) {
		BlockPos pos = new BlockPos(3, 1, 3);
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf mother = wolf(helper, pos);
		Wolf father = wolf(helper, new BlockPos(4, 1, 3));
		tame(mother, owner);
		tame(father, owner);
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		BiomeCategory category = category(helper, pos);
		float before = pools.frequency(WOLF, category, PELT);
		pools.setFrequency(WOLF, category, PELT, 1.0F);
		mother.spawnChildFromBreeding(helper.getLevel(), father);
		pools.setFrequency(WOLF, category, PELT, before);
		var babies = helper.getLevel().getEntitiesOfClass(Wolf.class, mother.getBoundingBox().inflate(2.0), Wolf::isBaby);
		helper.assertValueEqual(babies.size(), 1, "the parents make one baby");
		Wolf baby = babies.getFirst();
		helper.assertTrue(baby.isTame(), "the baby of tame wolves is tame");
		CarriedGenes genes = baby.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null && genes.genes().contains(PELT), "the bred baby takes the gene from the pool");
		helper.succeed();
	}

	@GameTest
	public void aCallOfATameWolfReachesOnlyTheWolvesOfItsOwner(GameTestHelper helper) {
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		var stranger = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf caller = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_yelp");
		Wolf packmate = wolf(helper, new BlockPos(3, 1, 1));
		Wolf otherPet = wolf(helper, new BlockPos(5, 1, 1));
		Wolf wild = wolf(helper, new BlockPos(7, 1, 1));
		tame(caller, owner);
		tame(packmate, owner);
		tame(otherPet, stranger);
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 7));
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(caller,
				helper.getLevel().damageSources().mobAttack(pig));
		helper.assertValueEqual(packmate.getTarget(), pig, "a wolf of the same owner answers the call");
		helper.assertTrue(otherPet.getTarget() == null, "a wolf of another owner does not answer");
		helper.assertTrue(wild.getTarget() == null, "a wild wolf does not answer");
		helper.succeed();
	}

	@GameTest
	public void aCallOfAWildWolfReachesOnlyWildWolves(GameTestHelper helper) {
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf caller = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_yelp");
		Wolf wild = wolf(helper, new BlockPos(3, 1, 1));
		Wolf pet = wolf(helper, new BlockPos(5, 1, 1));
		tame(pet, owner);
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 1, 7));
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(caller,
				helper.getLevel().damageSources().mobAttack(pig));
		helper.assertValueEqual(wild.getTarget(), pig, "a wild wolf answers the call");
		helper.assertTrue(pet.getTarget() == null, "a tame wolf does not answer a wild wolf");
		helper.succeed();
	}

	@GameTest
	public void aTameWolfDoesNotCallItsPackAtItsOwner(GameTestHelper helper) {
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf caller = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_yelp");
		Wolf packmate = wolf(helper, new BlockPos(3, 1, 1));
		tame(caller, owner);
		tame(packmate, owner);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(caller,
				helper.getLevel().damageSources().playerAttack(owner));
		helper.assertTrue(packmate.getTarget() == null, "the pack does not attack the owner");
		helper.succeed();
	}

	static void anger(Wolf wolf, net.minecraft.world.entity.player.Player player) {
		wolf.setPersistentAngerTarget(EntityReference.of(player));
		wolf.startPersistentAngerTimer();
	}

	static void auraTick(GameTestHelper helper, Wolf wolf) {
		wolf.tickCount = 20;
		io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors.tick(wolf, helper.getLevel());
	}

	@GameTest
	public void theGainsOfAWildWolfActOnAPlayerOnlyWhileItIsAngry(GameTestHelper helper) {
		Wolf howler = wolf(helper, new BlockPos(1, 1, 1), "test_aura", "test_ambush_bite");
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(3, 1, 1))));
		var hit = helper.getLevel().damageSources().mobAttack(howler);
		auraTick(helper, howler);
		helper.assertFalse(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "a calm wolf gives no Weakness");
		helper.assertValueEqual(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(player, hit, 10.0F),
				10.0F, "a calm wolf has no first strike on the player");
		anger(howler, player);
		auraTick(helper, howler);
		helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "an angry wolf gives Weakness");
		helper.assertTrue(Math.abs(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(player, hit, 10.0F)
				- 15.0F) < 0.001F, "an angry wolf has its first strike on the player");
		helper.succeed();
	}

	@GameTest
	public void theGainsOfATameWolfActOnTheTargetsOfItsOwnerAndNotOnItsOwner(GameTestHelper helper) {
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		owner.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 3))));
		Wolf pet = wolf(helper, new BlockPos(1, 1, 1), "test_aura", "test_ambush_bite", "test_coral", "test_wolf_snarl");
		Wolf packmate = wolf(helper, new BlockPos(5, 1, 5));
		tame(pet, owner);
		tame(packmate, owner);
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		pet.setTarget(pig);
		helper.assertTrue(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.test(pet,
				io.github.stivengjekaj.originofspawns.definition.GeneEffect.When.TARGET_PLAYER),
				"the target of a tame wolf counts as the player of the gains");
		auraTick(helper, pet);
		helper.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "the aura acts on the target");
		helper.assertFalse(owner.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "the aura does not act on the owner");
		helper.assertTrue(Math.abs(io.github.stivengjekaj.originofspawns.evolution.GeneCombat.modifyDamage(pig,
				helper.getLevel().damageSources().mobAttack(pet), 10.0F) - 15.0F) < 0.001F, "the first strike acts on the target");
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(pet, pig);
		helper.assertValueEqual(packmate.getTarget(), pig, "the call on a hit sends the pack at the target");
		float health = owner.getHealth();
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHurt(pet, helper.getLevel().damageSources().playerAttack(owner));
		helper.assertValueEqual(owner.getHealth(), health, "the thorns do not hurt the owner");
		helper.succeed();
	}

	@GameTest
	public void theLastHowlOfATameWolfSendsItsPackAtItsKiller(GameTestHelper helper) {
		var owner = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf pet = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_last_howl");
		Wolf packmate = wolf(helper, new BlockPos(5, 1, 5));
		tame(pet, owner);
		tame(packmate, owner);
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		pet.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 1000.0F);
		helper.assertTrue(pet.isDeadOrDying(), "the pig kills the tame wolf");
		helper.assertValueEqual(packmate.getTarget(), pig, "the pack targets the killer");
		helper.succeed();
	}

	static void howl(GameTestHelper helper, String gene, boolean callsNow) {
		Wolf howler = wolf(helper, new BlockPos(1, 1, 1), gene);
		Wolf packmate = wolf(helper, new BlockPos(5, 1, 5));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(3, 1, 1))));
		howler.setTarget(player);
		auraTick(helper, howler);
		helper.assertTrue((packmate.getTarget() == player) == callsNow,
				callsNow ? "the howl calls the pack while its state is true" : "no howl while its state is false");
	}

	// The test world has one time of day for all tests, so the two tests check the two states of the call.
	@GameTest
	public void aMoonHowlCallsThePackOnlyAtNight(GameTestHelper helper) {
		howl(helper, "test_wolf_moon_howl", helper.getLevel().isDarkOutside());
		helper.succeed();
	}

	@GameTest
	public void aDayHowlCallsThePackOnlyInTheDay(GameTestHelper helper) {
		howl(helper, "test_wolf_day_howl", !helper.getLevel().isDarkOutside());
		helper.succeed();
	}

	@GameTest
	public void anAlphaMarkGivesStrengthToThePackWhenItHitsAPlayer(GameTestHelper helper) {
		Wolf alpha = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_alpha_mark");
		Wolf packmate = wolf(helper, new BlockPos(7, 1, 7));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(3, 1, 1));
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(alpha, pig);
		helper.assertFalse(packmate.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "a hit on prey gives nothing");
		anger(alpha, player);
		io.github.stivengjekaj.originofspawns.evolution.GeneCombat.onHit(alpha, player);
		var strength = packmate.getEffect(net.minecraft.world.effect.MobEffects.STRENGTH);
		helper.assertTrue(strength != null && strength.getDuration() == 100, "the pack gets Strength for 5 seconds");
		helper.assertFalse(alpha.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "the wolf itself gets none");
		helper.succeed();
	}

	@GameTest
	public void aHotTemperMakesTheAngerOfAWolfLastTwoTimesAsLong(GameTestHelper helper) {
		Wolf plain = wolf(helper, new BlockPos(1, 1, 1));
		Wolf hot = wolf(helper, new BlockPos(5, 1, 5), "test_wolf_hot_temper");
		long now = helper.getLevel().getGameTime();
		// The vanilla anger of a wolf lasts 20 to 39 seconds.
		for (int i = 0; i < 20; i++) {
			plain.startPersistentAngerTimer();
			hot.startPersistentAngerTimer();
			long plainTicks = plain.getPersistentAngerEndTime() - now;
			long hotTicks = hot.getPersistentAngerEndTime() - now;
			helper.assertTrue(plainTicks >= 400 && plainTicks <= 780, "the vanilla anger time, not " + plainTicks);
			helper.assertTrue(hotTicks >= 800 && hotTicks <= 1560, "two times the anger time, not " + hotTicks);
		}
		helper.succeed();
	}

	@GameTest
	public void anOldGrudgeStaysAngryAtAPlayerThatDied(GameTestHelper helper) {
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		Wolf plain = wolf(helper, new BlockPos(1, 1, 1));
		Wolf grudge = wolf(helper, new BlockPos(5, 1, 5), "test_wolf_old_grudge");
		for (Wolf each : java.util.List.of(plain, grudge)) {
			each.setPersistentAngerTarget(EntityReference.of(player));
			each.setTimeToRemainAngry(100L);
			each.setTarget(player);
			each.playerDied(helper.getLevel(), player);
		}
		helper.assertFalse(plain.isAngry(), "a plain wolf forgives a dead player");
		helper.assertTrue(grudge.isAngry(), "Old Grudge stays angry");
		helper.assertValueEqual(grudge.getPersistentAngerEndTime() - helper.getLevel().getGameTime(), 6000L,
				"it stays angry for 5 minutes");
		helper.assertTrue(grudge.getTarget() == null, "it does not chase the dead body");
		helper.succeed();
	}

	@GameTest
	public void aBayerGeneMakesTheAngerOfThePackReachTwoTimesAsFar(GameTestHelper helper) {
		var pig = CreeperEffectsTest.pigAt(helper, new BlockPos(4, 1, 1));
		Wolf other = wolf(helper, new BlockPos(6, 1, 4));
		// The test makes the follow range 2 blocks, so the call stays in the test area. The other wolf is 5 blocks
		// away: out of the vanilla call, and in the call that reaches two times as far.
		for (String gene : new String[] {null, "test_wolf_bayer"}) {
			Wolf caller = gene == null ? wolf(helper, new BlockPos(1, 1, 4)) : wolf(helper, new BlockPos(1, 1, 4), gene);
			caller.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(2.0);
			caller.setLastHurtByMob(pig);
			new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(caller).setAlertOthers().start();
			helper.assertValueEqual(other.getTarget() == pig, gene != null, "the call of the pack reaches the other wolf");
			other.setTarget(null);
			caller.discard();
		}
		helper.succeed();
	}

	@GameTest
	public void aSnowPawsWolfWalksOnPowderSnowAndFindsPathsOverIt(GameTestHelper helper) {
		Wolf plain = wolf(helper, new BlockPos(1, 1, 1));
		Wolf walker = wolf(helper, new BlockPos(5, 1, 5), "test_snowshoe");
		var onTop = net.minecraft.world.level.pathfinder.PathType.ON_TOP_OF_POWDER_SNOW;
		helper.assertTrue(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(walker),
				"the gene wolf walks on powder snow");
		helper.assertFalse(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(plain),
				"a plain wolf sinks in powder snow");
		helper.assertValueEqual(walker.getPathfindingMalus(onTop), 0.0F, "the gene wolf finds paths over powder snow");
		helper.assertValueEqual(plain.getPathfindingMalus(onTop), -1.0F, "a plain wolf finds no path over powder snow");
		helper.succeed();
	}

	static final ReusedEffects.Maker WOLF_MAKER = (helper, pos, genes) -> wolf(helper, pos, genes);

	@GameTest
	public void theReusedCombatEffectsWorkOnAWolf(GameTestHelper helper) {
		ReusedEffects.thorns(helper, WOLF_MAKER);
		ReusedEffects.damageTaken(helper, WOLF_MAKER);
		ReusedEffects.armorBreak(helper, WOLF_MAKER);
		helper.succeed();
	}

	@GameTest
	public void aPackBondAddsDamageForEachWolfNear(GameTestHelper helper) {
		ReusedEffects.rally(helper, WOLF_MAKER, "test_rally_cry");
		helper.succeed();
	}

	@GameTest
	public void aSpringLeapLeapsAtThePrey(GameTestHelper helper) {
		ReusedEffects.leap(helper, WOLF_MAKER);
		helper.succeed();
	}

	@GameTest
	public void aWeaveStepsAsideFromAnAimedBow(GameTestHelper helper) {
		ReusedEffects.sidestep(helper, WOLF_MAKER);
		helper.succeed();
	}

	@GameTest
	public void sharedWoundsSendAQuarterOfTheDamageToTheWolvesNear(GameTestHelper helper) {
		Wolf hit = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_shared_wounds");
		Wolf packmate = wolf(helper, new BlockPos(3, 1, 1));
		float full = hit.getHealth();
		hit.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 4.0F);
		helper.assertValueEqual(hit.getHealth(), full - 3.0F, "the hit wolf takes three quarters");
		helper.assertValueEqual(packmate.getHealth(), full - 1.0F, "the wolf near takes a quarter");
		helper.succeed();
	}

	@GameTest(maxTicks = 300)
	public void groomingHealsAWolfInAGroup(GameTestHelper helper) {
		Wolf groomer = wolf(helper, new BlockPos(1, 1, 1), "test_wolf_grooming");
		wolf(helper, new BlockPos(3, 1, 1));
		groomer.setHealth(4.0F);
		// The wolf heals at its next behavior tick. A busy test server can start that tick late, so the test waits.
		helper.succeedWhen(() -> helper.assertTrue(groomer.getHealth() > 4.0F, "health was " + groomer.getHealth()));
	}

	@GameTest
	public void aLoudWolfIsHeardFromTwoTimesFarther(GameTestHelper helper) {
		Wolf plain = wolf(helper, new BlockPos(1, 1, 1));
		Wolf loud = wolf(helper, new BlockPos(5, 1, 5), "test_wolf_loud");
		float volume = ((io.github.stivengjekaj.originofspawns.mixin.WolfAccessor) plain).originofspawns$soundVolume();
		helper.assertValueEqual(volume, 0.4F, "the vanilla volume of a wolf");
		helper.assertValueEqual(((io.github.stivengjekaj.originofspawns.mixin.WolfAccessor) loud).originofspawns$soundVolume(),
				0.8F, "two times the volume");
		helper.succeed();
	}
}
