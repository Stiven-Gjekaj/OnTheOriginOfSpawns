package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * The teleports of the genes. An enderman and an endermite use the same teleports.
 */
public final class GeneTeleports {
	/**
	 * The distances from the target of the places next to it, in blocks.
	 */
	private static final double[] NEAR_DISTANCES = {1.5, 2.5};
	/**
	 * The number of directions around the target.
	 */
	private static final int DIRECTIONS = 8;

	private GeneTeleports() {
	}

	/**
	 * Teleports the mob next to its target, if the state is true, the target is far enough, and the cooldown is over.
	 *
	 * @return true if the mob teleported
	 */
	public static boolean toTarget(Mob mob, ServerLevel level, GeneEffect.TeleportToTarget teleport) {
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive() || mob.distanceTo(target) <= teleport.minDistance()
				|| !GeneCombat.ready(mob, teleport, teleport.cooldownSeconds())
				|| !GeneCombat.test(mob, teleport.when())) {
			return false;
		}
		if (!near(mob, level, target)) {
			// No free place: the cooldown does not start, so the mob tries again soon.
			return false;
		}
		GeneCombat.startCooldown(mob, teleport);
		return true;
	}

	/**
	 * Teleports the mob to a free place next to the target. The places nearest to the mob come first, so the mob
	 * comes from its own side.
	 *
	 * @return true if the mob teleported
	 */
	public static boolean near(Mob mob, ServerLevel level, LivingEntity target) {
		List<Vec3> places = new ArrayList<>();
		for (double distance : NEAR_DISTANCES) {
			for (int i = 0; i < DIRECTIONS; i++) {
				double angle = i * 2.0 * Math.PI / DIRECTIONS;
				places.add(new Vec3(target.getX() + Math.cos(angle) * distance, target.getY(),
						target.getZ() + Math.sin(angle) * distance));
			}
		}
		places.sort(Comparator.comparingDouble(place -> place.distanceToSqr(mob.position())));
		return tryPlaces(mob, level, places);
	}

	/**
	 * Teleports the mob behind the target, after a hit, if the cooldown is over.
	 *
	 * @return true if the mob teleported
	 */
	public static boolean afterHit(Mob mob, ServerLevel level, GeneEffect.TeleportBehind teleport, LivingEntity target) {
		if (!GeneCombat.ready(mob, teleport, teleport.cooldownSeconds()) || !behind(mob, level, target)) {
			return false;
		}
		GeneCombat.startCooldown(mob, teleport);
		return true;
	}

	/**
	 * Teleports the mob to a free place behind the target: the side that the target does not face. The place right
	 * behind it comes first, then the places a little to each side.
	 *
	 * @return true if the mob teleported
	 */
	public static boolean behind(Mob mob, ServerLevel level, LivingEntity target) {
		List<Vec3> places = new ArrayList<>();
		for (double distance : NEAR_DISTANCES) {
			for (float turn : new float[] {0.0F, 45.0F, -45.0F}) {
				Vec3 back = Vec3.directionFromRotation(0.0F, target.getYRot() + 180.0F + turn).scale(distance);
				places.add(target.position().add(back.x, 0.0, back.z));
			}
		}
		return tryPlaces(mob, level, places);
	}

	/**
	 * Teleports the mob to the first of the places that is free. A place is free when the mob fits there, on a
	 * block that it can stand on, out of liquid.
	 */
	static boolean tryPlaces(Mob mob, ServerLevel level, List<Vec3> places) {
		Vec3 from = mob.position();
		for (Vec3 place : places) {
			// The vanilla random teleport looks down from one block above the place for the ground. The blocks that an
			// enderman does not teleport to stop each mob that uses these teleports.
			if (mob.randomTeleport(place.x, place.y + 1.0, place.z, true, BlockTags.ENDERMAN_DOES_NOT_TELEPORT_TO)) {
				arrive(mob, level, from);
				return true;
			}
		}
		return false;
	}

	/**
	 * Marks the time of the teleport, with the sound and the game event of the vanilla enderman teleport.
	 */
	public static void arrive(Mob mob, ServerLevel level, Vec3 from) {
		mob.setAttached(ModAttachments.TELEPORT_TIME, level.getGameTime());
		level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(mob));
		if (!mob.isSilent()) {
			level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, mob.getSoundSource(), 1.0F, 1.0F);
			mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
		}
	}
}
