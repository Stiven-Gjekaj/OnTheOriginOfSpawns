package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.mixin.FallingBlockEntityAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

/**
 * The block that an enderman throws. It flies as a falling block, and it hurts the first entity that it hits.
 */
public final class GeneThrows {
	/**
	 * The speed of a thrown block, in blocks for each tick.
	 */
	private static final double SPEED = 1.0;
	/**
	 * The gravity of a falling block, in blocks for each tick.
	 */
	private static final double GRAVITY = 0.04;

	private GeneThrows() {
	}

	/**
	 * The thrower of a thrown block, and the damage of its hit.
	 */
	public record Thrown(int thrower, float damage) {
	}

	/**
	 * Throws the carried block of the enderman at its target, if the target is in the range, the enderman sees it,
	 * and the cooldown is over.
	 *
	 * @return the thrown block, or null
	 */
	public static FallingBlockEntity throwBlock(Enderman enderman, ServerLevel level, GeneEffect.BlockThrow thrown) {
		BlockState carried = enderman.getCarriedBlock();
		LivingEntity target = enderman.getTarget();
		if (carried == null || target == null || !target.isAlive()) {
			return null;
		}
		float distance = enderman.distanceTo(target);
		int cooldown = GeneBehaviors.scaleThrowInterval(enderman, Math.round(thrown.cooldownSeconds() * 20.0F));
		if (distance < thrown.minRange() || distance > thrown.range() || !enderman.hasLineOfSight(target)
				|| !GeneCombat.ready(enderman, thrown, cooldown / 20.0F)) {
			return null;
		}
		Vec3 from = enderman.getEyePosition();
		Vec3 to = target.position().add(0.0, target.getBbHeight() / 2.0, 0.0);
		Vec3 aim = to.subtract(from);
		// The block falls on its way, so the aim is higher by the drop in the time of the flight.
		double ticks = aim.horizontalDistance() / SPEED;
		Vec3 velocity = new Vec3(aim.x / ticks, (aim.y + 0.5 * GRAVITY * ticks * ticks) / ticks, aim.z / ticks);
		FallingBlockEntity block = spawn(level, from, carried, velocity, enderman,
				thrown.damage() * damageMultiplier(enderman), level.getGameRules().get(GameRules.MOB_GRIEFING));
		enderman.setCarriedBlock(null);
		GeneCombat.startCooldown(enderman, thrown);
		level.playSound(null, enderman.getX(), enderman.getY(), enderman.getZ(), SoundEvents.ENDERMAN_SCREAM,
				enderman.getSoundSource(), 0.6F, 1.4F);
		return block;
	}

	/**
	 * Returns the product of the thrown block damage multipliers of the mob.
	 */
	public static float damageMultiplier(LivingEntity mob) {
		float multiplier = 1.0F;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.ThrownBlockDamage damage) {
				multiplier *= damage.multiplier();
			}
		}
		return multiplier;
	}

	/**
	 * Adds a thrown block to the level. It drops no item, and it puts the block down where it lands only if
	 * {@code place} is true.
	 */
	public static FallingBlockEntity spawn(ServerLevel level, Vec3 from, BlockState state, Vec3 velocity, Entity thrower,
			float damage, boolean place) {
		FallingBlockEntity block = FallingBlockEntityAccessor.originofspawns$create(level, from.x, from.y, from.z, state);
		block.dropItem = false;
		if (!place) {
			block.disableDrop();
		}
		block.setDeltaMovement(velocity);
		block.setAttached(ModAttachments.THROWN_BLOCK, new Thrown(thrower.getId(), damage));
		level.addFreshEntity(block);
		return block;
	}

	/**
	 * Runs the effects of the thrower on the entity that its block hit: the status effects, and the stop of a
	 * shield that blocked the block.
	 *
	 * @param shield the shield that the target held up at the hit, or null
	 */
	private static void afterHit(LivingEntity thrower, ServerLevel level, LivingEntity target,
			net.minecraft.world.item.ItemStack shield) {
		for (GeneEffect effect : GeneEffects.of(thrower).toList()) {
			switch (effect) {
				case GeneEffect.ThrownBlockEffect hit when GeneCombat.test(thrower, hit.when()) ->
						target.addEffect(new net.minecraft.world.effect.MobEffectInstance(hit.effect(),
								Math.round(hit.seconds() * 20.0F), hit.amplifier()), thrower);
				case GeneEffect.ThrownBlockShield stop when shield != null
						&& shield.has(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS) ->
						shield.get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS)
								.disable(level, target, stop.seconds(), shield);
				default -> {
				}
			}
		}
	}

	/**
	 * Hurts the first living entity that the thrown block touches, and ends the block. The thrower is safe from its
	 * own block.
	 *
	 * @return true if the block hit an entity
	 */
	public static boolean hit(FallingBlockEntity block, ServerLevel level) {
		Thrown thrown = block.getAttached(ModAttachments.THROWN_BLOCK);
		if (thrown == null || block.isRemoved()) {
			return false;
		}
		Entity thrower = level.getEntity(thrown.thrower());
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, block.getBoundingBox().inflate(0.2),
				other -> other != thrower && other.isAlive())) {
			boolean blocking = target.isBlocking();
			net.minecraft.world.item.ItemStack shield = target.getItemBlockingWith();
			target.hurtServer(level, level.damageSources().thrown(block, thrower), thrown.damage());
			if (thrower instanceof LivingEntity mob) {
				afterHit(mob, level, target, blocking ? shield : null);
			}
			block.discard();
			return true;
		}
		return false;
	}
}
