package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;

/**
 * Mobs with genes get no random weapons or armor, and pick up no items. Natural weapons stay.
 */
public class EquipmentTest {
	private static final int MOBS = 200;

	/**
	 * Hard difficulty in a world that is old, so the vanilla chances for gear are high.
	 */
	private static DifficultyInstance hard(GameTestHelper helper) {
		return new DifficultyInstance(Difficulty.HARD, 3_600_000L, 3_600_000L, 1.0F);
	}

	private static Mob spawned(GameTestHelper helper, EntityType<? extends Mob> type) {
		Mob mob = type.create(helper.getLevel(), EntitySpawnReason.NATURAL);
		mob.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))), 0.0F, 0.0F);
		mob.finalizeSpawn(helper.getLevel(), hard(helper), EntitySpawnReason.NATURAL, null);
		return mob;
	}

	/**
	 * Tells if the mob has random gear. A husk that rides a camel husk keeps its iron spear, because the rider and its
	 * spear are one vanilla mob design.
	 */
	private static boolean hasGear(Mob mob) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot == EquipmentSlot.MAINHAND && mob.isPassenger() && mob.getMainHandItem().is(Items.IRON_SPEAR)) {
				continue;
			}
			if (slot != EquipmentSlot.OFFHAND && slot != EquipmentSlot.BODY && slot != EquipmentSlot.SADDLE
					&& !mob.getItemBySlot(slot).isEmpty() && !mob.getItemBySlot(slot).is(Items.CARVED_PUMPKIN)
					&& !mob.getItemBySlot(slot).is(Items.JACK_O_LANTERN)) {
				return true;
			}
		}
		return false;
	}

	@GameTest
	public void zombiesAndHusksGetNoRandomGear(GameTestHelper helper) {
		for (int i = 0; i < MOBS; i++) {
			helper.assertFalse(hasGear(spawned(helper, EntityTypes.ZOMBIE)), "a zombie with gear");
			helper.assertFalse(hasGear(spawned(helper, EntityTypes.HUSK)), "a husk with gear");
		}
		helper.killAllEntitiesOfClass(Mob.class);
		helper.succeed();
	}

	@GameTest
	public void drownedKeepTheirNaturalWeapons(GameTestHelper helper) {
		int armed = 0;
		for (int i = 0; i < MOBS; i++) {
			Mob drowned = spawned(helper, EntityTypes.DROWNED);
			if (drowned.getMainHandItem().is(Items.TRIDENT) || drowned.getMainHandItem().is(Items.FISHING_ROD)) {
				armed++;
			}
		}
		helper.assertTrue(armed > 0, "no drowned of " + MOBS + " has a trident or a fishing rod");
		helper.succeed();
	}

	@GameTest
	public void mobsWithGenesPickUpNoItems(GameTestHelper helper) {
		for (int i = 0; i < 50; i++) {
			Mob zombie = spawned(helper, EntityTypes.ZOMBIE);
			zombie.setPersistenceRequired();
			helper.getLevel().addFreshEntity(zombie);
			// Other tests raise the test genes in the pool, so a zombie can roll a gene that gives pickup.
			boolean geneGivesPickup = GeneCombat.has(zombie, GeneEffect.PickUpItems.class);
			helper.assertFalse(zombie.canPickUpLoot() && !geneGivesPickup, "a zombie that picks up items with no gene");
			zombie.discard();
		}
		helper.succeed();
	}
}
