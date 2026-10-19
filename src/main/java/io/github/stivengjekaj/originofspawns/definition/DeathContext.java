package io.github.stivengjekaj.originofspawns.definition;

import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;

import net.minecraft.world.damagesource.DamageSource;

/**
 * The facts about one death that a pressure can check.
 */
public record DeathContext(DamageSource source, CombatRecord combat) {
}
