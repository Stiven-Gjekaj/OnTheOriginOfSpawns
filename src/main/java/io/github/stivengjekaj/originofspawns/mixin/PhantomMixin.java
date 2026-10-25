package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Phantom.class)
public abstract class PhantomMixin {
	/**
	 * A silent phantom makes no sound with its wings. Vanilla plays the flap on the client for each phantom.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;playLocalSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	private void originofspawns$silentFlap(Level level, Entity entity, SoundEvent sound, SoundSource source, float volume,
			float pitch, Operation<Void> original) {
		if (!entity.isSilent()) {
			original.call(level, entity, sound, source, volume, pitch);
		}
	}
}
