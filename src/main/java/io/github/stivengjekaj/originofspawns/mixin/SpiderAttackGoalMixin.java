package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.PathfinderMob;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.spider.Spider$SpiderAttackGoal")
public abstract class SpiderAttackGoalMixin {
	/**
	 * A spider that hunts in light sees the light as dark, so the light does not stop its hunt.
	 */
	@WrapOperation(method = "canContinueToUse", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/PathfinderMob;getLightLevelDependentMagicValue()F"))
	private float originofspawns$huntInLight(PathfinderMob mob, Operation<Float> original) {
		return GeneCombat.has(mob, GeneEffect.HuntInLight.class) ? 0.0F : original.call(mob);
	}
}
