package martidi.sunnmore.visits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class VisitMathTest {
	@Test
	void chanceClimbsAndCaps() {
		assertEquals(50, VisitMath.chanceAfterMiss(25));
		assertEquals(75, VisitMath.chanceAfterMiss(50));
		assertEquals(75, VisitMath.chanceAfterMiss(75));
	}

	@Test
	void naturalNeedsBothRolls() {
		assertTrue(VisitMath.naturalVisitHits(10, 25, 0));
		assertFalse(VisitMath.naturalVisitHits(10, 25, 1));
		assertFalse(VisitMath.naturalVisitHits(25, 25, 0));
	}

	@Test
	void lureRollOncePerDay() {
		assertTrue(VisitMath.shouldRollLure(5, VisitMath.NEVER_ROLLED, true));
		assertTrue(VisitMath.shouldRollLure(5, 4, true));
		assertFalse(VisitMath.shouldRollLure(5, 5, true));
	}

	@Test
	void lureRollWaitsForDay() {
		assertFalse(VisitMath.shouldRollLure(5, 4, false));
	}

	@Test
	void lureIsFiftyFifty() {
		assertEquals(50, VisitMath.LURE_CHANCE);
		assertTrue(VisitMath.lureHits(49));
		assertFalse(VisitMath.lureHits(50));
	}

	@Test
	void spotsStartNextToTheLure() {
		List<int[]> offsets = VisitMath.spotOffsetsNearestFirst(4, 2, 1);
		int[] first = offsets.get(0);
		assertEquals(1, first[0] * first[0] + first[1] * first[1] + first[2] * first[2]);
		for (int[] o : offsets) {
			// Never on the lure itself, and never on the two blocks straight above it.
			assertFalse(o[0] == 0 && o[2] == 0 && o[1] >= 0);
		}
		// Sorted nearest first.
		for (int i = 1; i < offsets.size(); i++) {
			int[] a = offsets.get(i - 1);
			int[] b = offsets.get(i);
			assertTrue(a[0] * a[0] + a[1] * a[1] + a[2] * a[2] <= b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
		}
	}

	@Test
	void neverSearchesHigherThanOneAboveTheLure() {
		// A plant on a counter in a low hut must not put Glenn on the roof.
		for (int[] o : VisitMath.spotOffsetsNearestFirst(4, 2, 1)) {
			assertTrue(o[1] <= 1);
		}
	}

	@Test
	void visitingUsesUpTheDaysLureRoll() {
		// While he is visiting in daylight the day's roll counts as used, so he can't leave and come straight back.
		assertEquals(7L, VisitMath.lureDayWhileVisiting(7, 5, true, true));
		assertEquals(5L, VisitMath.lureDayWhileVisiting(7, 5, true, false));
		assertEquals(5L, VisitMath.lureDayWhileVisiting(7, 5, false, true));
	}
}
