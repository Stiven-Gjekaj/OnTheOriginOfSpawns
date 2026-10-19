package io.github.stivengjekaj.originofspawns.evolution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * What a mob did in combat with players during its life.
 *
 * @param targetedPlayer the mob targeted a player at least one time
 * @param damagedPlayer the mob damaged a player at least one time
 */
public record CombatRecord(boolean targetedPlayer, boolean damagedPlayer) {
	public static final CombatRecord NONE = new CombatRecord(false, false);

	public static final Codec<CombatRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("targeted_player", false).forGetter(CombatRecord::targetedPlayer),
			Codec.BOOL.optionalFieldOf("damaged_player", false).forGetter(CombatRecord::damagedPlayer)
	).apply(instance, CombatRecord::new));

	public CombatRecord withTargetedPlayer() {
		return new CombatRecord(true, damagedPlayer);
	}

	public CombatRecord withDamagedPlayer() {
		return new CombatRecord(targetedPlayer, true);
	}
}
