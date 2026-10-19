package io.github.stivengjekaj.originofspawns.client;

import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class OriginOfSpawnsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(GeneTextures::clear));
	}
}
