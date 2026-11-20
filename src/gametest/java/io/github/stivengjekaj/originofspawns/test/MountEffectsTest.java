package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.phys.Vec3;

/**
 * Checks the effects that all four mounts share: the guard of the riders, the effects on the riders, and the kick.
 * Each test runs on the camel husk, the zombie nautilus, the zombie horse, and the skeleton horse.
 */
public class MountEffectsTest {
	/**
	 * Each mount with the rider that it has in vanilla.
	 */
	record Pair(EntityType<? extends Mob> mount, EntityType<? extends Mob> rider) {
	}

	static final List<Pair> MOUNTS = List.of(
			new Pair(EntityTypes.CAMEL_HUSK, EntityTypes.HUSK),
			new Pair(EntityTypes.ZOMBIE_NAUTILUS, EntityTypes.DROWNED),
			new Pair(EntityTypes.ZOMBIE_HORSE, EntityTypes.ZOMBIE),
			new Pair(EntityTypes.SKELETON_HORSE, EntityTypes.SKELETON));

	/**
	 * Makes a mob with the test genes and no AI, at the place in the test area.
	 */
	static <T extends Mob> T mob(GameTestHelper helper, EntityType<T> type, BlockPos pos, String... genes) {
		T mob = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		mob.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		mob.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(mob);
		return mob;
	}

	/**
	 * Makes a mount with the test genes and puts a rider with no genes on it.
	 */
	static Mob rider(GameTestHelper helper, Pair pair, BlockPos pos, String... genes) {
		Mob mount = mob(helper, pair.mount(), pos, genes);
		Mob rider = mob(helper, pair.rider(), pos);
		helper.assertTrue(rider.startRiding(mount, true, false), "the rider gets on the " + pair.mount().toShortString());
		return rider;
	}

