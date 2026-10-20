package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.Evolution;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin {
	/**
	 * A species in the tag {@code originofspawns:no_random_equipment} gets no random weapon and no random armor.
	 * The drowned has its own method for its natural trident, so this does not change it.
	 */
	@Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noRandomEquipment(RandomSource random, DifficultyInstance difficulty, CallbackInfo info) {
		if (((Zombie) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_RANDOM_EQUIPMENT)) {
			info.cancel();
		}
	}

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
	 * A species in the tag {@code originofspawns:no_vanilla_babies} spawns no babies in vanilla ways. Its babies come
	 * only from its baby gene, for example Runt for the zombie.
	 */
	@WrapOperation(method = "finalizeSpawn", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;getSpawnAsBabyOdds(Lnet/minecraft/util/RandomSource;)Z"))
	private boolean originofspawns$noVanillaBabies(RandomSource random, Operation<Boolean> original) {
		if (((Zombie) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_VANILLA_BABIES)) {
			return false;
		}
		return original.call(random);
	}

	/**
	 * A mob with the no conversion effect does not convert in water. The hook is in the tick, because the husk
	 * has its own method for the question.
	 */
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;convertsInWater()Z"))
	private boolean originofspawns$noConversion(Zombie zombie, Operation<Boolean> original) {
		return original.call(zombie) && !GeneCombat.has(zombie, GeneEffect.NoConversion.class);
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

	@ModifyConstant(method = "playStepSound", constant = @Constant(floatValue = 0.15F))
	private float originofspawns$stepVolume(float volume) {
		return volume * GeneBehaviors.volumeMultiplier((Zombie) (Object) this);
	}

	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	private void originofspawns$silentSteps(BlockPos pos, BlockState state, CallbackInfo info) {
		GeneEffect.Silence silence = GeneBehaviors.silence((Zombie) (Object) this);
		if (silence != null && silence.steps()) {
			info.cancel();
		}
	}
}
