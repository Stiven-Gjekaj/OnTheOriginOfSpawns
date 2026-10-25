package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.GeneCubes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCubeMob.class)
public abstract class AbstractCubeMobMixin {
	/**
	 * A piece of a split gets the genes of the cube that split. It is not a new mob.
	 */
	@Inject(method = "setUpSplitCube", at = @At("TAIL"))
	private void originofspawns$splitGenes(AbstractCubeMob piece, int halfSize, float xd, float zd, CallbackInfo info) {
		GeneCubes.splitPiece((AbstractCubeMob) (Object) this, piece);
	}

	/**
	 * A touch hit of a cube runs the hit effects of its genes, as a melee hit of another mob does.
	 */
	@WrapOperation(method = "dealDamage", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private boolean originofspawns$touchHit(LivingEntity target, ServerLevel level, DamageSource source, float damage,
			Operation<Boolean> original) {
		boolean hurt = original.call(target, level, source, damage);
		if (hurt) {
			GeneCombat.onHit((AbstractCubeMob) (Object) this, target);
		}
		return hurt;
	}

	/**
	 * The genes change the wait between two hops. The magma cube waits 4 times as long after this.
	 */
	@ModifyReturnValue(method = "getJumpDelay", at = @At("RETURN"))
	private int originofspawns$hopWait(int ticks) {
		float wait = GeneCubes.hop((AbstractCubeMob) (Object) this)[0];
		return wait == 1.0F ? ticks : Math.max(1, Math.round(ticks * wait));
	}

	/**
	 * The genes make the hop higher.
	 */
	@Inject(method = "jumpFromGround", at = @At("TAIL"))
	private void originofspawns$hopHeight(CallbackInfo info) {
		GeneCubes.hopHeight((AbstractCubeMob) (Object) this);
	}
}
