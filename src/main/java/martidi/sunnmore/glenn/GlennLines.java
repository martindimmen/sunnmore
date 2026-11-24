package martidi.sunnmore.glenn;

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
 * What Glenn says in chat. Every line is a lang key (chat.sunnmore.glenn.<pool>.<n>) so the text
 * lives in en_us.json and is easy to change. Lines get the player's name as %s when there is one.
 */
public final class GlennLines {
	/** Players within this distance see what Glenn says. */
	public static final double HEARING_RANGE = 16.0;

	public enum Pool {
		GREET(3),
		GREET_REGULAR(2),
		TRADE(3),
		SCAM(2),
		HURT(3),
		DODGE(3),
		FAREWELL(1),
		NO_BUY(1),
		SUGAR(2),
		DANCE(3),
		LEVELUP(2),
		SOLDOUT(1),
		ARRIVE(2),
		LEAVE(2),
		DEATH(2);

		private final int size;

		Pool(int size) {
			this.size = size;
		}

		String key(int index) {
			return "chat.sunnmore.glenn." + this.name().toLowerCase(Locale.ROOT) + "." + index;
		}
	}

	private GlennLines() {
	}

	/**
	 * Builds "<Glennderman> line", picking a random line from the pool. Lines with %s get the
	 * entity's name; with nobody to name (a dispenser arrow, say) they say "kompis".
	 */
	public static Component line(Pool pool, RandomSource random, @Nullable Entity about) {
		Component name = about != null ? about.getDisplayName() : Component.translatable("chat.sunnmore.glenn.nobody");
		MutableComponent text = Component.translatable(pool.key(random.nextInt(pool.size)), name);
		return Component.literal("<")
			.append(Component.literal("Glennderman").withStyle(ChatFormatting.GOLD))
			.append("> ")
			.append(text);
	}

	/** Sends a line to every player within {@code range} blocks of Glenn. */
	public static void sayNearby(GlenndermanEntity glenn, Pool pool, @Nullable Entity about, double range) {
		if (!(glenn.level() instanceof ServerLevel level)) {
			return;
		}
		Component message = line(pool, glenn.getRandom(), about);
		double rangeSqr = range * range;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(glenn) <= rangeSqr) {
				player.sendSystemMessage(message);
			}
		}
	}
}
