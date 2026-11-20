package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneEffects;

import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Lets the genes of a skeleton horse trap change the trap.
 */
@Mixin(SkeletonTrapGoal.class)
public abstract class SkeletonTrapGoalMixin {
	@Shadow
	@Final
	private SkeletonHorse horse;

	/**
	 * A trap range gene changes the distance at which a player opens the trap. The vanilla distance is 10 blocks. With
	 * more than one, the largest counts.
	 */
	@ModifyArg(method = "canUse", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;hasNearbyAlivePlayer(DDDD)Z"), index = 3)
	private double originofspawns$trapRange(double range) {
		double result = range;
		for (GeneEffect effect : GeneEffects.of(this.horse).toList()) {
			if (effect instanceof GeneEffect.TrapRange trap) {
				result = Math.max(result, trap.range());
			}
		}
		return result;
	}
}
