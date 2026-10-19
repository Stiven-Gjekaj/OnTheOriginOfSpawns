package io.github.stivengjekaj.originofspawns.evolution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Remembers the recent deaths of a server, to find crowd deaths.
 * A crowd is 3 or more deaths of the same species in 8 blocks in 5 seconds. Each death of a crowd counts one time.
 */
public final class CrowdTracker {
	public static final int CROWD_SIZE = 3;
	public static final double RADIUS = 8.0;
	public static final long WINDOW_TICKS = 100L;

	/**
	 * One recent death.
	 *
	 * @param counted the death already counted for the crowd pressure
	 */
	public record Death(ResourceKey<Level> dimension, Identifier species, BiomeCategory category, Vec3 pos, long time,
			boolean counted) {
	}

	private final List<Death> recent = new ArrayList<>();

	/**
	 * Adds a death. Returns the deaths that now count for the crowd pressure, and marks them as counted.
	 * The result is empty if the death does not make a crowd.
	 */
	public List<Death> add(Death death) {
		recent.removeIf(old -> death.time() - old.time() > WINDOW_TICKS);
		List<Integer> near = new ArrayList<>();
		for (int index = 0; index < recent.size(); index++) {
			Death old = recent.get(index);
			if (old.dimension().equals(death.dimension()) && old.species().equals(death.species())
					&& old.pos().closerThan(death.pos(), RADIUS)) {
				near.add(index);
			}
		}
		if (near.size() + 1 < CROWD_SIZE) {
			recent.add(death);
			return List.of();
		}
		List<Death> counted = new ArrayList<>();
		for (int index : near) {
			Death old = recent.get(index);
			if (!old.counted()) {
				counted.add(old);
				recent.set(index, new Death(old.dimension(), old.species(), old.category(), old.pos(), old.time(), true));
			}
		}
		Death added = new Death(death.dimension(), death.species(), death.category(), death.pos(), death.time(), true);
		recent.add(added);
		counted.add(added);
		return counted;
	}

	public void clear() {
		recent.clear();
	}
}
