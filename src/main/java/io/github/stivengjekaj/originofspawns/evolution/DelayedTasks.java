package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerLevel;

/**
 * Runs short tasks in a level after a delay in ticks. The game does not save the tasks, so a task that still waits
 * when the server stops does not run.
 */
public final class DelayedTasks {
	private static final List<Task> TASKS = new ArrayList<>();

	private DelayedTasks() {
	}

	private record Task(ServerLevel level, long time, Runnable action) {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(DelayedTasks::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> TASKS.clear());
	}

	/**
	 * Runs the action at the end of the tick of the level that comes after the delay. A delay of 0 runs it at the end
	 * of this tick.
	 */
	public static void schedule(ServerLevel level, int ticks, Runnable action) {
		TASKS.add(new Task(level, level.getGameTime() + Math.max(0, ticks), action));
	}

	private static void tick(ServerLevel level) {
		long now = level.getGameTime();
		List<Task> due = new ArrayList<>();
		TASKS.removeIf(task -> {
			boolean ready = task.level() == level && task.time() <= now;
			if (ready) {
				due.add(task);
			}
			return ready;
		});
		// An action can schedule a new task, so the actions run after the list changes.
		for (Task task : due) {
			task.action().run();
		}
	}
}
