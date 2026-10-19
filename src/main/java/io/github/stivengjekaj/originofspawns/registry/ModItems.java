package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.notebook.BeagleNotebookItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final ResourceKey<Item> BEAGLE_NOTEBOOK_KEY =
			ResourceKey.create(Registries.ITEM, OriginOfSpawns.id("beagle_notebook"));
	public static final Item BEAGLE_NOTEBOOK = Registry.register(BuiltInRegistries.ITEM, BEAGLE_NOTEBOOK_KEY,
			new BeagleNotebookItem(new Item.Properties().setId(BEAGLE_NOTEBOOK_KEY).stacksTo(1)));

	private static final ResourceKey<CreativeModeTab> TOOLS =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"));

	private ModItems() {
	}

	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(TOOLS).register(output -> output.accept(BEAGLE_NOTEBOOK));
	}
}
