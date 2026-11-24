package martidi.sunnmore.glenn;

import java.util.function.IntUnaryOperator;

/** Picks distinct pool indices for Glenn's daily deals. Free of Minecraft classes so it can be unit tested. */
public final class LineupPicker {
	private LineupPicker() {
	}

	/**
	 * @param poolSize how many entries the pool has
	 * @param count    how many to pick; capped at poolSize
	 * @param nextInt  returns a random int in [0, bound) for the given bound
	 */
	public static int[] pick(int poolSize, int count, IntUnaryOperator nextInt) {
		int[] indices = new int[poolSize];
		for (int i = 0; i < poolSize; i++) {
			indices[i] = i;
		}
		int n = Math.min(count, poolSize);
		// Partial Fisher-Yates shuffle: the first n slots end up as a random distinct pick.
		for (int i = 0; i < n; i++) {
			int j = i + nextInt.applyAsInt(poolSize - i);
			int tmp = indices[i];
			indices[i] = indices[j];
			indices[j] = tmp;
		}
		int[] picked = new int[n];
		System.arraycopy(indices, 0, picked, 0, n);
		return picked;
	}
}
