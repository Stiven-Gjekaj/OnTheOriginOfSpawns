package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * The genes and the family of one mob. A mob gets them one time, when it first joins a world.
 */
public record CarriedGenes(List<Identifier> genes, FamilyResult family) {
	public static final CarriedGenes NONE = new CarriedGenes(List.of(), FamilyResult.NEUTRAL);

	/**
	 * Reads the old format too: a plain list of genes, from version 0.1.0-alpha.
	 */
	public static final Codec<CarriedGenes> CODEC = Codec.withAlternative(
			RecordCodecBuilder.create(instance -> instance.group(
					Identifier.CODEC.listOf().fieldOf("genes").forGetter(CarriedGenes::genes),
					FamilyResult.CODEC.optionalFieldOf("family", FamilyResult.NEUTRAL).forGetter(CarriedGenes::family)
			).apply(instance, CarriedGenes::new)),
			Identifier.CODEC.listOf().xmap(CarriedGenes::new, CarriedGenes::genes));

	public static final StreamCodec<RegistryFriendlyByteBuf, CarriedGenes> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).cast(), CarriedGenes::genes,
			FamilyResult.STREAM_CODEC, CarriedGenes::family,
			CarriedGenes::new);

	public CarriedGenes(List<Identifier> genes) {
		this(genes, FamilyResult.NEUTRAL);
	}

	public CarriedGenes {
		genes = List.copyOf(genes);
	}
}
