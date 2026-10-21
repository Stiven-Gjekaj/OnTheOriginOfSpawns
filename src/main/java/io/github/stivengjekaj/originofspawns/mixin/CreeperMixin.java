package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
	/**
	 * The radius of the last blast of the creeper, after the charge and the genes.
	 */
	@Unique
	private float originofspawns$blastRadius;

	/**
	 * The genes multiply the radius that the game gives the blast. This radius already has the charge in it.
	 */
	@ModifyArg(method = "explodeCreeper", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),
			index = 4)
	private float originofspawns$blastSize(float radius) {
		originofspawns$blastRadius = radius * GeneBlasts.sizeMultiplier((Creeper) (Object) this);
		return originofspawns$blastRadius;
	}

	/**
	 * The effects after the blast run when the vanilla blast and the vanilla cloud are done, before the creeper goes.
	 */
	@Inject(method = "explodeCreeper", at = @At("TAIL"))
	private void originofspawns$afterBlast(CallbackInfo info) {
		Creeper self = (Creeper) (Object) this;
		if (self.level() instanceof ServerLevel level) {
			GeneBlasts.afterBlast(self, level, originofspawns$blastRadius);
		}
	}

	/**
	 * The genes change the length of the fuse. The game saves the vanilla fuse, so the change does not add up after
	 * a load. The swell on the client uses the same length.
	 */
	@ModifyExpressionValue(method = {"tick", "getSwelling", "causeFallDamage"}, at = @At(value = "FIELD",
			target = "Lnet/minecraft/world/entity/monster/Creeper;maxSwell:I", opcode = Opcodes.GETFIELD))
	private int originofspawns$fuseTime(int ticks) {
		return GeneBlasts.fuseTicks((Creeper) (Object) this, ticks);
	}
}
