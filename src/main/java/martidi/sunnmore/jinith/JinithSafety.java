package martidi.sunnmore.jinith;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;

import martidi.sunnmore.Sunnmore;

/**
 * The safety window: for a few seconds after Jinith punches someone (or patches them up), nothing
 * can kill that player. A fall, lava or a mob leaves them at half a heart instead.
 * Only the void and /kill (damage that bypasses invulnerability) still go through.
 */
public final class JinithSafety {
	/** Game time until which the player is protected. Not saved: it only lasts seconds. */
	public static final AttachmentType<Long> SAFE_UNTIL = AttachmentRegistry.<Long>create(Sunnmore.id("jinith_safe_until"));

	private JinithSafety() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player
				&& !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& isProtected(player)) {
				player.setHealth(JinithMath.HALF_HEART);
				return false;
			}
			return true;
		});
	}

	public static void protect(ServerPlayer player) {
		protectUntil(player, player.level().getGameTime() + JinithMath.SAFETY_TICKS);
	}

	/** Protects until the given game time; never shortens a window that is already longer. */
	public static void protectUntil(ServerPlayer player, long until) {
		Long current = player.getAttached(SAFE_UNTIL);
		player.setAttached(SAFE_UNTIL, current != null ? Math.max(current, until) : until);
	}

	public static boolean isProtected(ServerPlayer player) {
		Long until = player.getAttached(SAFE_UNTIL);
		return until != null && JinithMath.isProtected(player.level().getGameTime(), until);
	}
}
