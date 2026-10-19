package io.github.stivengjekaj.originofspawns.definition;

import java.util.Locale;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * A condition of a death that a pressure can require.
 */
public enum DeathCondition implements StringRepresentable {
	/**
	 * The mob targeted a player, but did not damage a player before it died.
	 */
	UNREACHED(context -> context.combat().targetedPlayer() && !context.combat().damagedPlayer());

	public static final Codec<DeathCondition> CODEC = StringRepresentable.fromEnum(DeathCondition::values);

	private final Predicate<DeathContext> test;

	DeathCondition(Predicate<DeathContext> test) {
		this.test = test;
	}

	public boolean test(DeathContext context) {
		return test.test(context);
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
