package martidi.sunnmore.bard;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WeatherData;

/**
 * The bridge between Bård and the game's weather. In 26.3 the weather lives on the server
 * (MinecraftServer.getWeatherData) and only the Overworld has any.
 */
public final class BardWeather {
	private BardWeather() {
	}

	/** A copy of the game's weather countdowns right now. */
	public static WeatherState read(MinecraftServer server) {
		WeatherData data = server.getWeatherData();
		return new WeatherState(data.getClearWeatherTime(), data.getRainTime(), data.getThunderTime(),
			data.isRaining(), data.isThundering());
	}

	/** Bård only has signal in the Overworld. */
	public static boolean hasSignal(ServerLevel level) {
		return level.dimension() == Level.OVERWORLD;
	}

	/** False when an admin froze the weather with /gamerule doWeatherCycle false. */
	public static boolean cycleOn(ServerLevel level) {
		return level.getGameRules().get(GameRules.ADVANCE_WEATHER);
	}
}
