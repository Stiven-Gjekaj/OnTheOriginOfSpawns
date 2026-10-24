package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneThrows;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {
	/**
	 * A block that a mob threw hurts the entity that it hits.
	 */
	@Inject(method = "tick", at = @At("TAIL"))
	private void originofspawns$thrownHit(CallbackInfo info) {
		FallingBlockEntity self = (FallingBlockEntity) (Object) this;
		if (self.level() instanceof ServerLevel level) {
			GeneThrows.hit(self, level);
		}
	}
}
