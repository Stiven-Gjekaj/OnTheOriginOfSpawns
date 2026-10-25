package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.mixin.AbstractCubeMobAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

/**
 * The genes of the cube mobs: the slime and the magma cube. They split, they hop, and they grow.
 */
public final class GeneCubes {
	private GeneCubes() {
	}

	/**
	 * Gives a piece of a split the genes of the cube that split. The piece is not a new mob, so it gets no genes from
	 * the pool, and the attribute changes of the genes go on it as on the cube.
	 */
	public static void splitPiece(AbstractCubeMob cube, AbstractCubeMob piece) {
		CarriedGenes genes = cube.getAttached(ModAttachments.GENES);
		if (genes == null || !(cube.level() instanceof ServerLevel level)) {
			return;
		}
		piece.setAttached(ModAttachments.GENES, genes);
		GeneEffects.applyAttributes(piece, GeneEffects.sources(level.registryAccess(), genes));
		for (GeneEffect effect : GeneEffects.of(cube).toList()) {
			switch (effect) {
				case GeneEffect.SplitEffect split -> piece.addEffect(new MobEffectInstance(split.effect(),
						Math.round(split.seconds() * 20.0F), split.amplifier()), cube);
				case GeneEffect.SplitJump jump -> jump(cube, piece, jump);
				default -> {
				}
			}
		}
	}

	/**
	 * The speed up at the start of the jump of a piece, as the vanilla jump of a mob.
	 */
	private static final double JUMP_SPEED_UP = 0.42;
	/**
	 * The distance of the jump of a piece for each unit of its start speed. The jump is about 12 ticks in the air,
	 * and the air takes 9% of the speed each tick.
	 */
	private static final double HOP_REACH = 7.4;

	/**
	 * Lets the piece jump away from the cube, or toward the killer of the cube.
	 */
	private static void jump(AbstractCubeMob cube, AbstractCubeMob piece, GeneEffect.SplitJump jump) {
		net.minecraft.world.phys.Vec3 direction;
		net.minecraft.world.entity.LivingEntity killer = cube.getLastHurtByMob();
		if (jump.towardKiller() && killer != null) {
			direction = killer.position().subtract(piece.position());
		} else {
			direction = piece.position().subtract(cube.position());
		}
		direction = new net.minecraft.world.phys.Vec3(direction.x, 0.0, direction.z);
		if (direction.lengthSqr() < 1.0E-6) {
			double angle = cube.getRandom().nextDouble() * Math.PI * 2.0;
			direction = new net.minecraft.world.phys.Vec3(Math.cos(angle), 0.0, Math.sin(angle));
		}
		net.minecraft.world.phys.Vec3 speed = direction.normalize().scale(jump.blocks() / HOP_REACH);
		piece.setDeltaMovement(speed.x, JUMP_SPEED_UP, speed.z);
	}

	/**
	 * The sizes of a natural cube.
	 */
	private static final int[] NATURAL_SIZES = {1, 2, 4};

	/**
	 * Gives a new cube the size of its spawn size gene, if it has one.
	 */
	public static void spawnSize(AbstractCubeMob cube, GeneEffect.SpawnSize spawn) {
		cube.setSize(spawnSize(spawn, cube.getRandom().nextFloat(), cube.getRandom().nextFloat()), true);
	}

	/**
	 * Returns the size for the two rolls: the first roll picks the size of the gene with its chance, and the second
	 * roll picks one of the other natural sizes.
	 */
	public static int spawnSize(GeneEffect.SpawnSize spawn, float roll, float otherRoll) {
		if (roll < spawn.chance()) {
			return spawn.size();
		}
		int[] others = java.util.Arrays.stream(NATURAL_SIZES).filter(size -> size != spawn.size()).toArray();
		return others[Math.min(others.length - 1, (int) (otherRoll * others.length))];
	}

