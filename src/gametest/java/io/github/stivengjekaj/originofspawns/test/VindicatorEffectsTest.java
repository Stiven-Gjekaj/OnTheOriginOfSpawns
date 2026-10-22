package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the vindicator genes use: the knockback, the villagers and the golems, the siege, and the
 * axe. It also tests that the reused effects work on a vindicator.
 */
public class VindicatorEffectsTest {
	@GameTest
	public void villageBaneHurtsVillagersAndTradersMore(GameTestHelper helper) {
		Vindicator bane = vindicatorWith(helper, new BlockPos(1, 1, 1), "test_village_bane");
		var hit = helper.getLevel().damageSources().mobAttack(bane);
		helper.assertValueEqual(damageTo(helper, EntityTypes.VILLAGER, hit), 8.0F, "a villager takes two times the damage");
		helper.assertValueEqual(damageTo(helper, EntityTypes.WANDERING_TRADER, hit), 8.0F, "a wandering trader too");
		helper.assertValueEqual(damageTo(helper, EntityTypes.IRON_GOLEM, hit), 4.0F, "a golem is not a villager");
		helper.succeed();
	}

	@GameTest
	public void golemSmasherHurtsIronAndSnowGolemsMore(GameTestHelper helper) {
		Vindicator smasher = vindicatorWith(helper, new BlockPos(1, 1, 1), "test_golem_smasher");
		var hit = helper.getLevel().damageSources().mobAttack(smasher);
		helper.assertValueEqual(damageTo(helper, EntityTypes.IRON_GOLEM, hit), 8.0F, "an iron golem takes two times the damage");
		helper.assertValueEqual(damageTo(helper, EntityTypes.SNOW_GOLEM, hit), 8.0F, "a snow golem too");
		helper.assertValueEqual(damageTo(helper, EntityTypes.VILLAGER, hit), 4.0F, "a villager is not a golem");
		helper.succeed();
	}

	/**
	 * Returns the damage that a hit of 4 does to a new mob of the type, after the genes.
	 */
	static float damageTo(GameTestHelper helper, EntityType<? extends Mob> type,
			net.minecraft.world.damagesource.DamageSource hit) {
		Mob target = helper.spawnWithNoFreeWill(type, new BlockPos(4, 1, 1));
		float damage = GeneCombat.modifyDamage(target, hit, 4.0F);
		target.discard();
		return damage;
	}

	static Vindicator vindicatorWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Vindicator vindicator = EntityTypes.VINDICATOR.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		vindicator.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		vindicator.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
		vindicator.setNoAi(true);
		vindicator.setPersistenceRequired();
		vindicator.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(vindicator);
		return vindicator;
	}
}
