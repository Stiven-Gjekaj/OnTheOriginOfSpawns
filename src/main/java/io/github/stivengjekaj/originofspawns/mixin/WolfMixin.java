package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneAnger;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Lets the genes of a wolf change its anger. The anger range genes act in the call for help of the hurt wolf, which
 * is the vanilla call of the pack.
 */
@Mixin(Wolf.class)
public abstract class WolfMixin implements NeutralMob {
	/**
	 * The anger time genes make the anger last longer.
	 */
	@ModifyArg(method = "startPersistentAngerTimer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/animal/wolf/Wolf;setTimeToRemainAngry(J)V"))
	private long originofspawns$angerTime(long ticks) {
		return Math.round(ticks * GeneAnger.timeMultiplier((Wolf) (Object) this));
	}

	/**
	 * A wolf with the anger after death gene stays angry at a player that died. Else it works as the vanilla rule: it
	 * forgives the player if the gamerule says so.
	 */
	@Override
	public void playerDied(ServerLevel level, Player player) {
		if (GeneAnger.rememberDeadPlayer((Wolf) (Object) this, player)) {
			return;
		}
		EntityReference<LivingEntity> angry = getPersistentAngerTarget();
		if (level.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS) && angry != null && angry.matches(player)) {
			stopBeingAngry();
		}
	}
}
