package io.github.stivengjekaj.originofspawns.evolution;

/**
 * Short-lived state of the behavior effects of a mob. The game does not save it.
 *
 * @param callTime the game time of the last call on sight, or -1
 * @param stunUntil the game time when a stun ends
 * @param riseTicks the ticks that are left of a rise out of the ground
 * @param sidestepTime the game time of the last sidestep, or -1
 * @param throwTime the game time of the last throw, or -1
 */
public record BehaviorState(long callTime, long stunUntil, int riseTicks, long sidestepTime, long throwTime) {
	public static final BehaviorState NONE = new BehaviorState(-1L, -1L, 0, -1L, -1L);

	public BehaviorState withCallTime(long time) {
		return new BehaviorState(time, stunUntil, riseTicks, sidestepTime, throwTime);
	}

	public BehaviorState withStunUntil(long time) {
		return new BehaviorState(callTime, time, riseTicks, sidestepTime, throwTime);
	}

	public BehaviorState withRiseTicks(int ticks) {
		return new BehaviorState(callTime, stunUntil, ticks, sidestepTime, throwTime);
	}

	public BehaviorState withSidestepTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, time, throwTime);
	}

	public BehaviorState withThrowTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, time);
	}
}
