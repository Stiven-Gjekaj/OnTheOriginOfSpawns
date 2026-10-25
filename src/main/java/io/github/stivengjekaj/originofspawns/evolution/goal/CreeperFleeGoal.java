package io.github.stivengjekaj.originofspawns.evolution.goal;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Makes a creeper run from a mob that hunts creepers, such as a zoglin with Creeper Hunter.
 */
public class CreeperFleeGoal extends AvoidEntityGoal<LivingEntity> {
	private static final float FLEE_DISTANCE = 8.0F;

	public CreeperFleeGoal(Creeper creeper) {
		super(creeper, LivingEntity.class, FLEE_DISTANCE, 1.0, 1.2, CreeperFleeGoal::huntsCreepers);
	}

	/**
	 * Returns true if a gene of the entity hunts creepers.
	 */
	public static boolean huntsCreepers(LivingEntity entity) {
		return GeneEffects.of(entity).anyMatch(effect -> effect instanceof GeneEffect.HuntMobs hunt
				&& hunt.mobs().contains(EntityTypes.CREEPER.builtInRegistryHolder()));
	}
}
