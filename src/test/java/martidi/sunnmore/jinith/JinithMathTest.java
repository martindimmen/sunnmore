package martidi.sunnmore.jinith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JinithMathTest {
	private static final float EPS = 1e-6F;

	@Test
	void capKeepsHalfHeart() {
		assertEquals(8.0F, JinithMath.cappedDamage(8.0F, 20.0F, 0.0F), EPS);
		assertEquals(4.0F, JinithMath.cappedDamage(8.0F, 5.0F, 0.0F), EPS);
		assertEquals(0.5F, JinithMath.cappedDamage(8.0F, 1.5F, 0.0F), EPS);
	}

	@Test
	void capCountsAbsorption() {
		assertEquals(8.0F, JinithMath.cappedDamage(8.0F, 5.0F, 4.0F), EPS);
		assertEquals(8.0F, JinithMath.cappedDamage(20.0F, 5.0F, 4.0F), EPS);
	}

	@Test
	void capIsZeroAtHalfHeart() {
		assertEquals(0.0F, JinithMath.cappedDamage(8.0F, 1.0F, 0.0F), EPS);
		assertEquals(0.0F, JinithMath.cappedDamage(8.0F, 0.5F, 0.0F), EPS);
	}

	@Test
	void capNeverRaisesDamage() {
		for (float base : new float[] {0.5F, 2.0F, 8.0F}) {
			for (float health : new float[] {1.5F, 10.0F, 20.0F}) {
				assertTrue(JinithMath.cappedDamage(base, health, 0.0F) <= base);
			}
		}
	}

	@Test
	void patchUpAtHalfHeart() {
		assertTrue(JinithMath.shouldPatchUp(1.0F));
		assertTrue(JinithMath.shouldPatchUp(0.7F));
		assertFalse(JinithMath.shouldPatchUp(1.5F));
	}

	@Test
	void giveUp() {
		double near = 10.0 * 10.0;
		assertTrue(JinithMath.shouldGiveUp(100, 100, near, false, true));
		assertTrue(JinithMath.shouldGiveUp(100, 100, 97.0 * 97.0, true, true));
		// He hunts for two minutes without landing a hit before he gives up.
		assertTrue(JinithMath.shouldGiveUp(2501, 100, near, true, true));
		assertFalse(JinithMath.shouldGiveUp(2500, 100, near, true, true));
		// His punch launches people about 40 blocks, and he is fast: he chases far.
		assertFalse(JinithMath.shouldGiveUp(100, 100, 96.0 * 96.0, true, true));
		assertFalse(JinithMath.shouldGiveUp(100, 100, 64.0 * 64.0, true, true));
	}

	@Test
	void giveUpWhenUnattackable() {
		double near = 4.0;
		assertTrue(JinithMath.shouldGiveUp(161, 100, near, true, false));
		assertFalse(JinithMath.shouldGiveUp(159, 100, near, true, false));
	}

	@Test
	void renewRegenOnlyNearEnd() {
		assertTrue(JinithMath.needsRegenRenewal(false, 0));
		assertTrue(JinithMath.needsRegenRenewal(true, 19));
		assertFalse(JinithMath.needsRegenRenewal(true, 80));
	}

	@Test
	void renewRegenBeforeItLapsesBetweenChecks() {
		// The aura checks every 40 ticks: an effect with 40 left would run out before the next check.
		assertTrue(JinithMath.needsRegenRenewal(true, 40));
		assertTrue(JinithMath.needsRegenRenewal(true, 20));
		assertFalse(JinithMath.needsRegenRenewal(true, 41));
	}

	@Test
	void cappedHitIsTheFinishingBlow() {
		// Resistance or absorption rounding can leave 1.05 health after a capped hit: still the end of the fight.
		assertTrue(JinithMath.isFinishingBlow(8.0F, 4.0F, 1.05F));
		assertTrue(JinithMath.isFinishingBlow(8.0F, 8.0F, 1.0F));
		assertFalse(JinithMath.isFinishingBlow(8.0F, 8.0F, 6.0F));
	}

	@Test
	void flexLastsFiftyTicks() {
		assertEquals(50L, JinithMath.FLEX_TICKS);
		assertFalse(JinithMath.isFlexOver(149, 100));
		assertTrue(JinithMath.isFlexOver(150, 100));
	}

	@Test
	void protectionCoversTheWholeFlexAndAfter() {
		// Protected from the start of the flex until the usual window after the patch-up.
		long until = JinithMath.flexProtectionUntil(100);
		assertTrue(JinithMath.isProtected(100 + JinithMath.FLEX_TICKS, until));
		assertTrue(JinithMath.isProtected(100 + JinithMath.FLEX_TICKS + JinithMath.SAFETY_TICKS - 1, until));
		assertFalse(JinithMath.isProtected(100 + JinithMath.FLEX_TICKS + JinithMath.SAFETY_TICKS, until));
	}

	@Test
	void safetyCoversTheLongFlight() {
		assertEquals(100L, JinithMath.SAFETY_TICKS);
	}

	@Test
	void launchFliesAwayFromHimAtFullSpeed() {
		double[] v = JinithMath.launch(3.0, 4.0);
		assertEquals(JinithMath.LAUNCH_SPEED, Math.hypot(v[0], v[2]), 1e-9);
		assertEquals(JinithMath.LAUNCH_LIFT, v[1], 1e-9);
		assertTrue(v[0] > 0 && v[2] > 0);
		assertEquals(0.6, v[0] / JinithMath.LAUNCH_SPEED, 1e-9);
	}

	@Test
	void launchStillWorksWhenStandingInsideHim() {
		double[] v = JinithMath.launch(0.0, 0.0);
		assertEquals(JinithMath.LAUNCH_SPEED, Math.hypot(v[0], v[2]), 1e-9);
	}

	@Test
	void launchIsReallyFar() {
		assertTrue(JinithMath.LAUNCH_SPEED >= 4.0);
	}

	@Test
	void sameLineToSamePlayerOnlyEveryFiveSeconds() {
		assertTrue(JinithMath.canRepeatLine(1000, JinithMath.NEVER));
		assertFalse(JinithMath.canRepeatLine(1099, 1000));
		assertTrue(JinithMath.canRepeatLine(1100, 1000));
	}

	@Test
	void scareLastsFiveSeconds() {
		assertEquals(100L, JinithMath.SCARE_TICKS);
		assertFalse(JinithMath.isScareOver(199, 100));
		assertTrue(JinithMath.isScareOver(200, 100));
	}

	@Test
	void protectionCoversScareFlexAndAfter() {
		long until = JinithMath.scareProtectionUntil(100);
		assertEquals(100 + JinithMath.SCARE_TICKS + JinithMath.FLEX_TICKS + JinithMath.SAFETY_TICKS, until);
	}

	@Test
	void punchesEveryHalfSecond() {
		// 10 ticks: the fastest a player can take damage again (their hurt cooldown).
		assertEquals(10L, JinithMath.PUNCH_INTERVAL_TICKS);
		assertTrue(JinithMath.canPunchAgain(500, JinithMath.NEVER));
		assertFalse(JinithMath.canPunchAgain(509, 500));
		assertTrue(JinithMath.canPunchAgain(510, 500));
	}

	@Test
	void chasesSuperFast() {
		assertTrue(JinithMath.CHASE_SPEED >= 2.0);
	}

	@Test
	void protectedWindow() {
		assertTrue(JinithMath.isProtected(100, 160));
		assertFalse(JinithMath.isProtected(160, 160));
	}
}
