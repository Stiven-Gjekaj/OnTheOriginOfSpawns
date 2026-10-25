package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;

import net.minecraft.world.entity.monster.cubemob.MagmaCube;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MagmaCube.class)
public abstract class MagmaCubeMixin {
	/**
	 * The genes make the hop higher. The magma cube has its own hop, which does not call the hop of the slime.
	 */
	@Inject(method = "jumpFromGround", at = @At("TAIL"))
	private void originofspawns$hopHeight(CallbackInfo info) {
		GeneCubes.hopHeight((MagmaCube) (Object) this);
	}
}
