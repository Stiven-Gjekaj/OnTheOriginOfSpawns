package io.github.stivengjekaj.originofspawns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.stivengjekaj.originofspawns.evolution.Evolution;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.piglin.Piglin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Piglin.class)
public abstract class PiglinMixin {
	/**
	 * A species in the tag {@code originofspawns:no_random_equipment} gets no random gold armor. Its weapon stays.
	 */
	@Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
	private void originofspawns$noRandomArmor(RandomSource random, DifficultyInstance difficulty, CallbackInfo info) {
		if (((Piglin) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_RANDOM_EQUIPMENT)) {
			info.cancel();
		}
	}

	/**
	 * A species in the tag {@code originofspawns:no_vanilla_babies} spawns no babies in vanilla ways. The roll for a
	 * baby fails, so the piglin gets its weapon as an adult does.
	 */
	@ModifyExpressionValue(method = "finalizeSpawn", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 0))
	private float originofspawns$noVanillaBabies(float roll) {
		return ((Piglin) (Object) this).getType().builtInRegistryHolder().is(Evolution.NO_VANILLA_BABIES) ? 1.0F : roll;
	}
}
