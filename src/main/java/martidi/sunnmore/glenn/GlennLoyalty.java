package martidi.sunnmore.glenn;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import martidi.sunnmore.Sunnmore;

/**
 * "Stamkunde": how many times a player has traded with Glenn. Stored on the player (survives death),
 * shared by every Glenn, since there is only one Glenn really.
 */
public final class GlennLoyalty {
	public static final String ADVANCEMENT_MET = "glenn/yppa_du_eller";
	public static final String ADVANCEMENT_STAMKUNDE = "glenn/stamkunde";

	public static final AttachmentType<Integer> TRADES = AttachmentRegistry.<Integer>create(
		Sunnmore.id("glenn_trades"),
		builder -> builder.persistent(Codec.INT).initializer(() -> 0).copyOnDeath()
	);

	private GlennLoyalty() {
	}

	/** Called from Sunnmore.onInitialize; touching the class registers the attachment. */
	public static void register() {
	}

	public static int trades(Player player) {
		return player.getAttachedOrElse(TRADES, 0);
	}

	/** Counts one more trade and returns the new total. */
	public static int addTrade(ServerPlayer player) {
		int trades = trades(player) + 1;
		player.setAttached(TRADES, trades);
		return trades;
	}

	public static int xp(Player player) {
		return StamkundeMath.xpForTrades(trades(player));
	}

	public static int level(Player player) {
		return StamkundeMath.levelForTrades(trades(player));
	}

	/** Grants every criterion of one of our advancements (they use minecraft:impossible). */
	public static void award(ServerPlayer player, String path) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Sunnmore.id(path));
		if (advancement == null) {
			Sunnmore.LOGGER.warn("Missing advancement {}", path);
			return;
		}
		for (String criterion : advancement.value().criteria().keySet()) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
