package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
	/**
	 * When a player breaks a gold block or opens a chest, the mobs with the guard gold gene attack too. The vanilla
	 * anger reaches only the piglins.
	 */
	@Inject(method = "angerNearbyPiglins", at = @At("TAIL"))
	private static void originofspawns$guardGold(ServerLevel level, Player player, boolean onlyIfTheySeeThePlayer,
			CallbackInfo info) {
		GeneBehaviors.guardGold(level, player, onlyIfTheySeeThePlayer);
	}
}
