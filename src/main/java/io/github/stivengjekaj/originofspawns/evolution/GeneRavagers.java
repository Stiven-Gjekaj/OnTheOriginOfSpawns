package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;

/**
 * The genes of a ravager: its roar, its stun, its charge, and its rider.
 */
public final class GeneRavagers {
	/**
	 * The range of the vanilla roar, in blocks.
	 */
	public static final double VANILLA_ROAR_RANGE = 4.0;

	private GeneRavagers() {
	}

	/**
	 * Returns the range of the roar of the ravager: the largest roar range gene, or the vanilla range.
	 */
	public static double roarRange(Ravager ravager) {
		double range = VANILLA_ROAR_RANGE;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarRange roar) {
				range = Math.max(range, roar.blocks());
			}
		}
		return range;
	}

	/**
	 * Returns the damage of the roar of the ravager after its roar damage genes whose state is true.
	 */
	public static float roarDamage(Ravager ravager, float damage) {
		float result = damage;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarDamage roar && GeneCombat.test(ravager, roar.when())) {
				result *= roar.multiplier();
			}
		}
		return result;
	}

	/**
	 * Gives the roar effects of the ravager to an entity that its roar hurt.
	 */
	public static void roarHit(Ravager ravager, LivingEntity target) {
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarEffect roar && !roar.allies()) {
				target.addEffect(new MobEffectInstance(roar.effect(), Math.round(roar.seconds() * 20.0F), roar.amplifier()),
						ravager);
			}
		}
	}

	/**
	 * Gives the ally roar effects of the ravager to the other raiders in their radius, after a roar.
	 *
	 * @return the number of effects that the raiders got
	 */
	public static int roarAllies(Ravager ravager) {
		int given = 0;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarEffect roar && roar.allies()) {
				for (Raider raider : ravager.level().getEntitiesOfClass(Raider.class,
						ravager.getBoundingBox().inflate(roar.radius()), other -> other != ravager && other.isAlive())) {
					raider.addEffect(new MobEffectInstance(roar.effect(), Math.round(roar.seconds() * 20.0F), roar.amplifier()),
							ravager);
					given++;
				}
			}
		}
		return given;
	}

	/**
	 * The ticks of a roar before its blast, as the vanilla roar after a stun.
	 */
	private static final int ROAR_TICKS = 20;

	/**
	 * Starts a roar of the ravager now, as the vanilla roar at the end of a stun: the roar sound, and the blast of the
	 * roar half a second later. A ravager that roars or is stunned now does not start a second roar.
	 *
	 * @return true if the ravager started a roar
	 */
	public static boolean roarNow(Ravager ravager) {
		if (!ravager.isAlive() || ravager.getRoarTick() > 0 || ravager.getStunnedTick() > 0) {
			return false;
		}
		ravager.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
		((io.github.stivengjekaj.originofspawns.mixin.RavagerAccessor) ravager).originofspawns$setRoarTick(ROAR_TICKS);
		return true;
	}

	/**
	 * Starts a roar of the hurt ravager for each roar on hurt gene whose kind of hit matches and whose cooldown is over.
	 *
	 * @return true if the ravager started a roar
	 */
	public static boolean roarOnHurt(net.minecraft.world.entity.Mob mob, net.minecraft.world.damagesource.DamageSource source) {
		if (!(mob instanceof Ravager ravager) || source.getDirectEntity() == null) {
			return false;
		}
		boolean projectile = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarOnHurt roar && roar.hits().includes(projectile)
					&& GeneCombat.ready(ravager, roar, roar.cooldownSeconds()) && roarNow(ravager)) {
				GeneCombat.startCooldown(ravager, roar);
				return true;
			}
		}
		return false;
	}

	/**
	 * After the mob kills a player, each roar on kill gene heals it, and the ravager roars.
	 *
	 * @return true if a roar on kill gene acted
	 */
	public static boolean onPlayerKilled(net.minecraft.world.entity.Mob killer) {
		if (!(killer instanceof Ravager ravager)) {
			return false;
		}
		boolean acted = false;
		for (GeneEffect effect : GeneEffects.of(ravager).toList()) {
			if (effect instanceof GeneEffect.RoarOnKill roar) {
				ravager.heal(roar.heal());
				acted = true;
			}
		}
		if (acted) {
			roarNow(ravager);
		}
		return acted;
	}

	/**
	 * The vanilla chance that a shield block stuns a ravager.
	 */
	public static final double VANILLA_STUN_CHANCE = 0.5;

	/**
	 * Returns the chance that a shield block stuns the ravager: 0 with a no stun gene, else the largest stun chance gene,
	 * or the vanilla chance.
	 */
	public static double stunChance(Ravager ravager) {
		if (GeneEffects.of(ravager).anyMatch(GeneEffect.NoStun.class::isInstance)) {
			return 0.0;
		}
		var chance = GeneEffects.of(ravager).filter(GeneEffect.StunChance.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.StunChance) effect).chance()).max();
		return chance.orElse(VANILLA_STUN_CHANCE);
	}

	/**
	 * Starts a ground charge, or moves the mob on in its charge. In a charge, the mob runs at its target. A wall stops
	 * the charge. A touch hurts the target, pushes it back, and ends the charge.
	 *
	 * @return true while the mob charges
	 */
	public static boolean groundCharge(net.minecraft.world.entity.Mob mob, net.minecraft.server.level.ServerLevel level,
			GeneEffect.GroundCharge charge) {
		LivingEntity target = mob.getTarget();
		long now = level.getGameTime();
		Long until = mob.getAttached(ModAttachments.GROUND_CHARGE_UNTIL);
		if (until != null) {
			if (target == null || !target.isAlive() || now >= until || mob.horizontalCollision) {
				mob.removeAttached(ModAttachments.GROUND_CHARGE_UNTIL);
				return false;
			}
			if (mob.getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())) {
				mob.removeAttached(ModAttachments.GROUND_CHARGE_UNTIL);
				target.hurtServer(level, mob.damageSources().mobAttack(mob), charge.damage());
				net.minecraft.world.phys.Vec3 away = target.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0);
				if (away.lengthSqr() > 1.0E-6) {
					target.push(away.normalize().scale(1.2).add(0.0, 0.3, 0.0));
					target.needsSync = true;
				}
				return false;
			}
			run(mob, target, charge.speed());
			return true;
		}
		if (target == null || !target.isAlive() || !mob.onGround()) {
			return false;
		}
		double distance = mob.distanceTo(target);
		if (distance < charge.minRange() || distance > charge.range() || !mob.hasLineOfSight(target)
				|| !GeneCombat.ready(mob, charge, charge.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(mob, charge);
		mob.setAttached(ModAttachments.GROUND_CHARGE_UNTIL, now + Math.round(charge.seconds() * 20.0F));
		mob.getNavigation().stop();
		mob.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 1.0F, 1.2F);
		run(mob, target, charge.speed());
		return true;
	}

	private static void run(net.minecraft.world.entity.Mob mob, LivingEntity target, float speed) {
		net.minecraft.world.phys.Vec3 toTarget = target.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0);
		if (toTarget.lengthSqr() < 1.0E-6) {
			return;
		}
		net.minecraft.world.phys.Vec3 along = toTarget.normalize().scale(speed);
		mob.setDeltaMovement(along.x, mob.getDeltaMovement().y, along.z);
		mob.lookAt(target, 30.0F, 30.0F);
		mob.needsSync = true;
	}

	/**
	 * Breaks the blocks of the trample tag that the mob touches while it walks into a wall, as the vanilla ravager breaks
	 * leaves.
	 *
	 * @return the number of blocks that broke
	 */
	public static int trample(net.minecraft.world.entity.Mob mob, net.minecraft.server.level.ServerLevel level,
			GeneEffect.Trample trample) {
		if (!mob.horizontalCollision
				|| !level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)) {
			return 0;
		}
		int broken = 0;
		net.minecraft.world.phys.AABB box = mob.getBoundingBox().inflate(0.2);
		for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
				net.minecraft.util.Mth.floor(box.minX), net.minecraft.util.Mth.floor(box.minY), net.minecraft.util.Mth.floor(box.minZ),
				net.minecraft.util.Mth.floor(box.maxX), net.minecraft.util.Mth.floor(box.maxY), net.minecraft.util.Mth.floor(box.maxZ))) {
			if (level.getBlockState(pos).is(trample.blocks()) && level.destroyBlock(pos, true, mob)) {
				broken++;
			}
		}
		return broken;
	}
}
