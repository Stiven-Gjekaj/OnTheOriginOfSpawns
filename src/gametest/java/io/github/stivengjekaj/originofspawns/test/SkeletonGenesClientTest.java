package io.github.stivengjekaj.originofspawns.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.stivengjekaj.originofspawns.client.render.GeneModelLayer;
import io.github.stivengjekaj.originofspawns.client.render.GeneTextures;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.phys.Vec3;

/**
 * Shows skeletons with genes, and checks that the skeleton species get the textures with the gene marks. From left
 * to right: a plain skeleton, a skeleton with Venomtip and High Ground, a skeleton with Barrage and Soulfire, a
 * stray with Venomtip and gray cloth, and a wither skeleton with the Executioner hood and Ember Eyes. The gene models
 * of the wither skeleton must grow with its larger body. A bogged with Venomtip and gray cloth
 * stands at the far left. After the screenshot, a parched with a test gene checks the parched region map and the
 * gene model layer.
 */
public class SkeletonGenesClientTest implements FabricClientGameTest {
	private static Identifier gene(String name) {
		return Identifier.fromNamespaceAndPath("originofspawns", name);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// At night the skeletons do not burn.
			singleplayer.getServer().runCommand("time set midnight");
			context.runOnClient(client -> client.options.gamma().set(1.0));
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0.0F, 15.0F);
				Vec3 front = player.position().add(0.0, 0.0, 3.5);
				spawn(player, EntityTypes.SKELETON, front.add(2.4, 0.0, 0.0), List.of());
				spawn(player, EntityTypes.SKELETON, front.add(1.2, 0.0, 0.0), List.of(gene("venomtip"), gene("high_ground")));
				spawn(player, EntityTypes.SKELETON, front, List.of(gene("barrage"), gene("soulfire")));
				spawn(player, EntityTypes.STRAY, front.add(-1.2, 0.0, 0.0), List.of(gene("venomtip"),
						Identifier.fromNamespaceAndPath("originofspawns_test", "test_cloth_dye")));
				spawn(player, EntityTypes.WITHER_SKELETON, front.add(-2.6, 0.0, 0.4), List.of(gene("executioner"), gene("ember_eyes")));
				spawn(player, EntityTypes.PARCHED, front.add(-3.8, 0.0, 0.8), List.of(gene("venomtip")));
				spawn(player, EntityTypes.BOGGED, front.add(3.6, 0.0, 0.4), List.of(gene("venomtip"),
						Identifier.fromNamespaceAndPath("originofspawns_test", "test_cloth_dye")));
			});
			context.waitTicks(40);
			context.takeScreenshot("skeleton_genes");
			for (String texture : List.of("skeleton", "stray", "stray_overlay", "wither_skeleton", "bogged",
					"bogged_overlay", "parched")) {
				Identifier base = Identifier.withDefaultNamespace("textures/entity/skeleton/" + texture + ".png");
				if (!context.computeOnClient(client -> GeneTextures.wasBakedFrom(base))) {
					throw new AssertionError("No gene marks on " + base);
				}
			}
			checkMarkAndModel(context, singleplayer, EntityTypes.PARCHED, "test_parched_mark",
					Identifier.withDefaultNamespace("textures/entity/skeleton/parched.png"), "parched");
			checkMarkAndModel(context, singleplayer, EntityTypes.CREEPER, "test_creeper_mark",
					Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png"), "creeper");
			checkMarkAndModel(context, singleplayer, EntityTypes.SPIDER, "test_spider_mark",
					Identifier.withDefaultNamespace("textures/entity/spider/spider.png"), "spider");
			checkMarkAndModel(context, singleplayer, EntityTypes.CAVE_SPIDER, "test_cave_spider_mark",
					Identifier.withDefaultNamespace("textures/entity/spider/cave_spider.png"), "spider");
			checkMarkAndModel(context, singleplayer, EntityTypes.PILLAGER, "test_pillager_mark",
					Identifier.withDefaultNamespace("textures/entity/illager/pillager.png"), "illager");
			checkMarkAndModel(context, singleplayer, EntityTypes.VINDICATOR, "test_vindicator_mark",
					Identifier.withDefaultNamespace("textures/entity/illager/vindicator.png"), "illager");
			checkMarkAndModel(context, singleplayer, EntityTypes.EVOKER, "test_evoker_mark",
					Identifier.withDefaultNamespace("textures/entity/illager/evoker.png"), "illager");
			// The witch gene model sits on the hat, a part of the head.
			checkMarkAndModel(context, singleplayer, EntityTypes.WITCH, "test_witch_mark",
					Identifier.withDefaultNamespace("textures/entity/witch/witch.png"), "witch");
			checkMarkAndModel(context, singleplayer, EntityTypes.ENDERMAN, "test_enderman_mark",
					Identifier.withDefaultNamespace("textures/entity/enderman/enderman.png"), "enderman");
			// The endermite gene model sits on segment1, the body.
			checkMarkAndModel(context, singleplayer, EntityTypes.ENDERMITE, "test_endermite_mark",
					Identifier.withDefaultNamespace("textures/entity/endermite/endermite.png"), "endermite");
			checkArmModelOnlyWhileTheArmsAreApart(context, singleplayer);
			checkEvokerArmModelOnlyWhileItCasts(context, singleplayer);
		}
	}

	/**
	 * Spawns a mob with a test gene after the screenshot, so the screenshot does not change. The gene darkens each
	 * pixel that has a region in the region map of the species, and it has a gene model. The check reads the texture
	 * that the renderer of the mob gives, and it records the parts that the gene model layer draws.
	 */
	private static void checkMarkAndModel(ClientGameTestContext context, TestSingleplayerContext singleplayer,
			EntityType<? extends Mob> type, String gene, Identifier base, String regionMap) {
		Identifier mark = Identifier.fromNamespaceAndPath("originofspawns_test", gene);
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			spawn(player, type, player.position().add(0.0, 0.0, 2.0), List.of(mark));
		});
		context.waitTicks(20);
		String failure = context.computeOnClient(client -> {
			Mob mob = client.level.getEntitiesOfClass(Mob.class, client.player.getBoundingBox().inflate(8.0),
					other -> {
						CarriedGenes genes = other.getAttached(ModAttachments.GENES);
						return other.getType() == type && genes != null && genes.genes().contains(mark);
					}).stream().findFirst().orElse(null);
			if (mob == null) {
				return "The client has no " + type + " with the test gene";
			}
			EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
			EntityRenderState state = dispatcher.extractEntity(mob, 1.0F);
			if (!(dispatcher.getRenderer(mob) instanceof LivingEntityRenderer<?, ?, ?> renderer)) {
				return "The renderer of " + type + " is not a living entity renderer";
			}
			Identifier baked = textureOf(renderer, state);
			if (baked.equals(base)) {
				return "The renderer of " + type + " gives the base texture, with no gene marks";
			}
			String texture = checkRegionMark(client, base, baked, regionMap);
			if (texture != null) {
				return texture;
			}
			int drawn = drawnModelParts(renderer, state);
			return drawn > 0 ? null : "The gene model layer draws no part of the gene model on " + type;
		});
		if (failure != null) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * Spawns two vindicators with a gene model on the right arm. One attacks, so its arms are apart. The other has its
	 * arms crossed, and the crossed arms are a different part. Only the vindicator that attacks shows the model.
	 */
	private static void checkArmModelOnlyWhileTheArmsAreApart(ClientGameTestContext context,
			TestSingleplayerContext singleplayer) {
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_vindicator_arm");
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			spawn(player, EntityTypes.VINDICATOR, player.position().add(1.5, 0.0, 2.0), List.of(gene));
			spawn(player, EntityTypes.VINDICATOR, player.position().add(-1.5, 0.0, 2.0), List.of(gene));
			player.level().getEntitiesOfClass(Vindicator.class, player.getBoundingBox().inflate(8.0),
					vindicator -> vindicator.getX() > player.getX() && vindicator.getAttached(ModAttachments.GENES) != null
							&& vindicator.getAttached(ModAttachments.GENES).genes().contains(gene))
					.forEach(vindicator -> vindicator.setAggressive(true));
		});
		context.waitTicks(20);
		String failure = context.computeOnClient(client -> {
			List<Vindicator> vindicators = client.level.getEntitiesOfClass(Vindicator.class,
					client.player.getBoundingBox().inflate(8.0), vindicator -> {
						CarriedGenes genes = vindicator.getAttached(ModAttachments.GENES);
						return genes != null && genes.genes().contains(gene);
					});
			if (vindicators.size() != 2) {
				return "The client has " + vindicators.size() + " vindicators with the arm gene, not 2";
			}
			EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
			for (Vindicator vindicator : vindicators) {
				EntityRenderState state = dispatcher.extractEntity(vindicator, 1.0F);
				var renderer = (LivingEntityRenderer<?, ?, ?>) dispatcher.getRenderer(vindicator);
				poseModel(renderer, state);
				int drawn = drawnModelParts(renderer, state);
				if (vindicator.isAggressive() && drawn == 0) {
					return "The vindicator that attacks shows no arm model";
				}
				if (!vindicator.isAggressive() && drawn > 0) {
					return "The vindicator with crossed arms shows the arm model";
				}
			}
			return null;
		});
		if (failure != null) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * Spawns two evokers with a gene model on the right arm. One casts a spell, so its arms are up and apart. The other
	 * has its arms crossed. Only the evoker that casts shows the model.
	 */
	private static void checkEvokerArmModelOnlyWhileItCasts(ClientGameTestContext context,
			TestSingleplayerContext singleplayer) {
		Identifier gene = Identifier.fromNamespaceAndPath("originofspawns_test", "test_evoker_arm");
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			spawn(player, EntityTypes.EVOKER, player.position().add(1.5, 0.0, 3.0), List.of(gene));
			spawn(player, EntityTypes.EVOKER, player.position().add(-1.5, 0.0, 3.0), List.of(gene));
			player.level().getEntitiesOfClass(Evoker.class, player.getBoundingBox().inflate(8.0),
					evoker -> evoker.getX() > player.getX() && evoker.getAttached(ModAttachments.GENES) != null
							&& evoker.getAttached(ModAttachments.GENES).genes().contains(gene))
					.forEach(SkeletonGenesClientTest::castFangs);
		});
		context.waitTicks(20);
		String failure = context.computeOnClient(client -> {
			List<Evoker> evokers = client.level.getEntitiesOfClass(Evoker.class,
					client.player.getBoundingBox().inflate(8.0), evoker -> {
						CarriedGenes genes = evoker.getAttached(ModAttachments.GENES);
						return genes != null && genes.genes().contains(gene);
					});
			if (evokers.size() != 2) {
				return "The client has " + evokers.size() + " evokers with the arm gene, not 2";
			}
			EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
			for (Evoker evoker : evokers) {
				EntityRenderState state = dispatcher.extractEntity(evoker, 1.0F);
				var renderer = (LivingEntityRenderer<?, ?, ?>) dispatcher.getRenderer(evoker);
				poseModel(renderer, state);
				int drawn = drawnModelParts(renderer, state);
				if (evoker.isCastingSpell() && drawn == 0) {
					return "The evoker that casts shows no arm model";
				}
				if (!evoker.isCastingSpell() && drawn > 0) {
					return "The evoker with crossed arms shows the arm model";
				}
			}
			return evokers.stream().anyMatch(Evoker::isCastingSpell) ? null : "No evoker casts a spell on the client";
		});
		if (failure != null) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * Shows the fang spell on the evoker. The spell enum is not public, so the check sets the synced value of the spell.
	 * The evoker has no AI, so no goal ends the spell.
	 */
	private static void castFangs(Evoker evoker) {
		try {
			var field = net.minecraft.world.entity.monster.illager.SpellcasterIllager.class
					.getDeclaredField("DATA_SPELL_CASTING_ID");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			var data = (net.minecraft.network.syncher.EntityDataAccessor<Byte>) field.get(null);
			evoker.getEntityData().set(data, (byte) 2);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot set the spell of the evoker", exception);
		}
	}

	/**
	 * Poses the model of the renderer for the state, as the renderer does before it draws its layers.
	 */
	@SuppressWarnings("unchecked")
	private static <S extends LivingEntityRenderState> void poseModel(LivingEntityRenderer<?, ?, ?> renderer,
			EntityRenderState state) {
		((net.minecraft.client.model.EntityModel<S>) renderer.getModel()).setupAnim((S) state);
	}

	@SuppressWarnings("unchecked")
	private static <S extends LivingEntityRenderState> Identifier textureOf(LivingEntityRenderer<?, ?, ?> renderer,
			EntityRenderState state) {
		return ((LivingEntityRenderer<?, S, ?>) renderer).getTextureLocation((S) state);
	}

	/**
	 * Compares the baked texture with the base texture. A pixel with a region in the region map is black. A pixel with
	 * no region has the base color.
	 *
	 * @return the failure, or null if each pixel is correct
	 */
	private static String checkRegionMark(Minecraft client, Identifier base, Identifier baked, String name) {
		Identifier regionMap = Identifier.fromNamespaceAndPath("originofspawns", "textures/region/" + name + ".png");
		try (NativeImage source = read(client, base); NativeImage regions = read(client, regionMap)) {
			if (!(client.getTextureManager().getTexture(baked) instanceof DynamicTexture dynamic)) {
				return "The baked texture " + baked + " is not a dynamic texture";
			}
			NativeImage result = dynamic.getPixels();
			int covered = 0;
			for (int y = 0; y < source.getHeight(); y++) {
				for (int x = 0; x < source.getWidth(); x++) {
					int label = regions.getPixel(x * regions.getWidth() / source.getWidth(),
							y * regions.getHeight() / source.getHeight());
					int pixel = result.getPixel(x, y);
					if (ARGB.alpha(label) != 0) {
						covered++;
						if ((pixel & 0xFFFFFF) != 0) {
							return "The pixel " + x + ", " + y + " has a region but it is not black";
						}
					} else if (pixel != source.getPixel(x, y)) {
						return "The pixel " + x + ", " + y + " has no region but it changed";
					}
				}
			}
			return covered > 0 ? null : "The region map " + name + " covers no pixel";
		} catch (IOException exception) {
			return "Cannot read a texture: " + exception;
		}
	}

	private static NativeImage read(Minecraft client, Identifier texture) throws IOException {
		try (InputStream stream = client.getResourceManager().open(texture)) {
			return NativeImage.read(stream);
		}
	}

	/**
	 * Sends the render state of the mob to its gene model layer, and counts the model parts that the layer draws.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static int drawnModelParts(LivingEntityRenderer<?, ?, ?> renderer, EntityRenderState state) {
		List<RenderLayer> layers;
		try {
			Field field = LivingEntityRenderer.class.getDeclaredField("layers");
			field.setAccessible(true);
			layers = (List<RenderLayer>) field.get(renderer);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot read the layers of the renderer", exception);
		}
		int[] parts = {0};
		Object[] collector = new Object[1];
		collector[0] = Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),
				new Class<?>[] {SubmitNodeCollector.class}, (proxy, method, args) -> {
					if (method.getName().equals("submitModelPart")) {
						parts[0]++;
					}
					return method.getReturnType().isInstance(proxy) ? proxy : null;
				});
		for (RenderLayer layer : layers) {
			if (layer instanceof GeneModelLayer<?, ?> geneLayer) {
				((GeneModelLayer) geneLayer).submit(new PoseStack(), (SubmitNodeCollector) collector[0],
						LightCoordsUtil.FULL_BRIGHT, (LivingEntityRenderState) state, 0.0F, 0.0F);
			}
		}
		return parts[0];
	}

	private static void spawn(ServerPlayer player, EntityType<? extends Mob> type, Vec3 pos, List<Identifier> genes) {
		Mob mob = type.create(player.level(), EntitySpawnReason.COMMAND);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		mob.setPos(pos);
		mob.setYRot(180.0F);
		mob.setYHeadRot(180.0F);
		mob.setYBodyRot(180.0F);
		mob.setAttached(ModAttachments.GENES, new CarriedGenes(genes));
		player.level().addFreshEntity(mob);
	}
}
