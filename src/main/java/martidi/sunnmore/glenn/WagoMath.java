package martidi.sunnmore.glenn;

/**
 * The Wago's numbers. Free of Minecraft classes so it can be unit tested. The pinch can kill a
 * player on low health: that is Martin's call (see docs/glenn-design.md, "Wago").
 */
public final class WagoMath {
	/** 1 click in 10 pinches your finger. */
	public static final int PINCH_ONE_IN = 10;
	/** Two hearts. Armor and Protection don't help (see the damage type tags). */
	public static final float PINCH_DAMAGE = 4.0F;
	/** Glenn adds his own Wago as a fifth deal on 1 morning in 5. */
	public static final int RARE_DEAL_ONE_IN = 5;

	private WagoMath() {
	}

	/** @param roll a random int in [0, PINCH_ONE_IN) */
	public static boolean isPinch(int roll) {
		return roll == 0;
	}

	/** @param roll a random int in [0, RARE_DEAL_ONE_IN) */
	public static boolean rareDealToday(int roll) {
		return roll == 0;
	}
}
