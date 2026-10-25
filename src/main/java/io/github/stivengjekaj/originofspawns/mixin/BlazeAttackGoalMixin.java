package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;

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

	@Shadow
	private int attackStep;

	/**
	 * After the goal makes a fireball of the burst, the genes change it before the goal adds it to the level.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean originofspawns$burstShot(Level level, Entity entity, Operation<Boolean> original) {
		if (entity instanceof SmallFireball fireball) {
			// The steps 2 and on shoot, so the first fireball of the burst has the index 0.
			GeneBehaviors.turnInBurstFan(blaze, fireball, attackStep - 2);
			GeneBehaviors.fireballSpeed(blaze, fireball);
		}
		return original.call(level, entity);
	}

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
