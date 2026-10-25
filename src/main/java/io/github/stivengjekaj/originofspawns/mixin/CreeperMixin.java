package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

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
	 * A creeper runs from a mob that hunts creepers.
	 */
	@org.spongepowered.asm.mixin.injection.Inject(method = "registerGoals", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
	private void originofspawns$fleeHunters(org.spongepowered.asm.mixin.injection.callback.CallbackInfo info) {
		((MobAccessor) this).originofspawns$goalSelector().addGoal(3,
				new io.github.stivengjekaj.originofspawns.evolution.goal.CreeperFleeGoal((net.minecraft.world.entity.monster.Creeper) (Object) this));
	}

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
	 * The effects after the blast run when the vanilla blast and the vanilla cloud are done. The game already removed
	 * the creeper then, but its place, its genes, and its level stay, and the effects need only these.
	 */
	@Inject(method = "explodeCreeper", at = @At("TAIL"))
	private void originofspawns$afterBlast(CallbackInfo info) {
		Creeper self = (Creeper) (Object) this;
		if (self.level() instanceof ServerLevel level) {
			GeneBlasts.afterBlast(self, level, self.position(), originofspawns$blastRadius);
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

	/**
	 * A silent fuse starts with no hiss. The game gives the hiss a fixed volume, so a loud creeper changes it here.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
	private void originofspawns$fuseSound(Creeper creeper, SoundEvent sound, float volume, float pitch,
			Operation<Void> original) {
		if (!GeneCombat.has(creeper, GeneEffect.SilentFuse.class)) {
			original.call(creeper, sound, volume * GeneBehaviors.volumeMultiplier(creeper), pitch);
		}
	}

	/**
	 * The flint and steel or the fire charge that lights a silent fuse makes no sound.
	 */
	@WrapOperation(method = "mobInteract", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	private void originofspawns$igniteSound(Level level, Entity except, double x, double y, double z, SoundEvent sound,
			SoundSource source, float volume, float pitch, Operation<Void> original) {
		if (!GeneCombat.has((Creeper) (Object) this, GeneEffect.SilentFuse.class)) {
			original.call(level, except, x, y, z, sound, source, volume, pitch);
		}
	}

	/**
	 * The lightning of a blast does not charge the creeper. The lightning still burns and hurts it.
	 */
	@WrapOperation(method = "thunderHit", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V"))
	private void originofspawns$noChargeFromBlast(SynchedEntityData data, EntityDataAccessor<?> accessor, Object value,
			Operation<Void> original, @Local(argsOnly = true) LightningBolt bolt) {
		if (!bolt.getAttachedOrElse(ModAttachments.NO_CHARGE, false)) {
			original.call(data, accessor, value);
		}
	}
}
