package martidi.sunnmore.stian;

/** Stian's numbers, free of Minecraft classes so they can be unit tested. */
public final class StianMath {
	/** One block every half second; twice as fast after a Bjørneunge. */
	public static final int PLACE_TICKS = 10;
	public static final int BOOSTED_PLACE_TICKS = 5;
	public static final int BLOCKS_PER_CAN = 50;
	/** While boosted, about one block in twenty gets turned the wrong way. */
	public static final int MISTAKE_ONE_IN = 20;
	/** At most one thrown can every 3 seconds. */
	public static final int CAN_COOLDOWN = 60;
	/** "Never thrown": far enough back to allow a throw, without the overflow Long.MIN_VALUE caused before. */
	public static final long NEVER = -CAN_COOLDOWN;
	/** How far he looks for a plot to build on. */
	public static final int PLOT_SEARCH = 32;

	public static final int HICCUP_MIN = 400;
	public static final int HICCUP_MAX = 800;
	public static final int TRIP_MIN = 2400;
	public static final int TRIP_MAX = 4800;
	public static final int SIT_TICKS = 30;
	public static final int PUFF_MIN = 60;
	public static final int PUFF_MAX = 120;
	public static final int SMOKE_BREAK_MIN = 1800;
	public static final int SMOKE_BREAK_MAX = 3600;
	public static final int SMOKE_BREAK_TICKS = 80;

	private StianMath() {
	}

	public static boolean canThrow(long now, long lastThrow) {
		return now - lastThrow >= CAN_COOLDOWN;
	}

	/** roll is in [0, MISTAKE_ONE_IN). */
	public static boolean isMistake(int roll) {
		return roll == 0;
	}

	public static int placeTicks(boolean boosted) {
		return boosted ? BOOSTED_PLACE_TICKS : PLACE_TICKS;
	}

	/** min + roll, where roll is in [0, max - min]. */
	public static int between(int min, int max, int roll) {
		return min + Math.max(0, Math.min(roll, max - min));
	}
}
