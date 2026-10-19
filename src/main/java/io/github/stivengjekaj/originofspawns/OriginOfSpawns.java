package io.github.stivengjekaj.originofspawns;

import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OriginOfSpawns implements ModInitializer {
	public static final String MOD_ID = "originofspawns";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModRegistries.register();
		LOGGER.info("On the Origin of Spawns starts.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
