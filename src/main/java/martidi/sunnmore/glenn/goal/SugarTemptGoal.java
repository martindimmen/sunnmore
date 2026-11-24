package martidi.sunnmore.glenn.goal;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Items;

import martidi.sunnmore.glenn.GlennLines;
import martidi.sunnmore.glenn.GlenndermanEntity;

/** Hold sugar and Glenn follows you, like a cow follows wheat. He comments on it when he starts. */
public class SugarTemptGoal extends TemptGoal {
	private final GlenndermanEntity glenn;

	public SugarTemptGoal(GlenndermanEntity glenn) {
		super(glenn, 1.0, stack -> stack.is(Items.SUGAR), false);
		this.glenn = glenn;
	}

	@Override
	public boolean canUse() {
		return !this.glenn.isTrading() && super.canUse();
	}

	@Override
	public void start() {
		super.start();
		if (this.player != null) {
			this.glenn.sayLine(GlennLines.Pool.SUGAR, this.player, false);
		}
	}
}
