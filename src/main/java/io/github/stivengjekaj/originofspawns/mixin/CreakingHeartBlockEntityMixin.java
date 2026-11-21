package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.stivengjekaj.originofspawns.evolution.GeneCreakings;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the genes of a creaking change its heart, and marks the death of a creaking from a broken heart.
 */
@Mixin(CreakingHeartBlockEntity.class)
public abstract class CreakingHeartBlockEntityMixin {
	/**
	 * A heart that breaks from a player or a blast kills its creaking with the damage source of the break. The mark tells
	 * the pressures that the death came from the heart. The heart break genes of the creaking act first.
	 */
	@Inject(method = "removeProtector", at = @At("HEAD"))
	private void originofspawns$markHeartBreak(@Nullable DamageSource source, CallbackInfo info) {
		if (source != null) {
			((CreakingHeartBlockEntityAccessor) this).originofspawns$getCreakingProtector()
					.ifPresent(creaking -> {
						creaking.setAttached(ModAttachments.HEART_BREAK, true);
						GeneCreakings.onHeartBreak(creaking, source);
					});
		}
	}

	/**
	 * When a player hurts the creaking and the heart shows its trail, the creaking keeps the time of the trail.
	 */
	@Inject(method = "creakingHurt", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/CreakingHeartBlockEntity;emitParticles(Lnet/minecraft/server/level/ServerLevel;IZ)V"))
	private void originofspawns$markTrail(CallbackInfo info) {
		((CreakingHeartBlockEntityAccessor) this).originofspawns$getCreakingProtector()
				.ifPresent(creaking -> creaking.setAttached(ModAttachments.TRAIL_TIME, creaking.level().getGameTime()));
	}

	/**
	 * A heart leash gene of the creaking makes the heart let it go nearer to the heart.
	 */
	@ModifyConstant(method = "serverTick", constant = @Constant(doubleValue = 34.0))
	private static double originofspawns$heartRange(double range, @Local(argsOnly = true) CreakingHeartBlockEntity heart) {
		return ((CreakingHeartBlockEntityAccessor) heart).originofspawns$getCreakingProtector()
				.map(creaking -> GeneCreakings.heartRange(creaking) + GeneCreakings.HEART_RANGE_SLACK).orElse(range);
	}

	/**
	 * A dark heart looks dormant while it protects its creaking at night.
	 */
	@Inject(method = "updateCreakingState", at = @At("RETURN"), cancellable = true)
	private static void originofspawns$darkHeart(Level level, BlockState state, BlockPos pos, CreakingHeartBlockEntity heart,
			CallbackInfoReturnable<BlockState> info) {
		BlockState updated = info.getReturnValue();
		if (updated.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE && GeneCreakings.darkHeart(heart)) {
			info.setReturnValue(updated.setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT));
		}
	}

	/**
	 * A dark heart that looks dormant still grows resin, as an awake heart does.
	 */
	@ModifyExpressionValue(method = "creakingHurt", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
	private Comparable<?> originofspawns$darkHeartResin(Comparable<?> state) {
		return state == CreakingHeartState.DORMANT && GeneCreakings.darkHeart((CreakingHeartBlockEntity) (Object) this)
				? CreakingHeartState.AWAKE : state;
	}
}
