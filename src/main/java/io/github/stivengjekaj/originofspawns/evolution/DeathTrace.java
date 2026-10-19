package io.github.stivengjekaj.originofspawns.evolution;

import java.util.Optional;
import java.util.UUID;

/**
 * Short-lived facts about a mob that its death can need. The game does not save them.
 *
 * @param sunBurnTime the game time when the sun last set the mob on fire, or -1
 * @param criticalTime the game time when a player last started a critical hit on the mob, or -1
 * @param spotter the player that sees the mob from far away now, if one does
 * @param spottedSeconds how many seconds in a row the spotter saw the mob
 */
public record DeathTrace(long sunBurnTime, long criticalTime, Optional<UUID> spotter, int spottedSeconds) {
	public static final DeathTrace NONE = new DeathTrace(-1L, -1L, Optional.empty(), 0);

	public DeathTrace withSunBurn(long time) {
		return new DeathTrace(time, criticalTime, spotter, spottedSeconds);
	}

	public DeathTrace withCritical(long time) {
		return new DeathTrace(sunBurnTime, time, spotter, spottedSeconds);
	}

	/**
	 * Records one more second in the view of a player. A different player starts the count again.
	 */
	public DeathTrace seenBy(UUID player) {
		int seconds = spotter.filter(player::equals).isPresent() ? spottedSeconds + 1 : 1;
		return new DeathTrace(sunBurnTime, criticalTime, Optional.of(player), seconds);
	}

	public DeathTrace unseen() {
		return new DeathTrace(sunBurnTime, criticalTime, Optional.empty(), 0);
	}
}
