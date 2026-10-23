package io.github.stivengjekaj.originofspawns.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.entity.monster.illager.SpellcasterIllager$SpellcasterUseSpellGoal")
public interface SpellcasterUseSpellGoalAccessor {
	/**
	 * Returns the time between two casts of the spell, in ticks.
	 */
	@Invoker("getCastingInterval")
	int originofspawns$castingInterval();

	/**
	 * Casts the spell now.
	 */
	@Invoker("performSpellCasting")
	void originofspawns$performSpellCasting();
}
