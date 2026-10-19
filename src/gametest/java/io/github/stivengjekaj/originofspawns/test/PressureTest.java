package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.DeathContext;
import io.github.stivengjekaj.originofspawns.definition.Pressure;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class PressureTest {
	private static final CombatRecord UNREACHED = new CombatRecord(true, false);
	private static final CombatRecord REACHED = new CombatRecord(true, true);

	@GameTest
	public void damagePressureChecksTheDamageType(GameTestHelper helper) {
		Pressure heat = pressure(helper, "test_heat");
		helper.assertTrue(heat.matches(new DeathContext(fire(helper), CombatRecord.NONE)), "fire death");
		helper.assertFalse(heat.matches(new DeathContext(fall(helper), CombatRecord.NONE)), "fall death");
		helper.succeed();
	}

	@GameTest
	public void unreachedNeedsATargetAndNoHit(GameTestHelper helper) {
		Pressure unreached = pressure(helper, "test_unreached");
		helper.assertTrue(unreached.matches(new DeathContext(fall(helper), UNREACHED)), "targeted, no hit");
		helper.assertFalse(unreached.matches(new DeathContext(fall(helper), REACHED)), "targeted and hit");
		helper.assertFalse(unreached.matches(new DeathContext(fall(helper), CombatRecord.NONE)), "no target");
		helper.succeed();
	}

	@GameTest
	public void pressureNeedsAllItsParts(GameTestHelper helper) {
		Pressure pressure = pressure(helper, "test_unreached_heat");
		helper.assertTrue(pressure.matches(new DeathContext(fire(helper), UNREACHED)), "fire and unreached");
		helper.assertFalse(pressure.matches(new DeathContext(fall(helper), UNREACHED)), "fall and unreached");
		helper.assertFalse(pressure.matches(new DeathContext(fire(helper), REACHED)), "fire and reached");
		helper.succeed();
	}

	@GameTest
	public void zombieRecordsTargetAndHit(GameTestHelper helper) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		helper.assertValueEqual(record(zombie), CombatRecord.NONE, "record at spawn");
		zombie.setTarget(player);
		helper.assertValueEqual(record(zombie), UNREACHED, "record after target");
		zombie.doHurtTarget(helper.getLevel(), player);
		helper.assertValueEqual(record(zombie), REACHED, "record after hit");
		helper.succeed();
	}

	private static DamageSource fire(GameTestHelper helper) {
		return helper.getLevel().damageSources().inFire();
	}

	private static DamageSource fall(GameTestHelper helper) {
		return helper.getLevel().damageSources().fall();
	}

	private static CombatRecord record(Zombie zombie) {
		return zombie.getAttachedOrElse(ModAttachments.COMBAT, CombatRecord.NONE);
	}

	private static Pressure pressure(GameTestHelper helper, String path) {
		return helper.getLevel().registryAccess().lookupOrThrow(ModRegistries.PRESSURE).getValueOrThrow(
				ResourceKey.create(ModRegistries.PRESSURE, Identifier.fromNamespaceAndPath("originofspawns_test", path)));
	}
}
