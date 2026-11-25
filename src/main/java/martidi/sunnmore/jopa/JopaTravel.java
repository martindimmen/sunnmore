package martidi.sunnmore.jopa;

import java.util.Set;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import martidi.sunnmore.Sunnmore;

/**
 * Where a JOPA portal takes you. From the Overworld: the shared JOPA lobby (built if needed), and the
 * spot you left from is saved on you. From JOPA: back to that spot, or the world spawn if there is
 * none. Also keeps the lobby safe: survival players can't change it (see the ServerLevel mixin), and
 * anyone who still falls out into the void is put back in the lobby before they hit the bottom.
 */
public final class JopaTravel {
	public static final ResourceKey<Level> JOPA = ResourceKey.create(Registries.DIMENSION, Sunnmore.id("jopa"));

	/** The portal each player entered JOPA from. */
	public static final AttachmentType<GlobalPos> RETURN = AttachmentRegistry.<GlobalPos>create(Sunnmore.id("jopa_return"),
		builder -> builder.persistent(GlobalPos.CODEC));

	/** Falling this far below the lobby floor puts you back in the lobby. */
	private static final int CATCH_BELOW = 24;

	private JopaTravel() {
	}

	/** Called from Sunnmore.onInitialize. */
	public static void register() {
		// /jopalobby (op): builds the lobby, and takes a player there with the way back set to where they stand.
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
			dispatcher.register(Commands.literal("jopalobby")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.executes(context -> {
					ServerLevel jopa = context.getSource().getServer().getLevel(JOPA);
					if (jopa == null) {
						context.getSource().sendFailure(Component.literal("The JOPA dimension is missing."));
						return 0;
					}
					if (context.getSource().getEntity() instanceof ServerPlayer player && player.level().dimension() != JOPA) {
						player.setAttached(RETURN, GlobalPos.of(player.level().dimension(), player.blockPosition()));
						player.teleport(toLobby(jopa));
					} else {
						JopaLobbyBuilder.ensureBuilt(jopa);
					}
					context.getSource().sendSuccess(() -> Component.literal("JOPA lobby ready."), true);
					return 1;
				})));
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			if (level.dimension() != JOPA) {
				return;
			}
			for (ServerPlayer player : level.players()) {
				if (player.getY() < JopaLobbyBuilder.ORIGIN.getY() - CATCH_BELOW && !player.isSpectator()) {
					player.resetFallDistance();
					player.teleport(toLobby(level));
				}
			}
		});
	}

	public static @Nullable TeleportTransition destination(ServerLevel current, Entity entity, BlockPos entryPos) {
		if (!(entity instanceof ServerPlayer player)) {
			return null;
		}
		if (current.dimension() == JOPA) {
			return back(player);
		}
		if (current.dimension() != Level.OVERWORLD) {
			return null;
		}
		ServerLevel jopa = current.getServer().getLevel(JOPA);
		if (jopa == null) {
			return null;
		}
		player.setAttached(RETURN, GlobalPos.of(current.dimension(), entryPos));
		return toLobby(jopa);
	}

	private static TeleportTransition toLobby(ServerLevel jopa) {
		JopaLobbyBuilder.ensureBuilt(jopa);
		BlockPos spawn = JopaLobbyBuilder.ORIGIN.offset(JopaLobby.SPAWN[0], JopaLobby.SPAWN[1], JopaLobby.SPAWN[2]);
		return new TeleportTransition(jopa, Vec3.atBottomCenterOf(spawn), Vec3.ZERO, JopaLobby.SPAWN_YAW, 0.0F,
			TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
	}

	private static TeleportTransition back(ServerPlayer player) {
		GlobalPos home = player.getAttached(RETURN);
		ServerLevel level = home != null ? player.level().getServer().getLevel(home.dimension()) : null;
		if (home == null || level == null) {
			return TeleportTransition.createDefault(player, TeleportTransition.PLAY_PORTAL_SOUND);
		}
		// The spot may have changed since (portal broken, blocks placed): find room for the player nearby.
		Vec3 safe = PortalShape.findCollisionFreePosition(Vec3.atBottomCenterOf(home.pos()), level, player, player.getDimensions(player.getPose()));
		return new TeleportTransition(level, safe, Vec3.ZERO, player.getYRot(), 0.0F,
			TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
	}

	/** The lobby can't be changed by survival players, so nobody can break out into the void or block the portal. */
	public static boolean isProtected(ServerLevel level, Entity entity, BlockPos pos) {
		if (level.dimension() != JOPA || !(entity instanceof Player player) || player.getAbilities().instabuild) {
			return false;
		}
		BlockPos o = JopaLobbyBuilder.ORIGIN;
		return JopaLobby.contains(pos.getX() - o.getX(), pos.getY() - o.getY(), pos.getZ() - o.getZ());
	}
}
