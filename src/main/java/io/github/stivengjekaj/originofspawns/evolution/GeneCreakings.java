package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.phys.Vec3;

/**
 * The genes of a creaking: its twitch while a player looks at it, its heart, its wake, and its stare.
 */
public final class GeneCreakings {
	private GeneCreakings() {
	}

	/**
	 * Returns true while a player looks at the awake creaking and holds it still.
	 */
	public static boolean watched(Creaking creaking) {
		return creaking.isActive() && !creaking.canMove();
	}

	/**
	 * Lets a watched creaking take one step toward its target, if the shortest interval of its watched step genes is over.
	 *
	 * @return true if the creaking took a step
	 */
	public static boolean watchedStep(Creaking creaking) {
		var interval = GeneEffects.of(creaking).filter(GeneEffect.WatchedStep.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.WatchedStep) effect).intervalSeconds()).min();
		LivingEntity target = creaking.getTarget();
		if (interval.isEmpty() || target == null || !watched(creaking)) {
			return false;
		}
		long now = creaking.level().getGameTime();
		long last = creaking.getAttachedOrElse(ModAttachments.STEP_TIME, Long.MIN_VALUE / 2);
		if (now - last < Math.round(interval.getAsDouble() * 20.0)) {
			return false;
		}
		Vec3 toTarget = target.position().subtract(creaking.position()).multiply(1.0, 0.0, 1.0);
		if (toTarget.lengthSqr() < 1.0) {
			return false;
		}
		creaking.setAttached(ModAttachments.STEP_TIME, now);
		creaking.move(MoverType.SELF, toTarget.normalize());
		return true;
	}
}
