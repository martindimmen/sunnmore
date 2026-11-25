package martidi.sunnmore.jopa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.jopa.JopaFrame.Rect;
import martidi.sunnmore.jopa.JopaLobby.Cell;
import martidi.sunnmore.jopa.JopaLobby.Part;

class JopaLobbyTest {
	private static Map<String, Part> parts() {
		Map<String, Part> map = new HashMap<>();
		for (Cell cell : JopaLobby.layout()) {
			Part previous = map.put(cell.x() + "," + cell.y() + "," + cell.z(), cell.part());
			assertEquals(null, previous, "two parts at " + cell);
		}
		return map;
	}

	private static Part at(Map<String, Part> parts, int x, int y, int z) {
		return parts.get(x + "," + y + "," + z);
	}

	@Test
	void coversTheWholeBox() {
		int side = 2 * JopaLobby.HALF + 1;
		assertEquals(side * (JopaLobby.HEIGHT + 1) * side, parts().size());
		List<Cell> cells = JopaLobby.layout();
		assertTrue(cells.stream().allMatch(c -> JopaLobby.contains(c.x(), c.y(), c.z())));
	}

	@Test
	void closedOnEverySide() {
		// Nobody can walk or fall out into the void.
		Map<String, Part> parts = parts();
		int h = JopaLobby.HALF;
		for (Cell cell : JopaLobby.layout()) {
			boolean surface = Math.abs(cell.x()) == h || Math.abs(cell.z()) == h || cell.y() == 0 || cell.y() == JopaLobby.HEIGHT;
			if (surface) {
				assertNotEquals(Part.AIR, cell.part(), "hole at " + cell);
				assertNotEquals(Part.PORTAL, cell.part(), "portal on the outside at " + cell);
			}
		}
		assertEquals(Part.AIR, at(parts, 0, 2, 0));
	}

	@Test
	void returnPortalIsAFullFrame() {
		Map<String, Part> parts = parts();
		int z = JopaLobby.PORTAL_Z;
		Optional<Rect> rect = JopaFrame.find(0, 1,
			(u, v) -> at(parts, u, v, z) == Part.PORTAL,
			(u, v) -> at(parts, u, v, z) == Part.FRAME);
		assertEquals(Optional.of(new Rect(-1, 1, 2, 3)), rect);
	}

	@Test
	void portalHasAWallBehindIt() {
		// The portal has no collision: without a wall behind it you could walk through into the void.
		Map<String, Part> parts = parts();
		for (Cell cell : JopaLobby.layout()) {
			if (cell.part() == Part.PORTAL) {
				assertEquals(Part.WALL, at(parts, cell.x(), cell.y(), cell.z() - 1), "behind " + cell);
			}
		}
	}

	@Test
	void arrivalSpotIsSafeAndFacesThePortal() {
		Map<String, Part> parts = parts();
		int[] s = JopaLobby.SPAWN;
		assertEquals(Part.AIR, at(parts, s[0], s[1], s[2]));
		assertEquals(Part.AIR, at(parts, s[0], s[1] + 1, s[2]));
		assertEquals(Part.FLOOR, at(parts, s[0], s[1] - 1, s[2]));
		assertTrue(s[2] > JopaLobby.PORTAL_Z + 1, "not inside or right next to the portal");
		assertEquals(180.0F, JopaLobby.SPAWN_YAW);
	}
}
