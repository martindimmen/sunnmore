package martidi.sunnmore.visits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The visit rules as plain numbers, free of Minecraft classes so they can be unit tested.
 * Natural visits work like the wandering trader's; lures add a 50% roll each in-game morning.
 */
public final class VisitMath {
	/** The visit system checks once a minute. */
	public static final int CHECK_INTERVAL = 1200;
	public static final int DAY = 24000;
	public static final int MIN_CHANCE = 25;
	public static final int MAX_CHANCE = 75;
	/** An extra 1-in-3 roll on natural visits (the wandering trader uses 1-in-10): about once a week. */
	public static final int EXTRA_ROLL = 3;
	/** The chance a friend comes to his lure, once per in-game day. */
	public static final int LURE_CHANCE = 50;
	/** A visit lasts two in-game days. */
	public static final int VISIT_LENGTH = 2 * DAY;
	/** A lure counts when it is within this many blocks of an online player. */
	public static final int LURE_RANGE = 64;
	/** He arrives on a free spot within this many blocks of the lure. */
	public static final int LURE_SPAWN_RADIUS = 4;
	public static final long NEVER_ROLLED = -1L;

	private VisitMath() {
	}

	public static int chanceAfterMiss(int chance) {
		return Math.max(MIN_CHANCE, Math.min(MAX_CHANCE, chance + 25));
	}

	public static boolean naturalVisitHits(int roll100, int chance, int extraRoll) {
		return roll100 < chance && extraRoll == 0;
	}

	/** One lure roll per in-game day, in daylight. */
	public static boolean shouldRollLure(long today, long lastLureDay, boolean daytime) {
		return daytime && today != lastLureDay;
	}

	public static boolean lureHits(int roll100) {
		return roll100 < LURE_CHANCE;
	}

	/** While he is visiting in daylight, the day's lure roll counts as used: no leave-and-come-straight-back. */
	public static long lureDayWhileVisiting(long today, long lastLureDay, boolean daytime, boolean visiting) {
		return daytime && visiting ? today : lastLureDay;
	}

	/**
	 * Offsets {dx, dy, dz} around a lure to try as standing spots, nearest first. Searching around
	 * the lure itself (not by heightmap) keeps him indoors when the lure is indoors. At most one
	 * block up, so a lure on a counter in a low room never puts him on the roof. Skips the lure and
	 * the blocks straight above it.
	 */
	public static List<int[]> spotOffsetsNearestFirst(int radius, int down, int up) {
		List<int[]> offsets = new ArrayList<>();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -down; dy <= up; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx == 0 && dz == 0 && dy >= 0) {
						continue;
					}
					offsets.add(new int[] {dx, dy, dz});
				}
			}
		}
		offsets.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1] + o[2] * o[2]));
		return offsets;
	}
}
