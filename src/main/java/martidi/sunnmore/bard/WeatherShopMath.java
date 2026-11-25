package martidi.sunnmore.bard;

/**
 * Bård's weather shop as plain numbers: when the deals are open, and what a purchase writes to the
 * game's weather countdowns. A purchase never makes the weather it buys shorter, never uses the
 * forced-clear timer (vanilla follows that with a storm), and bought rain has no storm inside it.
 */
public final class WeatherShopMath {
	public static final int SOL_PRICE = 1;
	public static final int REGN_PRICE = 1;
	public static final int TORDEN_PRICE = 5;
	public static final long NEVER_CHANGED = -1L;

	/** Random durations drawn from vanilla's ranges, passed in so the rules stay testable. */
	public record Samples(int rainDelay, int rainDuration, int thunderDelay, int thunderDuration) {
	}

	private WeatherShopMath() {
	}

	/** One change per in-game day, only in the Overworld, and never while the weather is frozen. */
	public static boolean dealsOpen(long today, long lastChangeDay, boolean overworld, boolean cycleOn) {
		return overworld && cycleOn && today != lastChangeDay;
	}

	public static WeatherState purchase(Weather bought, WeatherState current, Samples samples) {
		Weather now = current.now();
		return switch (bought) {
			case CLEAR -> {
				int left = current.clearTime() > 0 ? current.clearTime() : now == Weather.CLEAR ? current.rainTime() : 0;
				yield new WeatherState(0, Math.max(left, samples.rainDelay()), samples.thunderDelay(), false, false);
			}
			case RAIN -> {
				int left = now == Weather.RAIN ? current.rainTime() : 0;
				int rain = Math.max(left, samples.rainDuration());
				// The thunder waits until after the rain, so 1 diamond never buys a storm.
				yield new WeatherState(0, rain, Math.max(samples.thunderDelay(), rain + 1), true, false);
			}
			case STORM -> {
				int left = now == Weather.STORM ? Math.min(current.rainTime(), current.thunderTime()) : 0;
				int storm = Math.max(left, samples.thunderDuration());
				// Same countdown for both, as /weather thunder does: the storm ends straight into clear.
				yield new WeatherState(0, storm, storm, true, true);
			}
		};
	}
}
