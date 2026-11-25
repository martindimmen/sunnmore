package martidi.sunnmore.bard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.bard.WeatherForecast.Forecast;
import martidi.sunnmore.bard.WeatherForecast.Phrase;
import martidi.sunnmore.bard.WeatherForecast.Unit;

class WeatherForecastTest {
	private static Forecast of(int clear, int rain, int thunder, boolean raining, boolean thundering) {
		return WeatherForecast.of(new WeatherState(clear, rain, thunder, raining, thundering));
	}

	private static void assertForecast(Weather now, Weather next, int ticks, Forecast forecast) {
		assertEquals(new Forecast(now, next, ticks), forecast);
	}

	@Test
	void clearWithRainComing() {
		assertForecast(Weather.CLEAR, Weather.RAIN, 4800, of(0, 4800, 100000, false, false));
	}

	@Test
	void clearWithStormComing() {
		// The thunder flag is already on and stays on past the rain start.
		assertForecast(Weather.CLEAR, Weather.STORM, 4800, of(0, 4800, 100000, false, true));
	}

	@Test
	void thunderFlipsOnTheSameTick() {
		// Thunder is processed before rain in the same tick, so the flag is on when the rain starts.
		assertForecast(Weather.CLEAR, Weather.STORM, 4800, of(0, 4800, 4800, false, false));
	}

	@Test
	void thunderFlipShortlyBeforeRainIsCertain() {
		// Flips on at 1000; the next flip is at least 1000 + 1 + 3600 away, after the rain starts.
		assertForecast(Weather.CLEAR, Weather.STORM, 4000, of(0, 4000, 1000, false, false));
	}

	@Test
	void thunderFlipLongBeforeRainIsUncertain() {
		// The thunder may flip again before the rain: only the rain itself is certain.
		assertForecast(Weather.CLEAR, Weather.RAIN, 100000, of(0, 100000, 1000, false, false));
	}

	@Test
	void dryWithZeroThunderCountdown() {
		// A fresh thunder delay (at least 12000) is drawn next tick, so the flag stays off.
		assertForecast(Weather.CLEAR, Weather.RAIN, 5000, of(0, 5000, 0, false, false));
	}

	@Test
	void rainEnding() {
		assertForecast(Weather.RAIN, Weather.CLEAR, 2400, of(0, 2400, 100000, true, false));
	}

	@Test
	void rainTurnsToStorm() {
		assertForecast(Weather.RAIN, Weather.STORM, 3000, of(0, 10000, 3000, true, false));
	}

	@Test
	void stormEndsIntoRain() {
		assertForecast(Weather.STORM, Weather.RAIN, 3000, of(0, 10000, 3000, true, true));
	}

	@Test
	void stormEndsIntoClear() {
		assertForecast(Weather.STORM, Weather.CLEAR, 3000, of(0, 3000, 10000, true, true));
	}

	@Test
	void forcedClearEndsInStorm() {
		// Vanilla flips both rain and thunder on one tick after a forced clear runs out.
		assertForecast(Weather.CLEAR, Weather.STORM, 6001, of(6000, 1, 1, false, false));
	}

	@Test
	void zeroRainCountdownIsUnsettled() {
		assertNull(of(0, 0, 5000, false, false).next());
		assertNull(of(0, 0, 5000, true, false).next());
	}

	@Test
	void stormWithZeroThunderIsUnsettled() {
		assertNull(of(0, 5000, 0, true, true).next());
	}

	@Test
	void phraseRounding() {
		assertEquals(Unit.SOON, WeatherForecast.phrase(1199).unit());
		assertEquals(new Phrase(Unit.MINUTES, 1), WeatherForecast.phrase(1200));
		assertEquals(new Phrase(Unit.MINUTES, 2), WeatherForecast.phrase(1201));
		assertEquals(new Phrase(Unit.MINUTES, 4), WeatherForecast.phrase(4800));
		assertEquals(new Phrase(Unit.HOURS, 1), WeatherForecast.phrase(71999));
		assertEquals(new Phrase(Unit.HOURS, 3), WeatherForecast.phrase(180000));
	}
}
