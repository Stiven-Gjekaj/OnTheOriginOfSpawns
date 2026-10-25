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

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.model.monster.hoglin.HoglinModel;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.model.monster.piglin.PiglinModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
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
			} else if (renderer instanceof VindicatorRenderer vindicator) {
				helper.register(new GeneModelLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(vindicator));
			} else if (renderer instanceof EvokerRenderer<?>) {
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<EvokerRenderState, IllagerModel<EvokerRenderState>>) renderer));
			} else if (renderer instanceof EndermanRenderer enderman) {
				helper.register(new GeneModelLayer<EndermanRenderState, EndermanModel<EndermanRenderState>>(enderman));
			} else if (renderer instanceof EndermiteRenderer) {
				// The endermite model takes any entity render state, so the layer sees it as a model of the living one.
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>>) (Object) renderer));
			} else if (renderer instanceof WitchRenderer witch) {
				helper.register(new GeneModelLayer<WitchRenderState, WitchModel>(witch));
			} else if (renderer instanceof BreezeRenderer breeze) {
				helper.register(new GeneModelLayer<BreezeRenderState, BreezeModel>(breeze));
			} else if (renderer instanceof BlazeRenderer blaze) {
				helper.register(new GeneModelLayer<LivingEntityRenderState, BlazeModel>(blaze));
			} else if (renderer instanceof GhastRenderer ghast) {
				helper.register(new GeneModelLayer<GhastRenderState, GhastModel>(ghast));
			} else if (renderer instanceof PhantomRenderer phantom) {
				helper.register(new GeneModelLayer<PhantomRenderState, PhantomModel>(phantom));
			} else if (renderer instanceof PiglinRenderer piglin) {
				helper.register(new GeneModelLayer<PiglinRenderState, PiglinModel>(piglin));
			} else if (renderer instanceof ZombifiedPiglinRenderer zombified) {
				helper.register(new GeneModelLayer<ZombifiedPiglinRenderState, ZombifiedPiglinModel>(zombified));
			} else if (renderer instanceof AbstractHoglinRenderer<?>) {
				helper.register(new GeneModelLayer<>((RenderLayerParent<HoglinRenderState, HoglinModel>) renderer));
			} else if (renderer instanceof SlimeRenderer || renderer instanceof MagmaCubeRenderer) {
				// The slime model takes any entity render state, so the layer sees it as a model of the living one.
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>>) (Object) renderer));
			} else if (renderer instanceof SpiderRenderer<?>) {
				helper.register(new GeneModelLayer<>(
						(RenderLayerParent<LivingEntityRenderState, SpiderModel>) renderer));
			}
		});
	}
}
