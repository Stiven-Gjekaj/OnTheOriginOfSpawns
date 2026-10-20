package io.github.stivengjekaj.originofspawns.notebook;

/**
 * How much a player knows about a family.
 */
public enum Knowledge {
	/**
	 * The player did not meet the family.
	 */
	NONE,
	/**
	 * The player was near a mob of the family for a time. The player knows its name, but not its genes.
	 */
	SEEN,
	/**
	 * The player killed a mob of the family, or a mob of the family killed the player.
	 */
	KNOWN
}
