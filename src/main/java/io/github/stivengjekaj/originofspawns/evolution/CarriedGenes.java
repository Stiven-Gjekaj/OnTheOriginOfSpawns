package io.github.stivengjekaj.originofspawns.evolution;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * The genes that one mob carries. A mob gets them one time, when it first joins a world.
 */
public record CarriedGenes(List<Identifier> genes) {
	public static final CarriedGenes NONE = new CarriedGenes(List.of());

	public static final Codec<CarriedGenes> CODEC =
			Identifier.CODEC.listOf().xmap(CarriedGenes::new, CarriedGenes::genes);

	public static final StreamCodec<RegistryFriendlyByteBuf, CarriedGenes> STREAM_CODEC =
			Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CarriedGenes::new, CarriedGenes::genes).cast();

	public CarriedGenes {
		genes = List.copyOf(genes);
	}
}
