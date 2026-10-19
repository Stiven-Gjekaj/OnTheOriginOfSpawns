package io.github.stivengjekaj.originofspawns.definition;

import java.util.Set;

import net.minecraft.world.damagesource.DamageSource;

/**
 * The facts about one death that a pressure can check.
 *
 * @param source the damage that killed
 * @param facts the conditions that are true for this death
 */
public record DeathContext(DamageSource source, Set<DeathCondition> facts) {
	public DeathContext {
		facts = Set.copyOf(facts);
	}
}
