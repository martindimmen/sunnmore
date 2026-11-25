package martidi.sunnmore.bard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.bard.WeatherShopMath.Samples;

class WeatherShopMathTest {
	/** rainDelay 20000, rainDuration 13000, thunderDelay 12000, thunderDuration 4000. */
	private static final Samples S = new Samples(20000, 13000, 12000, 4000);

	private static WeatherState state(int clear, int rain, int thunder, boolean raining, boolean thundering) {
		return new WeatherState(clear, rain, thunder, raining, thundering);
	}

	@Test
	void dealsOpenOncePerDay() {
		assertTrue(WeatherShopMath.dealsOpen(5, WeatherShopMath.NEVER_CHANGED, true, true));
		assertFalse(WeatherShopMath.dealsOpen(5, 5, true, true));
		assertTrue(WeatherShopMath.dealsOpen(6, 5, true, true));
	}

	@Test
	void dealsClosedWhenFrozenOrAway() {
		assertFalse(WeatherShopMath.dealsOpen(5, 4, false, true));
		assertFalse(WeatherShopMath.dealsOpen(5, 4, true, false));
	}

	@Test
	void solNeverShorter() {
		WeatherState after = WeatherShopMath.purchase(Weather.CLEAR, state(0, 100000, 50000, false, false), S);
		assertEquals(100000, after.rainTime());
		assertEquals(0, after.clearTime());
		assertFalse(after.raining());
		assertFalse(after.thundering());
	}

	@Test
	void solFromStormStartsNaturalClear() {
		assertEquals(state(0, 20000, 12000, false, false),
			WeatherShopMath.purchase(Weather.CLEAR, state(0, 5000, 9000, true, true), S));
	}

	@Test
	void solReplacesForcedClear() {
		WeatherState after = WeatherShopMath.purchase(Weather.CLEAR, state(6000, 1, 1, false, false), S);
		assertEquals(0, after.clearTime());
		assertEquals(20000, after.rainTime());
	}

	@Test
	void regnHasNoStormInside() {
		assertEquals(state(0, 13000, 13001, true, false),
			WeatherShopMath.purchase(Weather.RAIN, state(0, 50000, 50000, false, false), S));
	}

	@Test
	void regnNeverShorter() {
		assertEquals(20000, WeatherShopMath.purchase(Weather.RAIN, state(0, 20000, 90000, true, false), S).rainTime());
	}

	@Test
	void tordenEndsTogether() {
		assertEquals(state(0, 4000, 4000, true, true),
			WeatherShopMath.purchase(Weather.STORM, state(0, 50000, 50000, false, false), S));
	}

	@Test
	void tordenNeverShorter() {
		WeatherState after = WeatherShopMath.purchase(Weather.STORM, state(0, 9000, 7000, true, true), S);
		assertEquals(7000, after.rainTime());
		assertEquals(7000, after.thunderTime());
	}
}
