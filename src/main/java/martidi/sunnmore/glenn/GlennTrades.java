package martidi.sunnmore.glenn;

import java.util.List;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.timeline.Timelines;

/**
 * Glenn's deals. Each in-game morning a Glenn picks 2 deals from his own goods and 2 silly vanilla
 * ones. Each deal can be bought a few times, then it is sold out until the next morning. Price is
 * always sugar.
 */
public final class GlennTrades {
	public static final int GLENN_DEALS_PER_DAY = 2;
	public static final int VANILLA_DEALS_PER_DAY = 2;
	private static final int XP_PER_TRADE = StamkundeMath.XP_PER_TRADE;

	/** One possible deal: what you get, how much sugar it costs, and how many times a day it can be bought. */
	private record Deal(Supplier<ItemStack> result, int sugar, int maxUses) {
		MerchantOffer toOffer() {
			// priceMultiplier 0: no demand-based price changes; Stamkunde discounts come from special prices.
			return new MerchantOffer(new ItemCost(Items.SUGAR, this.sugar), this.result.get(), this.maxUses, XP_PER_TRADE, 0.0F);
		}
	}

	private static final List<Deal> GLENN_POOL = List.of(
		new Deal(() -> new ItemStack(GlennItems.TELEPORT_TYGGIS, 2), 6, 6),
		new Deal(() -> new ItemStack(GlennItems.MYSTERY_BRUS), 8, 4),
		new Deal(() -> new ItemStack(GlennItems.GINGER_WIG), 24, 1),
		new Deal(() -> new ItemStack(GlennItems.PREMIUM_JORD), 12, 3)
	);

	private static final List<Deal> VANILLA_POOL = List.of(
		new Deal(() -> new ItemStack(Items.CAKE), 10, 2),
		new Deal(() -> new ItemStack(Items.COOKIE, 6), 4, 6),
		new Deal(GlennTrades::partyPack, 6, 4),
		new Deal(() -> new ItemStack(Items.ENDER_PEARL), 10, 2)
	);

	private GlennTrades() {
	}

	public static MerchantOffers rollLineup(RandomSource random) {
		MerchantOffers offers = new MerchantOffers();
		for (int index : LineupPicker.pick(GLENN_POOL.size(), GLENN_DEALS_PER_DAY, random::nextInt)) {
			offers.add(GLENN_POOL.get(index).toOffer());
		}
		for (int index : LineupPicker.pick(VANILLA_POOL.size(), VANILLA_DEALS_PER_DAY, random::nextInt)) {
			offers.add(VANILLA_POOL.get(index).toOffer());
		}
		return offers;
	}

	/** The in-game day number, counted the same way villagers count it for restocking. */
	public static long currentDay(ServerLevel level) {
		return level.registryAccess()
			.get(Timelines.OVERWORLD_DAY)
			.map(timeline -> (long) timeline.value().getPeriodCount(level.clockManager()))
			.orElseGet(() -> level.getGameTime() / 24000L);
	}

	/** Three firework rockets in Glenn's colours: ginger and hoodie green. */
	private static ItemStack partyPack() {
		ItemStack rockets = new ItemStack(Items.FIREWORK_ROCKET, 3);
		FireworkExplosion explosion = new FireworkExplosion(
			FireworkExplosion.Shape.LARGE_BALL, IntList.of(0xE27024, 0x387A54), IntList.of(0xF5963C), true, true
		);
		rockets.set(DataComponents.FIREWORKS, new Fireworks(2, List.of(explosion)));
		// ITEM_NAME, not CUSTOM_NAME: a custom name shows in italics like an anvil rename.
		rockets.set(DataComponents.ITEM_NAME, Component.translatable("item.sunnmore.party_pack"));
		return rockets;
	}
}
