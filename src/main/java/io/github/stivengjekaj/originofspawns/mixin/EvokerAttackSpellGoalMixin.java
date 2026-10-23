package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.monster.illager.Evoker;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.illager.Evoker$EvokerAttackSpellGoal")
public abstract class EvokerAttackSpellGoalMixin {
	@Shadow
	@Final
	Evoker this$0;

	/**
	 * The genes change the time between the spells.
	 */
	@ModifyReturnValue(method = "getCastingInterval", at = @At("RETURN"))
	private int originofspawns$castingInterval(int ticks) {
		return GeneCombat.spellInterval(this.this$0, ticks);
	}
}
