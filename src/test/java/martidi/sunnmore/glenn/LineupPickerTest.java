package martidi.sunnmore.glenn;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

class LineupPickerTest {
	@Test
	void picksDistinctIndicesInsideThePool() {
		for (int seed = 0; seed < 200; seed++) {
			Random random = new Random(seed);
			int[] picked = LineupPicker.pick(4, 2, random::nextInt);
			assertEquals(2, picked.length);
			assertTrue(picked[0] != picked[1]);
			assertTrue(Arrays.stream(picked).allMatch(i -> i >= 0 && i < 4));
		}
	}

	@Test
	void sameSeedGivesSameLineup() {
		int[] first = LineupPicker.pick(4, 2, new Random(42)::nextInt);
		int[] second = LineupPicker.pick(4, 2, new Random(42)::nextInt);
		assertArrayEquals(first, second);
	}

	@Test
	void askingForMoreThanThePoolReturnsTheWholePool() {
		int[] picked = LineupPicker.pick(3, 10, new Random(1)::nextInt);
		assertEquals(3, picked.length);
		assertEquals(3, Arrays.stream(picked).distinct().count());
	}

	@Test
	void everyEntryCanBePicked() {
		boolean[] seen = new boolean[4];
		Random random = new Random(7);
		for (int i = 0; i < 100; i++) {
			for (int index : LineupPicker.pick(4, 2, random::nextInt)) {
				seen[index] = true;
			}
		}
		for (boolean s : seen) {
			assertTrue(s);
		}
	}
}
