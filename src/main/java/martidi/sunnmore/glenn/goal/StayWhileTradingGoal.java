package martidi.sunnmore.glenn.goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import martidi.sunnmore.glenn.GlenndermanEntity;

/** While someone has the trade screen open, Glenn stands still and looks at them. */
public class StayWhileTradingGoal extends Goal {
	private final GlenndermanEntity glenn;

	public StayWhileTradingGoal(GlenndermanEntity glenn) {
		this.glenn = glenn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		Player player = this.glenn.getTradingPlayer();
		return player != null && this.glenn.isAlive() && this.glenn.distanceToSqr(player) <= 16.0 * 16.0;
	}

	@Override
	public void start() {
		this.glenn.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		Player player = this.glenn.getTradingPlayer();
		if (player != null) {
			this.glenn.getLookControl().setLookAt(player, 30.0F, 30.0F);
		}
	}
}
