package martidi.sunnmore.stian;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.stian.ChurchBlueprint.Placement;
import org.jspecify.annotations.Nullable;

/**
 * Loads Stian's church drawing (data/sunnmore/church/stian_kyrkje.json) when the server starts
 * and after /reload, and parses every block state in it once.
 */
public final class StianChurch {
	private static final Identifier FILE = Sunnmore.id("church/stian_kyrkje.json");

	/** The drawing, its parsed block states, and a lookup from blueprint position to placement. */
	public record Loaded(ChurchBlueprint blueprint, Map<String, BlockState> states, @Nullable Placement[][][] grid) {
		public @Nullable Placement at(int x, int y, int z) {
			return this.grid[x][y][z];
		}
	}

	private static @Nullable Loaded loaded;

	private StianChurch() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(StianChurch::load);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> load(server));
	}

	public static Optional<Loaded> get() {
		return Optional.ofNullable(loaded);
	}

	private static void load(MinecraftServer server) {
		try (Reader reader = server.getResourceManager().getResourceOrThrow(FILE).openAsReader()) {
			StringBuilder json = new StringBuilder();
			char[] buffer = new char[4096];
			for (int n; (n = reader.read(buffer)) > 0; ) {
				json.append(buffer, 0, n);
			}
			ChurchBlueprint blueprint = ChurchBlueprint.parse(json.toString());
			Map<String, BlockState> states = new HashMap<>();
			Placement[][][] grid = new Placement[blueprint.width()][blueprint.height()][blueprint.depth()];
			for (Placement p : blueprint.buildOrder()) {
				if (!states.containsKey(p.block())) {
					states.put(p.block(), parse(p.block()));
				}
				grid[p.x()][p.y()][p.z()] = p;
			}
			loaded = new Loaded(blueprint, Map.copyOf(states), grid);
		} catch (Exception e) {
			Sunnmore.LOGGER.error("Could not load Stian's church from {}", FILE, e);
			loaded = null;
		}
	}

	private static BlockState parse(String state) throws CommandSyntaxException {
		return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, state, false).blockState();
	}
}
