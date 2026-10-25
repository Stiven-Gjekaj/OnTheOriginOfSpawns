package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Changes the fireball shot of a ghast with its genes.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastShootFireballGoalMixin {
	@Shadow
	@Final
	private Ghast ghast;

	/**
	 * After a shot, the goal sets the charge to -40, so that the next shot comes 60 ticks later. The throw rate
	 * changes the full time from one shot to the next. The charge of 20 ticks before a shot stays.
	 */
	@ModifyConstant(method = "tick", constant = @Constant(intValue = -40))
	private int originofspawns$waitAfterShot(int wait) {
		return -GeneBehaviors.ghastShotWait(ghast, -wait);
	}
}
