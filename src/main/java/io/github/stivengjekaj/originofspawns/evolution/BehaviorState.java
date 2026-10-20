package io.github.stivengjekaj.originofspawns.evolution;

/**
 * Short-lived state of the behavior effects of a mob. The game does not save it.
 *
 * @param callTime the game time of the last call on sight, or -1
 * @param stunUntil the game time when a stun ends
 * @param riseTicks the ticks that are left of a rise out of the ground
 * @param sidestepTime the game time of the last sidestep, or -1
 * @param throwTime the game time of the last throw, or -1
 * @param dragTarget the entity id of the player that the mob drags under, or -1
 * @param dragUntil the game time when the drag ends
 * @param boatId the entity id of the last boat that the mob hit, or -1
 * @param boatHits the number of hits on that boat
 * @param lureNearTime the last game time when a player was near the lure, or -1
 * @param lureArmed true when the next hit of the mob gets the lure damage
 * @param hurtEffectTime the game time of the last effect on hurt, or -1
 * @param hurtCallTime the game time of the last call on hurt, or -1
 * @param collapseUntil the game time when a mob that fell apart stands up again, or -1
 */
public record BehaviorState(long callTime, long stunUntil, int riseTicks, long sidestepTime, long throwTime,
		int dragTarget, long dragUntil, int boatId, int boatHits, long lureNearTime, boolean lureArmed,
		long hurtEffectTime, long hurtCallTime, long collapseUntil) {
	public static final BehaviorState NONE = new BehaviorState(-1L, -1L, 0, -1L, -1L, -1, -1L, -1, 0, -1L, false, -1L, -1L, -1L);

	public BehaviorState withCallTime(long time) {
		return new BehaviorState(time, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withStunUntil(long time) {
		return new BehaviorState(callTime, time, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withRiseTicks(int ticks) {
		return new BehaviorState(callTime, stunUntil, ticks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withSidestepTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, time, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withThrowTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, time, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	/**
	 * Starts a drag of the entity until the game time, or stops the drag with the target -1.
	 */
	public BehaviorState withDrag(int target, long until) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, target, until, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withBoatHits(int boat, int hits) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boat,
				hits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withLure(long nearTime, boolean armed) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, nearTime, armed, hurtEffectTime, hurtCallTime, collapseUntil);
	}

	public BehaviorState withHurtEffectTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, time, hurtCallTime, collapseUntil);
	}

	public BehaviorState withHurtCallTime(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, time, collapseUntil);
	}

	public BehaviorState withCollapseUntil(long time) {
		return new BehaviorState(callTime, stunUntil, riseTicks, sidestepTime, throwTime, dragTarget, dragUntil, boatId,
				boatHits, lureNearTime, lureArmed, hurtEffectTime, hurtCallTime, time);
	}
}
