package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

/**
 * A change to the pixels of a mob texture. The field {@code type} selects the kind of change.
 */
public sealed interface VisualEffect {
	Map<String, MapCodec<? extends VisualEffect>> TYPES = Map.of(
			"desaturate", Desaturate.CODEC,
			"spots", Spots.CODEC,
			"pattern", Pattern.CODEC,
			"erase", Erase.CODEC
	);

	Codec<VisualEffect> CODEC = Codec.STRING.partialDispatch("type",
			effect -> DataResult.success(effect.type()),
			type -> TYPES.containsKey(type)
					? DataResult.success(TYPES.get(type))
					: DataResult.error(() -> "Unknown visual effect type: " + type));

	String type();

	/**
	 * Moves the color toward gray, and makes it darker.
	 *
	 * @param amount the part of the way to gray, from 0 to 1
	 * @param darken the part of the brightness to remove, from 0 to 1
	 */
	record Desaturate(float amount, float darken) implements VisualEffect {
		public static final MapCodec<Desaturate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.floatRange(0.0F, 1.0F).fieldOf("amount").forGetter(Desaturate::amount),
				Codec.floatRange(0.0F, 1.0F).optionalFieldOf("darken", 0.0F).forGetter(Desaturate::darken)
		).apply(instance, Desaturate::new));

		@Override
		public String type() {
			return "desaturate";
		}
	}

	/**
	 * Puts spots of the colors on some pixels. The same gene always puts the spots on the same pixels.
	 *
	 * @param density the part of the pixels that get a spot, from 0 to 1
	 */
	record Spots(List<Integer> colors, float density) implements VisualEffect {
		public static final MapCodec<Spots> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.STRING_RGB_COLOR.listOf(1, 16).fieldOf("colors").forGetter(Spots::colors),
				Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(Spots::density)
		).apply(instance, Spots::new));

		@Override
		public String type() {
			return "spots";
		}
	}

	/**
	 * Stamps a tiled grayscale texture over the pixels, in the tint color. The alpha of the texture sets the strength.
	 */
	record Pattern(Identifier texture, int tint) implements VisualEffect {
		public static final MapCodec<Pattern> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Identifier.CODEC.fieldOf("texture").forGetter(Pattern::texture),
				ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("tint", 0xFFFFFF).forGetter(Pattern::tint)
		).apply(instance, Pattern::new));

		@Override
		public String type() {
			return "pattern";
		}
	}

	/**
	 * Makes the pixels transparent. A model change can then show a different part there.
	 */
	record Erase() implements VisualEffect {
		public static final MapCodec<Erase> CODEC = MapCodec.unit(new Erase());

		@Override
		public String type() {
			return "erase";
		}
	}
}
