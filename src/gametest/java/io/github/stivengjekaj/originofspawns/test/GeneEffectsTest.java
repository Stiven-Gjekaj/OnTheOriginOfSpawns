package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class GeneEffectsTest {
	private static final Identifier SWIFT = id("test_swift");
	private static final Identifier HEAT_SKIN = id("test_heat_skin");
	private static final Identifier IGNITE = id("test_ignite");

	@GameTest
	public void newMobGetsGenesFromItsPool(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(helper.absolutePos(pos)));
		GenePools.get(helper.getLevel().getServer())
				.setFrequency(Identifier.withDefaultNamespace("giant"), category, SWIFT, 1.0F);

		Giant giant = helper.spawnWithNoFreeWill(EntityTypes.GIANT, pos);

		CarriedGenes carried = giant.getAttached(ModAttachments.GENES);
		helper.assertTrue(carried != null && carried.genes().contains(SWIFT), "the giant carries the gene");
		helper.assertTrue(giant.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(GeneEffects.modifierId(SWIFT, 0)),
				"the speed modifier is on the giant");
		helper.succeed();
	}

	@GameTest
	public void damageTakenChangesTheDamage(GameTestHelper helper) {
		Zombie burnt = zombieWith(helper, new BlockPos(1, 1, 1), HEAT_SKIN);
		Zombie hit = zombieWith(helper, new BlockPos(3, 1, 1), HEAT_SKIN);
		float burntHealth = burnt.getHealth();
		float hitHealth = hit.getHealth();

		burnt.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 5.0F);
		hit.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 5.0F);

		helper.assertValueEqual(burnt.getHealth(), burntHealth, "health after fire");
		helper.assertValueEqual(hit.getHealth(), hitHealth - 5.0F, "health after a generic hit");
		helper.succeed();
	}

	@GameTest
	public void igniteTargetSetsThePlayerOnFire(GameTestHelper helper) {
		Zombie zombie = zombieWith(helper, new BlockPos(1, 1, 1), IGNITE);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		zombie.doHurtTarget(helper.getLevel(), player);
		helper.assertTrue(player.getRemainingFireTicks() > 0, "the player is on fire");
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void waterDamagesOnlyTheGeneCarrier(GameTestHelper helper) {
		BlockPos carrierPos = new BlockPos(1, 1, 1);
		BlockPos plainPos = new BlockPos(4, 1, 1);
		helper.setBlock(carrierPos, Blocks.WATER);
		helper.setBlock(plainPos, Blocks.WATER);
		Zombie carrier = zombieWith(helper, carrierPos, HEAT_SKIN);
		Zombie plain = zombieWith(helper, plainPos);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(carrier.getHealth() < carrier.getMaxHealth(), "the carrier lost health");
			helper.assertValueEqual(plain.getHealth(), plain.getMaxHealth(), "health of the plain zombie");
			helper.succeed();
		});
	}

	private static Zombie zombieWith(GameTestHelper helper, BlockPos pos, Identifier... genes) {
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(List.of(genes)));
		zombie.setHealth(zombie.getMaxHealth());
		return zombie;
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("originofspawns_test", path);
	}
}
