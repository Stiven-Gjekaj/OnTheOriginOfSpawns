package io.github.stivengjekaj.originofspawns.mixin;

import io.github.stivengjekaj.originofspawns.evolution.GeneAnger;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin implements NeutralMob {
	/**
	 * The anger range genes make the call to the other zombified piglins reach farther.
	 */
	@ModifyVariable(method = "alertOthers", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	private double originofspawns$angerRange(double within) {
		return within * GeneAnger.rangeMultiplier((ZombifiedPiglin) (Object) this);
	}

	/**
	 * The anger time genes make the anger last longer.
	 */
	@ModifyArg(method = "startPersistentAngerTimer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/zombie/ZombifiedPiglin;setTimeToRemainAngry(J)V"))
	private long originofspawns$angerTime(long ticks) {
		return Math.round(ticks * GeneAnger.timeMultiplier((ZombifiedPiglin) (Object) this));
	}

	/**
	 * A zombified piglin with the anger after death gene stays angry at a player that died. Else it works as the
	 * vanilla rule: it forgives the player if the gamerule says so.
	 */
	@Override
	public void playerDied(ServerLevel level, Player player) {
		if (GeneAnger.rememberDeadPlayer((ZombifiedPiglin) (Object) this, player)) {
			return;
		}
		EntityReference<LivingEntity> angry = getPersistentAngerTarget();
		if (level.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS) && angry != null && angry.matches(player)) {
			stopBeingAngry();
		}
	}
}
