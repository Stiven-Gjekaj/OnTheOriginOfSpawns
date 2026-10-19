package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
