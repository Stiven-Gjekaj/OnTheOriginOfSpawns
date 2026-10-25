package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobFleeMixin {
	/**
	 * After the goals, and before the move control moves the mob, a fleeing mob gets the place away from its target.
	 */
	@Inject(method = "serverAiStep", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/control/MoveControl;tick()V"))
	private void originofspawns$flee(CallbackInfo info) {
		Mob self = (Mob) (Object) this;
		if (self.hasAttached(ModAttachments.GENES)) {
			GeneBehaviors.fleeMove(self);
		}
	}
}
