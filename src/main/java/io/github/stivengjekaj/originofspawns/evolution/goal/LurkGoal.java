package io.github.stivengjekaj.originofspawns.evolution.goal;

import java.util.EnumSet;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Holds the mob still while its target player faces it, until the player comes near.
 * The check uses the direction of the player, so the camera mode does not change it.
 */
public class LurkGoal extends Goal {
	/**
	 * The cosine of the half angle of the view of a player, about 60 degrees.
	 */
	private static final double VIEW_COS = 0.5;

	private final Mob mob;
	private final GeneEffect.Lurk lurk;

	public LurkGoal(Mob mob, GeneEffect.Lurk lurk) {
		this.mob = mob;
		this.lurk = lurk;
		setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return mob.getTarget() instanceof Player player && isWatchedFromAfar(mob, player, lurk.radius());
	}

	@Override
	public void start() {
		mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		mob.getNavigation().stop();
	}

	public static boolean isWatchedFromAfar(Mob mob, Player player, double radius) {
		Vec3 toMob = mob.getEyePosition().subtract(player.getEyePosition());
		double distance = toMob.length();
		if (distance <= radius) {
			return false;
		}
		return player.getLookAngle().dot(toMob.scale(1.0 / distance)) >= VIEW_COS && player.hasLineOfSight(mob);
	}
}
