package martidi.sunnmore.jinith;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Vanilla melee chase, but super fast and with a punch every half second instead of every second.
 * MeleeAttackGoal keeps its own 20-tick cooldown private, so this goal keeps its own timer.
 */
public class JinithPunchGoal extends MeleeAttackGoal {
	private long lastPunch = JinithMath.NEVER;

	public JinithPunchGoal(JinithEntity jinith) {
		super(jinith, JinithMath.CHASE_SPEED, true);
	}

	@Override
	protected boolean canPerformAttack(LivingEntity target) {
		return JinithMath.canPunchAgain(this.mob.level().getGameTime(), this.lastPunch)
			&& this.mob.isWithinMeleeAttackRange(target)
			&& this.mob.getSensing().hasLineOfSight(target);
	}

	@Override
	protected void resetAttackCooldown() {
		super.resetAttackCooldown();
		this.lastPunch = this.mob.level().getGameTime();
	}
}
