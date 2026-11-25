package martidi.sunnmore.stian.goal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.state.BlockState;

import martidi.sunnmore.stian.ChurchProgress;
import martidi.sunnmore.stian.ChurchProgress.Missing;
import martidi.sunnmore.stian.ChurchProgress.Place;
import martidi.sunnmore.stian.ChurchProgress.Step;
import martidi.sunnmore.stian.ChurchSite;
import martidi.sunnmore.stian.KyrkjetomtBlockEntity;
import martidi.sunnmore.stian.StianEntity;
import martidi.sunnmore.stian.StianLines;
import martidi.sunnmore.stian.StianMath;
import martidi.sunnmore.visits.LureBlocks;
import org.jspecify.annotations.Nullable;

/**
 * Stian builds a church at the nearest Kyrkjetomt within 32 blocks that has materials for the next
 * block and nothing in the way: one block every half second (twice as fast after a Bjørneunge),
 * bottom up, every block taken from the plot. Says what is missing or in the way, once, and tries
 * again later. Takes a røykepause every few minutes.
 */
public class BuildChurchGoal extends Goal {
	private static final int SEARCH_INTERVAL = 40;
	private static final int RETRY_TICKS = 200;
	/** He builds from outside the church, so he needs a long reach: the spire is 14 up. */
	private static final double REACH = 16.0;
	private static final int MAX_FAILED_PLACEMENTS = 5;
	private static final String BJORNEUNGE = "sunnmore:bjorneunge";

	private final StianEntity stian;
	private @Nullable BlockPos plotPos;
	private long nextSearch;
	private long nextPlace;
	private long nextSmokeBreak;
	private long smokeBreakUntil;
	private int boostedLeft;
	private int failedPlacements;
	/** Plots with something in the way or missing material, and when to look at them again. */
	private final Map<BlockPos, Long> retryAt = new HashMap<>();
	/** Plots he already greeted with a start line, and missing items he already mentioned. */
	private final Set<BlockPos> started = new HashSet<>();
	private final Set<String> saidMissing = new HashSet<>();
	/** Plots he already said are blocked: once per plot until it is clear again. */
	private final Set<BlockPos> saidBlocked = new HashSet<>();

