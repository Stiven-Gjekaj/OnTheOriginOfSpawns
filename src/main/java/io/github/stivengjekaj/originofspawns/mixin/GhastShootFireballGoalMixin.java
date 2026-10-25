package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;

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
	/**
	 * After the goal adds the fireball to the level, the genes add the other fireballs of the shot.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean originofspawns$extraShots(Level level, Entity entity, Operation<Boolean> original) {
		boolean added = original.call(level, entity);
		if (added && entity instanceof LargeFireball fireball) {
			GeneBehaviors.ghastFanShot(ghast, fireball);
		}
		return added;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = -40))
	private int originofspawns$waitAfterShot(int wait) {
		return -GeneBehaviors.ghastShotWait(ghast, -wait);
	}
}