	@GameTest
	public void theRidersOfAMountGetItsGuardAndTheMountDoesNot(GameTestHelper helper) {
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(7, 1, 7));
		var player = CreeperEffectsTest.survivalPlayer(helper).player();
		var sword = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
		sword.enchant(helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SMITE), 2);
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, sword);
		int x = 1;
		for (Pair pair : MOUNTS) {
			Mob rider = rider(helper, pair, new BlockPos(x, 1, 2), "test_mount_guard");
			Mob mount = (Mob) rider.getVehicle();
			String name = pair.mount().toShortString();
			var level = helper.getLevel();
			helper.assertValueEqual(GeneCombat.modifyDamage(rider, level.damageSources().inFire(), 4.0F), 2.0F,
					"the rider of the " + name + " takes half the fire damage");
			helper.assertValueEqual(GeneCombat.modifyDamage(mount, level.damageSources().inFire(), 4.0F), 4.0F,
					"the " + name + " itself takes all the fire damage");
			helper.assertValueEqual(GeneCombat.modifyDamage(golem, level.damageSources().mobAttack(rider), 4.0F), 8.0F,
					"the rider of the " + name + " does two times the damage to a golem");
			helper.assertTrue(Math.abs(GeneCombat.modifyDamage(rider, level.damageSources().playerAttack(player), 11.0F)
					- 8.5F) < 0.001F, "the rider of the " + name + " takes half the extra damage of Smite II");
			helper.assertTrue(GeneEffects.translucentOpacity(rider).isPresent(),
					"the rider of the " + name + " is translucent");
			rider.stopRiding();
			helper.assertValueEqual(GeneCombat.modifyDamage(rider, level.damageSources().inFire(), 4.0F), 4.0F,
					"the rider off the " + name + " loses the guard");
			x += 2;
		}
		helper.succeed();
	}

	@GameTest
	public void aWoundedRiderGetsTheEffectOneTimeInTheCooldown(GameTestHelper helper) {
		int x = 1;
		for (Pair pair : MOUNTS) {
			String name = pair.mount().toShortString();
			Mob wounded = rider(helper, pair, new BlockPos(x, 1, 2), "test_rider_bond");
			Mob healthy = rider(helper, pair, new BlockPos(x, 1, 6), "test_rider_bond");
			wounded.setHealth(wounded.getMaxHealth() * 0.4F);
			healthy.setHealth(healthy.getMaxHealth() * 0.6F);
			for (Mob rider : List.of(wounded, healthy)) {
				Mob mount = (Mob) rider.getVehicle();
				mount.tickCount = 20;
				GeneBehaviors.tick(mount, helper.getLevel());
			}
			helper.assertTrue(wounded.hasEffect(MobEffects.RESISTANCE), "the wounded rider of the " + name + " gets Resistance");
			helper.assertFalse(healthy.hasEffect(MobEffects.RESISTANCE), "a rider over half health gets nothing");
			wounded.removeEffect(MobEffects.RESISTANCE);
			Mob mount = (Mob) wounded.getVehicle();
			mount.tickCount = 40;
			GeneBehaviors.tick(mount, helper.getLevel());
			helper.assertFalse(wounded.hasEffect(MobEffects.RESISTANCE), "the next one waits 30 seconds");
			x += 2;
		}
		helper.succeed();
	}

	@GameTest
	public void theRidersGetResistanceWhenTheMountDiesAndAMobNearItDoesNot(GameTestHelper helper) {
		int x = 1;
		for (Pair pair : MOUNTS) {
			String name = pair.mount().toShortString();
			Mob rider = rider(helper, pair, new BlockPos(x, 1, 2), "test_last_carry");
			Mob near = mob(helper, pair.rider(), new BlockPos(x, 1, 3));
			Mob mount = (Mob) rider.getVehicle();
			mount.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);
			helper.assertTrue(mount.isDeadOrDying(), "the " + name + " dies");
			helper.assertTrue(rider.hasEffect(MobEffects.RESISTANCE), "the rider of the " + name + " gets Resistance");
			helper.assertFalse(near.hasEffect(MobEffects.RESISTANCE), "a mob near the " + name + " gets nothing");
			x += 2;
		}
		helper.succeed();
	}

	@GameTest
	public void aMeleeHitMakesTheMountKickTheAttackerAndTheMobsNearIt(GameTestHelper helper) {
		var level = helper.getLevel();
		int x = 1;
		for (Pair pair : MOUNTS) {
			String name = pair.mount().toShortString();
			Mob rider = rider(helper, pair, new BlockPos(x, 1, 2), "test_kick");
			Mob mount = (Mob) rider.getVehicle();
			var attacker = CreeperEffectsTest.pigAt(helper, new BlockPos(x, 1, 4));
			var near = CreeperEffectsTest.pigAt(helper, new BlockPos(x + 1, 1, 3));
			mount.hurtServer(level, level.damageSources().mobAttack(attacker), 1.0F);
			helper.assertValueEqual(attacker.getMaxHealth() - attacker.getHealth(), 6.0F,
					"the kick of the " + name + " does the largest kick damage and 2 more");
			helper.assertTrue(attacker.hasEffect(MobEffects.BLINDNESS), "the kick of the " + name + " gives Blindness");
			helper.assertTrue(attacker.getDeltaMovement().z > 1.0, "the kick of the " + name + " pushes the attacker away, two times as far");
			helper.assertValueEqual(near.getMaxHealth() - near.getHealth(), 6.0F, "the kick of the " + name + " hits a pig near it");
			helper.assertValueEqual(rider.getHealth(), rider.getMaxHealth(), "the kick of the " + name + " does not hit its rider");
			attacker.discard();
			near.discard();
			x += 2;
		}
		helper.succeed();
	}

	@GameTest
	public void theKickActsOnlyOnAMeleeHitAndOnlyWithAKick(GameTestHelper helper) {
		var level = helper.getLevel();
		int x = 1;
		for (Pair pair : MOUNTS) {
			String name = pair.mount().toShortString();
			Mob kicker = mob(helper, pair.mount(), new BlockPos(x, 1, 2), "test_kick");
			Mob plain = mob(helper, pair.mount(), new BlockPos(x, 1, 6), "test_rider_bond");
			var shooter = CreeperEffectsTest.pigAt(helper, new BlockPos(x, 1, 4));
			var snowball = new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(level, shooter,
					new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SNOWBALL));
			kicker.hurtServer(level, level.damageSources().thrown(snowball, shooter), 1.0F);
			helper.assertValueEqual(shooter.getHealth(), shooter.getMaxHealth(), "a projectile hit makes no kick of the " + name);
			plain.hurtServer(level, level.damageSources().mobAttack(shooter), 1.0F);
			helper.assertValueEqual(shooter.getHealth(), shooter.getMaxHealth(), "a " + name + " with no kick does not kick");
			shooter.discard();
			x += 2;
		}
		helper.succeed();
	}

	@GameTest
	public void theKickDoesTwoTimesTheDamageToAGolemAndStopsAShield(GameTestHelper helper) {
		var level = helper.getLevel();
		Mob camel = mob(helper, EntityTypes.CAMEL_HUSK, new BlockPos(1, 1, 6), "test_kick");
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(6, 1, 6));
		GeneCombat.kick(camel, level, golem);
		helper.assertValueEqual(golem.getMaxHealth() - golem.getHealth(), 12.0F, "(4 + 2) times 2 to a golem");
		var player = CreeperEffectsTest.shieldedPlayer(helper);
		Mob plain = mob(helper, EntityTypes.ZOMBIE_HORSE, new BlockPos(4, 1, 1), "test_kick");
		GeneCombat.kick(plain, level, player);
		helper.assertFalse(player.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.SHIELD)), "a kick with no shield stop keeps the shield up");
		Mob stopper = mob(helper, EntityTypes.ZOMBIE_HORSE, new BlockPos(4, 1, 2), "test_shield_kick");
		GeneCombat.kick(stopper, level, player);
		helper.assertTrue(player.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(
				net.minecraft.world.item.Items.SHIELD)), "the shield kick stops the shield");
		helper.succeed();
	}
}
