package io.github.stivengjekaj.originofspawns.test;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.definition.GeneEffect;
import io.github.stivengjekaj.originofspawns.definition.Perk;
import io.github.stivengjekaj.originofspawns.definition.Pressure;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityTypes;

/**
 * Reads the definitions in the data pack of the test mod.
 */
public class DefinitionTest {
	private static final String NAMESPACE = "originofspawns_test";

	@GameTest
	public void pressureReadsDamageTag(GameTestHelper helper) {
		Pressure pressure = get(helper, ModRegistries.PRESSURE, "test_heat");
		helper.assertValueEqual(pressure.damageTypes(), Optional.of(DamageTypeTags.IS_FIRE), "damage types");
		helper.succeed();
	}

	@GameTest
	public void geneReadsAllFields(GameTestHelper helper) {
		Gene gene = get(helper, ModRegistries.GENE, "test_heat_skin");
		helper.assertTrue(carries(gene, EntityTypes.ZOMBIE), "zombie carries the gene");
		helper.assertTrue(carries(gene, EntityTypes.HUSK), "husk carries the gene");
		helper.assertFalse(carries(gene, EntityTypes.SKELETON), "skeleton carries the gene");
		helper.assertValueEqual(gene.counters(), key(ModRegistries.PRESSURE, "test_heat"), "counters");
		helper.assertValueEqual(gene.family(), Identifier.fromNamespaceAndPath(NAMESPACE, "stalker"), "family");
		helper.assertValueEqual(gene.subFamily(), Optional.of(Identifier.fromNamespaceAndPath(NAMESPACE, "predator")),
				"sub family");
		helper.assertValueEqual(gene.mutationRate(), 0.5F, "mutation rate");
		helper.assertValueEqual(gene.selectionRate(), 0.02F, "default selection rate");
		helper.assertValueEqual(gene.decayRate(), 0.01F, "default decay rate");
		helper.assertValueEqual(gene.gains(), List.of(
				new GeneEffect.DamageTaken(DamageTypeTags.IS_FIRE, 0.0F),
				new GeneEffect.IgniteTarget(0.2F, 4.0F)), "gains");
		helper.assertValueEqual(gene.downsides(), List.of(
				new GeneEffect.AttributeChange(Attributes.MAX_HEALTH, -4.0, AttributeModifier.Operation.ADD_VALUE),
				new GeneEffect.WaterDamage(1.0F, 20),
				new GeneEffect.AirLoss(2)), "downsides");
		helper.succeed();
	}

	@GameTest
	public void perkReadsIncompatiblePerks(GameTestHelper helper) {
		Perk left = get(helper, ModRegistries.PERK, "test_left");
		Perk right = get(helper, ModRegistries.PERK, "test_right");
		helper.assertValueEqual(left.xpCost(), 5, "xp cost");
		helper.assertValueEqual(left.incompatibleWith(), List.of(key(ModRegistries.PERK, "test_right")),
				"incompatible perks");
		helper.assertValueEqual(right.incompatibleWith(), List.of(), "default incompatible perks");
		helper.succeed();
	}

	@GameTest
	public void unknownEffectTypeIsAnError(GameTestHelper helper) {
		JsonObject json = JsonParser.parseString("{\"type\": \"no_such_effect\"}").getAsJsonObject();
		DataResult<GeneEffect> result = GeneEffect.CODEC.parse(JsonOps.INSTANCE, json);
		helper.assertTrue(result.error().isPresent(), "the result is an error");
		helper.assertTrue(result.error().get().message().contains("no_such_effect"), "the error names the type");
		helper.succeed();
	}

	private static boolean carries(Gene gene, EntityType<?> type) {
		return gene.species().stream().anyMatch(holder -> holder.value() == type);
	}

	private static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registry, String path) {
		return ResourceKey.create(registry, Identifier.fromNamespaceAndPath(NAMESPACE, path));
	}

	private static <T> T get(GameTestHelper helper, ResourceKey<Registry<T>> registry, String path) {
		T value = helper.getLevel().registryAccess().lookupOrThrow(registry).getValue(key(registry, path));
		if (value == null) {
			throw helper.assertionException("Registry %s has no entry %s", registry.identifier(), path);
		}
		return value;
	}
}
