package martidi.sunnmore.jinith;

/**
 * Jinith's rules as plain numbers, free of Minecraft classes so they can be unit tested:
 * how hard he may hit, when he patches you up, when he gives up, and the safety window.
 */
public final class JinithMath {
	/** Half a heart: his punches never take a player below this. */
	public static final float HALF_HEART = 1.0F;
	/** Far: his own launches send people ~40 blocks, and he is fast enough to come after them. */
	public static final double GIVE_UP_DISTANCE = 96.0;
	/** He gives up when he hasn't landed a hit for this long (two minutes). */
	public static final long GIVE_UP_TICKS = 2400;
	/** He gives up quickly on someone he can't hit (Creative, Spectator, Peaceful). */
	public static final long UNATTACKABLE_GIVE_UP_TICKS = 60;
	/** After each punch nothing can take the player below half a heart for this long (covers the long flight). */
	public static final long SAFETY_TICKS = 100;
	/** The heal aura checks players this often. */
	public static final int HEAL_INTERVAL_TICKS = 40;
	/**
	 * Renew his Regeneration only when it would run out before the next check, so the effect never
	 * lapses (the icon would blink) and its heal timer is not reset while time is left.
	 */
	public static final int REGEN_RENEW_BELOW = HEAL_INTERVAL_TICKS + 1;

	private JinithMath() {
	}

	/** Damage that leaves the player at exactly half a heart at the lowest; absorption hearts count as health. */
	public static float cappedDamage(float base, float health, float absorption) {
		return Math.max(0.0F, Math.min(base, health + absorption - HALF_HEART));
	}

	public static boolean shouldPatchUp(float healthAfter) {
		return healthAfter <= HALF_HEART;
	}

	/**
	 * A punch ends the fight when the cap kicked in (it was aimed at half a heart), even if
	 * Resistance or float rounding leaves the player a hair above it.
	 */
	public static boolean isFinishingBlow(float base, float capped, float healthAfter) {
		return capped < base || shouldPatchUp(healthAfter);
	}

	public static boolean shouldGiveUp(long now, long lastProgressTick, double distanceSqr, boolean sameLevel, boolean attackable) {
		long patience = attackable ? GIVE_UP_TICKS : UNATTACKABLE_GIVE_UP_TICKS;
		return !sameLevel
			|| distanceSqr > GIVE_UP_DISTANCE * GIVE_UP_DISTANCE
			|| now - lastProgressTick > patience;
	}

	public static boolean needsRegenRenewal(boolean hasRegen, int remainingTicks) {
		return !hasRegen || remainingTicks < REGEN_RENEW_BELOW;
	}

	public static boolean isProtected(long now, long protectedUntil) {
		return now < protectedUntil;
	}

	/**
	 * His punch sets the victim's speed directly (blocks per tick): far past the vanilla knockback
	 * cap. About 40 blocks of flight on open ground.
	 */
	public static final double LAUNCH_SPEED = 4.0;
	public static final double LAUNCH_LIFT = 0.6;

	/** Velocity {x, y, z} that sends the player flying straight away from him. */
	public static double[] launch(double dxFromHim, double dzFromHim) {
		double length = Math.hypot(dxFromHim, dzFromHim);
		if (length < 1.0E-4) {
			// Standing inside him: just pick a direction.
			return new double[] {LAUNCH_SPEED, LAUNCH_LIFT, 0.0};
		}
		return new double[] {dxFromHim / length * LAUNCH_SPEED, LAUNCH_LIFT, dzFromHim / length * LAUNCH_SPEED};
	}

	/** "Never said" marker for line timestamps (not Long.MIN_VALUE, which overflows in now - last). */
	public static final long NEVER = -1L;
	/** The same line to the same player at most this often, even when forced: no chat spam. */
	public static final long LINE_REPEAT_TICKS = 100;

	public static boolean canRepeatLine(long now, long lastSameLine) {
		return lastSameLine == NEVER || now - lastSameLine >= LINE_REPEAT_TICKS;
	}

	/** Chase speed multiplier on his base speed: super fast once he is angry. */
	public static final double CHASE_SPEED = 2.0;
	/** A punch every half second: the fastest a player can take damage again (their hurt cooldown). */
	public static final long PUNCH_INTERVAL_TICKS = 10;

	public static boolean canPunchAgain(long now, long lastPunch) {
		return lastPunch == NEVER || now - lastPunch >= PUNCH_INTERVAL_TICKS;
	}

	/** At half a heart he keeps chasing and punching (no damage) this long, to scare them, before the flex. */
	public static final long SCARE_TICKS = 100;

	public static boolean isScareOver(long now, long scareStartedAt) {
		return now - scareStartedAt >= SCARE_TICKS;
	}

	/** The beaten player stays protected through the scare, the flex and the window after the patch-up. */
	public static long scareProtectionUntil(long scareStartedAt) {
		return scareStartedAt + SCARE_TICKS + FLEX_TICKS + SAFETY_TICKS;
	}

	/** How long he flexes over a beaten player before patching them up. */
	public static final long FLEX_TICKS = 50;

	public static boolean isFlexOver(long now, long flexStartedAt) {
		return now - flexStartedAt >= FLEX_TICKS;
	}

	/** The beaten player stays protected through the flex and the usual window after the patch-up. */
	public static long flexProtectionUntil(long flexStartedAt) {
		return flexStartedAt + FLEX_TICKS + SAFETY_TICKS;
	}
}
