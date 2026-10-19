package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin {
	/**
	 * A zombie with the convert villagers effect turns each villager that it kills, on each difficulty.
	 */
	@Inject(method = "killedEntity", at = @At("HEAD"), cancellable = true)
	private void originofspawns$alwaysConvert(ServerLevel level, LivingEntity entity, DamageSource source,
			CallbackInfoReturnable<Boolean> info) {
		Zombie self = (Zombie) (Object) this;
		if (entity instanceof Villager villager && GeneCombat.has(self, GeneEffect.ConvertVillagers.class)) {
			info.setReturnValue(!self.convertVillagerToZombieVillager(level, villager));
		}
	}

	/**
	 * Baby zombies come only from the Runt gene. The husk and the drowned keep the vanilla babies.
	 */
	@WrapOperation(method = "finalizeSpawn", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;getSpawnAsBabyOdds(Lnet/minecraft/util/RandomSource;)Z"))
	private boolean originofspawns$noVanillaBabies(RandomSource random, Operation<Boolean> original) {
		if (((Zombie) (Object) this).getType() == EntityTypes.ZOMBIE) {
			return false;
		}
		return original.call(random);
	}

	@Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
	private void originofspawns$silentIdle(CallbackInfoReturnable<SoundEvent> info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Zombie) (Object) this);
		if (silence != null && silence.idle()) {
			info.setReturnValue(null);
		}
	}

	@Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
	private void originofspawns$silentHurt(DamageSource source, CallbackInfoReturnable<SoundEvent> info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Zombie) (Object) this);
		if (silence != null && silence.hurt()) {
			info.setReturnValue(null);
		}
	}

	@Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
	private void originofspawns$silentDeath(CallbackInfoReturnable<SoundEvent> info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Zombie) (Object) this);
		if (silence != null && silence.death()) {
			info.setReturnValue(null);
		}
	}

	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	private void originofspawns$silentSteps(BlockPos pos, BlockState state, CallbackInfo info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Zombie) (Object) this);
		if (silence != null && silence.steps()) {
			info.cancel();
		}
	}
}
