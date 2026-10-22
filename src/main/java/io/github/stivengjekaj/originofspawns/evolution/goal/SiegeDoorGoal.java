package io.github.stivengjekaj.originofspawns.evolution.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;

/**
 * Makes a mob with the siege effect break doors on each difficulty, in the time of the effect. The vanilla goal never
 * breaks a door in less than 12 seconds.
 */
public class SiegeDoorGoal extends BreakDoorGoal {
	private final int ticks;

	public SiegeDoorGoal(Mob mob, int ticks) {
		super(mob, difficulty -> true);
		this.ticks = Math.max(1, ticks);
	}

	@Override
	protected int getDoorBreakTime() {
		return ticks;
	}

	/**
	 * Returns the ticks that the mob needs to break a door.
	 */
	public int breakTicks() {
		return getDoorBreakTime();
	}
}
