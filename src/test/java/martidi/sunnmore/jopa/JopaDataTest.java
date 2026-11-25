package martidi.sunnmore.jopa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** The JOPA data files must agree with each other and with the code. */
class JopaDataTest {
	private static JsonObject read(String path) throws IOException {
		try (InputStream in = JopaDataTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(in, path + " is missing");
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	@Test
	void dimensionUsesTheJopaTypeAndIsEmpty() throws IOException {
		JsonObject dimension = read("data/sunnmore/dimension/jopa.json");
		assertEquals("sunnmore:jopa", dimension.get("type").getAsString());
		JsonObject settings = dimension.getAsJsonObject("generator").getAsJsonObject("settings");
		assertEquals("minecraft:the_void", settings.get("biome").getAsString());
		assertFalse(settings.get("features").getAsBoolean(), "no void start platform");
	}

	@Test
	void lobbyFitsInTheDimension() throws IOException {
		JsonObject type = read("data/sunnmore/dimension_type/jopa.json");
		int minY = type.get("min_y").getAsInt();
		int top = minY + type.get("height").getAsInt();
		int lobbyFloor = JopaLobby.ORIGIN_Y;
		assertTrue(lobbyFloor - 24 > minY, "the void catch must happen above the bottom of the world");
		assertTrue(lobbyFloor + JopaLobby.HEIGHT < top);
	}

	@Test
	void bedsNeverWorkAndNeverExplode() throws IOException {
		JsonObject attributes = read("data/sunnmore/dimension_type/jopa.json").getAsJsonObject("attributes");
		JsonObject lang = read("assets/sunnmore/lang/en_us.json");
		for (String rule : new String[] {"minecraft:gameplay/bed_rule", "minecraft:gameplay/straw_bed_rule"}) {
			JsonObject bed = attributes.getAsJsonObject(rule);
			assertEquals("never", bed.get("can_sleep").getAsString(), rule);
			assertEquals("never", bed.get("can_set_spawn").getAsString(), rule);
			assertFalse(bed.has("destroy_on_use"), rule + " must not blow up the bed");
			String key = bed.getAsJsonObject("error_message").get("translate").getAsString();
			assertTrue(lang.has(key), key);
		}
	}

	@Test
	void textsExist() throws IOException {
		JsonObject lang = read("assets/sunnmore/lang/en_us.json");
		for (String key : new String[] {"block.sunnmore.jopa_ramme", "block.sunnmore.jopa_portal", "item.sunnmore.jopa_kort",
			"item.sunnmore.jopa_kort.lore", "chat.sunnmore.jopa.open", "chat.sunnmore.jopa.denied", "chat.sunnmore.jopa.wrong_place"}) {
			assertTrue(lang.has(key), key);
		}
	}

	@Test
	void recipesAndDrops() throws IOException {
		assertEquals("sunnmore:jopa_kort", read("data/sunnmore/recipe/jopa_kort.json").getAsJsonObject("result").get("id").getAsString());
		JsonObject frame = read("data/sunnmore/recipe/jopa_ramme.json");
		assertEquals("sunnmore:jopa_ramme", frame.getAsJsonObject("result").get("id").getAsString());
		assertEquals(4, frame.getAsJsonObject("result").get("count").getAsInt());
		assertTrue(read("data/sunnmore/loot_table/blocks/jopa_ramme.json").toString().contains("sunnmore:jopa_ramme"));
	}
}
