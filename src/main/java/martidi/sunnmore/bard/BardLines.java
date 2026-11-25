package martidi.sunnmore.bard;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import martidi.sunnmore.bard.WeatherForecast.Forecast;
import martidi.sunnmore.bard.WeatherForecast.Phrase;
import org.jspecify.annotations.Nullable;

/**
 * What Bård says in chat, shown as "<Bård> ...". Lines are lang keys (chat.sunnmore.bard.<pool>.<n>)
 * in en_us.json; %s is the player's name, or "kompis". The forecast is built from two keys: the
 * change (chat.sunnmore.bard.forecast.<now>_to_<next>) and the time (chat.sunnmore.bard.time.*).
 * The cooldowns live in BardEntity, the same split as Glenn's and Jinith's.
 */
public final class BardLines {
	public static final double HEARING_RANGE = 16.0;

	public enum Pool {
		STRESS(2),
		STORM(2),
		HIT(3),
		BOUGHT_SOL(1),
		BOUGHT_REGN(1),
		BOUGHT_TORDEN(1),
		SOLDOUT(1),
		FROZEN(1),
		NO_SIGNAL(1),
		ARRIVE(2),
		LEAVE(2),
		DEATH(1);

		private final int size;

		Pool(int size) {
			this.size = size;
		}

		String key(int index) {
			return "chat.sunnmore.bard." + this.name().toLowerCase(Locale.ROOT) + "." + index;
		}
	}

	private BardLines() {
	}

	public static Component poolLine(Pool pool, RandomSource random, @Nullable Entity about) {
		Component name = about != null ? about.getDisplayName() : Component.translatable("chat.sunnmore.bard.nobody");
		return Component.translatable(pool.key(random.nextInt(pool.size)), name);
	}

	/** "Yr sei regn om 4 minutt." The change and the time come from separate keys. */
	public static Component forecastLine(Forecast forecast) {
		if (forecast.next() == null) {
			return Component.translatable("chat.sunnmore.bard.forecast.unsettled");
		}
		String change = name(forecast.now()) + "_to_" + name(forecast.next());
		return Component.translatable("chat.sunnmore.bard.forecast." + change, time(forecast.ticks()));
	}

	private static Component time(int ticks) {
		Phrase phrase = WeatherForecast.phrase(ticks);
		return switch (phrase.unit()) {
			case SOON -> Component.translatable("chat.sunnmore.bard.time.soon");
			case MINUTES -> Component.translatable("chat.sunnmore.bard.time.minutes", phrase.amount());
			case HOURS -> Component.translatable("chat.sunnmore.bard.time.hours", phrase.amount());
		};
	}

	private static String name(Weather weather) {
		return weather.name().toLowerCase(Locale.ROOT);
	}

	/** "<Bård> " in front of a line. */
	public static MutableComponent withPrefix(Component body) {
		return Component.literal("<")
			.append(Component.literal("Bård").withStyle(ChatFormatting.YELLOW))
			.append("> ")
			.append(body);
	}

	public static void sayNearby(BardEntity bard, Component body, @Nullable Entity about, double range) {
		if (!(bard.level() instanceof ServerLevel level)) {
			return;
		}
		Component message = withPrefix(body);
		double rangeSqr = range * range;
		for (ServerPlayer player : level.players()) {
			// The player a line is about always hears it.
			if (player.distanceToSqr(bard) <= rangeSqr || player == about) {
				player.sendSystemMessage(message);
			}
		}
	}
}
