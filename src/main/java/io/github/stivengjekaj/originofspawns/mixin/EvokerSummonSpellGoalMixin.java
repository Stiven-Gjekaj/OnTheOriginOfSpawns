package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.monster.illager.Evoker$EvokerSummonSpellGoal")
public abstract class EvokerSummonSpellGoalMixin {
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
	 * The genes change the number of vexes.
	 */
	@ModifyConstant(method = "performSpellCasting", constant = @Constant(intValue = 3))
	private int originofspawns$vexCount(int count) {
		return GeneBehaviors.vexCount(this.this$0, count);
	}

	/**
	 * The genes of the evoker shape each vex before it joins the world.
	 */
	@ModifyArg(method = "performSpellCasting", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private Entity originofspawns$shapeVex(Entity vex) {
		if (vex instanceof Vex summoned) {
			GeneBehaviors.shapeVex(this.this$0, summoned);
		}
		return vex;
	}
}
