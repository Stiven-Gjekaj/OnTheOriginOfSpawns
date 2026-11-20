package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

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
}
