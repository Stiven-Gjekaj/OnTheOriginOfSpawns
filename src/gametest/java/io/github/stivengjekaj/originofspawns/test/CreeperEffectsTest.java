package io.github.stivengjekaj.originofspawns.test;

import io.github.stivengjekaj.originofspawns.definition.GeneEffect.When;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.GeneBlasts;
import io.github.stivengjekaj.originofspawns.evolution.GeneCombat;
import io.github.stivengjekaj.originofspawns.mixin.CreeperAccessor;
import io.github.stivengjekaj.originofspawns.registry.ModAttachments;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.netty.channel.embedded.EmbeddedChannel;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Tests the effects that the creeper genes use: the blast, the fuse, the charge, and the states of a creeper.
 */
public class CreeperEffectsTest {
	/**
	 * The test environment for blasts: no block breaks, and players take the normal damage.
	 */
	static final String BLASTS = "originofspawns_test:blasts";

	@GameTest
	public void aChargedCreeperIsInTheChargedState(GameTestHelper helper) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1));
		helper.assertFalse(GeneCombat.test(creeper, When.CHARGED), "a plain creeper is not charged");
		charge(creeper);
		helper.assertTrue(GeneCombat.test(creeper, When.CHARGED), "a charged creeper");
		var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 1));
		helper.assertFalse(GeneCombat.test(zombie, When.CHARGED), "a zombie is never charged");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 16)
	public void aLargerBlastReachesFarther(GameTestHelper helper) {
		Creeper plain = creeperWith(helper, new BlockPos(1, 1, 1));
		Creeper big = creeperWith(helper, new BlockPos(1, 1, 12), "test_big_bang");
		// The blast of a plain creeper reaches 6 blocks. A pig at 8 blocks is out of its reach.
		Pig nearPlain = pigAt(helper, new BlockPos(9, 1, 1));
		Pig nearBig = pigAt(helper, new BlockPos(9, 1, 12));
		explode(plain);
		explode(big);
		helper.assertValueEqual(nearPlain.getHealth(), nearPlain.getMaxHealth(), "the plain blast misses the pig");
		helper.assertTrue(!nearBig.isAlive() || nearBig.getHealth() < nearBig.getMaxHealth(),
				"the larger blast reaches the pig");
		helper.succeed();
	}

	@GameTest
	public void aBlastSizeCanNeedTheCharge(GameTestHelper helper) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1), "test_overcharge");
		helper.assertValueEqual(GeneBlasts.sizeMultiplier(creeper), 1.0F, "no change without the charge");
		charge(creeper);
		helper.assertValueEqual(GeneBlasts.sizeMultiplier(creeper), 1.25F, "a larger blast with the charge");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8, maxTicks = 100)
	public void aLongerFuseExplodesLater(GameTestHelper helper) {
		Creeper slow = creeperWith(helper, new BlockPos(1, 1, 1), "test_long_fuse");
		helper.assertValueEqual(GeneBlasts.fuseTicks(slow, 30), 60, "two times the vanilla fuse");
		slow.ignite();
		// The vanilla fuse ends after 30 ticks.
		helper.runAfterDelay(45, () -> {
			helper.assertFalse(slow.isRemoved(), "the slow creeper still burns its fuse");
			helper.assertTrue(slow.getSwelling(1.0F) > 0.5F, "the fuse burns");
		});
		helper.runAfterDelay(70, () -> {
			helper.assertTrue(slow.isRemoved(), "the slow creeper exploded after 60 ticks");
			helper.succeed();
		});
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void damageEffectsChangeTheBlastDamage(GameTestHelper helper) {
		// Each blast has the same place, so only the genes change the damage.
		float plain = blastDamageToGolem(helper);
		float soft = blastDamageToGolem(helper, "test_soft_blast");
		helper.assertTrue(plain > 0.0F, "the plain blast hurts the golem");
		helper.assertTrue(Math.abs(soft - plain * 0.5F) < 0.01F, "half the damage, was " + soft + " of " + plain);
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void moreBlastDamageToATag(GameTestHelper helper) {
		float plain = blastDamageToGolem(helper);
		float breaker = blastDamageToGolem(helper, "test_golem_breaker");
		helper.assertTrue(Math.abs(breaker - plain * 2.0F) < 0.01F, "two times the damage, was " + breaker + " of " + plain);
		helper.succeed();
	}

	/**
	 * Explodes a creeper with the genes 4 blocks from a new iron golem, and returns the health that the golem lost.
	 * The golem goes away after the blast.
	 */
	static float blastDamageToGolem(GameTestHelper helper, String... genes) {
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 2), genes);
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 2));
		float before = golem.getHealth();
		explode(creeper);
		float lost = before - golem.getHealth();
		golem.discard();
		return lost;
	}

	@GameTest(environment = BLASTS, padding = 8, maxTicks = 80)
	public void aBlastCloudHasTheRadiusOfTheBlast(GameTestHelper helper) {
		Creeper plain = creeperWith(helper, new BlockPos(1, 1, 1), "test_toxic_blast");
		Creeper big = creeperWith(helper, new BlockPos(6, 1, 6), "test_toxic_blast", "test_big_bang");
		explode(plain);
		explode(big);
		AreaEffectCloud small = cloudNear(helper, plain);
		AreaEffectCloud large = cloudNear(helper, big);
		helper.assertValueEqual(small.getRadius(), 3.0F, "the radius of a plain blast");
		helper.assertValueEqual(large.getRadius(), 6.0F, "the radius of a larger blast");
		helper.assertValueEqual(small.getDuration(), 120, "the cloud stays 6 seconds");
		// A pig that walks in after the blast gets the effect of the cloud.
		Pig pig = pigAt(helper, new BlockPos(1, 1, 1));
		helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.POISON), "the pig is poisoned"));
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void aBlastGivesItsEffectsToTheEntitiesThatItHurts(GameTestHelper helper) {
		Creeper plain = creeperWith(helper, new BlockPos(1, 1, 1));
		IronGolem plainGolem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 1));
		explode(plain);
		helper.assertTrue(plainGolem.getHealth() < plainGolem.getMaxHealth(), "the plain blast hurts the golem");
		helper.assertTrue(plainGolem.getActiveEffects().isEmpty(), "the plain blast gives no effects");
		plainGolem.discard();
		Creeper stunner = creeperWith(helper, new BlockPos(1, 1, 1), "test_concussion");
		IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 1));
		explode(stunner);
		helper.assertTrue(golem.hasEffect(MobEffects.NAUSEA), "the blast gives Nausea");
		helper.assertValueEqual(golem.getEffect(MobEffects.SLOWNESS).getDuration(), 100, "Slowness for 5 seconds");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8, maxTicks = 40)
	public void shrapnelGoesThroughAShield(GameTestHelper helper) {
		ServerPlayer plainTarget = shieldedPlayer(helper);
		explode(creeperWith(helper, new BlockPos(1, 1, 1)));
		helper.assertTrue(plainTarget.getHealth() > plainTarget.getMaxHealth() - 1.0F, "the shield stops a plain blast");
		helper.assertTrue(plainTarget.getOffhandItem().getDamageValue() > 0, "a plain blast damages the shield");
		ServerPlayer target = shieldedPlayer(helper);
		explode(creeperWith(helper, new BlockPos(1, 1, 1), "test_shrapnel"));
		helper.assertTrue(target.getHealth() < target.getMaxHealth() - 5.0F, "shrapnel hurts the player behind the shield");
		helper.assertValueEqual(target.getOffhandItem().getDamageValue(), 0, "shrapnel does not damage the shield");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8)
	public void aChainReactionLightsTheCreepersNearIt(GameTestHelper helper) {
		Creeper first = creeperWith(helper, new BlockPos(1, 1, 1), "test_chain_reaction");
		Creeper near = creeperWith(helper, new BlockPos(5, 1, 1), "test_chain_reaction");
		Creeper swelling = creeperWith(helper, new BlockPos(1, 1, 5));
		Creeper far = creeperWith(helper, new BlockPos(1, 1, 8));
		swelling.setSwellDir(1);
		explode(first);
		helper.assertTrue(near.isIgnited(), "the near creeper burns its fuse");
		helper.assertTrue(near.getAttachedOrElse(ModAttachments.CHAIN_LIT, false), "the chain lit the near creeper");
		helper.assertFalse(swelling.hasAttached(ModAttachments.CHAIN_LIT), "a creeper that was lit stays as it was");
		helper.assertFalse(far.isIgnited(), "a creeper out of the radius is not lit");
		// The near creeper has the gene too, but a creeper that a chain lit does not light more creepers.
		Creeper next = creeperWith(helper, new BlockPos(9, 1, 1));
		explode(near);
		helper.assertFalse(next.isIgnited(), "the chain stops after one step");
		helper.succeed();
	}

	@GameTest(environment = BLASTS, padding = 8, maxTicks = 120)
	public void anAftershockIsASmallerSecondBlast(GameTestHelper helper) {
		explode(creeperWith(helper, new BlockPos(1, 1, 1), "test_aftershock"));
		// The golems come after the first blast. The second blast has a radius of 1.5, so it reaches 3 blocks.
		IronGolem near = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(3, 1, 1));
		IronGolem far = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(5, 1, 1));
		helper.runAfterDelay(30, () -> helper.assertValueEqual(near.getHealth(), near.getMaxHealth(),
				"no second blast before 2 seconds"));
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(near.getHealth() < near.getMaxHealth(), "the second blast hurts the near golem");
			helper.assertValueEqual(far.getHealth(), far.getMaxHealth(), "the second blast is smaller");
			near.discard();
		});
		helper.runAfterDelay(50, () -> {
			// A golem at the place of the blasts takes damage from a blast of each size.
			IronGolem later = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new BlockPos(1, 1, 1));
			helper.runAfterDelay(50, () -> {
				helper.assertValueEqual(later.getHealth(), later.getMaxHealth(), "no third blast");
				helper.succeed();
			});
		});
	}

	@GameTest(environment = BLASTS, padding = 24, maxTicks = 20)
	public void aPlainFuseHisses(GameTestHelper helper) {
		Listener listener = listener(helper);
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1));
		lightWithFlintAndSteel(helper, creeper);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(listener.heard(SoundEvents.FLINTANDSTEEL_USE), "the flint and steel makes a sound");
			helper.assertTrue(listener.heard(SoundEvents.CREEPER_PRIMED), "the fuse hisses");
			creeper.discard();
			helper.succeed();
		});
	}

	@GameTest(environment = BLASTS, padding = 24, maxTicks = 20)
	public void aSilentFuseMakesNoSound(GameTestHelper helper) {
		Listener listener = listener(helper);
		Creeper creeper = creeperWith(helper, new BlockPos(1, 1, 1), "test_silent_fuse");
		lightWithFlintAndSteel(helper, creeper);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(creeper.getSwelling(1.0F) > 0.0F, "the fuse burns");
			helper.assertFalse(listener.heard(SoundEvents.FLINTANDSTEEL_USE), "the flint and steel makes no sound");
			helper.assertFalse(listener.heard(SoundEvents.CREEPER_PRIMED), "the fuse does not hiss");
			creeper.discard();
			helper.succeed();
		});
	}

	/**
	 * Lights the fuse of the creeper with the flint and steel of a second player, as a player does.
	 */
	static void lightWithFlintAndSteel(GameTestHelper helper, Creeper creeper) {
		ServerPlayer lighter = survivalPlayer(helper).player();
		lighter.setPos(creeper.position().add(1.0, 0.0, 0.0));
		lighter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
		creeper.interact(lighter, InteractionHand.MAIN_HAND, creeper.position());
	}

	/**
	 * A player next to the test that keeps the packets that the server sends to it.
	 */
	record Listener(ServerPlayer player, EmbeddedChannel channel) {
		/**
		 * Returns true if the server sent this sound to the player.
		 */
		boolean heard(SoundEvent sound) {
			Object packet;
			boolean heard = false;
			java.util.List<Object> kept = new java.util.ArrayList<>();
			while ((packet = channel.readOutbound()) != null) {
				kept.add(packet);
				heard |= packet instanceof ClientboundSoundPacket soundPacket && soundPacket.getSound().value() == sound;
			}
			kept.forEach(channel::writeOutbound);
			return heard;
		}
	}

	static Listener listener(GameTestHelper helper) {
		Listener listener = survivalPlayer(helper);
		listener.player().setNoGravity(true);
		listener.player().setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 4))));
		return listener;
	}

	/**
	 * Makes a player in survival 4 blocks east of the blast place, who holds a shield up to the west.
	 */
	static ServerPlayer shieldedPlayer(GameTestHelper helper) {
		ServerPlayer player = survivalPlayer(helper).player();
		player.setNoGravity(true);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
		player.startUsingItem(InteractionHand.OFF_HAND);
		// A shield blocks only after it is up for a short time. A mock player is not ticked by the server.
		for (int i = 0; i < 10; i++) {
			player.doTick();
		}
		player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 1, 1))));
		player.setYRot(90.0F);
		player.setYHeadRot(90.0F);
		helper.assertTrue(player.isBlocking(), "the player holds the shield up");
		return player;
	}

	/**
	 * Makes a player in the level in survival mode, who can take damage. The mock player of the test helper is always
	 * in creative mode, so its items take no damage. The channel keeps the packets that the server sends to the player.
	 */
	static Listener survivalPlayer(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-survival-player"), false);
		ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
			@Override
			public GameType gameMode() {
				return GameType.SURVIVAL;
			}
		};
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		EmbeddedChannel channel = new EmbeddedChannel(connection);
		server.getPlayerList().placeNewPlayer(connection, player, cookie);
		player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
		// A player takes no damage until its client tells the server that the world loaded.
		player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		return new Listener(player, channel);
	}

	static AreaEffectCloud cloudNear(GameTestHelper helper, Creeper creeper) {
		// A large cloud reaches far, so only a cloud whose center is at the creeper counts.
		var clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class, creeper.getBoundingBox().inflate(0.5),
				cloud -> cloud.position().distanceTo(creeper.position()) < 0.5);
		helper.assertValueEqual(clouds.size(), 1, "one cloud at the blast");
		return clouds.getFirst();
	}

	static void explode(Creeper creeper) {
		((CreeperAccessor) creeper).originofspawns$explode();
	}

	static Pig pigAt(GameTestHelper helper, BlockPos pos) {
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
		pig.setNoGravity(true);
		return pig;
	}

	static void charge(Creeper creeper) {
		creeper.getEntityData().set(CreeperAccessor.originofspawns$poweredData(), true);
	}

	static Creeper creeperWith(GameTestHelper helper, BlockPos pos, String... genes) {
		Creeper creeper = EntityTypes.CREEPER.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
		creeper.setAttached(ModAttachments.GENES, new CarriedGenes(java.util.Arrays.stream(genes)
				.map(gene -> Identifier.fromNamespaceAndPath("originofspawns_test", gene)).toList()));
		creeper.setNoAi(true);
		creeper.setPersistenceRequired();
		creeper.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(pos)), 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(creeper);
		return creeper;
	}
}
