package io.github.stivengjekaj.originofspawns.client.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.visual.BabyLayout;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;
import io.github.stivengjekaj.originofspawns.visual.TextureBaker;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Makes and keeps the textures of mobs with genes. Each mix of a base texture and genes gets one texture.
 */
public final class GeneTextures {
	/**
	 * The genes of the mob in a render state.
	 */
	public static final RenderStateDataKey<List<Identifier>> GENES = RenderStateDataKey.create(() -> "originofspawns:genes");

	/**
	 * The opacity of a translucent mob in a render state, from 0 to 1. It is not set for a normal mob.
	 */
	public static final RenderStateDataKey<Float> OPACITY = RenderStateDataKey.create(() -> "originofspawns:opacity");

	private static final Map<String, Identifier> BAKED = new HashMap<>();
	private static final Map<Identifier, TextureBaker.Image> IMAGES = new HashMap<>();
	private static final Map<Identifier, TextureBaker.Image> BABY_IMAGES = new HashMap<>();
	private static final Map<Identifier, TextureBaker.Image> BAKED_IMAGES = new HashMap<>();
	private static final Map<Identifier, Identifier> ADULT_LAYOUT = new HashMap<>();
	private static int nextId;

	private GeneTextures() {
	}

	/**
	 * Returns the texture for a mob with these genes. It is the base texture if no gene changes how the mob looks.
	 */
	public static Identifier textureFor(Identifier base, List<Identifier> genes) {
		return textureFor(base, genes, false);
	}

	/**
	 * Returns the texture for a mob with these genes. A baby gets the marks moved to the baby layout of its texture.
	 */
	public static Identifier textureFor(Identifier base, List<Identifier> genes, boolean baby) {
		if (genes == null || genes.isEmpty()) {
			return base;
		}
		List<Identifier> visible = new ArrayList<>();
		for (Identifier id : genes) {
			if (GeneVisuals.get(id) != null) {
				visible.add(id);
			}
		}
		if (visible.isEmpty()) {
			return base;
		}
		visible.sort(null);
		String key = base + "|" + baby + "|" + visible;
		Identifier baked = BAKED.get(key);
		if (baked == null) {
			baked = bake(base, visible, baby);
			BAKED.put(key, baked);
		}
		return baked;
	}

	private static Identifier bake(Identifier base, List<Identifier> genes, boolean baby) {
		TextureBaker.Image source = image(base);
		if (source == null) {
			return base;
		}
		TextureBaker.Image texture = new TextureBaker.Image(source.width(), source.height(), source.pixels().clone());
		List<TextureBaker.GeneLayers> layers = new ArrayList<>();
		for (Identifier id : genes) {
			GeneVisual visual = GeneVisuals.get(id);
			TextureBaker.Image regionMap = baby ? babyImage(visual.regionMap()) : image(visual.regionMap());
			if (regionMap != null) {
				layers.add(new TextureBaker.GeneLayers(regionMap, visual.layers(), id.hashCode()));
			}
		}
		TextureBaker.bakeAll(texture, layers, baby ? GeneTextures::babyPattern : GeneTextures::image);
		return register(texture);
	}

	/**
	 * Returns a texture in the adult layout with the pixels of a baby texture, for gene models that use the texture of
	 * the mob. Their cubes have adult UVs.
	 */
	public static Identifier adultLayoutOf(Identifier babyTexture) {
		return ADULT_LAYOUT.computeIfAbsent(babyTexture, id -> {
			TextureBaker.Image baby = textureImage(id);
			return baby == null ? id : register(BabyLayout.toAdult(baby));
		});
	}

	private static TextureBaker.Image babyImage(Identifier texture) {
		TextureBaker.Image adult = image(texture);
		return adult == null ? null : BABY_IMAGES.computeIfAbsent(texture, id -> BabyLayout.toBaby(adult));
	}

	/**
	 * Returns a pattern for a baby. A pattern with the size of a whole mob texture is a mark in the adult layout, so it
	 * moves to the baby layout. A smaller pattern is a tile, and it stays as it is.
	 */
	private static TextureBaker.Image babyPattern(Identifier texture) {
		TextureBaker.Image pattern = image(texture);
		return pattern != null && pattern.width() >= 64 && pattern.height() >= 64 ? babyImage(texture) : pattern;
	}

	/**
	 * Reads a texture that is a baked gene texture or a resource.
	 */
	private static TextureBaker.Image textureImage(Identifier texture) {
		TextureBaker.Image baked = BAKED_IMAGES.get(texture);
		return baked != null ? baked : image(texture);
	}

	private static Identifier register(TextureBaker.Image texture) {
		NativeImage result = new NativeImage(texture.width(), texture.height(), false);
		for (int y = 0; y < texture.height(); y++) {
			for (int x = 0; x < texture.width(); x++) {
				result.setPixel(x, y, texture.get(x, y));
			}
		}
		Identifier id = OriginOfSpawns.id("gene_texture/" + nextId++);
		Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, result));
		BAKED_IMAGES.put(id, texture);
		return id;
	}

	/**
	 * Reads a texture as ARGB pixels, or returns null if it does not exist.
	 */
	private static TextureBaker.Image image(Identifier texture) {
		return IMAGES.computeIfAbsent(texture, id -> {
			try (InputStream stream = Minecraft.getInstance().getResourceManager().open(id);
					NativeImage image = NativeImage.read(stream)) {
				int[] pixels = new int[image.getWidth() * image.getHeight()];
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						pixels[x + y * image.getWidth()] = image.getPixel(x, y);
					}
				}
				return new TextureBaker.Image(image.getWidth(), image.getHeight(), pixels);
			} catch (IOException exception) {
				OriginOfSpawns.LOGGER.warn("Cannot read texture {}", id, exception);
				return null;
			}
		});
	}

	/**
	 * Returns true if one of the genes has a visual with this model change.
	 */
	public static boolean hasModel(List<Identifier> genes, Identifier model) {
		if (genes == null) {
			return false;
		}
		for (Identifier id : genes) {
			GeneVisual visual = GeneVisuals.get(id);
			if (visual != null && visual.model().filter(model::equals).isPresent()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Releases all baked textures. The next frame bakes them again.
	 */
	public static void clear() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && minecraft.getTextureManager() != null) {
			BAKED.values().forEach(minecraft.getTextureManager()::release);
			ADULT_LAYOUT.values().forEach(minecraft.getTextureManager()::release);
		}
		BAKED.clear();
		IMAGES.clear();
		BABY_IMAGES.clear();
		BAKED_IMAGES.clear();
		ADULT_LAYOUT.clear();
	}
}
