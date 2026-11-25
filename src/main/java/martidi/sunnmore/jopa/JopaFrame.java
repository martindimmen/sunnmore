package martidi.sunnmore.jopa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Finds a JOPA portal frame in one upright plane, the same shape rules as a Nether portal: an
 * empty rectangle, inside at least 2 wide and 3 tall and at most 21 by 21, with frame blocks on all
 * four sides. Corners are optional. Free of Minecraft classes so it can be unit tested: u runs
 * along the frame's width (x or z), v is the height (y).
 */
public final class JopaFrame {
	public static final int MIN_WIDTH = 2;
	public static final int MIN_HEIGHT = 3;
	public static final int MAX_SIZE = 21;

	/** The inside of a frame: the cells that become portal. */
	public record Rect(int minU, int minV, int width, int height) {
		public int maxU() {
			return this.minU + this.width - 1;
		}

		public int maxV() {
			return this.minV + this.height - 1;
		}
	}

	@FunctionalInterface
	public interface Cell {
		boolean test(int u, int v);
	}

	private JopaFrame() {
	}

	/**
	 * The frame around the empty cell (startU, startV), or empty if there is none. Fills outward
	 * from the start cell, so an area that runs off into the open is rejected as soon as it grows
	 * past the largest size.
	 */
	public static Optional<Rect> find(int startU, int startV, Cell isEmpty, Cell isFrame) {
		if (!isEmpty.test(startU, startV)) {
			return Optional.empty();
		}
		int minU = startU;
		int maxU = startU;
		int minV = startV;
		int maxV = startV;
		Set<Long> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		seen.add(key(startU, startV));
		queue.add(new int[] {startU, startV});
		int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		while (!queue.isEmpty()) {
			int[] cell = queue.poll();
			for (int[] step : steps) {
				int u = cell[0] + step[0];
				int v = cell[1] + step[1];
				if (seen.contains(key(u, v)) || !isEmpty.test(u, v)) {
					continue;
				}
				seen.add(key(u, v));
				queue.add(new int[] {u, v});
				minU = Math.min(minU, u);
				maxU = Math.max(maxU, u);
				minV = Math.min(minV, v);
				maxV = Math.max(maxV, v);
				if (maxU - minU + 1 > MAX_SIZE || maxV - minV + 1 > MAX_SIZE) {
					return Optional.empty();
				}
			}
		}
		int width = maxU - minU + 1;
		int height = maxV - minV + 1;
		if (width < MIN_WIDTH || height < MIN_HEIGHT || seen.size() != width * height) {
			return Optional.empty();
		}
		for (int u = minU; u <= maxU; u++) {
			if (!isFrame.test(u, minV - 1) || !isFrame.test(u, maxV + 1)) {
				return Optional.empty();
			}
		}
		for (int v = minV; v <= maxV; v++) {
			if (!isFrame.test(minU - 1, v) || !isFrame.test(maxU + 1, v)) {
				return Optional.empty();
			}
		}
		return Optional.of(new Rect(minU, minV, width, height));
	}

	/** The frame cells to light around a found frame: all four sides, plus the corners that are frame blocks. */
	public static List<int[]> ring(Rect rect, Cell isFrame) {
		List<int[]> cells = new ArrayList<>();
		for (int u = rect.minU(); u <= rect.maxU(); u++) {
			cells.add(new int[] {u, rect.minV() - 1});
			cells.add(new int[] {u, rect.maxV() + 1});
		}
		for (int v = rect.minV(); v <= rect.maxV(); v++) {
			cells.add(new int[] {rect.minU() - 1, v});
			cells.add(new int[] {rect.maxU() + 1, v});
		}
		int[][] corners = {
			{rect.minU() - 1, rect.minV() - 1}, {rect.maxU() + 1, rect.minV() - 1},
			{rect.minU() - 1, rect.maxV() + 1}, {rect.maxU() + 1, rect.maxV() + 1}};
		for (int[] corner : corners) {
			if (isFrame.test(corner[0], corner[1])) {
				cells.add(corner);
			}
		}
		return cells;
	}

	private static long key(int u, int v) {
		return ((long) u << 32) | (v & 0xffffffffL);
	}
}
