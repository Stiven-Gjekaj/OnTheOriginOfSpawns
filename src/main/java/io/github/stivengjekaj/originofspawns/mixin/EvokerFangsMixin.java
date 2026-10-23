package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.EvokerFangs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EvokerFangs.class)
public abstract class EvokerFangsMixin {
	/**
	 * The genes of the owner act on each target that the fangs hurt. Vanilla runs the enchantment effects only after a
	 * hit that does damage, so the genes act there too.
	 */
	@Inject(method = "dealDamageTo", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffects(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
	private void originofspawns$fangHit(LivingEntity target, CallbackInfo info) {
		if (((EvokerFangs) (Object) this).getOwner() instanceof Mob owner) {
			GeneCombat.onFangHit(owner, target);
		}
	}
}
