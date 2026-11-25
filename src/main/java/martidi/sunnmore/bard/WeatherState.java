package martidi.sunnmore.bard;

/**
 * A copy of the game's weather countdowns (vanilla WeatherData), in ticks. clearTime is the
 * forced-clear timer that /weather clear sets; rainTime and thunderTime count down to the next
 * flip of the raining and thundering flags.
 */
public record WeatherState(int clearTime, int rainTime, int thunderTime, boolean raining, boolean thundering) {
	/** What players see right now: a storm needs both rain and thunder. */
	public Weather now() {
		if (!this.raining) {
			return Weather.CLEAR;
		}
		return this.thundering ? Weather.STORM : Weather.RAIN;
	}
}
