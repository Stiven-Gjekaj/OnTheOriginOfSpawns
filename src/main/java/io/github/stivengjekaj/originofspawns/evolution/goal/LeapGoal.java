package io.github.stivengjekaj.originofspawns.evolution.goal;

import java.util.EnumSet;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.evolution.GeneBehaviors;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Makes the mob leap at its target from a distance: a jump from the ground, a dash through water, or a jump out of
 * the water. A missed leap stuns the mob.
 */
public class LeapGoal extends Goal {
	private static final double HIT_DISTANCE = 2.0;
	/**
	 * A breach reaches a target that is up to this height above the mob.
	 */
	private static final double BREACH_HEIGHT = 3.0;
	private static final int DASH_TICKS = 8;
	private static final double DASH_SPEED = 0.6;

	private final Mob mob;
	private final GeneEffect.Leap leap;
	private long readyTime;
	private boolean airborne;
	private int dashTicks;
	private boolean hit;
	private Vec3 dashDirection = Vec3.ZERO;

	public LeapGoal(Mob mob, GeneEffect.Leap leap) {
		this.mob = mob;
		this.leap = leap;
		setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = mob.getTarget();
		if (target == null || mob.level().getGameTime() < readyTime) {
			return false;
		}
		boolean ready = switch (leap.medium()) {
			case GROUND -> mob.onGround();
			case WATER -> mob.isInWater() && target.isInWater();
			case BREACH -> {
				double height = target.getY() - waterSurface();
				yield mob.isInWater() && !target.isInWater() && height >= -1.0 && height < BREACH_HEIGHT;
			}
		};
		double distance = mob.distanceTo(target);
		return ready && distance >= leap.minDistance() && distance <= leap.maxDistance()
				&& GeneCombat.test(mob, leap.when());
	}

	/**
	 * Returns the height of the top of the water above the mob.
	 */
	private double waterSurface() {
		BlockPos pos = mob.blockPosition();
		while (mob.level().getFluidState(pos).is(FluidTags.WATER) && pos.getY() < mob.level().getMaxY()) {
			pos = pos.above();
		}
		return pos.getY();
	}

	@Override
	public boolean canContinueToUse() {
		if (leap.medium() == GeneEffect.Leap.Medium.WATER) {
			return dashTicks > 0;
		}
		return !airborne || !mob.onGround() && !mob.isInWater();
	}

	@Override
	public void start() {
		LivingEntity target = mob.getTarget();
		Vec3 flat = new Vec3(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ()).normalize();
		switch (leap.medium()) {
			case GROUND -> mob.setDeltaMovement(flat.x * 0.9, 0.42, flat.z * 0.9);
			case WATER -> {
				dashDirection = target.position().subtract(mob.position()).normalize();
				dashTicks = DASH_TICKS;
			}
			case BREACH -> mob.setDeltaMovement(flat.x * 0.7, 0.9, flat.z * 0.7);
		}
		airborne = false;
		hit = false;
		GeneBehaviors.markLeap(mob);
		readyTime = mob.level().getGameTime() + Math.round(leap.cooldownSeconds() * 20.0F);
	}

	@Override
	public void tick() {
		if (dashTicks > 0) {
			mob.setDeltaMovement(dashDirection.scale(DASH_SPEED));
			dashTicks--;
		}
		if (!mob.onGround() && !mob.isInWater()) {
			airborne = true;
		}
		// A leap that reaches the target does a normal hit, one time.
		LivingEntity target = mob.getTarget();
		if (!hit && target != null && mob.level() instanceof ServerLevel level && mob.isWithinMeleeAttackRange(target)) {
			hit = true;
			mob.doHurtTarget(level, target);
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
		dashTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
