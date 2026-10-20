package io.github.stivengjekaj.originofspawns.notebook;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Records what a player learns about families.
 * A player knows the family of a mob when the player kills the mob, or when the mob kills the player.
 * A player sees the family of a mob after the player was near the mob, with a clear line of sight, for some seconds.
 */
public final class Discovery {
	/**
	 * A player sees a mob in this distance.
	 */
	public static final double SEEN_RADIUS = 16.0;

	/**
	 * The seconds near a mob, in total, that a player needs to see its family.
	 */
	public static final int SEEN_SECONDS = 8;

	/**
	 * The seconds that each player was near each mob, by the UUIDs of the player and the mob.
	 */
	private static final Map<UUID, Map<UUID, Integer>> NEAR = new HashMap<>();

	private Discovery() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Discovery::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> NEAR.remove(handler.getPlayer().getUUID()));
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % 20 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.isSpectator()) {
				watch(player);
			}
		}
	}

	/**
	 * Counts one second for each mob with genes that the player is near and can see. A mob that reaches the time shows
	 * its family to the player.
	 */
	public static void watch(ServerPlayer player) {
		Map<UUID, Integer> seconds = NEAR.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>());
		ServerLevel level = player.level();
		seconds.keySet().removeIf(uuid -> level.getEntity(uuid) == null);
		for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(SEEN_RADIUS),
				mob -> mob.isAlive() && mob.hasAttached(ModAttachments.GENES) && mob.distanceTo(player) <= SEEN_RADIUS
						&& player.hasLineOfSight(mob))) {
			int total = seconds.merge(mob.getUUID(), 1, Integer::sum);
			if (total >= SEEN_SECONDS) {
				see(player, mob);
			}
		}
	}

	/**
	 * Adds the family of the mob to the families that the player saw, unless the player knows it.
	 *
	 * @return true if the family is new to the player
	 */
	public static boolean see(ServerPlayer player, Mob mob) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		if (genes == null) {
			return false;
		}
		String key = genes.family().translationKey();
		DiscoveredFamilies found = player.getAttachedOrElse(ModAttachments.DISCOVERED, DiscoveredFamilies.NONE);
		if (found.knowledge(key) != Knowledge.NONE) {
			return false;
		}
		player.setAttached(ModAttachments.DISCOVERED, found.withSeen(key));
		return true;
	}

	/**
	 * Adds the family of the mob to the families that the player knows.
	 *
	 * @return true if the player did not know the family
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
