package io.github.stivengjekaj.originofspawns.client;

import io.github.stivengjekaj.originofspawns.client.render.GeneModelLayer;
import io.github.stivengjekaj.originofspawns.client.render.GeneModels;
import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.client.render.GeneVisuals;
import io.github.stivengjekaj.originofspawns.client.render.MobInfoHud;
import io.github.stivengjekaj.originofspawns.client.screen.NotebookScreen;
import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.notebook.NotebookPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.server.packs.PackType;

public class OriginOfSpawnsClient implements ClientModInitializer {
	@Override
	@SuppressWarnings("unchecked")
	public void onInitializeClient() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(OriginOfSpawns.id("gene_visuals"), new GeneVisuals());
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(OriginOfSpawns.id("gene_models"), new GeneModels());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(GeneTextures::clear));
		ClientPlayNetworking.registerGlobalReceiver(NotebookPayload.TYPE,
				(payload, context) -> context.client().gui.setScreen(new NotebookScreen(payload)));
		HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, OriginOfSpawns.id("mob_info"), new MobInfoHud());
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof AbstractZombieRenderer<?, ?, ?>) {
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<ZombieRenderState, HumanoidModel<ZombieRenderState>>) renderer));
			} else if (renderer instanceof AbstractSkeletonRenderer<?, ?>) {
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<SkeletonRenderState, SkeletonModel<SkeletonRenderState>>) renderer));
			} else if (renderer instanceof CreeperRenderer creeper) {
				helper.register(new GeneModelLayer<CreeperRenderState, CreeperModel>(creeper));
			} else if (renderer instanceof PillagerRenderer pillager) {
				helper.register(new GeneModelLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(pillager));
			} else if (renderer instanceof SpiderRenderer<?>) {
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<LivingEntityRenderState, SpiderModel>) renderer));
			}
		});
	}
}
