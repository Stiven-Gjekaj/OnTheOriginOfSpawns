package io.github.stivengjekaj.originofspawns.notebook;

import java.util.HashSet;
import java.util.Set;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Records the families of the mobs near each player, for the notebook.
 */
public final class Discovery {
	/**
	 * A player finds the family of a mob in this distance.
	 */
	public static final double RADIUS = 16.0;

	private static final int INTERVAL = 40;

	private Discovery() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Discovery::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % INTERVAL != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.isSpectator()) {
				discover(player);
			}
		}
	}

	/**
	 * Adds the families of the mobs near the player to the families that the player found.
	 *
	 * @return the number of new families
	 */
	public static int discover(ServerPlayer player) {
		DiscoveredFamilies found = player.getAttachedOrElse(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		Set<String> near = new HashSet<>();
		for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
				mob -> mob.isAlive() && mob.distanceTo(player) <= RADIUS)) {
			CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
			if (genes != null && !found.contains(genes.family().translationKey())) {
				near.add(genes.family().translationKey());
			}
		}
		if (!near.isEmpty()) {
			player.setAttached(ModAttachments.DISCOVERED, found.with(near));
		}
		return near.size();
	}
}
