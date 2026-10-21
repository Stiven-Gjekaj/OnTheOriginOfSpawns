package io.github.stivengjekaj.originofspawns.test;

import java.util.concurrent.atomic.AtomicLong;

import io.github.stivengjekaj.originofspawns.evolution.DelayedTasks;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Tests the tasks that run after a delay.
 */
public class DelayedTasksTest {
	@GameTest(maxTicks = 40)
	public void aTaskRunsAfterItsDelay(GameTestHelper helper) {
		long start = helper.getLevel().getGameTime();
		AtomicLong ran = new AtomicLong(-1L);
		DelayedTasks.schedule(helper.getLevel(), 10, () -> ran.set(helper.getLevel().getGameTime()));
		helper.assertValueEqual(ran.get(), -1L, "the task does not run at once");
		helper.succeedWhen(() -> {
			helper.assertTrue(ran.get() >= 0L, "the task ran");
			helper.assertValueEqual(ran.get() - start, 10L, "the task ran after 10 ticks");
		});
	}
}
