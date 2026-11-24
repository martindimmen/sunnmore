package martidi.sunnmore.glenn.goal;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import martidi.sunnmore.glenn.GlennLines;
import martidi.sunnmore.glenn.GlenndermanEntity;

import org.jspecify.annotations.Nullable;

/**
 * When a jukebox plays nearby, Glenn walks over and dances: hops, spins his head around and gives
 * off music notes.
 */
public class DanceToJukeboxGoal extends Goal {
	private static final int SEARCH_RADIUS = 8;
	private static final int SEARCH_HEIGHT = 4;
	private static final double DANCE_DISTANCE_SQR = 3.0 * 3.0;
	private static final int GIVE_UP_WALKING_TICKS = 200;

	private final GlenndermanEntity glenn;
	private @Nullable BlockPos jukebox;
	private int searchCooldown;
	private int ticks;
	private int walkTicksLeft;
	private boolean announced;

	public DanceToJukeboxGoal(GlenndermanEntity glenn) {
		this.glenn = glenn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.glenn.isTrading() || --this.searchCooldown > 0) {
			return false;
		}
		this.searchCooldown = this.adjustedTickDelay(20);
		this.jukebox = findPlayingJukebox(this.glenn.level(), this.glenn.blockPosition());
		return this.jukebox != null;
	}

	@Override
	public boolean canContinueToUse() {
		// Checked on every goal-selector update (every other tick); it is one block lookup.
		return this.jukebox != null
			&& !this.glenn.isTrading()
			&& this.walkTicksLeft > 0
			&& isPlaying(this.glenn.level(), this.jukebox);
	}

	@Override
	public void start() {
		this.ticks = 0;
		this.walkTicksLeft = this.adjustedTickDelay(GIVE_UP_WALKING_TICKS);
		this.announced = false;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.jukebox == null) {
			return;
		}
		this.ticks++;
		Vec3 center = Vec3.atCenterOf(this.jukebox);
		if (this.glenn.distanceToSqr(center) > DANCE_DISTANCE_SQR) {
			// Give up on a jukebox he can't reach instead of walking at it forever.
			this.walkTicksLeft--;
			if (this.glenn.getNavigation().isDone() || this.ticks % 20 == 0) {
				this.glenn.getNavigation().moveTo(center.x, center.y, center.z, 1.0);
			}
			return;
		}

		this.glenn.getNavigation().stop();
		if (!this.announced) {
			this.glenn.sayLine(GlennLines.Pool.DANCE, null, false);
			this.announced = true;
		}
		// Spin: look at a point that circles around him.
		float angle = this.ticks * 0.35F;
		this.glenn.getLookControl().setLookAt(
			this.glenn.getX() + Mth.cos(angle) * 2.0, this.glenn.getEyeY(), this.glenn.getZ() + Mth.sin(angle) * 2.0, 40.0F, 30.0F
		);
		if (this.glenn.onGround() && this.glenn.getRandom().nextInt(12) == 0) {
			this.glenn.getJumpControl().jump();
		}
		if (this.ticks % 10 == 0 && this.glenn.level() instanceof ServerLevel level) {
			// Count 0 makes the first "spread" value the note colour.
			level.sendParticles(ParticleTypes.NOTE, this.glenn.getX(), this.glenn.getY() + 3.1, this.glenn.getZ(), 0,
				this.glenn.getRandom().nextInt(25) / 24.0, 0.0, 0.0, 1.0);
		}
	}

	@Override
	public void stop() {
		this.jukebox = null;
	}

	private static @Nullable BlockPos findPlayingJukebox(Level level, BlockPos around) {
		for (BlockPos pos : BlockPos.betweenClosed(
			around.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS),
			around.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS)
		)) {
			if (isPlaying(level, pos)) {
				return pos.immutable();
			}
		}
		return null;
	}

	private static boolean isPlaying(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.is(Blocks.JUKEBOX) || !state.getValue(JukeboxBlock.HAS_RECORD)) {
			return false;
		}
		return level.getBlockEntity(pos, BlockEntityTypes.JUKEBOX)
			.map(jukebox -> jukebox.getSongPlayer().isPlaying())
			.orElse(false);
	}
}
