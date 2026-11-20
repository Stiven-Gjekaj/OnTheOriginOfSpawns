package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneSilverfish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Silverfish;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a wake range gene change the range of the wake of a hurt silverfish. A silverfish with no such gene wakes the
 * others as in vanilla.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class SilverfishWakeUpFriendsGoalMixin {
	@Shadow
	@Final
	private Silverfish silverfish;

	@Shadow
	private int lookForFriends;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void originofspawns$wakeRange(CallbackInfo info) {
		if (!(this.silverfish.level() instanceof ServerLevel level) || !GeneSilverfish.hasWakeRange(this.silverfish)) {
			return;
		}
		info.cancel();
		this.lookForFriends--;
		if (this.lookForFriends <= 0) {
			GeneSilverfish.wake(this.silverfish, level, GeneSilverfish.wakeRange(this.silverfish));
		}
	}
}
