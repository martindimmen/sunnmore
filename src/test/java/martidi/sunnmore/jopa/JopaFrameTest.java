package martidi.sunnmore.jopa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.jopa.JopaFrame.Rect;

/** Frames drawn as text: F = frame block, . = empty, anything else = something in the way. Row 0 is the top. */
class JopaFrameTest {
	private static Optional<Rect> find(int startU, int startV, String... rows) {
		return JopaFrame.find(startU, startV, (u, v) -> at(rows, u, v) == '.', (u, v) -> at(rows, u, v) == 'F');
	}

	/** v grows upwards (like y), so the last text row is v = 0. Outside the drawing is ' '. */
	private static char at(String[] rows, int u, int v) {
		int row = rows.length - 1 - v;
		return row >= 0 && row < rows.length && u >= 0 && u < rows[row].length() ? rows[row].charAt(u) : ' ';
	}

	@Test
	void smallestFrameWithCorners() {
		Optional<Rect> rect = find(1, 1,
			"FFFF",
			"F..F",
			"F..F",
			"F..F",
			"FFFF");
		assertEquals(Optional.of(new Rect(1, 1, 2, 3)), rect);
	}

	@Test
	void cornersAreOptional() {
		assertEquals(Optional.of(new Rect(1, 1, 2, 3)), find(2, 3,
			" FF ",
			"F..F",
			"F..F",
			"F..F",
			" FF "));
	}

	@Test
	void startAnywhereInside() {
		String[] frame = {
			"FFFFF",
			"F...F",
			"F...F",
			"F...F",
			"F...F",
			"FFFFF"};
		for (int u = 1; u <= 3; u++) {
			for (int v = 1; v <= 4; v++) {
				assertEquals(Optional.of(new Rect(1, 1, 3, 4)), find(u, v, frame));
			}
		}
	}

	@Test
	void missingFrameBlockFails() {
		assertTrue(find(1, 1,
			"FFFF",
			"F..F",
			"F...",
			"F..F",
			"FFFF").isEmpty());
	}

	@Test
	void somethingInsideFails() {
		assertTrue(find(1, 1,
			"FFFF",
			"F..F",
			"F.xF",
			"F..F",
			"FFFF").isEmpty());
	}

	@Test
	void tooSmallFails() {
		assertTrue(find(1, 1,
			"FFF",
			"F.F",
			"F.F",
			"F.F",
			"FFF").isEmpty());
		assertTrue(find(1, 1,
			"FFFF",
			"F..F",
			"F..F",
			"FFFF").isEmpty());
	}

	@Test
	void tooBigFails() {
		// 22 wide inside: one more than the largest Nether-size portal.
		String wall = "F".repeat(24);
		String inside = "F" + ".".repeat(22) + "F";
		assertTrue(find(1, 1, wall, inside, inside, inside, wall).isEmpty());
	}

	@Test
	void openToTheSkyFails() {
		// No top row: the empty area runs off into the open and must not be accepted.
		assertTrue(find(1, 1,
			"F..F",
			"F..F",
			"F..F",
			"FFFF").isEmpty());
	}

	@Test
	void ringLightsCornersOnlyWhenTheyAreFrame() {
		Rect rect = new Rect(1, 1, 2, 3);
		Set<Long> ring = new HashSet<>();
		for (int[] cell : JopaFrame.ring(rect, (u, v) -> !(u == 0 && v == 0))) {
			ring.add(((long) cell[0] << 32) | (cell[1] & 0xffffffffL));
		}
		// 10 sides + 3 corners (the bottom-left corner is not a frame block).
		assertEquals(13, ring.size());
		assertTrue(!ring.contains(0L));
	}
}
