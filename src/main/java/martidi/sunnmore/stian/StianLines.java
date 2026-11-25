package martidi.sunnmore.stian;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

/**
 * What Stian says, shown as "<Stian> ...". Lines are lang keys (chat.sunnmore.stian.<pool>.<n>) in
 * en_us.json. %s is the player's name or "kompis", or for MISSING the item that is missing.
 */
public final class StianLines {
	public static final double HEARING_RANGE = 16.0;

	public enum Pool {
		ARRIVE(2),
		LEAVE(2),
		HIT(3),
		HICCUP(2),
		TRIP(2),
		SMOKE_BREAK(2),
		START(2),
		MISSING(2),
		BLOCKED(2),
		BOOSTED(2),
		FINISHED(2),
		DEATH(1);

		private final int size;

		Pool(int size) {
			this.size = size;
		}

		String key(int index) {
			return "chat.sunnmore.stian." + this.name().toLowerCase(Locale.ROOT) + "." + index;
		}
	}

	private StianLines() {
	}

	/** A line from the pool; arg fills %s (defaults to the name of whoever it is about). */
	public static Component line(Pool pool, RandomSource random, @Nullable Entity about, @Nullable Component arg) {
		Component fill = arg != null ? arg
			: about != null ? about.getDisplayName() : Component.translatable("chat.sunnmore.stian.nobody");
		return Component.translatable(pool.key(random.nextInt(pool.size)), fill);
	}

	public static MutableComponent withPrefix(Component body) {
		return Component.literal("<")
			.append(Component.literal("Stian").withStyle(ChatFormatting.GREEN))
			.append("> ")
			.append(body);
	}

	public static void sayNearby(StianEntity stian, Component body, @Nullable Entity about, double range) {
		if (!(stian.level() instanceof ServerLevel level)) {
			return;
		}
		Component message = withPrefix(body);
		double rangeSqr = range * range;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(stian) <= rangeSqr || player == about) {
				player.sendSystemMessage(message);
			}
		}
	}
}
