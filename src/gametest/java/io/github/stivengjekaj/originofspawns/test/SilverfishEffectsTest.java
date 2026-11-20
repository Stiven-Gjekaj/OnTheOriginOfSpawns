package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneSilverfish;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the genes of a silverfish: the wake of the hidden silverfish, the hide in stone, the gnaw, the eggs, and the
 * reused effects.
 */
public class SilverfishEffectsTest {
	static Silverfish silverfish(GameTestHelper helper, BlockPos pos, String... genes) {
		return MountEffectsTest.mob(helper, EntityTypes.SILVERFISH, pos, genes);
	}

	/**
	 * Hurts the silverfish, and then runs its vanilla wake goal until the wake comes. The silverfish has no AI, so the
	 * test runs the goal itself.
	 */
	static void hurtAndWake(GameTestHelper helper, Silverfish silverfish, Pig attacker) {
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), 0.5F);
		WrappedGoal wake = ((MobAccessor) silverfish).originofspawns$goalSelector().getAvailableGoals().stream()
				.filter(goal -> goal.getGoal().getClass().getSimpleName().equals("SilverfishWakeUpFriendsGoal"))
				.findFirst().orElseThrow();
		for (int i = 0; i < 25; i++) {
			wake.getGoal().tick();
		}
	}

	@GameTest(padding = 16)
	public void aWakeRangeGeneWakesTheHiddenSilverfishFartherAway(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 3));
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1));
		// 12 blocks to the east: past the vanilla range of 10, in the gene range of 14.
		BlockPos far = new BlockPos(13, 1, 1);
		helper.setBlock(far, Blocks.INFESTED_STONE);
		hurtAndWake(helper, silverfish, pig);
		helper.assertBlockPresent(Blocks.INFESTED_STONE, far);
		silverfish.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_nest_alarm"))));
		hurtAndWake(helper, silverfish, pig);
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, far);
		helper.succeed();
	}

	@GameTest(padding = 16)
	public void aStoneAlarmWakesTheHiddenSilverfishWhenItSeesItsTarget(GameTestHelper helper) {
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1), "test_stone_alarm");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 1, 1))));
		silverfish.setTarget(player);
		BlockPos hidden = new BlockPos(1, 1, 6);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		var alarm = new GeneEffect.WakeOnSight(8, 10.0F);
		helper.assertTrue(GeneSilverfish.wakeOnSight(silverfish, helper.getLevel(), alarm), "the silverfish wakes the others");
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, hidden);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		helper.assertFalse(GeneSilverfish.wakeOnSight(silverfish, helper.getLevel(), alarm), "the next wake waits");
		helper.assertBlockPresent(Blocks.INFESTED_STONE, hidden);
		helper.succeed();
	}

	@GameTest(padding = 16)
	public void aDeathCryWakesTheHiddenSilverfishWhenABlastKillsIt(GameTestHelper helper) {
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1), "test_death_cry");
		BlockPos hidden = new BlockPos(1, 1, 7);
		helper.setBlock(hidden, Blocks.INFESTED_STONE);
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().explosion(null, null), 100.0F);
		helper.assertTrue(silverfish.isDeadOrDying(), "the blast kills the silverfish");
		helper.assertBlockNotPresent(Blocks.INFESTED_STONE, hidden);
		helper.succeed();
	}

	/**
	 * Puts stone on the six sides of the place, so that a silverfish there can hide in each direction.
	 */
	static void encase(GameTestHelper helper, BlockPos pos) {
		for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
			helper.setBlock(pos.relative(direction), Blocks.STONE);
		}
	}

	/**
	 * Returns the silverfish in the block at the place, other than the one that the test made.
	 */
	static java.util.List<Silverfish> cameOut(GameTestHelper helper, BlockPos pos, Silverfish old) {
		return helper.getLevel().getEntitiesOfClass(Silverfish.class,
				new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(1.5), other -> other != old);
	}

	@GameTest
	public void aSilverfishThatHidesInStoneComesOutWithItsGenesAndFullHealth(GameTestHelper helper) {
		BlockPos place = new BlockPos(2, 2, 2);
		encase(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_lodge_health");
		WrappedGoal merge = ((MobAccessor) silverfish).originofspawns$goalSelector().getAvailableGoals().stream()
				.filter(goal -> goal.getGoal().getClass().getSimpleName().equals("SilverfishMergeWithStoneGoal"))
				.findFirst().orElseThrow();
		// The vanilla goal hides with a chance at each try. Else it starts a walk, and the mob has no AI to walk.
		for (int i = 0; i < 500 && !silverfish.isRemoved(); i++) {
			if (merge.getGoal().canUse()) {
				merge.getGoal().start();
			}
		}
		helper.assertTrue(silverfish.isRemoved(), "the silverfish hides in the stone");
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		java.util.List<Silverfish> out = cameOut(helper, hidden, silverfish);
		helper.assertValueEqual(out.size(), 1, "one silverfish comes out");
		Silverfish next = out.getFirst();
		helper.assertValueEqual(next.getAttached(ModAttachments.GENES).genes(), java.util.List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_lodge_health")), "it has the genes of the first one");
		helper.assertValueEqual(next.getMaxHealth(), 12.0F, "its genes change its maximum health");
		helper.assertValueEqual(next.getHealth(), 12.0F, "it has full health");
		helper.succeed();
	}

	/**
	 * Fills a cube of stone round the place, with the place itself empty.
	 */
	static void bury(GameTestHelper helper, BlockPos pos) {
		for (BlockPos stone : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 3, 2))) {
			helper.setBlock(stone, stone.equals(pos) ? Blocks.AIR : Blocks.STONE);
		}
	}

	/**
	 * Hurts the silverfish. A second hit in the same second does only the damage above the first hit.
	 */
	static void hit(GameTestHelper helper, Silverfish silverfish, Pig attacker, float damage) {
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), damage);
	}

	@GameTest
	public void aDeepLodgeHidesInStoneUnderHalfHealthOneTimeInTheCooldown(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_deep_lodge");
		hit(helper, silverfish, pig, 1.0F);
		helper.assertFalse(silverfish.isRemoved(), "a hit that leaves half health or more does not make it hide");
		// The second hit does 4 damage more than the first: 3 health is left, under half of 8.
		hit(helper, silverfish, pig, 5.0F);
		helper.assertTrue(silverfish.isRemoved(), "a hit under half health makes it hide");
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		Silverfish next = cameOut(helper, hidden, silverfish).getFirst();
		helper.assertValueEqual(next.getHealth(), 8.0F, "it comes out with full health");
		hit(helper, next, pig, 5.0F);
		helper.assertTrue(next.isAlive() && !next.isRemoved(), "the cooldown of the hide goes on after it comes out");
		helper.succeed();
	}

	@GameTest
	public void anEggLodgeLetsTwoSilverfishOutOfItsBlock(GameTestHelper helper) {
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_egg_lodge");
		helper.assertTrue(GeneSilverfish.hideInStone(silverfish, helper.getLevel()), "the silverfish hides");
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		java.util.List<Silverfish> out = cameOut(helper, hidden, silverfish);
		helper.assertValueEqual(out.size(), 2, "two silverfish come out");
		Identifier lodge = Identifier.fromNamespaceAndPath("originofspawns_test", "test_egg_lodge");
		helper.assertValueEqual(out.stream().filter(fish -> fish.getAttachedOrElse(ModAttachments.GENES, CarriedGenes.NONE)
				.genes().contains(lodge)).count(), 1L, "only the first one has the genes of the silverfish that hid");
		helper.succeed();
	}

	@GameTest
	public void aStoneSeedInfestsTwoMoreBlocksWhenItHides(GameTestHelper helper) {
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_stone_seed");
		helper.assertTrue(GeneSilverfish.hideInStone(silverfish, helper.getLevel()), "the silverfish hides");
		long infested = java.util.stream.StreamSupport.stream(
				BlockPos.betweenClosed(place.offset(-2, -1, -2), place.offset(2, 3, 2)).spliterator(), false)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).count();
		helper.assertValueEqual(infested, 3L, "the block of the silverfish and two blocks next to it are infested");
		helper.succeed();
	}

	@GameTest
	public void aTunnelGnawGnawsThroughTheStoneToATargetThatItCannotReach(GameTestHelper helper) {
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(7, 2, 3));
		Silverfish silverfish = silverfish(helper, place, "test_tunnel_gnaw");
		silverfish.setTarget(pig);
		var gnaw = new GeneEffect.Gnaw(2.0F);
		var gnawed = GeneSilverfish.gnaw(silverfish, helper.getLevel(), gnaw);
		helper.assertValueEqual(gnawed.orElseThrow(), helper.absolutePos(place.east()), "it gnaws the block toward the pig");
		helper.assertBlockPresent(Blocks.AIR, place.east());
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				silverfish.getBoundingBox().inflate(3.0)).isEmpty(), "the block drops nothing");
		helper.assertTrue(GeneSilverfish.gnaw(silverfish, helper.getLevel(), gnaw).isEmpty(), "the next gnaw waits");
		helper.succeed();
	}

	@GameTest
	public void aShellSlipLetsAMeleeHitMissButNotAnArrow(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1), "test_shell_slip");
		helper.assertFalse(silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(pig), 2.0F),
				"the melee hit misses");
		helper.assertValueEqual(silverfish.getHealth(), 8.0F, "a missed hit does no damage");
		helper.assertValueEqual(silverfish.getDeltaMovement().horizontalDistance(), 0.0, "a missed hit does no knockback");
		var arrow = EntityTypes.ARROW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		helper.assertTrue(silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(arrow, pig), 2.0F),
				"an arrow hits");
		helper.succeed();
	}

	@GameTest
	public void aBanePlateTakesNoExtraDamageAndNoSlownessFromBaneOfArthropods(GameTestHelper helper) {
		Silverfish plated = silverfish(helper, new BlockPos(1, 1, 1), "test_bane_plate");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		var sword = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
		sword.enchant(helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS), 2);
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, sword);
		var slowness = new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 40, 3);
		helper.assertTrue(plated.addEffect(new net.minecraft.world.effect.MobEffectInstance(slowness)),
				"Slowness from other sources still acts");
		plated.removeAllEffects();
		// 7 damage of the sword and 5 extra damage of Bane of Arthropods II.
		helper.assertTrue(Math.abs(GeneCombat.modifyDamage(plated, helper.getLevel().damageSources().playerAttack(player), 12.0F)
				- 7.0F) < 0.001F, "no extra damage from Bane of Arthropods II");
		helper.assertFalse(plated.addEffect(new net.minecraft.world.effect.MobEffectInstance(slowness)),
				"the hit of the enchantment gives no Slowness");
		helper.succeed();
	}

	@GameTest
	public void anEggSacLetsASilverfishOutWhenAProjectileHurtsIt(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(5, 1, 5));
		Silverfish silverfish = silverfish(helper, new BlockPos(2, 1, 2), "test_egg_sac");
		hit(helper, silverfish, pig, 1.0F);
		helper.assertTrue(cameOut(helper, new BlockPos(2, 1, 2), silverfish).isEmpty(), "a melee hit lets nothing out");
		var arrow = EntityTypes.ARROW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		silverfish.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(arrow, pig), 2.0F);
		java.util.List<Silverfish> out = cameOut(helper, new BlockPos(2, 1, 2), silverfish);
		helper.assertValueEqual(out.size(), 1, "an arrow lets one silverfish out");
		helper.assertValueEqual(out.getFirst().getTarget(), pig, "the new silverfish attacks the shooter");
		helper.assertValueEqual(GeneCombat.broodOnHurt(silverfish, helper.getLevel(),
				helper.getLevel().damageSources().arrow(arrow, pig)), 0, "the next brood waits");
		helper.succeed();
	}

	@GameTest
	public void aStoneAmbushHitsHarderOneTimeAfterItComesOutOfABlock(GameTestHelper helper) {
		BlockPos place = new BlockPos(3, 2, 3);
		bury(helper, place);
		Silverfish silverfish = silverfish(helper, place, "test_stone_ambush");
		GeneSilverfish.hideInStone(silverfish, helper.getLevel());
		BlockPos hidden = java.util.Arrays.stream(net.minecraft.core.Direction.values()).map(place::relative)
				.filter(pos -> helper.getBlockState(pos).is(Blocks.INFESTED_STONE)).findFirst().orElseThrow();
		helper.getLevel().destroyBlock(helper.absolutePos(hidden), true);
		Silverfish next = cameOut(helper, hidden, silverfish).getFirst();
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		var bite = helper.getLevel().damageSources().mobAttack(next);
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 1.0F), 3.0F, "the first hit does 2 more damage");
		helper.assertValueEqual(GeneCombat.modifyDamage(pig, bite, 1.0F), 1.0F, "the second hit does normal damage");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void anEggSpitGivesInfestedToItsTargetFromSixBlocks(GameTestHelper helper) {
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(1, 1, 5));
		Silverfish silverfish = silverfish(helper, new BlockPos(1, 1, 1));
		silverfish.setTarget(pig);
		var spit = new GeneEffect.Spit(6.0F, 6.0F, 1.0F,
				java.util.Optional.of(net.minecraft.world.effect.MobEffects.INFESTED), 4.0F, 0);
		helper.assertTrue(GeneBehaviors.spit(silverfish, helper.getLevel(), spit), "the silverfish spits at the pig");
		helper.assertFalse(GeneBehaviors.spit(silverfish, helper.getLevel(), spit), "the next spit waits");
		Silverfish far = silverfish(helper, new BlockPos(7, 1, 7));
		far.setTarget(pig);
		helper.assertFalse(GeneBehaviors.spit(far, helper.getLevel(), spit), "a target past 6 blocks gets no spit");
		helper.succeedWhen(() -> {
			helper.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.INFESTED), "the spit gives Infested");
			helper.assertValueEqual(pig.getMaxHealth() - pig.getHealth(), 1.0F, "the spit does 1 damage");
		});
	}

	static final ReusedEffects.Maker SILVERFISH = (helper, pos, genes) -> silverfish(helper, pos, genes);

	@GameTest
	public void theReusedHitEffectsWorkOnASilverfish(GameTestHelper helper) {
		ReusedEffects.effectOnHit(helper, SILVERFISH);
		ReusedEffects.armorBreak(helper, SILVERFISH);
		// The reused heal check starts at 10 health, more than a silverfish has, so this check starts lower.
		Silverfish feeder = silverfish(helper, new BlockPos(6, 1, 1), "test_bloodlust");
		feeder.setHealth(4.0F);
		GeneCombat.onHit(feeder, CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 3)));
		helper.assertValueEqual(feeder.getHealth(), 5.0F, "Feeding Bite: each hit heals 1 health");
		ReusedEffects.damageTaken(helper, SILVERFISH);
		helper.succeed();
	}

	@GameTest
	public void theReusedMovesWorkOnASilverfish(GameTestHelper helper) {
		ReusedEffects.leap(helper, SILVERFISH);
		ReusedEffects.sidestep(helper, SILVERFISH);
		Silverfish climber = silverfish(helper, new BlockPos(6, 1, 6), "test_climb");
		climber.horizontalCollision = true;
		helper.assertTrue(climber.onClimbable(), "Wall Climb: a silverfish climbs at a wall");
		helper.succeed();
	}

	@GameTest
	public void aGroupGeneWorksOnASilverfish(GameTestHelper helper) {
		ReusedEffects.groupedAndAlone(helper, SILVERFISH);
		helper.succeed();
	}

	@GameTest
	public void aSwarmRallyCallsTheSilverfishToThePlayer(GameTestHelper helper) {
		ReusedEffects.callOnSight(helper, SILVERFISH);
		helper.succeed();
	}

	@GameTest
	public void aBloodCallCallsOnlyTheSilverfishWithNoTarget(GameTestHelper helper) {
		Silverfish caller = silverfish(helper, new BlockPos(4, 1, 4), "test_blood_call");
		Silverfish free = silverfish(helper, new BlockPos(2, 1, 4));
		Silverfish busy = silverfish(helper, new BlockPos(6, 1, 4));
		Pig other = CreeperEffectsTest.pigAt(helper, new BlockPos(6, 1, 6));
		busy.setTarget(other);
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		GeneCombat.onHit(caller, player);
		helper.assertTrue(free.getTarget() == player, "a silverfish with no target attacks the player");
		helper.assertTrue(busy.getTarget() == other, "a silverfish with a target keeps it");
		helper.succeed();
	}

	@GameTest
	public void anAlarmDashGivesSpeedToTheSilverfishAndToTheSilverfishNearIt(GameTestHelper helper) {
		Silverfish dasher = silverfish(helper, new BlockPos(1, 1, 1), "test_alarm_dash");
		Silverfish ally = silverfish(helper, new BlockPos(4, 1, 4));
		Pig pig = CreeperEffectsTest.pigAt(helper, new BlockPos(2, 1, 1));
		GeneCombat.onHurt(dasher, helper.getLevel().damageSources().mobAttack(pig));
		helper.assertTrue(dasher.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the hurt silverfish gets Speed");
		helper.assertTrue(ally.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the silverfish near it gets Speed");
		helper.assertFalse(pig.hasEffect(net.minecraft.world.effect.MobEffects.SPEED), "the attacker does not");
		helper.succeed();
	}

	@GameTest
	public void aBroodBurstLetsTwoSilverfishOutAtItsDeath(GameTestHelper helper) {
		Silverfish mother = silverfish(helper, new BlockPos(2, 1, 2), "test_brood_burst");
		GeneCombat.onDeath(mother, helper.getLevel());
		helper.assertValueEqual(cameOut(helper, new BlockPos(2, 1, 2), mother).size(), 2, "two silverfish come out");
		helper.succeed();
	}

	@GameTest
	public void aFlatBodyTakesNoExtraDamageFromACriticalHit(GameTestHelper helper) {
		Silverfish flat = silverfish(helper, new BlockPos(1, 1, 1), "test_crit_immune");
		ServerPlayer player = CreeperEffectsTest.survivalPlayer(helper).player();
		io.github.stivengjekaj.originofspawns.evolution.Evolution.markCritical(flat);
		helper.assertValueEqual(GeneCombat.modifyDamage(flat, helper.getLevel().damageSources().playerAttack(player), 6.0F),
				4.0F, "a critical hit does the damage of a normal hit");
		helper.succeed();
	}
}