	/**
	 * Returns the products of the hop multipliers of the mob: the wait, the distance, and the height.
	 */
	public static float[] hop(net.minecraft.world.entity.Mob mob) {
		float[] hop = {1.0F, 1.0F, 1.0F};
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.Hop more) {
				hop[0] *= more.waitMultiplier();
				hop[1] *= more.distanceMultiplier();
				hop[2] *= more.heightMultiplier();
			}
		}
		return hop;
	}

	/**
	 * Multiplies the speed up of the hop that starts now by the height multiplier of the genes.
	 */
	public static void hopHeight(AbstractCubeMob cube) {
		float height = hop(cube)[2];
		if (height != 1.0F) {
			net.minecraft.world.phys.Vec3 movement = cube.getDeltaMovement();
			cube.setDeltaMovement(movement.x, movement.y * height, movement.z);
		}
	}

	/**
	 * Runs the landing effects of the cube, when it lands from a hop.
	 */
	public static void onLand(AbstractCubeMob cube, ServerLevel level) {
		for (GeneEffect effect : GeneEffects.of(cube).toList()) {
			switch (effect) {
				case GeneEffect.LandingDamage landing -> {
					for (Player player : playersNear(cube, level, landing.radius())) {
						player.hurtServer(level, level.damageSources().mobAttack(cube), landing.damage());
					}
				}
				case GeneEffect.LandingEffect landing -> {
					for (Player player : playersNear(cube, level, landing.radius())) {
						player.addEffect(new MobEffectInstance(landing.effect(), Math.round(landing.seconds() * 20.0F),
								landing.amplifier()), cube);
					}
				}
				case GeneEffect.LandingArmorDamage landing -> {
					for (Player player : playersNear(cube, level, landing.radius())) {
						for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
							ItemStack stack = player.getItemBySlot(slot);
							if (!stack.isEmpty() && stack.isDamageableItem()) {
								stack.hurtAndBreak(landing.amount(), player, slot);
							}
						}
					}
				}
				case GeneEffect.LandingShieldStop stop -> {
					for (Player player : level.getEntitiesOfClass(Player.class, cube.getBoundingBox().inflate(0.1))) {
						ItemStack shield = player.getItemBlockingWith();
						if (shield != null && shield.has(DataComponents.BLOCKS_ATTACKS)) {
							shield.get(DataComponents.BLOCKS_ATTACKS).disable(level, player, stop.seconds(), shield);
						}
					}
				}
				default -> {
				}
			}
		}
	}

	private static java.util.List<Player> playersNear(AbstractCubeMob cube, ServerLevel level, float radius) {
		return level.getEntitiesOfClass(Player.class, cube.getBoundingBox().inflate(radius),
				player -> player.isAlive() && !player.isSpectator());
	}

	/**
	 * Returns the number of pieces of the split of the cube, with the split count genes. Vanilla puts the pieces in
	 * rows of 2, so a count over 4 puts the extra pieces in a third row.
	 */
	public static int splitCount(AbstractCubeMob cube, int count) {
		Optional<Integer> fixed = Optional.empty();
		int extra = 0;
		for (GeneEffect effect : GeneEffects.of(cube).toList()) {
			if (effect instanceof GeneEffect.SplitCount split) {
				if (fixed.isEmpty()) {
					fixed = split.count();
				}
				extra += split.extra();
			}
		}
		return fixed.orElse(count) + extra;
	}

	/**
	 * Runs the effects of the cube on the players near it, when it splits.
	 */
	public static void onSplit(AbstractCubeMob cube, ServerLevel level) {
		for (GeneEffect effect : GeneEffects.of(cube).toList()) {
			if (effect instanceof GeneEffect.SplitAreaEffect area) {
				for (Player player : playersNear(cube, level, area.radius())) {
					area.effect().ifPresent(type -> player.addEffect(new MobEffectInstance(type,
							Math.round(area.seconds() * 20.0F), area.amplifier()), cube));
					if (area.fireSeconds() > 0.0F) {
						player.igniteForSeconds(area.fireSeconds());
					}
				}
			}
		}
	}

	/**
	 * Lets the cube grow one size, if it was out of combat for the seconds of the gene.
	 *
	 * @return true if the cube grew
	 */
	public static boolean grow(AbstractCubeMob cube, ServerLevel level, GeneEffect.Grow grow) {
		if (cube.getSize() >= grow.maxSize() || !quietFor(cube, level, ModAttachments.GROW_TIME, grow.seconds())) {
			return false;
		}
		cube.setSize(Math.min(grow.maxSize(), cube.getSize() * 2), true);
		cube.setAttached(ModAttachments.GROW_TIME, level.getGameTime());
		return true;
	}

	/**
	 * Lets a new cube of size 1 bud off the cube, if the cube was out of combat for the seconds of the gene.
	 *
	 * @return the new cube, or null
	 */
	public static AbstractCubeMob bud(AbstractCubeMob cube, ServerLevel level, GeneEffect.Bud bud) {
		if (cube.getAttachedOrElse(ModAttachments.BUD_CHILD, false)
				|| !quietFor(cube, level, ModAttachments.BUD_TIME, bud.seconds())
				|| !(cube.getType().create(level, net.minecraft.world.entity.EntitySpawnReason.REINFORCEMENT)
						instanceof AbstractCubeMob child)) {
			return null;
		}
		child.setSize(1, true);
		child.snapTo(cube.getX(), cube.getY(), cube.getZ(), cube.getRandom().nextFloat() * 360.0F, 0.0F);
		child.setAttached(ModAttachments.BUD_CHILD, true);
		// The bud has the genes of the cube, as a piece of a split, so the load does not roll new genes.
		splitPiece(cube, child);
		level.addFreshEntity(child);
		cube.setAttached(ModAttachments.BUD_TIME, level.getGameTime());
		return child;
	}

	/**
	 * Returns true if the cube was out of combat since the mark, for the seconds. The first question only sets the
	 * mark, so the time starts there.
	 */
	private static boolean quietFor(AbstractCubeMob cube, ServerLevel level,
			net.fabricmc.fabric.api.attachment.v1.AttachmentType<Long> mark, float seconds) {
		long now = level.getGameTime();
		Long since = cube.getAttached(mark);
		if (since == null) {
			cube.setAttached(mark, now);
			return false;
		}
		long combat = cube.getAttachedOrElse(ModAttachments.TRACE, DeathTrace.NONE).combatTime();
		// A combat time under 0 means no combat.
		long start = combat < 0 ? since : Math.max(since, combat);
		return now - start >= Math.round(seconds * 20.0F);
	}

	/**
	 * The vanilla reach of a melee attack past the box of the mob, in blocks.
	 */
	private static final double MELEE_REACH = Math.sqrt(2.04F) - 0.6F;

	/**
	 * Returns the extra blocks of the touch attack of the cube, from its genes.
	 */
	public static float touchReach(AbstractCubeMob cube) {
		return (float) GeneEffects.of(cube).filter(GeneEffect.TouchReach.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.TouchReach) effect).blocks()).sum();
	}

	/**
	 * Returns true if the target is in the reach of the touch attack of the cube, with the extra reach of the genes.
	 */
	public static boolean inReach(AbstractCubeMob cube, LivingEntity target, boolean vanilla) {
		float extra = touchReach(cube);
		if (vanilla || extra <= 0.0F) {
			return vanilla;
		}
		double reach = MELEE_REACH + extra;
		return cube.getBoundingBox().inflate(reach, 0.0, reach).intersects(target.getBoundingBox());
	}

	/**
	 * Lets a cube with more reach hit its target in the reach. Vanilla hits only at a touch.
	 */
	public static void reachHit(AbstractCubeMob cube) {
		LivingEntity target = cube.getTarget();
		AbstractCubeMobAccessor accessor = (AbstractCubeMobAccessor) cube;
		if (target != null && target.isAlive() && !cube.getBoundingBox().intersects(target.getBoundingBox())
				&& accessor.originofspawns$canDealDamage()) {
			accessor.originofspawns$dealDamage(target);
		}
	}

	/**
	 * Stops the shield of a target that held it up at a melee hit or a touch hit of the mob, for the seconds of the
	 * genes.
	 */
	public static void shieldBreak(net.minecraft.world.entity.Mob cube, ServerLevel level, LivingEntity target, ItemStack shield) {
		shieldBreak(cube, level, target, shield, false);
	}

	/**
	 * Stops the shield of a target that held it up at a hit of the mob, for the seconds of the genes whose hits
	 * include this hit.
	 *
	 * @param projectile true for the hit of a projectile of the mob
	 */
	public static void shieldBreak(net.minecraft.world.entity.Mob cube, ServerLevel level, LivingEntity target, ItemStack shield,
			boolean projectile) {
		if (shield == null || !shield.has(DataComponents.BLOCKS_ATTACKS)) {
			return;
		}
		GeneEffects.of(cube).filter(effect -> effect instanceof GeneEffect.ShieldBreak shieldBreak
						&& shieldBreak.hits().includes(projectile))
				.mapToDouble(effect -> ((GeneEffect.ShieldBreak) effect).seconds()).max()
				.ifPresent(seconds -> shield.get(DataComponents.BLOCKS_ATTACKS)
						.disable(level, target, (float) seconds, shield));
	}
}
