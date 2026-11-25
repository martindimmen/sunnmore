package martidi.sunnmore.bard;

import martidi.sunnmore.bard.WeatherForecast.Forecast;
import org.jspecify.annotations.Nullable;

/** Bård's timers as plain numbers, free of Minecraft classes so they can be unit tested. */
public final class BardMath {
	/** At most one lightning bolt every 3 seconds per Bård, so spam-clicking is not a strobe light. */
	public static final int BOLT_COOLDOWN = 60;
	/** "Never struck": far enough back to allow a bolt, without the overflow Long.MIN_VALUE caused before. */
	public static final long NEVER = -BOLT_COOLDOWN;
	/** He checks his phone every 2 to 4 minutes. */
	public static final int CHECK_MIN = 2400;
	public static final int CHECK_MAX = 4800;
	/** How long he stares at the phone before he says the forecast. */
	public static final int PHONE_TICKS = 60;
	/** Rain this close makes him stressed. */
	public static final int STRESS_BELOW = 1200;

	private BardMath() {
	}

	public static boolean canStrike(long now, long lastBolt) {
		return now - lastBolt >= BOLT_COOLDOWN;
	}

	/** roll is in [0, CHECK_MAX - CHECK_MIN]. */
	public static int nextCheckDelay(int roll) {
		return CHECK_MIN + roll;
	}

	/**
	 * Whether an open shop screen must be re-sent: the weather deals it shows (open or sold out)
	 * no longer match the server, after a new day or when the weather was frozen. Null: no screen.
	 */
	public static boolean shopNeedsRefresh(@Nullable Boolean shownOpen, boolean openNow) {
		return shownOpen != null && shownOpen != openNow;
	}

	/** Once per dry spell, when rain or a storm is less than a minute away. */
	public static boolean shouldStress(Forecast forecast, boolean alreadyStressed) {
		return !alreadyStressed
			&& forecast.now() == Weather.CLEAR
			&& (forecast.next() == Weather.RAIN || forecast.next() == Weather.STORM)
			&& forecast.ticks() < STRESS_BELOW;
	}
}
