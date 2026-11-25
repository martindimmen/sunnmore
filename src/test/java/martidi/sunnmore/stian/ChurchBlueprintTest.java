package martidi.sunnmore.stian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import martidi.sunnmore.stian.ChurchBlueprint.Placement;

class ChurchBlueprintTest {
	static ChurchBlueprint church() throws IOException {
		try (InputStream in = ChurchBlueprintTest.class.getClassLoader().getResourceAsStream("data/sunnmore/church/stian_kyrkje.json")) {
			assertNotNull(in, "stian_kyrkje.json is not on the classpath");
			return ChurchBlueprint.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		}
	}

	@Test
	void parsesTheChurch() throws IOException {
		ChurchBlueprint church = church();
		assertEquals(7, church.width());
		assertEquals(15, church.height());
		assertEquals(11, church.depth());
	}

	@Test
	void materialsMatchTheDesign() throws IOException {
		ChurchBlueprint church = church();
		assertEquals(Map.of(
			"minecraft:birch_planks", 172,
			"minecraft:birch_log", 40,
			"minecraft:birch_stairs", 12,
			"minecraft:birch_door", 1,
			"minecraft:cobblestone", 51,
			"minecraft:cobblestone_stairs", 68,
			"minecraft:cobblestone_wall", 1,
			"minecraft:glass_pane", 15,
			"minecraft:lantern", 1,
			"minecraft:torch", 4
		), church.materials());
		// The door's upper half is a placement too, with no item of its own.
		assertEquals(366, church.buildOrder().size());
	}

	@Test
	void fragileBlocksComeLast() throws IOException {
		List<Placement> order = church().buildOrder();
		boolean seenLast = false;
		for (Placement p : order) {
			if (p.last()) {
				seenLast = true;
			} else {
				assertFalse(seenLast, "a normal block comes after a fragile one: " + p);
			}
			String id = ChurchBlueprint.blockId(p.block());
			if (id.equals("minecraft:wall_torch") || id.equals("minecraft:lantern") || id.equals("minecraft:birch_door")) {
				assertTrue(p.last(), "must be built last: " + p);
			}
		}
	}

	@Test
	void bottomUp() throws IOException {
		int y = -1;
		for (Placement p : church().buildOrder()) {
			if (p.last()) {
				break;
			}
			assertTrue(p.y() >= y, "went down a layer at " + p);
			y = p.y();
		}
	}

	@Test
	void rejectsUnknownSymbol() {
		String json = "{\"legend\":{\"C\":{\"block\":\"minecraft:cobblestone\",\"item\":\"minecraft:cobblestone\",\"last\":false}},"
			+ "\"layers\":[[\"CX\"]]}";
		assertThrows(IllegalArgumentException.class, () -> ChurchBlueprint.parse(json));
	}

	@Test
	void rejectsRaggedRows() {
		String json = "{\"legend\":{\"C\":{\"block\":\"minecraft:cobblestone\",\"item\":\"minecraft:cobblestone\",\"last\":false}},"
			+ "\"layers\":[[\"CC\",\"C\"]]}";
		assertThrows(IllegalArgumentException.class, () -> ChurchBlueprint.parse(json));
	}

	@Test
	void blockIdDropsTheState() {
		assertEquals("minecraft:birch_stairs", ChurchBlueprint.blockId("minecraft:birch_stairs[facing=north]"));
		assertEquals("minecraft:cobblestone", ChurchBlueprint.blockId("minecraft:cobblestone"));
	}
}
