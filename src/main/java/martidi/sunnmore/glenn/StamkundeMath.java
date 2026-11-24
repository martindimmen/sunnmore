package martidi.sunnmore.glenn;

/**
 * Glenn's loyalty ("Stamkunde") numbers. Kept free of Minecraft classes so it can be unit tested.
 * Levels reuse the vanilla merchant thresholds, so the trade screen's progress bar matches.
 */
public final class StamkundeMath {
	public static final int XP_PER_TRADE = 5;
	/** Trades needed for the "Stamkunde" advancement. */
	public static final int STAMKUNDE_TRADES = 10;
	/** Minimum XP for levels 2..5 (same as VillagerData). */
	private static final int[] LEVEL_MIN_XP = {10, 70, 150, 250};

	private StamkundeMath() {
	}

	public static int xpForTrades(int trades) {
		return Math.max(0, trades) * XP_PER_TRADE;
	}

	public static int levelForXp(int xp) {
		int level = 1;
		for (int min : LEVEL_MIN_XP) {
			if (xp >= min) {
				level++;
			}
		}
		return level;
	}

	public static int levelForTrades(int trades) {
		return levelForXp(xpForTrades(trades));
	}

	/** Sugar taken off each deal. */
	public static int discount(int level) {
		return Math.max(0, level - 1);
	}

	public static boolean becameStamkunde(int tradesAfter) {
		return tradesAfter == STAMKUNDE_TRADES;
	}

	public static boolean leveledUp(int tradesAfter) {
		return tradesAfter > 0 && levelForTrades(tradesAfter) > levelForTrades(tradesAfter - 1);
	}
}
