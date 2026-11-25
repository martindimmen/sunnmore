package martidi.sunnmore.stian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StianMathTest {
	@Test
	void canThrowEvery3s() {
		assertTrue(StianMath.canThrow(100, 40));
		assertFalse(StianMath.canThrow(100, 41));
		assertTrue(StianMath.canThrow(0, StianMath.NEVER));
	}

	@Test
	void mistakeOneIn20() {
		assertTrue(StianMath.isMistake(0));
		for (int roll = 1; roll < StianMath.MISTAKE_ONE_IN; roll++) {
			assertFalse(StianMath.isMistake(roll));
		}
	}

	@Test
	void placeTicks() {
		assertEquals(10, StianMath.placeTicks(false));
		assertEquals(5, StianMath.placeTicks(true));
	}

	@Test
	void betweenBounds() {
		assertEquals(400, StianMath.between(400, 800, 0));
		assertEquals(800, StianMath.between(400, 800, 400));
	}
}
