package martidi.sunnmore.glenn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StamkundeMathTest {
	@Test
	void levelsFollowTheVanillaMerchantThresholds() {
		assertEquals(1, StamkundeMath.levelForTrades(0));
		assertEquals(1, StamkundeMath.levelForTrades(1));
		assertEquals(2, StamkundeMath.levelForTrades(2));
		assertEquals(2, StamkundeMath.levelForTrades(13));
		assertEquals(3, StamkundeMath.levelForTrades(14));
		assertEquals(4, StamkundeMath.levelForTrades(30));
		assertEquals(5, StamkundeMath.levelForTrades(50));
		assertEquals(5, StamkundeMath.levelForTrades(5000));
	}

	@Test
	void xpIsFivePerTrade() {
		assertEquals(0, StamkundeMath.xpForTrades(0));
		assertEquals(50, StamkundeMath.xpForTrades(10));
		assertEquals(0, StamkundeMath.xpForTrades(-3));
	}

	@Test
	void discountIsOneSugarPerLevelAboveOne() {
		assertEquals(0, StamkundeMath.discount(1));
		assertEquals(1, StamkundeMath.discount(2));
		assertEquals(4, StamkundeMath.discount(5));
	}

	@Test
	void stamkundeAdvancementAtExactlyTenTrades() {
		assertFalse(StamkundeMath.becameStamkunde(9));
		assertTrue(StamkundeMath.becameStamkunde(10));
		assertFalse(StamkundeMath.becameStamkunde(11));
	}

	@Test
	void levelUpOnlyWhenTheLevelChanges() {
		assertTrue(StamkundeMath.leveledUp(2));
		assertFalse(StamkundeMath.leveledUp(3));
		assertTrue(StamkundeMath.leveledUp(14));
		assertFalse(StamkundeMath.leveledUp(1));
	}
}
