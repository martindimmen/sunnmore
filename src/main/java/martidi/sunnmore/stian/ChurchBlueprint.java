package martidi.sunnmore.stian;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jspecify.annotations.Nullable;

/**
 * Stian's church as a drawing: a legend of symbols plus one text grid per layer (see
 * data/sunnmore/church/stian_kyrkje.json). Plain Java, so the build order and the materials list
 * can be unit tested. '.' and ' ' mean "leave as it is".
 */
public final class ChurchBlueprint {
	/** One block of the church, in blueprint coordinates: x across, y up, z from the front (0) backwards. */
	public record Placement(int x, int y, int z, String block, @Nullable String item, boolean last) {
	}

	private record Symbol(String block, @Nullable String item, boolean last) {
	}

	private final int width;
	private final int height;
	private final int depth;
	private final List<Placement> buildOrder;

	private ChurchBlueprint(int width, int height, int depth, List<Placement> buildOrder) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.buildOrder = buildOrder;
	}

	public static ChurchBlueprint parse(String json) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		Map<Character, Symbol> legend = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("legend").entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new IllegalArgumentException("legend keys are single characters: " + entry.getKey());
			}
			JsonObject value = entry.getValue().getAsJsonObject();
			JsonElement item = value.get("item");
			legend.put(entry.getKey().charAt(0), new Symbol(value.get("block").getAsString(),
				item == null || item.isJsonNull() ? null : item.getAsString(),
				value.has("last") && value.get("last").getAsBoolean()));
		}
		JsonArray layers = root.getAsJsonArray("layers");
		int width = -1;
		int depth = -1;
		List<Placement> placements = new ArrayList<>();
		for (int y = 0; y < layers.size(); y++) {
			JsonArray rows = layers.get(y).getAsJsonArray();
			if (depth == -1) {
				depth = rows.size();
			} else if (rows.size() != depth) {
				throw new IllegalArgumentException("layer " + y + " has " + rows.size() + " rows, expected " + depth);
			}
			for (int z = 0; z < rows.size(); z++) {
				String row = rows.get(z).getAsString();
				if (width == -1) {
					width = row.length();
				} else if (row.length() != width) {
					throw new IllegalArgumentException("layer " + y + " row " + z + " is " + row.length() + " wide, expected " + width);
				}
				for (int x = 0; x < row.length(); x++) {
					char c = row.charAt(x);
					if (c == '.' || c == ' ') {
						continue;
					}
					Symbol symbol = legend.get(c);
					if (symbol == null) {
						throw new IllegalArgumentException("unknown symbol '" + c + "' at x=" + x + " y=" + y + " z=" + z);
					}
					placements.add(new Placement(x, y, z, symbol.block(), symbol.item(), symbol.last()));
				}
			}
		}
		// Bottom up; anything that needs support (torches, lantern, door) after everything else.
		placements.sort(Comparator.comparing(Placement::last)
			.thenComparingInt(Placement::y)
			.thenComparingInt(Placement::z)
			.thenComparingInt(Placement::x));
		return new ChurchBlueprint(Math.max(width, 0), layers.size(), Math.max(depth, 0), List.copyOf(placements));
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	public int depth() {
		return this.depth;
	}

	public List<Placement> buildOrder() {
		return this.buildOrder;
	}

	/** Item id to how many the whole church needs. Placements without an item (a door's upper half) are free. */
	public Map<String, Integer> materials() {
		Map<String, Integer> materials = new TreeMap<>();
		for (Placement p : this.buildOrder) {
			if (p.item() != null) {
				materials.merge(p.item(), 1, Integer::sum);
			}
		}
		return materials;
	}

	/** "minecraft:birch_stairs[facing=north]" to "minecraft:birch_stairs". */
	public static String blockId(String state) {
		int bracket = state.indexOf('[');
		return bracket < 0 ? state : state.substring(0, bracket);
	}
}
