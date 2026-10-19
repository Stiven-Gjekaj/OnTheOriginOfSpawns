package io.github.stivengjekaj.originofspawns.definition;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * A fact about a death that a pressure can require. The mod finds the facts of each death.
 */
public enum DeathCondition implements StringRepresentable {
	/**
	 * The mob targeted a player, but did not damage a player before it died.
	 */
	UNREACHED,
	/**
	 * The sun burned the mob, and fire killed it.
	 */
	SUNLIGHT,
	/**
	 * The mob was in the view of its killer, at more than 8 blocks, for 2 seconds or more before the death.
	 */
	SPOTTED,
	/**
	 * No other mob of the same species was in 8 blocks.
	 */
	ALONE,
	/**
	 * 3 or more mobs of the same species died in 8 blocks in 5 seconds.
	 */
	CROWD,
	/**
	 * A critical hit of a player killed the mob.
	 */
	CRITICAL,
	/**
	 * An iron golem killed the mob.
	 */
	GOLEM,
	/**
	 * The mob killed a player. This is not a death of the mob.
	 */
	PLAYER_DEATH;

	public static final Codec<DeathCondition> CODEC = StringRepresentable.fromEnum(DeathCondition::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
