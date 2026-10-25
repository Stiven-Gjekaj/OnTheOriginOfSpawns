package io.github.stivengjekaj.originofspawns.evolution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * The genes of the piglins and the hoglins: the gold armor of a player, and the vanilla fears.
 */
public final class GenePiglins {
	private GenePiglins() {
	}

	/**
	 * Returns true if the piglin attacks the player, also in gold armor. The piglin rolls its chance one time for each
	 * player, and keeps the answer.
	 */
	public static boolean attacksInGold(LivingEntity piglin, Player player) {
		var chance = GeneEffects.of(piglin).filter(GeneEffect.GoldArmorAttack.class::isInstance)
				.mapToDouble(effect -> ((GeneEffect.GoldArmorAttack) effect).chance()).max();
		if (chance.isEmpty()) {
			return false;
		}
		Map<UUID, Boolean> rolls = piglin.getAttachedOrElse(ModAttachments.GOLD_ROLLS, Map.of());
		Boolean roll = rolls.get(player.getUUID());
		if (roll == null) {
			roll = piglin.getRandom().nextFloat() < chance.getAsDouble();
			Map<UUID, Boolean> next = new HashMap<>(rolls);
			next.put(player.getUUID(), roll);
			piglin.setAttached(ModAttachments.GOLD_ROLLS, next);
		}
		return roll;
	}

	/**
	 * Returns true if a gene of the mob stops the fear, or a mob of its species near it guards it from the fear.
	 */
	public static boolean fearless(LivingEntity mob, GeneEffect.NoFear.Fear fear) {
		if (GeneEffects.of(mob).anyMatch(effect -> effect instanceof GeneEffect.NoFear noFear && noFear.fears().contains(fear))) {
			return true;
		}
		AABB area = mob.getBoundingBox().inflate(GUARD_SEARCH);
		return mob.level().getEntities(mob.getType(), area, other -> other != mob && other instanceof LivingEntity guard
				&& GeneEffects.of(guard).anyMatch(effect -> effect instanceof GeneEffect.FearGuard fearGuard
						&& fearGuard.fear() == fear && guard.distanceTo(mob) <= fearGuard.radius()))
				.size() > 0;
	}

	/**
	 * The largest radius in which the game looks for a mob that guards the others from a fear.
	 */
	private static final double GUARD_SEARCH = 32.0;

	/**
	 * Finds the nearest block that the hoglin fears, as the vanilla sensor does. A block of a fear that the genes stop
	 * does not count.
	 */
	public static Optional<BlockPos> hoglinRepellent(ServerLevel level, LivingEntity hoglin) {
		boolean fungus = fearless(hoglin, GeneEffect.NoFear.Fear.WARPED_FUNGUS);
		boolean portal = fearless(hoglin, GeneEffect.NoFear.Fear.NETHER_PORTAL);
		boolean anchor = fearless(hoglin, GeneEffect.NoFear.Fear.RESPAWN_ANCHOR);
		return level.findBlocksInBoxByManhattanDistance(hoglin.blockPosition(), 8, 4).filterState(state ->
				state.is(BlockTags.HOGLIN_REPELLENTS) && !(fungus && state.is(Blocks.WARPED_FUNGUS))
						&& !(portal && state.is(Blocks.NETHER_PORTAL)) && !(anchor && state.is(Blocks.RESPAWN_ANCHOR)))
				.findFirst();
	}
}
