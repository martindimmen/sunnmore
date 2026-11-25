package martidi.sunnmore.bard.goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

import martidi.sunnmore.bard.BardEntity;
import martidi.sunnmore.bard.BardMath;

/**
 * Every 2 to 4 minutes, when someone is around, Bård stops, stares at his phone for a few seconds
 * and then tells everyone nearby what the weather will do. Being hit or a trade interrupts him;
 * then he tries again later, without the forecast.
 */
public class CheckForecastGoal extends Goal {
	private static final double AUDIENCE_RANGE = 16.0;
	private static final long UNSET = -1L;

	private final BardEntity bard;
	private long nextCheckTick = UNSET;
	private int ticksLeft;

	public CheckForecastGoal(BardEntity bard) {
		this.bard = bard;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		long now = this.bard.level().getGameTime();
		if (this.nextCheckTick == UNSET) {
			this.scheduleNext(now);
			return false;
		}
		return now >= this.nextCheckTick
			&& this.bard.level().getNearestPlayer(this.bard, AUDIENCE_RANGE) != null;
	}

	@Override
	public void start() {
		this.bard.getNavigation().stop();
		this.bard.setOnPhone(true);
		this.ticksLeft = BardMath.PHONE_TICKS;
	}

	@Override
	public boolean canContinueToUse() {
		return this.ticksLeft > 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.ticksLeft--;
	}

	@Override
	public void stop() {
		this.bard.setOnPhone(false);
		if (this.ticksLeft <= 0) {
			this.bard.sayForecast(null);
		}
		this.scheduleNext(this.bard.level().getGameTime());
	}

	private void scheduleNext(long now) {
		int roll = this.bard.getRandom().nextInt(BardMath.CHECK_MAX - BardMath.CHECK_MIN + 1);
		this.nextCheckTick = now + BardMath.nextCheckDelay(roll);
	}
}
