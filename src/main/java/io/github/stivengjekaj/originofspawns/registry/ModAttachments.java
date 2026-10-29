package io.github.stivengjekaj.originofspawns.registry;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.evolution.BehaviorState;
import io.github.stivengjekaj.originofspawns.evolution.CarriedGenes;
import io.github.stivengjekaj.originofspawns.evolution.CombatRecord;
import io.github.stivengjekaj.originofspawns.evolution.DeathTrace;
import io.github.stivengjekaj.originofspawns.notebook.DiscoveredFamilies;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.network.codec.ByteBufCodecs;

/**
 * The data that the mod attaches to entities.
 */
public final class ModAttachments {
	public static final AttachmentType<CombatRecord> COMBAT =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("combat"), CombatRecord.CODEC);

	/**
	 * The genes of a mob. The client needs them to draw the mob.
	 */
	public static final AttachmentType<CarriedGenes> GENES = AttachmentRegistry.create(OriginOfSpawns.id("genes"),
			builder -> builder.persistent(CarriedGenes.CODEC)
					.syncWith(CarriedGenes.STREAM_CODEC, AttachmentSyncPredicate.all()));

	/**
	 * Short-lived facts for the death of a mob. The game does not save them.
	 */
	public static final AttachmentType<DeathTrace> TRACE = AttachmentRegistry.create(OriginOfSpawns.id("trace"));

	/**
	 * Short-lived state of the behavior effects. The game does not save it.
	 */
	public static final AttachmentType<BehaviorState> STATE = AttachmentRegistry.create(OriginOfSpawns.id("state"));

	/**
	 * How a mob spawned. The genes need it one time, when the mob first joins a world.
	 */
	public static final AttachmentType<net.minecraft.world.entity.EntitySpawnReason> SPAWN_REASON =
			AttachmentRegistry.create(OriginOfSpawns.id("spawn_reason"));

	/**
	 * The families that a player found. The player keeps them after death. Only the player gets them on the client,
	 * for the mob info panel.
	 */
	public static final AttachmentType<DiscoveredFamilies> DISCOVERED = AttachmentRegistry.create(
			OriginOfSpawns.id("discovered"), builder -> builder.persistent(DiscoveredFamilies.CODEC).copyOnDeath()
					.syncWith(DiscoveredFamilies.STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

	/**
	 * The game time when a mob first got its genes. Living evolution needs the age of the mob.
	 */
	public static final AttachmentType<Long> BORN =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("born"), com.mojang.serialization.Codec.LONG);

	/**
	 * True when a mob used its one survival of a lethal hit.
	 */
	public static final AttachmentType<Boolean> UNDYING_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("undying_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * True when a mob used its one heal to full health.
	 */
	public static final AttachmentType<Boolean> HEAL_ONCE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("heal_once_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * True when a mob used its one escape jump.
	 */
	public static final AttachmentType<Boolean> ESCAPE_JUMP_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("escape_jump_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * True when a mob used its one push on hurt with {@code once}.
	 */
	public static final AttachmentType<Boolean> PUSH_ONCE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("push_once_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * The game time when a player last blocked a hit with a shield. The game does not save it.
	 */
	public static final AttachmentType<Long> SHIELD_BLOCK = AttachmentRegistry.create(OriginOfSpawns.id("shield_block"));

	/**
	 * True when a mob used its one fall apart and return.
	 */
	public static final AttachmentType<Boolean> REASSEMBLE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("reassemble_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * The game time of the last fan shot of a mob.
	 */
	public static final AttachmentType<Long> FAN_SHOT_TIME = AttachmentRegistry.create(OriginOfSpawns.id("fan_shot_time"));

	/**
	 * On an arrow: the distance past which players do not see the arrow while it flies. The clients get it.
	 */
	public static final AttachmentType<Float> GHOST_ARROW = AttachmentRegistry.create(OriginOfSpawns.id("ghost_arrow"),
			builder -> builder.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.all()));

	/**
	 * The game time of the last shear of a bogged.
	 */
	public static final AttachmentType<Long> SHEAR_TIME = AttachmentRegistry.createPersistent(OriginOfSpawns.id("shear_time"),
			com.mojang.serialization.Codec.LONG);

	/**
	 * The game time of the last parry of a mob.
	 */
	public static final AttachmentType<Long> PARRY_TIME = AttachmentRegistry.create(OriginOfSpawns.id("parry_time"));

	/**
	 * The game time of the last melee hit of a mob on each player, by the UUID of the player.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Long>> PLAYER_HITS =
			AttachmentRegistry.create(OriginOfSpawns.id("player_hits"));

	/**
	 * The game time of the last skull shot of a mob.
	 */
	public static final AttachmentType<Long> SKULL_TIME = AttachmentRegistry.create(OriginOfSpawns.id("skull_time"));

	/**
	 * On a player: the game time of the last item that a gene made the player drop.
	 */
	public static final AttachmentType<Long> DISARM_TIME = AttachmentRegistry.create(OriginOfSpawns.id("disarm_time"));

	/**
	 * The game time of the last cobweb of the web trail of a mob.
	 */
	public static final AttachmentType<Long> WEB_TRAIL_TIME = AttachmentRegistry.create(OriginOfSpawns.id("web_trail_time"));

	/**
	 * The game time of the last web shot of a mob.
	 */
	public static final AttachmentType<Long> WEB_SHOT_TIME = AttachmentRegistry.create(OriginOfSpawns.id("web_shot_time"));

	/**
	 * The game time of the last timed rocket shot of a mob.
	 */
	public static final AttachmentType<Long> ROCKET_TIME = AttachmentRegistry.create(OriginOfSpawns.id("rocket_time"));

	/**
	 * The items in the sack of a mob that picks up items and has no inventory of its own, such as a vindicator.
	 */
	public static final AttachmentType<java.util.List<net.minecraft.world.item.ItemStack>> SACK =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("sack"),
					net.minecraft.world.item.ItemStack.CODEC.listOf());

	/**
	 * True on a raider that dies as a part of a raid. The raid lets the raider go before the death event.
	 */
	public static final AttachmentType<Boolean> RAID_DEATH = AttachmentRegistry.create(OriginOfSpawns.id("raid_death"));

	/**
	 * The game time of the last leap of a mob with genes. The first melee hit after it removes it.
	 */
	public static final AttachmentType<Long> LEAP_TIME = AttachmentRegistry.create(OriginOfSpawns.id("leap_time"));

	/**
	 * The game time of the last hit of a mob on each player, by the UUID of the player. The first strike uses it.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Long>> STRIKE_TIMES =
			AttachmentRegistry.create(OriginOfSpawns.id("strike_times"));

	/**
	 * The game time of the last use of each effect on hurt of a mob. Each effect has its own cooldown.
	 */
	public static final AttachmentType<java.util.Map<io.github.stivengjekaj.originofspawns.definition.GeneEffect.EffectOnHurt, Long>>
			HURT_EFFECT_TIMES = AttachmentRegistry.create(OriginOfSpawns.id("hurt_effect_times"));

	/**
	 * The game time of the last cast of each hex of a mob. Each hex has its own cooldown.
	 */
	public static final AttachmentType<java.util.Map<io.github.stivengjekaj.originofspawns.definition.GeneEffect.Hex, Long>>
			HEX_TIMES = AttachmentRegistry.create(OriginOfSpawns.id("hex_times"));

	/**
	 * On a thrown potion: true when a mob with genes threw it at a target. The effects of a potion hit act only on
	 * such a potion, not on a potion that heals an ally.
	 */
	public static final AttachmentType<Boolean> GENE_POTION = AttachmentRegistry.create(OriginOfSpawns.id("gene_potion"));

	/**
	 * The game time of the last drink of each drink effect of a witch. Each drink has its own cooldown.
	 */
	public static final AttachmentType<java.util.Map<io.github.stivengjekaj.originofspawns.definition.GeneEffect.Drink, Long>>
			DRINK_TIMES = AttachmentRegistry.create(OriginOfSpawns.id("drink_times"));

	/**
	 * The game time of the last heal throw of a mob.
	 */
	public static final AttachmentType<Long> HEAL_THROW_TIME = AttachmentRegistry.create(OriginOfSpawns.id("heal_throw_time"));

	/**
	 * The game time of the last use of each effect with a cooldown, for the effects that keep no time of their own.
	 */
	public static final AttachmentType<java.util.Map<io.github.stivengjekaj.originofspawns.definition.GeneEffect, Long>>
			EFFECT_TIMES = AttachmentRegistry.create(OriginOfSpawns.id("effect_times"));

	/**
	 * True after a mob used its one escape teleport. It is saved with the mob.
	 */
	public static final AttachmentType<Boolean> ESCAPE_USED =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("escape_used"), com.mojang.serialization.Codec.BOOL);

	/**
	 * On a falling block that a mob threw: the thrower and the damage of the hit.
	 */
	public static final AttachmentType<io.github.stivengjekaj.originofspawns.evolution.GeneThrows.Thrown> THROWN_BLOCK =
			AttachmentRegistry.create(OriginOfSpawns.id("thrown_block"));

	/**
	 * The rolls of a piglin with the gold armor attack gene: for each player that it saw, true if it attacks the
	 * player in gold armor.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Boolean>> GOLD_ROLLS =
			AttachmentRegistry.create(OriginOfSpawns.id("gold_rolls"));

	/**
	 * The last game time that the mob was in lava.
	 */
	public static final AttachmentType<Long> LAVA_TIME = AttachmentRegistry.create(OriginOfSpawns.id("lava_time"));

	/**
	 * The game time of the last growth of a cube, or of the first time that the game asked about it.
	 */
	public static final AttachmentType<Long> GROW_TIME = AttachmentRegistry.create(OriginOfSpawns.id("grow_time"));

	/**
	 * The game time of the last bud of a cube, or of the first time that the game asked about it.
	 */
	public static final AttachmentType<Long> BUD_TIME = AttachmentRegistry.create(OriginOfSpawns.id("bud_time"));

	/**
	 * The game time at which the brain attack cooldown of a mob ends, after the attack interval genes changed it.
	 */
	public static final AttachmentType<Long> COOLDOWN_END = AttachmentRegistry.create(OriginOfSpawns.id("cooldown_end"));

	/**
	 * The blocks that the mob ran with no stop, and its place in the last tick.
	 */
	public static final AttachmentType<Double> RUN_BLOCKS = AttachmentRegistry.create(OriginOfSpawns.id("run_blocks"));
	public static final AttachmentType<net.minecraft.world.phys.Vec3> RUN_POS = AttachmentRegistry.create(OriginOfSpawns.id("run_pos"));

	/**
	 * The last game time that the sun hurt a mob with a sun shade.
	 */
	public static final AttachmentType<Long> SUN_SHADE_TIME = AttachmentRegistry.create(OriginOfSpawns.id("sun_shade_time"));

	/**
	 * The hits that a mob with a spin up gene took, one after the other, and the game time of the last one.
	 */
	public static final AttachmentType<Integer> SPIN_HITS = AttachmentRegistry.create(OriginOfSpawns.id("spin_hits"));
	public static final AttachmentType<Long> SPIN_TIME = AttachmentRegistry.create(OriginOfSpawns.id("spin_time"));

	/**
	 * The damage multiplier of a projectile that a mob with a rebound gene sent back.
	 */
	public static final AttachmentType<Float> REBOUND_DAMAGE = AttachmentRegistry.create(OriginOfSpawns.id("rebound_damage"));

	/**
	 * True after a breeze shot its wind charge at the top of the long jump that it does now.
	 */
	public static final AttachmentType<Boolean> JUMP_SHOT_DONE = AttachmentRegistry.create(OriginOfSpawns.id("jump_shot_done"));

	/**
	 * True when the next laser of a guardian charges at once, after it killed a player.
	 */
	public static final AttachmentType<Boolean> LASER_READY = AttachmentRegistry.create(OriginOfSpawns.id("laser_ready"));

	/**
	 * The last game time that the mob shot a fireball.
	 */
	public static final AttachmentType<Long> SHOT_TIME = AttachmentRegistry.create(OriginOfSpawns.id("shot_time"));

	/**
	 * The place where the mob spawned. It is saved with the mob, so a mob that a player moves keeps the old place.
	 */
	public static final AttachmentType<net.minecraft.core.BlockPos> HOME =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("home"), net.minecraft.core.BlockPos.CODEC);

	/**
	 * True on a cube that budded off another cube. It makes no buds of its own. It is saved with the cube.
	 */
	public static final AttachmentType<Boolean> BUD_CHILD =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("bud_child"), com.mojang.serialization.Codec.BOOL);

	/**
	 * The game time of the last teleport of a mob.
	 */
	public static final AttachmentType<Long> TELEPORT_TIME = AttachmentRegistry.create(OriginOfSpawns.id("teleport_time"));

	/**
	 * The game time of the last decoy summon of a mob.
	 */
	public static final AttachmentType<Long> DECOY_TIME = AttachmentRegistry.create(OriginOfSpawns.id("decoy_time"));

	/**
	 * True when a chain reaction lit the fuse of a creeper. Such a creeper does not light more creepers.
	 */
	public static final AttachmentType<Boolean> CHAIN_LIT =
			AttachmentRegistry.createPersistent(OriginOfSpawns.id("chain_lit"), com.mojang.serialization.Codec.BOOL);

	/**
	 * On a lightning bolt: true when the bolt does not charge the creepers that it hits.
	 */
	public static final AttachmentType<Boolean> NO_CHARGE = AttachmentRegistry.create(OriginOfSpawns.id("no_charge"));

	/**
	 * The game time when each player last looked at a mob, by the UUID of the player. The game does not save it.
	 */
	public static final AttachmentType<java.util.Map<java.util.UUID, Long>> LOOK_TIMES =
			AttachmentRegistry.create(OriginOfSpawns.id("look_times"));

	private ModAttachments() {
	}

	/**
	 * Loads this class, so that the attachment types register.
	 */
	public static void register() {
	}
}
