package martidi.sunnmore.bard;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WeatherData;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.bard.WeatherShopMath.Samples;
import martidi.sunnmore.glenn.GlennTrades;

/**
 * The bridge between Bård and the game's weather. In 26.3 the weather lives on the server
 * (MinecraftServer.getWeatherData) and only the Overworld has any. The day of the last weather
 * change is saved on the Overworld and shared by every Bård: one change per in-game day.
 */
public final class BardWeather {
	public static final AttachmentType<Long> WEATHER_DAY = AttachmentRegistry.<Long>create(Sunnmore.id("bard_weather_day"),
		builder -> builder.persistent(Codec.LONG).initializer(() -> WeatherShopMath.NEVER_CHANGED));

	/** Vanilla's ServerLevel.THUNDER_DELAY is private; these are the same numbers. */
	private static final IntProvider THUNDER_DELAY = UniformInt.of(12000, 180000);

	private BardWeather() {
	}

	/** Called from BardEntities.register; touching the class registers the attachment. */
	public static void register() {
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

	public static boolean usedToday(ServerLevel anyLevel) {
		ServerLevel overworld = anyLevel.getServer().overworld();
		return overworld.getAttachedOrCreate(WEATHER_DAY) == GlennTrades.currentDay(overworld);
	}

	/** Whether the weather deals are open for a Bård standing in this level. */
	public static boolean dealsOpen(ServerLevel bardLevel) {
		ServerLevel overworld = bardLevel.getServer().overworld();
		return WeatherShopMath.dealsOpen(GlennTrades.currentDay(overworld), overworld.getAttachedOrCreate(WEATHER_DAY),
			hasSignal(bardLevel), cycleOn(bardLevel));
	}

	/**
	 * Changes the weather right away and uses up the day's change. The countdowns are written
	 * directly: /weather's setter would give thunder the same countdown as rain.
	 */
	public static void buy(MinecraftServer server, Weather weather) {
		ServerLevel overworld = server.overworld();
		RandomSource random = overworld.getRandom();
		Samples samples = new Samples(ServerLevel.RAIN_DELAY.sample(random), ServerLevel.RAIN_DURATION.sample(random),
			THUNDER_DELAY.sample(random), ServerLevel.THUNDER_DURATION.sample(random));
		WeatherState after = WeatherShopMath.purchase(weather, read(server), samples);
		WeatherData data = server.getWeatherData();
		data.setClearWeatherTime(after.clearTime());
		data.setRainTime(after.rainTime());
		data.setThunderTime(after.thunderTime());
		data.setRaining(after.raining());
		data.setThundering(after.thundering());
		overworld.setAttached(WEATHER_DAY, GlennTrades.currentDay(overworld));
	}

	/**
	 * Closes the weather deals on every open Bård screen in the same tick as a purchase, so a second
	 * buyer never pays diamonds for a change that can't happen.
	 */
	public static void closeShopsToday(ServerLevel overworld) {
		for (BardEntity bard : overworld.getEntities(BardEntities.BARD, BardEntity::isTrading)) {
			bard.refreshTradingScreen();
		}
	}
}
