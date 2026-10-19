package io.github.stivengjekaj.originofspawns.evolution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * What a mob did in combat with players during its life.
 *
 * @param targetedPlayer the mob targeted a player at least one time
 * @param damagedPlayer the mob damaged a player at least one time
 * @param leechedXp the XP points that the mob took from players
 * @param graveCalls the mobs that the mob called when it was hurt
 */
public record CombatRecord(boolean targetedPlayer, boolean damagedPlayer, int leechedXp, int graveCalls) {
	public static final CombatRecord NONE = new CombatRecord(false, false, 0, 0);

	public static final Codec<CombatRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("targeted_player", false).forGetter(CombatRecord::targetedPlayer),
			Codec.BOOL.optionalFieldOf("damaged_player", false).forGetter(CombatRecord::damagedPlayer),
			Codec.INT.optionalFieldOf("leeched_xp", 0).forGetter(CombatRecord::leechedXp),
			Codec.INT.optionalFieldOf("grave_calls", 0).forGetter(CombatRecord::graveCalls)
	).apply(instance, CombatRecord::new));

	public CombatRecord(boolean targetedPlayer, boolean damagedPlayer) {
		this(targetedPlayer, damagedPlayer, 0, 0);
	}

	public CombatRecord withTargetedPlayer() {
		return new CombatRecord(true, damagedPlayer, leechedXp, graveCalls);
	}

	public CombatRecord withDamagedPlayer() {
		return new CombatRecord(targetedPlayer, true, leechedXp, graveCalls);
	}

	public CombatRecord withLeechedXp(int amount) {
		return new CombatRecord(targetedPlayer, damagedPlayer, leechedXp + amount, graveCalls);
	}

	public CombatRecord withGraveCall() {
		return new CombatRecord(targetedPlayer, damagedPlayer, leechedXp, graveCalls + 1);
	}
}
