package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the time of a shear, so that the mushrooms of a bogged can grow back.
 */
@Mixin(Bogged.class)
public abstract class BoggedMixin {
	@Inject(method = "shear", at = @At("TAIL"))
	private void originofspawns$recordShear(ServerLevel level, SoundSource source, ItemStack tool, CallbackInfo info) {
		((Bogged) (Object) this).setAttached(ModAttachments.SHEAR_TIME, level.getGameTime());
	}
}
