package martidi.sunnmore.jopa;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;

/**
 * JOPA-kort, the access card. Swipe it on a JOPA-ramme (right-click): a high beep and the portal
 * opens, or a low beep and "Tilgang nekta." The card is never used up. Portals only open in the
 * Overworld; the way back is the lobby's own portal.
 */
public class JopaCardItem extends Item {
	public JopaCardItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof JopaFrameBlock)) {
			return InteractionResult.PASS;
		}
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		Player player = context.getPlayer();
		if (state.getValue(JopaFrameBlock.LIT)) {
			beep(server, pos, true);
			return InteractionResult.SUCCESS_SERVER;
		}
		if (server.dimension() != Level.OVERWORLD) {
			deny(server, pos, player, "chat.sunnmore.jopa.wrong_place");
			return InteractionResult.SUCCESS_SERVER;
		}
		if (JopaPortals.tryOpen(server, pos)) {
			beep(server, pos, true);
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("chat.sunnmore.jopa.open"));
			}
		} else {
			deny(server, pos, player, "chat.sunnmore.jopa.denied");
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void deny(ServerLevel level, BlockPos pos, Player player, String key) {
		beep(level, pos, false);
		if (player != null) {
			player.sendOverlayMessage(Component.translatable(key));
		}
	}

	/** A card reader: a high bleep when it lets you in, a low one when it doesn't. */
	private static void beep(ServerLevel level, BlockPos pos, boolean ok) {
		level.playSound(null, pos, ok ? SoundEvents.NOTE_BLOCK_BIT.value() : SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS,
			1.0F, ok ? 2.0F : 0.6F);
	}
}
