package io.github.stivengjekaj.originofspawns.notebook;

import java.util.Set;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Records the families that a player learns. A player learns the family of a mob only when the player kills the mob,
 * or when the mob kills the player.
 */
public final class Discovery {
	private Discovery() {
	}

	/**
	 * Adds the family of the mob to the families that the player found.
	 *
	 * @return true if the family is new to the player
	 */
	public static boolean learn(ServerPlayer player, Mob mob) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		if (genes == null) {
			return false;
		}
		String key = genes.family().translationKey();
		DiscoveredFamilies found = player.getAttachedOrElse(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		if (found.contains(key)) {
			return false;
		}
		player.setAttached(ModAttachments.DISCOVERED, found.with(Set.of(key)));
		return true;
	}
}
