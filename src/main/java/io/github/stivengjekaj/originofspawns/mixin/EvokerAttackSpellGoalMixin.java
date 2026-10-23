package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.monster.illager.Evoker;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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

	/**
	 * The genes change the number of fangs in the line.
	 */
	@ModifyConstant(method = "performSpellCasting", constant = @Constant(intValue = 16))
	private int originofspawns$fangLine(int fangs) {
		return GeneCombat.fangLine(this.this$0, fangs);
	}

	/**
	 * The genes change the range of the ring of fangs. Vanilla compares the square of the distance with 9.
	 */
	@ModifyConstant(method = "performSpellCasting", constant = @Constant(doubleValue = 9.0))
	private double originofspawns$fangRing(double squared) {
		return GeneCombat.fangRingSquared(this.this$0, squared);
	}
}
