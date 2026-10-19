package io.github.stivengjekaj.originofspawns.test;

import java.util.List;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import io.github.stivengjekaj.originofspawns.visual.BodyMaterial;
import io.github.stivengjekaj.originofspawns.visual.BodyRegion;
import io.github.stivengjekaj.originofspawns.visual.GeneVisual;
import io.github.stivengjekaj.originofspawns.visual.TextureBaker;
import io.github.stivengjekaj.originofspawns.visual.TextureBaker.Image;
import io.github.stivengjekaj.originofspawns.visual.VisualEffect;
import io.github.stivengjekaj.originofspawns.visual.VisualLayer;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class TextureBakerTest {
	private static final int GREEN = ARGB.color(255, 80, 160, 80);
	private static final int BLUE = ARGB.color(255, 40, 60, 200);

	/**
	 * Four pixels: skin on the hands, cloth on the torso, skin on the face, and an unused pixel.
	 */
	private static Image regionMap() {
		return new Image(4, 1, new int[] {
				label(BodyRegion.HANDS, BodyMaterial.SKIN),
				label(BodyRegion.TORSO, BodyMaterial.CLOTH),
				label(BodyRegion.FACE, BodyMaterial.SKIN),
				0
		});
	}

	private static Image texture() {
		return new Image(4, 1, new int[] {GREEN, BLUE, GREEN, GREEN});
	}

	@GameTest
	public void layerChangesOnlyItsRegionsAndMaterials(GameTestHelper helper) {
		Image texture = texture();
		VisualLayer skinOnly = new VisualLayer(List.of(), List.of(BodyMaterial.SKIN), new VisualEffect.Desaturate(1.0F, 0.0F));
		TextureBaker.bake(texture, regionMap(), List.of(skinOnly), 0, id -> null);

		helper.assertTrue(isGray(texture.get(0, 0)), "hand skin is gray");
		helper.assertValueEqual(texture.get(1, 0), BLUE, "torso cloth");
		helper.assertTrue(isGray(texture.get(2, 0)), "face skin is gray");
		helper.assertValueEqual(texture.get(3, 0), GREEN, "unused pixel");
		helper.succeed();
	}

	@GameTest
	public void regionListLimitsTheLayer(GameTestHelper helper) {
		Image texture = texture();
		VisualLayer hands = new VisualLayer(List.of(BodyRegion.HANDS), List.of(), new VisualEffect.Erase());
		TextureBaker.bake(texture, regionMap(), List.of(hands), 0, id -> null);

		helper.assertValueEqual(texture.get(0, 0), 0, "hand pixel is erased");
		helper.assertValueEqual(texture.get(2, 0), GREEN, "face pixel");
		helper.succeed();
	}

	@GameTest
	public void spotsAreTheSameForTheSameSeed(GameTestHelper helper) {
		VisualLayer spots = new VisualLayer(List.of(), List.of(),
				new VisualEffect.Spots(List.of(0x110000, 0x880000), 0.5F));
		Image big = new Image(16, 16, new int[256]);
		java.util.Arrays.fill(big.pixels(), GREEN);
		Image map = new Image(16, 16, new int[256]);
		java.util.Arrays.fill(map.pixels(), label(BodyRegion.HANDS, BodyMaterial.SKIN));
		Image again = new Image(16, 16, big.pixels().clone());

		TextureBaker.bake(big, map, List.of(spots), 7, id -> null);
		TextureBaker.bake(again, map, List.of(spots), 7, id -> null);

		int changed = 0;
		for (int i = 0; i < 256; i++) {
			if (big.pixels()[i] != GREEN) {
				changed++;
			}
		}
		helper.assertTrue(changed > 64 && changed < 192, "about half the pixels have spots: " + changed);
		helper.assertTrue(java.util.Arrays.equals(big.pixels(), again.pixels()), "the same seed gives the same spots");
		helper.succeed();
	}

	@GameTest
	public void patternStampsTheTint(GameTestHelper helper) {
		Image texture = texture();
		Identifier plates = Identifier.fromNamespaceAndPath("originofspawns_test", "plates");
		Image pattern = new Image(1, 1, new int[] {ARGB.color(255, 255, 255, 255)});
		VisualLayer layer = new VisualLayer(List.of(BodyRegion.TORSO), List.of(),
				new VisualEffect.Pattern(plates, 0x8090A0));
		TextureBaker.bake(texture, regionMap(), List.of(layer), 0, id -> id.equals(plates) ? pattern : null);

		helper.assertValueEqual(texture.get(1, 0), ARGB.color(255, 0x80, 0x90, 0xA0), "torso has the tint");
		helper.assertValueEqual(texture.get(0, 0), GREEN, "hand pixel");
		helper.succeed();
	}

	@GameTest
	public void visualReadsFromJson(GameTestHelper helper) {
		String json = """
				{
					"layers": [
						{ "materials": ["skin"], "effect": { "type": "desaturate", "amount": 0.4, "darken": 0.1 } },
						{ "regions": ["hands", "feet"], "effect": { "type": "spots", "colors": ["#1A1A1A", "#8B2E1F"], "density": 0.3 } }
					]
				}
				""";
		GeneVisual visual = GeneVisual.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
		helper.assertValueEqual(visual.regionMap(), GeneVisual.HUMANOID, "default region map");
		helper.assertValueEqual(visual.layers().size(), 2, "layer count");
		helper.assertValueEqual(visual.layers().get(1).effect(),
				new VisualEffect.Spots(List.of(0xFF1A1A1A, 0xFF8B2E1F), 0.3F), "spots effect");
		helper.succeed();
	}

	private static int label(BodyRegion region, BodyMaterial material) {
		return ARGB.color(255, region.id(), material.id(), 0);
	}

	private static boolean isGray(int color) {
		return ARGB.red(color) == ARGB.green(color) && ARGB.green(color) == ARGB.blue(color);
	}
}
