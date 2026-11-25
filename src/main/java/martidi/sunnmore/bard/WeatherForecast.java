package martidi.sunnmore.bard;

import org.jspecify.annotations.Nullable;

/**
 * Bård's weather report, read from the game's own countdowns, so it is right. It mirrors
 * ServerLevel.advanceWeatherCycle: each tick thunder is processed before rain, a countdown that
 * reaches 0 flips its flag, and a countdown at 0 is redrawn at random on the next tick. The game
 * only knows when the current weather ends, so the forecast is the next change and when it comes,
 * never how long that next weather lasts.
 */
public final class WeatherForecast {
	/** The shortest vanilla thunderstorm (ServerLevel.THUNDER_DURATION). */
	public static final int THUNDER_DURATION_MIN = 3600;
	/** The shortest vanilla gap between thunder spells (ServerLevel.THUNDER_DELAY). */
	public static final int THUNDER_DELAY_MIN = 12000;
	private static final int TICKS_PER_MINUTE = 1200;
	private static final int TICKS_PER_HOUR = 72000;

	/** The weather now, the next weather and how many ticks until it. A null next: not decided yet. */
	public record Forecast(Weather now, @Nullable Weather next, int ticks) {
	}

	public enum Unit {
		SOON,
		MINUTES,
		HOURS
	}

	/** How Bård says a time: "straks", "om 4 minutt" or "om 2 timar". */
	public record Phrase(Unit unit, int amount) {
	}

	private WeatherForecast() {
	}

	public static Forecast of(WeatherState s) {
		Weather now = s.now();
		if (s.clearTime() > 0) {
			// When a forced clear runs out, vanilla flips rain and thunder on together one tick later.
			return new Forecast(now, Weather.STORM, s.clearTime() + 1);
		}
		if (s.raining()) {
			if (s.rainTime() == 0 || s.thunderTime() == 0) {
				return unsettled(now);
			}
			if (s.thunderTime() < s.rainTime()) {
				// The thunder flips first: a storm calms to rain, or rain turns into a storm.
				return new Forecast(now, s.thundering() ? Weather.RAIN : Weather.STORM, s.thunderTime());
			}
			return new Forecast(now, Weather.CLEAR, s.rainTime());
		}
		if (s.rainTime() == 0) {
			return unsettled(now);
		}
		Boolean thunderAtRainStart = thunderFlagAt(s.rainTime(), s.thunderTime(), s.thundering());
		boolean storm = thunderAtRainStart != null && thunderAtRainStart;
		return new Forecast(now, storm ? Weather.STORM : Weather.RAIN, s.rainTime());
	}

	/** The thundering flag at tick {@code at}, or null when a random redraw could change it before then. */
	private static @Nullable Boolean thunderFlagAt(int at, int thunderTime, boolean thundering) {
		if (thunderTime > at) {
			return thundering;
		}
		if (thunderTime == at) {
			// Flips on the same tick, before the rain is processed.
			return !thundering;
		}
		// The flag has flipped (or, at 0, just did). A new countdown is drawn the tick after.
		boolean flag = thunderTime == 0 ? thundering : !thundering;
		int earliestNextFlip = thunderTime + 1 + (flag ? THUNDER_DURATION_MIN : THUNDER_DELAY_MIN);
		return at < earliestNextFlip ? flag : null;
	}

	private static Forecast unsettled(Weather now) {
		return new Forecast(now, null, 0);
	}

	public static Phrase phrase(int ticks) {
		if (ticks < TICKS_PER_MINUTE) {
			return new Phrase(Unit.SOON, 0);
		}
		int minutes = (ticks + TICKS_PER_MINUTE - 1) / TICKS_PER_MINUTE;
		if (minutes < 60) {
			return new Phrase(Unit.MINUTES, minutes);
		}
		return new Phrase(Unit.HOURS, Math.max(1, (int) Math.round(ticks / (double) TICKS_PER_HOUR)));
	}
}
