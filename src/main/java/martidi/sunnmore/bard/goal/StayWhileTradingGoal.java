package martidi.sunnmore.bard.goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import martidi.sunnmore.bard.BardEntity;

/** While someone has his shop open, Bård stands still and looks at them. A copy of Glenn's. */
public class StayWhileTradingGoal extends Goal {
	private final BardEntity bard;

	public StayWhileTradingGoal(BardEntity bard) {
		this.bard = bard;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		Player player = this.bard.getTradingPlayer();
		return player != null && this.bard.isAlive() && this.bard.distanceToSqr(player) <= 16.0 * 16.0;
	}

	@Override
	public void start() {
		this.bard.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		Player player = this.bard.getTradingPlayer();
		if (player != null) {
			this.bard.getLookControl().setLookAt(player, 30.0F, 30.0F);
		}
	}
}
