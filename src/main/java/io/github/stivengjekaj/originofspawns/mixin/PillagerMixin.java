package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;

import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pillager.class)
public abstract class PillagerMixin {
	/**
	 * A vanilla pillager wants only the banner of a raid. A pillager that picks up items puts each item that it picks up
	 * in its sack, its inventory of 5 slots.
	 */
	@ModifyReturnValue(method = "wantsItem", at = @At("RETURN"))
	private boolean originofspawns$plunder(boolean wants, ItemStack stack) {
		return wants || GeneCombat.has((Pillager) (Object) this, GeneEffect.PickUpItems.class);
	}
}
