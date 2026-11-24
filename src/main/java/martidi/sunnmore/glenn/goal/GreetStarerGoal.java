package martidi.sunnmore.glenn.goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import martidi.sunnmore.glenn.GlenndermanEntity;

import org.jspecify.annotations.Nullable;

/**
 * Look at Glenn and he notices: he walks over and says "Yppa du eller?". Uses the same stare test
 * as a vanilla Enderman, but instead of getting angry he comes to say hi.
 */
public class GreetStarerGoal extends Goal {
	private static final double NOTICE_RANGE = 16.0;
	private static final double GREET_DISTANCE_SQR = 3.0 * 3.0;
	private static final int GIVE_UP_TICKS = 200;
	/** How long he stays with you after the greeting. */
	private static final int HANG_AROUND_TICKS = 30 * 20;
	/** While hanging around he catches up when you are further away than this. */
	private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;

	private final GlenndermanEntity glenn;
	private @Nullable Player target;
	private int ticksLeft;
	private boolean greeted;

	public GreetStarerGoal(GlenndermanEntity glenn) {
		this.glenn = glenn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.glenn.isTrading() || this.glenn.getRandom().nextInt(5) != 0) {
			return false;
		}
		this.target = this.findStarer();
		return this.target != null;
	}

	private @Nullable Player findStarer() {
		Player nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Player player : this.glenn.level().getEntitiesOfClass(
			Player.class,
			this.glenn.getBoundingBox().inflate(NOTICE_RANGE),
			p -> p.isAlive() && !p.isSpectator() && this.glenn.canGreet(p)
		)) {
			double distance = this.glenn.distanceToSqr(player);
			if (distance < nearestDistance && distance <= NOTICE_RANGE * NOTICE_RANGE
				&& this.glenn.isLookingAtMe(player, 0.025, true, false, this.glenn.getEyeY())) {
				nearest = player;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	@Override
	public void start() {
		this.ticksLeft = this.adjustedTickDelay(GIVE_UP_TICKS);
		this.greeted = false;
		if (this.target != null) {
			this.glenn.getNavigation().moveTo(this.target, 1.0);
		}
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null
			&& this.target.isAlive()
			&& !this.glenn.isTrading()
			&& this.ticksLeft > 0
			&& this.glenn.distanceToSqr(this.target) <= NOTICE_RANGE * NOTICE_RANGE * 4.0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) {
			return;
		}
		this.ticksLeft--;
		this.glenn.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
		double distanceSqr = this.glenn.distanceToSqr(this.target);
		if (!this.greeted) {
			if (distanceSqr <= GREET_DISTANCE_SQR) {
				this.glenn.getNavigation().stop();
				this.glenn.greet(this.target);
				this.greeted = true;
				// After saying hi he hangs around, so you have time to trade.
				this.ticksLeft = this.adjustedTickDelay(HANG_AROUND_TICKS);
			} else if (this.glenn.getNavigation().isDone() || this.ticksLeft % 20 == 0) {
				this.glenn.getNavigation().moveTo(this.target, 1.0);
			}
		} else if (distanceSqr > FOLLOW_DISTANCE_SQR) {
			if (this.glenn.getNavigation().isDone() || this.ticksLeft % 20 == 0) {
				this.glenn.getNavigation().moveTo(this.target, 0.8);
			}
		} else {
			this.glenn.getNavigation().stop();
		}
	}

	@Override
	public void stop() {
		this.target = null;
		this.glenn.getNavigation().stop();
	}
}
