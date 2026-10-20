package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.MobInfo;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.goal.LeapGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.LurkGoal;
import io.github.stivengjekaj.originofspawns.evolution.goal.SiegeGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BehaviorEffectsTest {
	@GameTest
	public void silenceAndLoudJoinTheirEffects(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_quiet", "test_loud");
		GeneEffect.Silence silence = GeneBehaviors.silence(zombie);
		helper.assertValueEqual(silence, new GeneEffect.Silence(true, true, false, false), "silence");
		helper.assertValueEqual(GeneBehaviors.volumeMultiplier(zombie), 2.0F, "volume");
		helper.succeed();
	}

	@GameTest
	public void attackIntervalScales(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_fast_attacks");
		helper.assertValueEqual(GeneBehaviors.attackIntervalMultiplier(zombie), 0.5F, "multiplier");
		helper.succeed();
	}

	@GameTest(maxTicks = 300)
	public void regenerateHealsAnIdleZombie(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_regen");
		zombie.setHealth(10.0F);
		// The zombie heals at its next behavior tick. A busy test server can start that tick late, so the test waits.
		helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() > 10.0F, "health was " + zombie.getHealth()));
	}

	@GameTest(maxTicks = 160)
	public void auraGivesThePlayerTheEffect(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_aura");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		helper.runAfterDelay(70, () -> player.setPos(zombie.position()));
		helper.runAfterDelay(120, () -> {
			helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "the player has weakness");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 160)
	public void auraCanHaveItsOwnTime(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_short_aura");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		helper.runAfterDelay(70, () -> player.setPos(zombie.position()));
		helper.succeedWhen(() -> {
			MobEffectInstance darkness = player.getEffect(MobEffects.DARKNESS);
			helper.assertTrue(darkness != null, "the player has darkness");
			helper.assertTrue(darkness.getDuration() <= 40, "the darkness lasts 2 seconds, not "
					+ darkness.getDuration() + " ticks");
		});
	}

	@GameTest
	public void glowingMakesTheZombieGlow(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_glow");
		GeneBehaviors.installGoals(zombie);
		helper.assertTrue(zombie.hasGlowingTag(), "the zombie glows");
		helper.succeed();
	}

	@GameTest
	public void runtMakesASmallBaby(GameTestHelper helper) {
		// The zombie is not in the level, so it gets no genes from the pool when it loads.
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.NATURAL);
		zombie.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)));
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_runt"))));
		double adultHealth = zombie.getAttributeValue(Attributes.MAX_HEALTH);
		GeneBehaviors.onSpawn(zombie, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(zombie.isBaby(), "the zombie is a baby");
		helper.assertValueEqual(zombie.getAttributeValue(Attributes.MAX_HEALTH), adultHealth / 2.0, "maximum health");
		helper.succeed();
	}

	@GameTest
	public void zombiesHusksAndDrownedHaveNoVanillaBabies(GameTestHelper helper) {
		for (EntityType<? extends Zombie> type : List.of(EntityTypes.ZOMBIE, EntityTypes.HUSK, EntityTypes.DROWNED)) {
			int babies = 0;
			for (int i = 0; i < 200; i++) {
				Zombie zombie = type.create(helper.getLevel(), EntitySpawnReason.NATURAL);
				zombie.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(helper.absolutePos(BlockPos.ZERO)),
						EntitySpawnReason.NATURAL, null);
				babies += zombie.isBaby() ? 1 : 0;
			}
			helper.assertValueEqual(babies, 0, "vanilla babies of " + type);
		}
		helper.succeed();
	}

	@GameTest
	public void broodSpawnsAZombieWithTheSameGenes(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(2, 1, 2), "test_brood");
		AABB area = new AABB(zombie.blockPosition()).inflate(3);
		int before = helper.getLevel().getEntitiesOfClass(Zombie.class, area).size();
		GeneBehaviors.onSpawn(zombie, helper.getLevel(), EntitySpawnReason.NATURAL);
		List<Zombie> zombies = helper.getLevel().getEntitiesOfClass(Zombie.class, area);
		helper.assertValueEqual(zombies.size(), before + 1, "zombies");
		helper.assertTrue(zombies.stream().filter(other -> other != zombie)
				.anyMatch(other -> zombie.getAttached(ModAttachments.GENES).equals(other.getAttached(ModAttachments.GENES))),
				"the new zombie has the same genes");
		helper.succeed();
	}

	@GameTest
	public void killedPlayerRisesWithTheHead(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_raise");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 1, 3))));
		GeneBehaviors.raiseVictim(zombie, player, helper.getLevel());
		List<Zombie> raised = helper.getLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(1),
				other -> other != zombie);
		helper.assertValueEqual(raised.size(), 1, "raised zombies");
		helper.assertTrue(raised.getFirst().getItemBySlot(EquipmentSlot.HEAD).is(Items.PLAYER_HEAD), "it wears a head");
		helper.assertTrue(raised.getFirst().getItemBySlot(EquipmentSlot.HEAD).has(DataComponents.PROFILE), "the head has a profile");
		helper.assertValueEqual(raised.getFirst().getAttached(ModAttachments.GENES), zombie.getAttached(ModAttachments.GENES),
				"genes");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void callOnSightCallsTheZombiesNear(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_call_sight");
		Zombie ally = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.setTarget(player);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(ally.getTarget() == player, "the ally targets the player");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 100)
	public void frenzyTargetsAnotherSpecies(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_frenzy_target");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(zombie.getTarget() instanceof Pig, "the zombie targets the pig");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 160)
	public void huntFindsAnInvisiblePlayer(GameTestHelper helper) {
		// The mock server player is always creative, so the test asks the check directly.
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_hunt");
		GeneEffect.Hunt hunt = new GeneEffect.Hunt(8.0F, 1.0F);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(zombie.position().add(4.0, 0.0, 0.0));
		helper.assertFalse(GeneBehaviors.isHunted(zombie, player, hunt), "a visible healthy player");
		player.setInvisible(true);
		helper.assertTrue(GeneBehaviors.isHunted(zombie, player, hunt), "an invisible player at 4 blocks");
		player.setPos(zombie.position().add(12.0, 0.0, 0.0));
		helper.assertFalse(GeneBehaviors.isHunted(zombie, player, hunt), "an invisible player at 12 blocks");
		player.setInvisible(false);
		player.setHealth(4.0F);
		helper.assertTrue(GeneBehaviors.isHunted(zombie, player, hunt), "a wounded player at 12 blocks");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void speedChangesInWater(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, Blocks.WATER);
		Zombie zombie = zombieWith(helper, pos, "test_swim_slow");
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(zombie.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(OriginOfSpawns.id("speed_when/0")),
					"the water modifier is on");
			helper.succeed();
		});
	}

	@GameTest
	public void goalsAreInstalled(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_leap", "test_lurk", "test_siege");
		GeneBehaviors.installGoals(zombie);
		var goals = ((MobAccessor) zombie).originofspawns$goalSelector().getAvailableGoals();
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof LeapGoal), "leap goal");
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof LurkGoal), "lurk goal");
		helper.assertTrue(goals.stream().anyMatch(goal -> goal.getGoal() instanceof SiegeGoal), "siege goal");
		helper.succeed();
	}

	@GameTest
	public void siegeFindsTheGlassOnTheWayToTheTarget(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		Zombie zombie = zombieWith(helper, pos, "test_siege");
		helper.setBlock(pos.east(), Blocks.GLASS);
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos.east(4));
		zombie.setTarget(pig);
		SiegeGoal goal = new SiegeGoal(zombie, new GeneEffect.Siege(10.0F));
		helper.assertTrue(goal.canUse(), "the goal finds the glass");
		helper.setBlock(pos.east(), Blocks.AIR);
		helper.assertFalse(goal.canUse(), "no glass, no goal");
		helper.succeed();
	}

	@GameTest
	public void leapStartsInItsRange(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_leap");
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 1));
		zombie.setTarget(pig);
		// A zombie with no AI does not update its ground state, so the test sets it.
		zombie.setOnGround(true);
		LeapGoal goal = new LeapGoal(zombie, new GeneEffect.Leap(3.0F, 5.0F, 5.0F, 1.0F));
		helper.assertTrue(goal.canUse(), "the leap starts at 4 blocks");
		pig.setPos(zombie.position().add(1.0, 0.0, 0.0));
		helper.assertFalse(goal.canUse(), "no leap at 1 block");
		helper.succeed();
	}

	@GameTest
	public void lurkWaitsOnlyWhenWatchedFromAfar(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_lurk");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		// The player stands 5 blocks south of the zombie. A yaw of 180 faces north, to the zombie.
		player.setPos(zombie.getX(), zombie.getY(), zombie.getZ() + 5.0);
		player.setYRot(180.0F);
		player.setXRot(0.0F);
		helper.assertTrue(LurkGoal.isWatchedFromAfar(zombie, player, 3.0), "watched from 5 blocks");
		player.setYRot(0.0F);
		helper.assertFalse(LurkGoal.isWatchedFromAfar(zombie, player, 3.0), "player looks away");
		player.setYRot(180.0F);
		helper.assertFalse(LurkGoal.isWatchedFromAfar(zombie, player, 6.0), "player is in the radius");
		helper.succeed();
	}

	@GameTest
	public void sunburnHurtsOnlyWhileTheSunBurns(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_sunburn");
		DamageSource fire = helper.getLevel().damageSources().onFire();
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, fire, 2.0F), 2.0F, "fire with no sun");
		Evolution.markSunBurn(zombie);
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, fire, 2.0F), 4.0F, "fire from the sun");
		helper.assertValueEqual(GeneCombat.modifyDamage(zombie, helper.getLevel().damageSources().generic(), 2.0F), 2.0F,
				"other damage while the sun burns");
		helper.succeed();
	}

	@GameTest
	public void golemsTurnToTheBait(GameTestHelper helper) {
		Zombie bait = zombieWith(helper, new BlockPos(1, 1, 1), "test_attract_golems");
		Zombie plain = zombieWith(helper, new BlockPos(5, 1, 5));
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(4, 1, 1));
		golem.setTarget(plain);
		GeneBehaviors.attractGolems(bait, helper.getLevel(), new GeneEffect.AttractGolems(16.0F));
		helper.assertTrue(golem.getTarget() == bait, "the golem attacks the bait first");
		helper.succeed();
	}

	@GameTest
	public void summonReadsGenesFromEntityData(GameTestHelper helper) {
		Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
		List<String> feedback = new java.util.ArrayList<>();
		var source = helper.getLevel().getServer().createCommandSourceStack()
				.withLevel(helper.getLevel()).withPosition(pos)
				.withSource(new net.minecraft.commands.CommandSource() {
					@Override
					public void sendSystemMessage(net.minecraft.network.chat.Component message) {
						feedback.add(message.getString());
					}

					@Override
					public boolean acceptsSuccess() {
						return true;
					}

					@Override
					public boolean acceptsFailure() {
						return true;
					}

					@Override
					public boolean shouldInformAdmins() {
						return false;
					}
				});
		helper.getLevel().getServer().getCommands().performPrefixedCommand(source,
				"summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:[\"oos_summon_test\"],"
						+ "\"fabric:attachments\":{\"originofspawns:genes\":[\"originofspawns:death_throes\"]}}");
		Zombie zombie = helper.getLevel().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Zombie.class),
				z -> z.entityTags().contains("oos_summon_test")).stream().findFirst().orElse(null);
		helper.assertTrue(zombie != null, "the summon made no zombie: " + feedback);
		zombie.discard();
		helper.assertValueEqual(zombie.getAttached(ModAttachments.GENES).genes(),
				List.of(Identifier.fromNamespaceAndPath("originofspawns", "death_throes")), "the genes from the command");
		helper.succeed();
	}

	@GameTest
	public void translucentMobsHideTheirPanelFromAfar(GameTestHelper helper) {
		Zombie shade = zombieWith(helper, new BlockPos(1, 1, 1), "test_always_translucent");
		Zombie plain = zombieWith(helper, new BlockPos(1, 1, 3));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		helper.assertTrue(GeneEffects.translucentOpacity(shade).isPresent(), "the test mob is translucent");
		helper.assertTrue(GeneEffects.translucentOpacity(plain).isEmpty(), "a plain mob is not translucent");
		player.setPos(shade.position().add(5.0, 0.0, 0.0));
		helper.assertFalse(MobInfo.shows(shade, player), "the panel of a translucent mob at 5 blocks");
		helper.assertTrue(MobInfo.shows(plain, player), "the panel of a plain mob at 5 blocks");
		player.setPos(shade.position().add(1.5, 0.0, 0.0));
		helper.assertTrue(MobInfo.shows(shade, player), "the panel of a translucent mob at 1.5 blocks");
		helper.succeed();
	}

	@GameTest
	public void aConvertedMobGetsNewGenes(GameTestHelper helper) {
		Identifier cinder = Identifier.fromNamespaceAndPath("originofspawns", "cinder");
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(cinder)));
		zombie.snapTo(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zombie);
		var drowned = zombie.convertTo(EntityTypes.DROWNED, net.minecraft.world.entity.ConversionParams.single(zombie, true, false),
				converted -> {
				});
		helper.assertTrue(drowned != null, "the zombie converts");
		CarriedGenes genes = drowned.getAttached(ModAttachments.GENES);
		helper.assertTrue(genes != null, "the drowned gets genes from its own pool");
		helper.assertFalse(genes.genes().contains(cinder), "the drowned keeps a zombie gene");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void attributeWhenFollowsTheSand(GameTestHelper helper) {
		BlockPos ground = new BlockPos(1, 0, 1);
		helper.setBlock(ground, Blocks.SAND);
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_sand_armor");
		double base = zombie.getAttribute(Attributes.ARMOR).getBaseValue();
		helper.runAfterDelay(25, () -> {
			helper.assertValueEqual(zombie.getAttributeValue(Attributes.ARMOR), base + 4.0, "armor on sand");
			helper.setBlock(ground, Blocks.STONE);
		});
		helper.runAfterDelay(50, () -> {
			helper.assertValueEqual(zombie.getAttributeValue(Attributes.ARMOR), base, "armor off sand");
			helper.succeed();
		});
	}

	@GameTest
	public void effectOnHitWaitsForItsState(GameTestHelper helper) {
		BlockPos ground = new BlockPos(1, 0, 1);
		helper.setBlock(ground, Blocks.STONE);
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_sand_hit");
		var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 3));
		GeneCombat.onHit(zombie, pig);
		helper.assertFalse(pig.hasEffect(MobEffects.SLOWNESS), "a hit off sand");
		helper.setBlock(ground, Blocks.SAND);
		GeneCombat.onHit(zombie, pig);
		helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "a hit on sand");
		helper.succeed();
	}

	@GameTest
	public void newOptionsReadFromJson(GameTestHelper helper) {
		var ops = com.mojang.serialization.JsonOps.INSTANCE;
		var burrow = (GeneEffect.Burrow) GeneEffect.CODEC.parse(ops, com.google.gson.JsonParser.parseString(
				"{ \"type\": \"burrow\", \"blocks\": \"#minecraft:sand\", \"night_only\": false }")).getOrThrow();
		helper.assertValueEqual(burrow.blocks(), net.minecraft.tags.BlockTags.SAND, "burrow blocks");
		helper.assertFalse(burrow.nightOnly(), "burrow at day");
		var call = (GeneEffect.CallOnHurt) GeneEffect.CODEC.parse(ops, com.google.gson.JsonParser.parseString(
				"{ \"type\": \"call_on_hurt\", \"radius\": 32, \"smoke\": true }")).getOrThrow();
		helper.assertTrue(call.smoke(), "smoke");
		var plain = (GeneEffect.Burrow) GeneEffect.CODEC.parse(ops, com.google.gson.JsonParser.parseString(
				"{ \"type\": \"burrow\" }")).getOrThrow();
		helper.assertTrue(plain.nightOnly(), "a burrow is at night by default");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void burrowRisesToTheSurfaceAndStuns(GameTestHelper helper) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
				helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT);
			}
		}
		Zombie zombie = zombieWith(helper, new BlockPos(1, 3, 1), "test_burrow_stun");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(3, 3, 3))));
		double surface = zombie.getY();
		var burrow = (GeneEffect.Burrow) GeneEffects.of(zombie).findFirst().orElseThrow();
		helper.assertTrue(GeneBehaviors.burrow(zombie, helper.getLevel(), burrow), "the zombie goes under");
		helper.assertValueEqual(zombie.getY(), surface - 2.0, "2 blocks under");
		helper.runAfterDelay(30, () -> {
			helper.assertValueEqual(zombie.getY(), surface, "the zombie ends on the surface");
			long stun = zombie.getAttachedOrElse(ModAttachments.STATE,
					io.github.stivengjekaj.originofspawns.evolution.BehaviorState.NONE).stunUntil();
			helper.assertTrue(stun > helper.getLevel().getGameTime(), "the zombie is stunned after it rises");
			helper.succeed();
		});
	}

	@GameTest
	public void burrowCanRiseNextToThePlayer(GameTestHelper helper) {
		for (int x = 0; x < 6; x++) {
			for (int z = 0; z < 6; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
				helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT);
			}
		}
		Zombie zombie = zombieWith(helper, new BlockPos(0, 3, 0), "test_burrow_near");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 3, 4))));
		var burrow = (GeneEffect.Burrow) GeneEffects.of(zombie).findFirst().orElseThrow();
		helper.assertTrue(GeneBehaviors.burrow(zombie, helper.getLevel(), burrow), "the zombie goes under");
		double distance = Math.hypot(zombie.getX() - player.getX(), zombie.getZ() - player.getZ());
		helper.assertTrue(distance <= 3.0, "the zombie is next to the player, at " + distance);
		helper.succeed();
	}

	@GameTest
	public void sidestepMovesAwayFromAFarWatcher(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			helper.setBlock(new BlockPos(x, 0, 1), Blocks.STONE);
		}
		Zombie zombie = zombieWith(helper, new BlockPos(4, 1, 1), "test_sidestep");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		// The player stays inside the test area, so nothing blocks its view. The range in this test is short.
		player.setPos(zombie.position().add(0.0, 0.0, 5.0));
		player.setYRot(180.0F);
		player.setXRot(0.0F);
		Vec3 before = zombie.position();
		helper.assertTrue(GeneBehaviors.sidestep(zombie, helper.getLevel(), new GeneEffect.Sidestep(4.0F, 3.0F, 10.0F)),
				"the zombie moves");
		helper.assertValueEqual(Math.round(Math.abs(zombie.getX() - before.x)), 3L, "the side distance");
		helper.assertFalse(GeneBehaviors.sidestep(zombie, helper.getLevel(), new GeneEffect.Sidestep(4.0F, 3.0F, 10.0F)),
				"a second move in the cooldown");
		helper.succeed();
	}

	@GameTest
	public void climbersClimbIntoWalls(GameTestHelper helper) {
		Zombie climber = zombieWith(helper, new BlockPos(1, 1, 1), "test_climb");
		Zombie plain = zombieWith(helper, new BlockPos(3, 1, 3));
		climber.horizontalCollision = true;
		plain.horizontalCollision = true;
		helper.assertTrue(climber.onClimbable(), "a climber at a wall");
		helper.assertFalse(plain.onClimbable(), "a plain zombie at a wall");
		climber.horizontalCollision = false;
		helper.assertFalse(climber.onClimbable(), "a climber with no wall");
		helper.succeed();
	}

	@GameTest
	public void throwEffectHitsTheTargetOnce(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_throw");
		var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 1));
		zombie.setTarget(pig);
		var thrown = new GeneEffect.ThrowEffect(MobEffects.BLINDNESS, 2.0F, 0, 8.0F, 8.0F);
		helper.assertTrue(GeneBehaviors.throwEffect(zombie, helper.getLevel(), thrown), "the first throw");
		helper.assertTrue(pig.hasEffect(MobEffects.BLINDNESS), "blindness");
		helper.assertFalse(GeneBehaviors.throwEffect(zombie, helper.getLevel(), thrown), "a throw in the cooldown");
		helper.succeed();
	}

	@GameTest
	public void pickUpItemsLetsTheMobPickUp(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), "test_pick_up");
		zombie.setCanPickUpLoot(false);
		GeneBehaviors.onSpawn(zombie, helper.getLevel(), EntitySpawnReason.NATURAL);
		helper.assertTrue(zombie.canPickUpLoot(), "pickup");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void noConversionKeepsAHuskInWater(GameTestHelper helper) {
		for (BlockPos pos : List.of(new BlockPos(1, 1, 1), new BlockPos(1, 2, 1), new BlockPos(4, 1, 4), new BlockPos(4, 2, 4))) {
			helper.setBlock(pos, Blocks.WATER);
		}
		var salted = net.minecraft.world.entity.EntityTypes.HUSK.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		salted.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(
				Identifier.fromNamespaceAndPath("originofspawns_test", "test_no_conversion"))));
		salted.setPersistenceRequired();
		salted.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(salted);
		var plain = helper.spawnWithNoFreeWill(EntityTypes.HUSK, new BlockPos(4, 1, 4));
		// A husk starts to convert after 600 ticks in water. The test starts the husks near that time.
		salted.setInWaterTime(599);
		plain.setInWaterTime(599);
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(plain.isUnderWaterConverting(), "a plain husk starts to convert");
			helper.assertFalse(salted.isUnderWaterConverting(), "a husk with no conversion");
			helper.succeed();
		});
	}

	private static Zombie zombieWith(GameTestHelper helper, BlockPos pos, String... genes) {
		// The genes go on before the zombie enters the level, so it gets no genes from the pool.
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zombie);
		return zombie;
	}
}
