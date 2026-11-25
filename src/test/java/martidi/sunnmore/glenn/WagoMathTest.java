package martidi.sunnmore.glenn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WagoMathTest {
	@Test
	void pinchOneIn10() {
		assertEquals(10, WagoMath.PINCH_ONE_IN);
		assertTrue(WagoMath.isPinch(0));
		for (int roll = 1; roll < WagoMath.PINCH_ONE_IN; roll++) {
			assertFalse(WagoMath.isPinch(roll));
		}
	}

	@Test
	void rareDealOneIn5() {
		assertEquals(5, WagoMath.RARE_DEAL_ONE_IN);
		assertTrue(WagoMath.rareDealToday(0));
		for (int roll = 1; roll < WagoMath.RARE_DEAL_ONE_IN; roll++) {
			assertFalse(WagoMath.rareDealToday(roll));
		}
	}

	@Test
	void wearsOutAndGlennsLastsTwiceAsLong() {
		assertEquals(250, WagoMath.WAGO_CLICKS);
		assertEquals(2 * WagoMath.WAGO_CLICKS, WagoMath.GLENNDERMAN_WAGO_CLICKS);
	}

	@Test
	void pinchIsTwoHearts() {
		assertEquals(4.0F, WagoMath.PINCH_DAMAGE);
	}
}
