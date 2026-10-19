package io.github.stivengjekaj.originofspawns.evolution.goal;

import java.util.EnumSet;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Makes the mob break a block of the tag {@code originofspawns:siege_breakable} that stands between it and its target.
 */
public class SiegeGoal extends Goal {
	public static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, OriginOfSpawns.id("siege_breakable"));

	private final Mob mob;
	private final GeneEffect.Siege siege;
	private BlockPos block;
	private int progress;

	public SiegeGoal(Mob mob, GeneEffect.Siege siege) {
		this.mob = mob;
		this.siege = siege;
		setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!(mob.level() instanceof ServerLevel level) || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return false;
		}
		block = blockToBreak();
		return block != null;
	}

	@Override
	public boolean canContinueToUse() {
		return block != null && mob.level().getBlockState(block).is(BREAKABLE) && mob.getTarget() != null
				&& mob.blockPosition().closerThan(block, 2.5);
	}

	@Override
	public void start() {
		progress = 0;
	}

	@Override
	public void stop() {
		if (block != null) {
			mob.level().destroyBlockProgress(mob.getId(), block, -1);
		}
		block = null;
	}

	@Override
	public void tick() {
		mob.getNavigation().stop();
		mob.getLookControl().setLookAt(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
		int total = Math.max(1, Math.round(siege.breakSeconds() * 20.0F));
		progress++;
		mob.level().destroyBlockProgress(mob.getId(), block, progress * 10 / total);
		if (progress >= total) {
			mob.level().destroyBlock(block, true, mob);
			block = null;
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	/**
	 * Finds a breakable block next to the mob, on the side of its target, at its feet or its head.
	 */
	private BlockPos blockToBreak() {
		LivingEntity target = mob.getTarget();
		if (target == null) {
			return null;
		}
		Direction direction = Direction.getApproximateNearest(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
		BlockPos front = mob.blockPosition().relative(direction);
		for (BlockPos pos : new BlockPos[] {front, front.above()}) {
			if (mob.level().getBlockState(pos).is(BREAKABLE)) {
				return pos;
			}
		}
		return null;
	}
}
