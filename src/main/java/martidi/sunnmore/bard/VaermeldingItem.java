package martidi.sunnmore.bard;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Bård's weather report on paper: right-click it and the forecast shows in your own chat. */
public class VaermeldingItem extends Item {
	public VaermeldingItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel) {
			player.sendSystemMessage(Component.literal("<")
				.append(Component.translatable("chat.sunnmore.bard.vaermelding").withStyle(ChatFormatting.AQUA))
				.append("> ")
				.append(report(serverLevel, player)));
		}
		return InteractionResult.SUCCESS;
	}

	private static Component report(ServerLevel level, Player player) {
		if (!BardWeather.hasSignal(level)) {
			return BardLines.poolLine(BardLines.Pool.NO_SIGNAL, level.getRandom(), player);
		}
		if (!BardWeather.cycleOn(level)) {
			return BardLines.poolLine(BardLines.Pool.FROZEN, level.getRandom(), player);
		}
		return BardLines.forecastLine(WeatherForecast.of(BardWeather.read(level.getServer())));
	}
}
