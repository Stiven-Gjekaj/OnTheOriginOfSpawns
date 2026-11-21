package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;

import net.minecraft.world.entity.monster.creaking.Creaking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lets a heart leash gene of a creaking keep its paths nearer to its heart.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.creaking.Creaking$HomeNodeEvaluator")
public abstract class CreakingHomeNodeEvaluatorMixin {
	@Shadow
	@Final
	Creaking this$0;

	@ModifyConstant(method = "getPathType", constant = @Constant(doubleValue = 1024.0))
	private double originofspawns$heartRange(double rangeSquared) {
		double range = GeneCreakings.heartRange(this.this$0);
		return range * range;
	}
}
