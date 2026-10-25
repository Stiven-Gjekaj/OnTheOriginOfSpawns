package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;

/**
 * The genes of the anger of a neutral mob, such as the zombified piglin: how far its anger reaches, how long it
 * lasts, and the anger after the death of a player.
 */
public final class GeneAnger {
	private GeneAnger() {
	}

	/**
	 * Returns the product of the anger range multipliers of the mob. A mob with no such gene gives 1.
	 */
	public static double rangeMultiplier(Mob mob) {
		return GeneEffects.of(mob).filter(GeneEffect.AngerRange.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.AngerRange) effect).multiplier()).reduce(1.0, (a, b) -> a * b);
	}

	/**
	 * Returns the product of the anger time multipliers of the mob. A mob with no such gene gives 1.
	 */
	public static double timeMultiplier(Mob mob) {
		return GeneEffects.of(mob).filter(GeneEffect.AngerTime.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.AngerTime) effect).multiplier()).reduce(1.0, (a, b) -> a * b);
	}

	/**
	 * Keeps the anger of the mob at a player that died, for the longest time of its genes.
	 *
	 * @return true if the mob stays angry, so the vanilla rule does not forgive the player
	 */
	public static boolean rememberDeadPlayer(Mob mob, Player player) {
		if (!(mob instanceof NeutralMob neutral)) {
			return false;
		}
		EntityReference<LivingEntity> angry = neutral.getPersistentAngerTarget();
		var seconds = GeneEffects.of(mob).filter(GeneEffect.AngerAfterDeath.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.AngerAfterDeath) effect).seconds()).max();
		if (angry == null || !angry.matches(player) || seconds.isEmpty()) {
			return false;
		}
		// The mob forgets the dead body as a target, and it finds the player again by the anger when it comes back.
		mob.setTarget(null);
		neutral.setTimeToRemainAngry(Math.round(seconds.getAsDouble() * 20.0));
		return true;
	}
}
