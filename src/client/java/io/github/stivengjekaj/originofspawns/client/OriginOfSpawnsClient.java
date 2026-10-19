package io.github.stivengjekaj.originofspawns.client;

import io.github.stivengjekaj.originofspawns.client.render.BoneLegsLayer;
import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.client.render.GeneVisuals;
import io.github.stivengjekaj.originofspawns.client.screen.NotebookScreen;
import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.server.packs.PackType;

public class OriginOfSpawnsClient implements ClientModInitializer {
	@Override
	@SuppressWarnings("unchecked")
	public void onInitializeClient() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(OriginOfSpawns.id("gene_visuals"), new GeneVisuals());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(GeneTextures::clear));
		ClientPlayNetworking.registerGlobalReceiver(NotebookPayload.TYPE,
				(payload, context) -> context.client().gui.setScreen(new NotebookScreen(payload)));
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof AbstractZombieRenderer<?, ?, ?>) {
				helper.register(new BoneLegsLayer(
						(RenderLayerParent<ZombieRenderState, HumanoidModel<ZombieRenderState>>) renderer));
			}
		});
	}
}
