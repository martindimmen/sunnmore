package martidi.sunnmore.glenn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** The Wago's data files must agree with each other: damage type, death messages, tags, recipe and names. */
class WagoDataTest {
	private static final String LANG = "assets/sunnmore/lang/en_us.json";

	private static JsonObject read(String path) throws IOException {
		try (InputStream in = WagoDataTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(in, path + " is missing");
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static Set<String> strings(JsonElement array) {
		Set<String> values = new HashSet<>();
		array.getAsJsonArray().forEach(value -> values.add(value.getAsString()));
		return values;
	}

	@Test
	void damageTypeMatchesDeathMessages() throws IOException {
		JsonObject damageType = read("data/sunnmore/damage_type/wago_pinch.json");
		assertEquals("sunnmore.wago", damageType.get("message_id").getAsString());
		JsonObject lang = read(LANG);
		assertTrue(lang.has("death.attack.sunnmore.wago"));
		// Used when someone else hurt the player shortly before the fatal pinch.
		assertTrue(lang.has("death.attack.sunnmore.wago.player"));
	}

	@Test
	void pinchIgnoresArmor() throws IOException {
		for (String tag : new String[] {"bypasses_armor", "bypasses_enchantments"}) {
			JsonObject json = read("data/minecraft/tags/damage_type/" + tag + ".json");
			assertTrue(strings(json.get("values")).contains("sunnmore:wago_pinch"), tag);
		}
	}

	@Test
	void recipeIsIronAndGlass() throws IOException {
		JsonObject recipe = read("data/sunnmore/recipe/wago.json");
		assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
		assertEquals(Set.of("minecraft:iron_ingot", "minecraft:glass"), strings(recipe.get("ingredients")));
		assertEquals(2, recipe.getAsJsonArray("ingredients").size());
		assertEquals("sunnmore:wago", recipe.getAsJsonObject("result").get("id").getAsString());
	}

	@Test
	void itemNamesExist() throws IOException {
		JsonObject lang = read(LANG);
		for (String key : new String[] {"item.sunnmore.wago", "item.sunnmore.wago.lore",
			"item.sunnmore.glennderman_wago", "item.sunnmore.glennderman_wago.lore"}) {
			assertTrue(lang.has(key), key);
		}
	}
}
