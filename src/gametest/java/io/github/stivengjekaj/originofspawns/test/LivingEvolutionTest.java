package io.github.stivengjekaj.originofspawns.test;

import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import io.github.stivengjekaj.originofspawns.mixin.MobAccessor;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import java.util.List;

import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.evolution.LivingEvolution;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

/**
 * A mob that lives long changes one gene toward its pool, only where no player sees it.
 */
public class LivingEvolutionTest {
	private static final Identifier ZOMBIE = Identifier.withDefaultNamespace("zombie");
	private static final Identifier BONERUNNER = Identifier.fromNamespaceAndPath("originofspawns", "bonerunner");
	private static final Identifier FAVORED = Identifier.fromNamespaceAndPath("originofspawns_test", "test_regen");

	private static Zombie zombie(GameTestHelper helper, List<Identifier> genes, long age) {
		Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		zombie.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 1))), 0.0F, 0.0F);
		zombie.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		zombie.setAttached(ModAttachments.BORN, helper.getLevel().getGameTime() - age);
		helper.getLevel().addFreshEntity(zombie);
		return zombie;
	}

	@GameTest
	public void youngNamedOrWatchedMobsDoNotChange(GameTestHelper helper) {
		Zombie young = zombie(helper, List.of(BONERUNNER), LivingEvolution.MIN_AGE / 2);
		helper.assertFalse(LivingEvolution.canChange(young, helper.getLevel()), "a young mob");
		Zombie named = zombie(helper, List.of(BONERUNNER), LivingEvolution.MIN_AGE * 2);
		named.setCustomName(Component.literal("Steve"));
		helper.assertFalse(LivingEvolution.canChange(named, helper.getLevel()), "a mob with a name tag");
		Zombie watched = zombie(helper, List.of(BONERUNNER), LivingEvolution.MIN_AGE * 2);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setPos(watched.position().add(5.0, 0.0, 0.0));
		helper.assertFalse(LivingEvolution.canChange(watched, helper.getLevel()), "a mob with a player near");
		helper.succeed();
	}

	@GameTest
	public void theWeakestGeneGivesWayToTheFavoredGene(GameTestHelper helper) {
		Zombie zombie = zombie(helper, List.of(BONERUNNER), LivingEvolution.MIN_AGE * 2);
		BiomeCategory category = BiomeCategory.of(helper.getLevel().getBiome(zombie.blockPosition()));
		GenePools pools = GenePools.get(helper.getLevel().getServer());
		pools.setFrequency(ZOMBIE, category, BONERUNNER, 0.0F);
		pools.setFrequency(ZOMBIE, category, FAVORED, 1.0F);
		var speed = zombie.getAttribute(Attributes.MOVEMENT_SPEED);
		speed.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
				GeneEffects.modifierId("gene/originofspawns/bonerunner", 0), 0.2,
				net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

		helper.assertTrue(LivingEvolution.evolve(zombie, helper.getLevel()), "the zombie changes");
		List<Identifier> genes = zombie.getAttached(ModAttachments.GENES).genes();
		helper.assertTrue(genes.contains(FAVORED), "the favored gene comes in");
		helper.assertFalse(genes.contains(BONERUNNER), "the weakest gene goes");
		helper.assertFalse(speed.hasModifier(GeneEffects.modifierId("gene/originofspawns/bonerunner", 0)),
				"the speed of the old gene goes");
		pools.setFrequency(ZOMBIE, category, FAVORED, 0.0F);
		helper.succeed();
	}

	@GameTest
	public void aLostSiegeStopsTheDoorBreaking(GameTestHelper helper) {
		Identifier siege = Identifier.fromNamespaceAndPath("originofspawns_test", "test_siege");
		Zombie zombie = zombie(helper, List.of(siege), 0L);
		GeneBehaviors.installGoals(zombie);
		helper.assertTrue(zombie.canBreakDoors(), "a siege zombie breaks doors");
		var access = helper.getLevel().registryAccess();
		GeneBehaviors.onLoss(zombie, GeneEffects.sources(access, new CarriedGenes(List.of(siege))),
				GeneEffects.sources(access, new CarriedGenes(List.of())));
		helper.assertFalse(zombie.canBreakDoors(), "no door breaking after the loss");
		boolean goal = ((MobAccessor) zombie).originofspawns$goalSelector().getAvailableGoals().stream()
				.anyMatch(wrapped -> wrapped.getGoal() instanceof BreakDoorGoal);
		helper.assertFalse(goal, "no door goal after the loss");
		helper.succeed();
	}

	@GameTest
	public void aGainedGeneRunsItsSpawnEffects(GameTestHelper helper) {
		Zombie zombie = zombie(helper, List.of(), 0L);
		zombie.setCanPickUpLoot(false);
		GeneBehaviors.onGain(zombie, List.of(new GeneEffect.PickUpItems()));
		helper.assertTrue(zombie.canPickUpLoot(), "the zombie picks up items");
		var access = helper.getLevel().registryAccess();
		Identifier pickUp = Identifier.fromNamespaceAndPath("originofspawns_test", "test_pick_up");
		GeneBehaviors.onLoss(zombie, GeneEffects.sources(access, new CarriedGenes(List.of(pickUp))),
				GeneEffects.sources(access, new CarriedGenes(List.of())));
		helper.assertFalse(zombie.canPickUpLoot(), "no pickup after the loss");
		helper.succeed();
	}
}
