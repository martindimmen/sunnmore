package martidi.sunnmore.bard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.bard.WeatherForecast.Forecast;

class BardMathTest {
	@Test
	void boltAtMostEvery3Seconds() {
		assertTrue(BardMath.canStrike(100, 40));
		assertFalse(BardMath.canStrike(100, 41));
		assertTrue(BardMath.canStrike(0, BardMath.NEVER));
	}

	@Test
	void checksEvery2To4Minutes() {
		assertEquals(2400, BardMath.nextCheckDelay(0));
		assertEquals(4800, BardMath.nextCheckDelay(BardMath.CHECK_MAX - BardMath.CHECK_MIN));
	}

	@Test
	void stressOnceBeforeRain() {
		assertTrue(BardMath.shouldStress(new Forecast(Weather.CLEAR, Weather.RAIN, 1199), false));
		assertFalse(BardMath.shouldStress(new Forecast(Weather.CLEAR, Weather.RAIN, 1199), true));
		assertTrue(BardMath.shouldStress(new Forecast(Weather.CLEAR, Weather.STORM, 1199), false));
		assertFalse(BardMath.shouldStress(new Forecast(Weather.CLEAR, Weather.RAIN, 1200), false));
		assertFalse(BardMath.shouldStress(new Forecast(Weather.RAIN, Weather.CLEAR, 100), false));
		assertFalse(BardMath.shouldStress(new Forecast(Weather.CLEAR, null, 0), false));
	}
}
