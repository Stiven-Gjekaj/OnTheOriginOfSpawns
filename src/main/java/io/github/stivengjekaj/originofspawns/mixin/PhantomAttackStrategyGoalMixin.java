package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.world.entity.monster.Phantom;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackStrategyGoal")
public abstract class PhantomAttackStrategyGoalMixin {
	@Shadow
	@Final
	Phantom this$0;

	/**
	 * When the phantom starts a swoop, its swoop call makes the phantoms near it target the same mob.
	 */
	@Inject(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Phantom;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
	private void originofspawns$swoopCall(CallbackInfo info) {
		GeneBehaviors.swoopCall(this$0);
	}
}
