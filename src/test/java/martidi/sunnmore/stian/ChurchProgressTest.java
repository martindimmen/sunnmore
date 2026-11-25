package martidi.sunnmore.stian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.stian.ChurchBlueprint.Placement;
import martidi.sunnmore.stian.ChurchProgress.Missing;
import martidi.sunnmore.stian.ChurchProgress.Place;

class ChurchProgressTest {
	private static final String PLANKS = "minecraft:birch_planks";
	private static final String COBBLE = "minecraft:cobblestone";

	private static Placement p(int x, String item) {
		return new Placement(x, 0, 0, item == null ? "minecraft:birch_door[half=upper]" : item, item, false);
	}

	private static final List<Placement> ORDER = List.of(p(0, COBBLE), p(1, PLANKS), p(2, PLANKS), p(3, PLANKS));

	@Test
	void firstNotDone() {
		Set<Placement> done = new HashSet<>(List.of(ORDER.get(0)));
		ChurchProgress.Step step = ChurchProgress.next(ORDER, done::contains, item -> 10);
		assertEquals(new Place(ORDER.get(1)), step);
	}

	@Test
	void missingCountsWhatIsLeft() {
		Set<Placement> done = new HashSet<>(List.of(ORDER.get(0)));
		ChurchProgress.Step step = ChurchProgress.next(ORDER, done::contains, item -> 0);
		assertEquals(new Missing(PLANKS, 3), step);
	}

	@Test
	void finishedWhenAllDone() {
		assertInstanceOf(ChurchProgress.Finished.class, ChurchProgress.next(ORDER, p -> true, item -> 0));
	}

	@Test
	void takeOnlyWhenPlaceable() {
		// Planks run out: never hand out a planks placement with none in stock.
		ChurchProgress.Step step = ChurchProgress.next(ORDER, p -> false, item -> item.equals(COBBLE) ? 1 : 0);
		assertEquals(new Place(ORDER.get(0)), step);
		Set<Placement> done = new HashSet<>(List.of(ORDER.get(0)));
		assertInstanceOf(Missing.class, ChurchProgress.next(ORDER, done::contains, item -> item.equals(COBBLE) ? 1 : 0));
	}

	@Test
	void skipsNullItems() {
		List<Placement> order = List.of(p(0, null), p(1, PLANKS));
		assertEquals(new Place(order.get(1)), ChurchProgress.next(order, p -> false, item -> 5));
	}

	@Test
	void stillNeededPerItem() {
		Set<Placement> done = new HashSet<>(List.of(ORDER.get(1)));
		assertEquals(Map.of(COBBLE, 1, PLANKS, 2), ChurchProgress.stillNeeded(ORDER, done::contains));
	}
}
