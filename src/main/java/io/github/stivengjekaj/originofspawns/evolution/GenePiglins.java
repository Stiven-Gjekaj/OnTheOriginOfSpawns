package io.github.stivengjekaj.originofspawns.evolution;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * The genes of the piglins and the hoglins: the gold armor of a player, and the vanilla fears.
 */
public final class GenePiglins {
	private GenePiglins() {
	}

	/**
	 * Returns true if the piglin attacks the player, also in gold armor. The piglin rolls its chance one time for each
	 * player, and keeps the answer.
	 */
	public static boolean attacksInGold(LivingEntity piglin, Player player) {
		var chance = GeneEffects.of(piglin).filter(GeneEffect.GoldArmorAttack.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.GoldArmorAttack) effect).chance()).max();
		if (chance.isEmpty()) {
			return false;
		}
		Map<UUID, Boolean> rolls = piglin.getAttachedOrElse(ModAttachments.GOLD_ROLLS, Map.of());
		Boolean roll = rolls.get(player.getUUID());
		if (roll == null) {
			roll = piglin.getRandom().nextFloat() < chance.getAsDouble();
			Map<UUID, Boolean> next = new HashMap<>(rolls);
			next.put(player.getUUID(), roll);
			piglin.setAttached(ModAttachments.GOLD_ROLLS, next);
		}
		return roll;
	}

	/**
	 * Returns true if a gene of the mob stops the fear.
	 */
	public static boolean fearless(LivingEntity mob, GeneEffect.NoFear.Fear fear) {
		return GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.NoFear noFear && noFear.fears().contains(fear));
	}
}
