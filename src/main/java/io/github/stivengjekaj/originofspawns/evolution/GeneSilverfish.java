package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * The genes of a silverfish: the wake of the hidden silverfish, the hide in stone, and the gnaw through stone.
 */
public final class GeneSilverfish {
	/**
	 * The range of the wake of a vanilla silverfish, to each side. The wake reaches half as far up and down.
	 */
	public static final int VANILLA_WAKE_RANGE = 10;

	private GeneSilverfish() {
	}

	/**
	 * Returns the range of the wake when something hurts the mob: the largest wake range gene, or the vanilla range.
	 */
	public static int wakeRange(Mob mob) {
		int range = VANILLA_WAKE_RANGE;
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.WakeRange wake) {
				range = Math.max(range, wake.range());
			}
		}
		return range;
	}

	/**
	 * Returns true if the mob has a wake range gene.
	 */
	public static boolean hasWakeRange(Mob mob) {
		return GeneEffects.of(mob).anyMatch(GeneEffect.WakeRange.class::isInstance);
	}

	/**
	 * Wakes the hidden silverfish when the mob sees its target player, if the cooldown of the gene is over.
	 *
	 * @return true if the mob made a wake now. The cooldown starts also when no block woke
	 */
	public static boolean wakeOnSight(Mob mob, ServerLevel level, GeneEffect.WakeOnSight sight) {
		if (!(mob.getTarget() instanceof Player player) || !player.isAlive() || !mob.hasLineOfSight(player)
				|| !GeneCombat.ready(mob, sight, sight.cooldownSeconds())) {
			return false;
		}
		GeneCombat.startCooldown(mob, sight);
		wake(mob, level, sight.range());
		return true;
	}

	/**
	 * Wakes the hidden silverfish near the mob, as the vanilla silverfish does when something hurts it. The search
	 * starts at the place of the mob and goes out. Each infested block breaks and lets its silverfish out, or with no
	 * {@code mobGriefing}, it becomes its normal block again. After each block, the wake stops with a chance of one half.
	 *
	 * @param range the blocks to each side. The wake reaches half as far up and down
	 * @return the number of blocks that woke
	 */
	public static int wake(Mob mob, ServerLevel level, int range) {
		int vertical = range / 2;
		RandomSource random = mob.getRandom();
		BlockPos base = mob.blockPosition();
		boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
		int woke = 0;
		for (int y = 0; y <= vertical && y >= -vertical; y = (y <= 0 ? 1 : 0) - y) {
			for (int x = 0; x <= range && x >= -range; x = (x <= 0 ? 1 : 0) - x) {
				for (int z = 0; z <= range && z >= -range; z = (z <= 0 ? 1 : 0) - z) {
					BlockPos pos = base.offset(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.getBlock() instanceof InfestedBlock infested) {
						if (griefing) {
							level.destroyBlock(pos, true, mob);
						} else {
							level.setBlockAndUpdate(pos, infested.hostStateByInfested(state));
						}
						woke++;
						if (random.nextBoolean()) {
							return woke;
						}
					}
				}
			}
		}
		return woke;
	}

	/**
	 * Keeps the genes of a silverfish in the block where it hides now. The block is already an infested block. A
	 * silverfish with no genes leaves nothing, so a plain silverfish comes out of the block, with genes from the pool.
	 */
	public static void hide(Mob mob, ServerLevel level, BlockPos pos) {
		CarriedGenes genes = mob.getAttached(ModAttachments.GENES);
		if (genes != null) {
			int extra = GeneEffects.of(mob).filter(GeneEffect.HideBrood.class::isInstance)
					.mapToInt(effect -> ((GeneEffect.HideBrood) effect).extra()).sum();
			HiddenSilverfish.get(level.getServer()).put(level, pos, genes, extra);
		}
	}

	/**
	 * Gives a silverfish that comes out of an infested block the genes of the silverfish that hid there, before it joins
	 * the world. It has full health. The extra silverfish of the hide come out too, with genes from the pool. A block where no silverfish with genes hid gives a silverfish with genes from the
	 * pool, as a natural spawn.
	 */
	public static void emerge(ServerLevel level, BlockPos pos, Mob silverfish) {
		HiddenSilverfish.get(level.getServer()).take(level, pos).ifPresent(entry -> {
			silverfish.setAttached(ModAttachments.GENES, entry.genes());
			GeneEffects.applyAttributes(silverfish, GeneEffects.sources(level.registryAccess(), entry.genes()));
			silverfish.setHealth(silverfish.getMaxHealth());
			// The cooldown of each hide gene goes on from the hide.
			java.util.Map<GeneEffect, Long> times = new java.util.HashMap<>(
					silverfish.getAttachedOrElse(ModAttachments.EFFECT_TIMES, java.util.Map.of()));
			GeneEffects.of(silverfish).filter(GeneEffect.HideInStone.class::isInstance)
					.forEach(effect -> times.put(effect, entry.hideTime()));
			silverfish.setAttached(ModAttachments.EFFECT_TIMES, times);
			for (int i = 0; i < entry.extra(); i++) {
				Mob brood = net.minecraft.world.entity.EntityTypes.SILVERFISH.create(level,
						net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
				if (brood != null) {
					brood.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
					level.addFreshEntity(brood);
					brood.spawnAnim();
				}
			}
		});
	}

	/**
	 * Hides the mob in a stone block next to it when a hit took it under the threshold of a hide gene, if the cooldown of
	 * the gene is over.
	 *
	 * @return true if the mob hid
	 */
	public static boolean hideWhenWounded(Mob mob, ServerLevel level) {
		if (!mob.isAlive() || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return false;
		}
		for (GeneEffect effect : GeneEffects.of(mob).toList()) {
			if (effect instanceof GeneEffect.HideInStone hide && mob.getHealth() < mob.getMaxHealth() * hide.threshold()
					&& GeneCombat.ready(mob, hide, hide.cooldownSeconds()) && hideInStone(mob, level)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Hides the mob in a stone block next to it, as the vanilla silverfish hides. The block is one of the six blocks
	 * next to the middle of the mob, in a random order.
	 *
	 * @return true if the mob found a block and hid
	 */
	public static boolean hideInStone(Mob mob, ServerLevel level) {
		BlockPos middle = BlockPos.containing(mob.getX(), mob.getY() + 0.5, mob.getZ());
		java.util.List<net.minecraft.core.Direction> directions = new java.util.ArrayList<>(
				java.util.List.of(net.minecraft.core.Direction.values()));
		net.minecraft.util.Util.shuffle(directions, mob.getRandom());
		for (net.minecraft.core.Direction direction : directions) {
			BlockPos pos = middle.relative(direction);
			BlockState state = level.getBlockState(pos);
			if (InfestedBlock.isCompatibleHostBlock(state)) {
				level.setBlockAndUpdate(pos, InfestedBlock.infestedStateByHost(state));
				hide(mob, level, pos);
				mob.spawnAnim();
				mob.discard();
				return true;
			}
		}
		return false;
	}
}
