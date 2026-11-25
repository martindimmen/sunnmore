package martidi.sunnmore.glenn;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A Wago lever connector (both the crafted one and Glenndermans Wago). Right-click to click it; 1
 * click in 10 pinches your finger for 2 hearts. The pinch can kill you on low health, on purpose
 * (Martin's call). No cooldown: holding right-click keeps clicking.
 */
public class WagoItem extends Item {
	public WagoItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel server) {
			// The lever's own pitches: 0.6 when it switches on, 0.5 when it switches off.
			float pitch = player.getRandom().nextBoolean() ? 0.6F : 0.5F;
			server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.6F, pitch);
			if (WagoMath.isPinch(player.getRandom().nextInt(WagoMath.PINCH_ONE_IN))) {
				player.hurtServer(server, server.damageSources().source(WagoDamage.PINCH), WagoMath.PINCH_DAMAGE);
			}
		}
		// SUCCESS on both sides: the arm swings.
		return InteractionResult.SUCCESS;
	}
}
