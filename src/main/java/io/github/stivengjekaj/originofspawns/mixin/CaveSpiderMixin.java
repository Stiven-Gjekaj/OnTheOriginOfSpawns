package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.monster.spider.CaveSpider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CaveSpider.class)
public abstract class CaveSpiderMixin {
	/**
	 * The genes change the time of the Poison of the bite.
	 */
	@ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/core/Holder;II)V"), index = 1)
	private int originofspawns$poisonTicks(int ticks) {
		return GeneCombat.bitePoisonTicks((CaveSpider) (Object) this, ticks);
	}

	/**
	 * The genes make the Poison of the bite stronger.
	 */
	@ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/core/Holder;II)V"), index = 2)
	private int originofspawns$poisonLevel(int amplifier) {
		return GeneCombat.bitePoisonAmplifier((CaveSpider) (Object) this, amplifier);
	}
}
