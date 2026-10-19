package io.github.stivengjekaj.originofspawns.evolution.goal;

import java.util.EnumSet;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Makes the mob leap at its target from a distance. A missed leap stuns the mob.
 */
public class LeapGoal extends Goal {
	private static final double HIT_DISTANCE = 2.0;

	private final Mob mob;
	private final GeneEffect.Leap leap;
	private long readyTime;
	private boolean airborne;

	public LeapGoal(Mob mob, GeneEffect.Leap leap) {
		this.mob = mob;
		this.leap = leap;
		setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = mob.getTarget();
		if (target == null || !mob.onGround() || mob.level().getGameTime() < readyTime) {
			return false;
		}
		double distance = mob.distanceTo(target);
		return distance >= leap.minDistance() && distance <= leap.maxDistance();
	}

	@Override
	public boolean canContinueToUse() {
		return !airborne || !mob.onGround();
	}

	@Override
	public void start() {
		LivingEntity target = mob.getTarget();
		Vec3 direction = new Vec3(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ()).normalize();
		mob.setDeltaMovement(direction.x * 0.9, 0.42, direction.z * 0.9);
		airborne = false;
		readyTime = mob.level().getGameTime() + Math.round(leap.cooldownSeconds() * 20.0F);
	}

	@Override
	public void tick() {
		if (!mob.onGround()) {
			airborne = true;
		}
	}

	@Override
	public void stop() {
		LivingEntity target = mob.getTarget();
		if (target == null || mob.distanceTo(target) > HIT_DISTANCE) {
			long stunUntil = mob.level().getGameTime() + Math.round(leap.missStunSeconds() * 20.0F);
			mob.setAttached(ModAttachments.STATE,
					mob.getAttachedOrElse(ModAttachments.STATE, BehaviorState.NONE).withStunUntil(stunUntil));
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
