package martidi.sunnmore.visits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.bard.BardEntities;
import martidi.sunnmore.glenn.GlennEntities;
import martidi.sunnmore.glenn.GlennTrades;
import martidi.sunnmore.jinith.JinithEntities;

import org.jspecify.annotations.Nullable;

/**
 * The friends drop by now and then, like the wandering trader: daytime only, about once a week
 * near a random player. A lure block (Glenns Plante, Treningsbenk, Værstasjon) near a player gives a 50% chance
 * each in-game morning that its friend comes to the lure instead. A visit lasts two days unless he
 * gets a name tag. /glennvisit, /jinithvisit and /bardvisit force a visit for testing.
 */
public final class FriendVisits {
	private static final int NATURAL_RADIUS = 48;
	private static final int COMMAND_RADIUS = 12;
	/** A friend already this close to his lure counts as being there. */
	private static final double FRIEND_NEAR_LURE = 32.0;
	private static final List<int[]> LURE_SPOTS =
		VisitMath.spotOffsetsNearestFirst(VisitMath.LURE_SPAWN_RADIUS, 2, 1);

	/** Saved on the overworld, per friend. lastLureDay is optional so older saves still load. */
	public record VisitState(int spawnDelay, int chance, Optional<UUID> visitor, long lastLureDay) {
		public static final Codec<VisitState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("spawn_delay").forGetter(VisitState::spawnDelay),
			Codec.INT.fieldOf("chance").forGetter(VisitState::chance),
			UUIDUtil.CODEC.optionalFieldOf("visitor").forGetter(VisitState::visitor),
			Codec.LONG.optionalFieldOf("last_lure_day", VisitMath.NEVER_ROLLED).forGetter(VisitState::lastLureDay)
		).apply(instance, VisitState::new));

		static VisitState initial() {
			return new VisitState(VisitMath.DAY, VisitMath.MIN_CHANCE, Optional.empty(), VisitMath.NEVER_ROLLED);
		}
	}

	/** Everything that differs between the friends. */
	public record Friend(String name, EntityType<? extends Mob> type, AttachmentType<VisitState> state, PoiType lure) {
	}

	// Glenn keeps the id he had before the lures, so existing worlds keep their visit timer.
	public static final AttachmentType<VisitState> GLENN_STATE = state("glenn_visits");
	public static final AttachmentType<VisitState> JINITH_STATE = state("jinith_visits");
	public static final AttachmentType<VisitState> BARD_STATE = state("bard_visits");

	private static final List<Friend> FRIENDS = new ArrayList<>();

	private FriendVisits() {
	}

	/** Called from Sunnmore.onInitialize, after the entity types and LureBlocks are registered. */
	public static void register() {
		FRIENDS.add(new Friend("glenn", GlennEntities.GLENNDERMAN, GLENN_STATE, LureBlocks.GLENN_LURE));
		FRIENDS.add(new Friend("jinith", JinithEntities.JINITH, JINITH_STATE, LureBlocks.JINITH_LURE));
		FRIENDS.add(new Friend("bard", BardEntities.BARD, BARD_STATE, LureBlocks.BARD_LURE));
		ServerTickEvents.END_LEVEL_TICK.register(FriendVisits::tick);
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			for (Friend friend : FRIENDS) {
				dispatcher.register(Commands.literal(friend.name() + "visit")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						Mob visitor = forceVisit(player.level(), friend, player);
						if (visitor == null) {
							context.getSource().sendFailure(Component.literal("Found no room for " + friend.name() + " nearby."));
							return 0;
						}
						context.getSource().sendSuccess(() -> Component.literal(friend.name() + " is on his way."), true);
						return 1;
					}));
			}
		});
	}

	private static AttachmentType<VisitState> state(String path) {
		return AttachmentRegistry.<VisitState>create(Sunnmore.id(path),
			builder -> builder.persistent(VisitState.CODEC).initializer(VisitState::initial));
	}

	// ---------------------------------------------------------------- the daily rolls

	private static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD || level.getGameTime() % VisitMath.CHECK_INTERVAL != 0) {
			return;
		}
		for (Friend friend : FRIENDS) {
			tickFriend(level, friend);
		}
	}

	private static void tickFriend(ServerLevel level, Friend friend) {
		VisitState state = level.getAttachedOrCreate(friend.state());
		RandomSource random = level.getRandom();
		boolean daytime = level.isBrightOutside();
		boolean visiting = isStillVisiting(level, state);
		long lastLureDay = state.lastLureDay();

		// 1. The lure: one 50% roll per in-game morning, if a lure is near a player.
		long today = GlennTrades.currentDay(level);
		lastLureDay = VisitMath.lureDayWhileVisiting(today, lastLureDay, daytime, visiting);
		if (!visiting && VisitMath.shouldRollLure(today, lastLureDay, daytime)) {
			BlockPos lure = findLure(level, friend, random);
			if (lure != null) {
				lastLureDay = today;
				// Already hanging out at his lure (name-tagged, spawn egg, forced): no second copy.
				boolean alreadyThere = isFriendNear(level, friend, lure);
				Mob visitor = !alreadyThere && VisitMath.lureHits(random.nextInt(100)) ? arriveForLure(level, friend, lure) : null;
				if (visitor != null) {
					level.setAttached(friend.state(), new VisitState(VisitMath.DAY, VisitMath.MIN_CHANCE,
						Optional.of(visitor.getUUID()), lastLureDay));
					return;
				}
			}
		}

		// 2. Natural visits, like the wandering trader.
		int delay = state.spawnDelay() - VisitMath.CHECK_INTERVAL;
		if (delay > 0) {
			level.setAttached(friend.state(), new VisitState(delay, state.chance(), state.visitor(), lastLureDay));
			return;
		}
		if (!daytime) {
			// Only in daytime: keep the timer at zero and try again each minute.
			level.setAttached(friend.state(), new VisitState(0, state.chance(), state.visitor(), lastLureDay));
			return;
		}
		if (visiting) {
			level.setAttached(friend.state(), new VisitState(VisitMath.DAY, state.chance(), state.visitor(), lastLureDay));
			return;
		}
		Mob visitor = null;
		if (VisitMath.naturalVisitHits(random.nextInt(100), state.chance(), random.nextInt(VisitMath.EXTRA_ROLL))) {
			ServerPlayer player = level.getRandomPlayer();
			if (player != null) {
				visitor = spawnNearPlayer(level, friend, player, NATURAL_RADIUS);
			}
		}
		int chance = visitor != null ? VisitMath.MIN_CHANCE : VisitMath.chanceAfterMiss(state.chance());
		Optional<UUID> visitorId = visitor != null ? Optional.of(visitor.getUUID()) : Optional.empty();
		level.setAttached(friend.state(), new VisitState(VisitMath.DAY, chance, visitorId, lastLureDay));
	}

	/** Only a loaded visitor counts. A name-tagged friend has moved in and no longer blocks visits. */
	private static boolean isStillVisiting(ServerLevel level, VisitState state) {
		return state.visitor()
			.map(level::getEntity)
			.filter(entity -> entity.isAlive() && entity instanceof VisitingFriend friend && friend.isVisiting())
			.isPresent();
	}

	// ---------------------------------------------------------------- lures

	/** A lure of this friend within range of any player (so its chunk is loaded); a random one if several. */
	private static @Nullable BlockPos findLure(ServerLevel level, Friend friend, RandomSource random) {
		List<BlockPos> found = new ArrayList<>();
		PoiManager pois = level.getPoiManager();
		for (ServerPlayer player : level.players()) {
			if (player.isSpectator()) {
				continue;
			}
			pois.findClosest(holder -> holder.value() == friend.lure(), level::isPositionEntityTicking, player.blockPosition(),
				VisitMath.LURE_RANGE, PoiManager.Occupancy.ANY).ifPresent(found::add);
		}
		return found.isEmpty() ? null : found.get(random.nextInt(found.size()));
	}

	/** Arrives on the nearest free standing spot around the lure (searched around it, so indoors stays indoors). */
	private static @Nullable Mob spawnAtLure(ServerLevel level, Friend friend, BlockPos lure) {
		for (int[] offset : LURE_SPOTS) {
			BlockPos pos = lure.offset(offset[0], offset[1], offset[2]);
			if (isStandingSpot(level, friend, pos) && canSeeFromLure(level, lure, pos)) {
				return spawnVisitor(level, friend, pos);
			}
		}
		return null;
	}

	/** No room at the lure (Glenn needs 3 blocks of headroom): he turns up near the player closest to it instead. */
	private static @Nullable Mob arriveForLure(ServerLevel level, Friend friend, BlockPos lure) {
		Mob visitor = spawnAtLure(level, friend, lure);
		if (visitor != null) {
			return visitor;
		}
		Vec3 center = Vec3.atCenterOf(lure);
		ServerPlayer nearest = null;
		for (ServerPlayer player : level.players()) {
			if (!player.isSpectator() && (nearest == null || player.distanceToSqr(center) < nearest.distanceToSqr(center))) {
				nearest = player;
			}
		}
		return nearest != null ? spawnNearPlayer(level, friend, nearest, COMMAND_RADIUS) : null;
	}

	/** The spot must be visible from the lure: never on the other side of a wall, floor or ceiling. */
	private static boolean canSeeFromLure(ServerLevel level, BlockPos lure, BlockPos spot) {
		Vec3 from = Vec3.atCenterOf(lure);
		Vec3 to = Vec3.atBottomCenterOf(spot).add(0.0, 0.5, 0.0);
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(lure);
	}

	/** Any Glenn or Jinith (visitor, resident, spawn egg) already near the lure. */
	private static boolean isFriendNear(ServerLevel level, Friend friend, BlockPos lure) {
		return !level.getEntitiesOfClass(Mob.class, new AABB(lure).inflate(FRIEND_NEAR_LURE),
			mob -> mob.isAlive() && mob.getType() == friend.type()).isEmpty();
	}

	/** Called when a visitor leaves: his day's lure roll is used up, so he doesn't come straight back. */
	public static void onVisitorLeft(ServerLevel level, EntityType<?> type) {
		ServerLevel overworld = level.getServer().overworld();
		for (Friend friend : FRIENDS) {
			if (friend.type() == type) {
				VisitState state = overworld.getAttachedOrCreate(friend.state());
				overworld.setAttached(friend.state(), new VisitState(state.spawnDelay(), state.chance(), Optional.empty(),
					GlennTrades.currentDay(overworld)));
			}
		}
	}

	// ---------------------------------------------------------------- spawning

	/**
	 * For the test commands: at a lure near the player if there is one, otherwise near the player.
	 * The visitor is remembered like a natural one, so a lure doesn't add a second copy.
	 */
	private static @Nullable Mob forceVisit(ServerLevel level, Friend friend, ServerPlayer player) {
		BlockPos lure = level.getPoiManager().findClosest(holder -> holder.value() == friend.lure(), player.blockPosition(),
			VisitMath.LURE_RANGE, PoiManager.Occupancy.ANY).orElse(null);
		Mob visitor = lure != null ? spawnAtLure(level, friend, lure) : null;
		if (lure != null && visitor == null) {
			player.sendSystemMessage(Component.literal("No room at the " + friend.name() + " lure (needs free space next to it). Spawning near you."));
		}
		if (visitor == null) {
			visitor = spawnNearPlayer(level, friend, player, COMMAND_RADIUS);
		}
		if (visitor != null) {
			ServerLevel overworld = level.getServer().overworld();
			VisitState state = overworld.getAttachedOrCreate(friend.state());
			overworld.setAttached(friend.state(), new VisitState(state.spawnDelay(), state.chance(),
				Optional.of(visitor.getUUID()), state.lastLureDay()));
		}
		return visitor;
	}

	private static @Nullable Mob spawnNearPlayer(ServerLevel level, Friend friend, ServerPlayer player, int radius) {
		RandomSource random = level.getRandom();
		BlockPos around = player.blockPosition();
		for (int attempt = 0; attempt < 10; attempt++) {
			int x = around.getX() + random.nextInt(radius * 2 + 1) - radius;
			int z = around.getZ() + random.nextInt(radius * 2 + 1) - radius;
			BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
			if (isStandingSpot(level, friend, pos)) {
				return spawnVisitor(level, friend, pos);
			}
		}
		return null;
	}

	private static boolean isStandingSpot(ServerLevel level, Friend friend, BlockPos pos) {
		BlockPos below = pos.below();
		boolean solidGround = !level.getBlockState(below).getCollisionShape(level, below).isEmpty()
			&& level.getFluidState(below).isEmpty();
		return solidGround
			&& level.getFluidState(pos).isEmpty()
			&& level.noCollision(friend.type().getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
	}

	private static @Nullable Mob spawnVisitor(ServerLevel level, Friend friend, BlockPos pos) {
		Mob visitor = friend.type().spawn(level, pos, EntitySpawnReason.EVENT);
		if (visitor instanceof VisitingFriend visiting) {
			visiting.setDespawnDelay(VisitMath.VISIT_LENGTH);
			visiting.sayArrival();
		}
		return visitor;
	}
}
