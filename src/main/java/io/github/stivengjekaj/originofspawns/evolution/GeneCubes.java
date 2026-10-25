package io.github.stivengjekaj.originofspawns.evolution;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.server.level.ServerLevel;
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
}
