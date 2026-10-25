package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the genes of the zoglin: the gore, the rush, the rot, the bones, and the madness.
 */
public class ZoglinEffectsTest {
	/**
	 * Adds an adult zoglin with the test genes and no AI. The genes go on before it enters the level, so it gets no
	 * genes from the pool.
	 */
	static Zoglin zoglinWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Zoglin zoglin = EntityTypes.ZOGLIN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		zoglin.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		zoglin.setNoAi(true);
		zoglin.setPersistenceRequired();
		zoglin.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(zoglin);
		return zoglin;
	}
}
