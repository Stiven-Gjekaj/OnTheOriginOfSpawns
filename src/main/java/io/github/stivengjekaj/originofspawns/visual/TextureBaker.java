package io.github.stivengjekaj.originofspawns.visual;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Applies visual layers to the pixels of a mob texture. The pixels are ARGB values, row by row.
 * This class has no client code, so that tests can run it on the server.
 */
public final class TextureBaker {
	private TextureBaker() {
	}

	/**
	 * Pixels and their size.
	 */
	public record Image(int width, int height, int[] pixels) {
		public int get(int x, int y) {
			return pixels[x + y * width];
		}
	}

	/**
	 * Applies the layers of one gene to the texture, in place.
	 *
	 * @param texture the mob texture
	 * @param regionMap the region map. Its size can differ from the texture size
	 * @param layers the layers of the gene
	 * @param seed a value that makes the spots of each gene different
	 * @param patterns gives the pixels of a pattern texture
	 */
	public static void bake(Image texture, Image regionMap, List<VisualLayer> layers, int seed,
			Function<Identifier, Image> patterns) {
		for (int y = 0; y < texture.height(); y++) {
			for (int x = 0; x < texture.width(); x++) {
				int label = regionMap.get(x * regionMap.width() / texture.width(), y * regionMap.height() / texture.height());
				if (ARGB.alpha(label) == 0) {
					continue;
				}
				int regionId = ARGB.red(label);
				int materialId = ARGB.green(label);
				int index = x + y * texture.width();
				for (VisualLayer layer : layers) {
					if (layer.covers(regionId, materialId)) {
						texture.pixels()[index] = apply(layer.effect(), texture.pixels()[index], x, y, seed, patterns);
					}
				}
			}
		}
	}

	private static int apply(VisualEffect effect, int color, int x, int y, int seed, Function<Identifier, Image> patterns) {
		return switch (effect) {
			case VisualEffect.Desaturate desaturate -> desaturate(color, desaturate.amount(), desaturate.darken());
			case VisualEffect.Spots spots -> spots(color, spots, x, y, seed);
			case VisualEffect.Pattern pattern -> pattern(color, patterns.apply(pattern.texture()), pattern.tint(), x, y);
			case VisualEffect.Erase erase -> 0;
		};
	}

	static int desaturate(int color, float amount, float darken) {
		int gray = (ARGB.red(color) * 299 + ARGB.green(color) * 587 + ARGB.blue(color) * 114) / 1000;
		float keep = 1.0F - darken;
		return ARGB.color(ARGB.alpha(color),
				(int) (Mth.lerp(amount, ARGB.red(color), gray) * keep),
				(int) (Mth.lerp(amount, ARGB.green(color), gray) * keep),
				(int) (Mth.lerp(amount, ARGB.blue(color), gray) * keep));
	}

	private static int spots(int color, VisualEffect.Spots spots, int x, int y, int seed) {
		int hash = hash(x, y, seed);
		if ((hash & 0xFFFF) >= spots.density() * 0x10000) {
			return color;
		}
		int spot = spots.colors().get(((hash >>> 16) & 0x7FFF) % spots.colors().size());
		return ARGB.color(ARGB.alpha(color), ARGB.red(spot), ARGB.green(spot), ARGB.blue(spot));
	}

	private static int pattern(int color, Image pattern, int tint, int x, int y) {
		if (pattern == null) {
			return color;
		}
		int stamp = pattern.get(x % pattern.width(), y % pattern.height());
		float strength = ARGB.alpha(stamp) / 255.0F;
		float shade = ARGB.red(stamp) / 255.0F;
		return ARGB.color(ARGB.alpha(color),
				(int) Mth.lerp(strength, ARGB.red(color), ARGB.red(tint) * shade),
				(int) Mth.lerp(strength, ARGB.green(color), ARGB.green(tint) * shade),
				(int) Mth.lerp(strength, ARGB.blue(color), ARGB.blue(tint) * shade));
	}

	private static int hash(int x, int y, int seed) {
		int h = seed;
		h = h * 31 + x;
		h = h * 31 + y;
		h ^= h >>> 16;
		h *= 0x85EBCA6B;
		h ^= h >>> 13;
		h *= 0xC2B2AE35;
		h ^= h >>> 16;
		return h;
	}
}
