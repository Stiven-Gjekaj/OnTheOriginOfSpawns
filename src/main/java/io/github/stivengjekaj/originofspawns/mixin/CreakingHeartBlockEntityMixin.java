package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the genes of a creaking change its heart, and marks the death of a creaking from a broken heart.
 */
@Mixin(CreakingHeartBlockEntity.class)
public abstract class CreakingHeartBlockEntityMixin {
	/**
	 * A heart that breaks from a player or a blast kills its creaking with the damage source of the break. The mark tells
	 * the pressures that the death came from the heart.
	 */
	@Inject(method = "removeProtector", at = @At("HEAD"))
	private void originofspawns$markHeartBreak(@Nullable DamageSource source, CallbackInfo info) {
		if (source != null) {
			((CreakingHeartBlockEntityAccessor) this).originofspawns$getCreakingProtector()
					.ifPresent(creaking -> creaking.setAttached(ModAttachments.HEART_BREAK, true));
		}
	}
}