	public BuildChurchGoal(StianEntity stian) {
		this.stian = stian;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!(this.stian.level() instanceof ServerLevel level) || this.stian.isSitting()) {
			return false;
		}
		long now = level.getGameTime();
		if (now < this.nextSearch) {
			return false;
		}
		this.nextSearch = now + SEARCH_INTERVAL;
		this.plotPos = this.findPlot(level, now);
		return this.plotPos != null;
	}

	/** The nearest plot whose next block can be placed now. Says once what is missing or blocking at the others. */
	private @Nullable BlockPos findPlot(ServerLevel level, long now) {
		return level.getPoiManager()
			.findAllClosestFirstWithType(holder -> holder.value() == LureBlocks.STIAN_LURE, pos -> true,
				this.stian.blockPosition(), StianMath.PLOT_SEARCH, PoiManager.Occupancy.ANY)
			.map(pair -> pair.getSecond())
			.filter(pos -> now >= this.retryAt.getOrDefault(pos, 0L))
			.filter(pos -> this.ready(level, pos, now))
			.findFirst()
			.orElse(null);
	}

	private boolean ready(ServerLevel level, BlockPos pos, long now) {
		Optional<ChurchSite> site = site(level, pos);
		KyrkjetomtBlockEntity plot = plot(level, pos);
		if (site.isEmpty() || plot == null) {
			return false;
		}
		Step step = site.get().next(plot);
		if (step instanceof Missing missing) {
			this.sayMissing(missing);
			this.retryAt.put(pos, now + RETRY_TICKS);
			return false;
		}
		if (!(step instanceof Place)) {
			return false;
		}
		if (!site.get().obstacles().isEmpty()) {
			if (this.saidBlocked.add(pos)) {
				this.stian.sayLine(StianLines.Pool.BLOCKED, null, true);
			}
			this.retryAt.put(pos, now + RETRY_TICKS);
			return false;
		}
		this.saidBlocked.remove(pos);
		return true;
	}

	@Override
	public void start() {
		this.failedPlacements = 0;
		long now = this.stian.level().getGameTime();
		if (this.nextSmokeBreak == 0) {
			this.nextSmokeBreak = now + this.stian.roll(StianMath.SMOKE_BREAK_MIN, StianMath.SMOKE_BREAK_MAX);
		}
		if (this.plotPos != null && this.started.add(this.plotPos)) {
			this.stian.sayLine(StianLines.Pool.START, null, true);
		}
	}

	@Override
	public boolean canContinueToUse() {
		return this.plotPos != null && !this.stian.isSitting()
			&& this.stian.level() instanceof ServerLevel level && plot(level, this.plotPos) != null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!(this.stian.level() instanceof ServerLevel level) || this.plotPos == null) {
			return;
		}
		long now = level.getGameTime();
		// Røykepause: stand still, puff, then carry on.
		if (now < this.smokeBreakUntil) {
			this.stian.getNavigation().stop();
			if (now % 20 == 0) {
				this.stian.puff(level, 4);
			}
			return;
		}
		if (this.stian.isSmoking()) {
			this.stian.setSmoking(false);
		}
		if (now >= this.nextSmokeBreak) {
			this.nextSmokeBreak = now + this.stian.roll(StianMath.SMOKE_BREAK_MIN, StianMath.SMOKE_BREAK_MAX);
			this.smokeBreakUntil = now + StianMath.SMOKE_BREAK_TICKS;
			this.stian.setSmoking(true);
			this.stian.sayLine(StianLines.Pool.SMOKE_BREAK, null, false);
			return;
		}

		Optional<ChurchSite> site = site(level, this.plotPos);
		KyrkjetomtBlockEntity plot = plot(level, this.plotPos);
		if (site.isEmpty() || plot == null) {
			this.plotPos = null;
			return;
		}
		Step step = site.get().next(plot);
		if (step instanceof ChurchProgress.Finished) {
			this.stian.sayLine(StianLines.Pool.FINISHED, null, true);
			this.retryAt.put(this.plotPos, Long.MAX_VALUE);
			this.plotPos = null;
			return;
		}
		if (step instanceof Missing missing) {
			this.sayMissing(missing);
			this.retryAt.put(this.plotPos, now + RETRY_TICKS);
			this.plotPos = null;
			return;
		}
		Place place = (Place) step;
		this.stian.setBuilding(true);
		BlockPos target = site.get().worldPos(place.placement());
		this.stian.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
		// Build from outside: walk out of the church, then to the side nearest the block.
		if (site.get().inside(this.stian.blockPosition()) || !target.closerToCenterThan(this.stian.position(), REACH)) {
			if (now % 20 == 0) {
				BlockPos stand = site.get().standingSpotFor(place.placement());
				this.stian.getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.8);
			}
			return;
		}
		if (now < this.nextPlace) {
			return;
		}
		if (this.boostedLeft <= 0 && plot.take(BJORNEUNGE)) {
			this.boostedLeft = StianMath.BLOCKS_PER_CAN;
			this.stian.sayLine(StianLines.Pool.BOOSTED, null, true);
		}
		boolean boosted = this.boostedLeft > 0;
		boolean mistake = boosted && StianMath.isMistake(this.stian.getRandom().nextInt(StianMath.MISTAKE_ONE_IN));
		if (site.get().place(place.placement(), plot, mistake)) {
			this.failedPlacements = 0;
			// Progress again: missing items may be mentioned again later.
			this.saidMissing.clear();
			if (boosted) {
				this.boostedLeft--;
			}
			this.stian.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
			BlockState placed = level.getBlockState(target);
			level.playSound(null, target, placed.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
			this.nextPlace = now + StianMath.placeTicks(this.boostedLeft > 0);
		} else if (++this.failedPlacements >= MAX_FAILED_PLACEMENTS) {
			// Someone put something in the way after he started: say so (once) and look again later.
			if (this.saidBlocked.add(this.plotPos)) {
				this.stian.sayLine(StianLines.Pool.BLOCKED, null, true);
			}
			this.retryAt.put(this.plotPos, now + RETRY_TICKS);
			this.plotPos = null;
		} else {
			this.nextPlace = now + 20;
		}
	}

	@Override
	public void stop() {
		this.stian.setBuilding(false);
		this.stian.setSmoking(false);
		this.smokeBreakUntil = 0;
		this.stian.getNavigation().stop();
	}

	private void sayMissing(Missing missing) {
		if (this.saidMissing.add(missing.item())) {
			Component what = Component.literal(missing.stillNeeded() + " ").append(Component.translatable(
				BuiltInRegistries.ITEM.getValue(Identifier.parse(missing.item())).getDescriptionId()));
			this.stian.sayLine(StianLines.Pool.MISSING, null, what, true);
		}
	}

	private static Optional<ChurchSite> site(ServerLevel level, BlockPos pos) {
		return ChurchSite.of(level, pos, level.getBlockState(pos));
	}

	private static @Nullable KyrkjetomtBlockEntity plot(ServerLevel level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof KyrkjetomtBlockEntity plot ? plot : null;
	}
}
