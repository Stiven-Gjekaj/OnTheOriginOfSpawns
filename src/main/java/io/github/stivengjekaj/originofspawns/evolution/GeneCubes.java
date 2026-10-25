package io.github.stivengjekaj.originofspawns.evolution;

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
}
