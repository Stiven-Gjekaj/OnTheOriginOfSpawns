package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Blaze;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Changes the bursts of small fireballs of a blaze with its genes. In each burst, the vanilla goal charges for 3
 * seconds, shoots one fireball in each of the steps 2 to 4, 6 ticks apart, and then rests for 5 seconds.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeAttackGoalMixin {
	@Shadow
	@Final
	private Blaze blaze;

	/**
	 * The last step of a burst that shoots. The burst count genes add steps.
	 */
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 4))
	private int originofspawns$lastShotStep(int step) {
		return step + GeneBehaviors.burstExtra(blaze);
	}

	/**
	 * The rest after a burst. The throw rate changes it.
	 */
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 100))
	private int originofspawns$restAfterBurst(int rest) {
		return GeneBehaviors.scaleThrowInterval(blaze, rest);
	}
}
