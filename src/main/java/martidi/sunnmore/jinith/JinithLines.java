package martidi.sunnmore.jinith;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

/**
 * What Jinith says in chat, shown as "<Jinith> ...". Lines are lang keys
 * (chat.sunnmore.jinith.<pool>.<n>) in en_us.json; %s is the player's name, or "kompis".
 * The per-Jinith cooldown lives in JinithEntity.sayLine, the same split as Glenn's.
 */
public final class JinithLines {
	public static final double HEARING_RANGE = 16.0;

	public enum Pool {
		HEAL(3),
		ANGRY(3),
		HIT(2),
		FLEX(1),
		PATCHED_UP(2),
		CALM(2),
		ARRIVE(2),
		LEAVE(2);

		private final int size;

		Pool(int size) {
			this.size = size;
		}

		String key(int index) {
			return "chat.sunnmore.jinith." + this.name().toLowerCase(Locale.ROOT) + "." + index;
		}
	}

	private JinithLines() {
	}

	public static Component line(Pool pool, RandomSource random, @Nullable Entity about) {
		Component name = about != null ? about.getDisplayName() : Component.translatable("chat.sunnmore.jinith.nobody");
		return Component.literal("<")
			.append(Component.literal("Jinith").withStyle(ChatFormatting.GOLD))
			.append("> ")
			.append(Component.translatable(pool.key(random.nextInt(pool.size)), name));
	}

	public static void sayNearby(JinithEntity jinith, Pool pool, @Nullable Entity about, double range) {
		if (!(jinith.level() instanceof ServerLevel level)) {
			return;
		}
		Component message = line(pool, jinith.getRandom(), about);
		double rangeSqr = range * range;
		for (ServerPlayer player : level.players()) {
			// The player a line is about always hears it, even after he launched them 40 blocks away.
			if (player.distanceToSqr(jinith) <= rangeSqr || player == about) {
				player.sendSystemMessage(message);
			}
		}
	}
}
