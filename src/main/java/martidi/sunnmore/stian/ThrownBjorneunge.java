package martidi.sunnmore.stian;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Stian's can, thrown at whoever hits him: a "klonk" and a small push, never damage. It always
 * disappears on impact, so hitting him never gives free beer.
 */
public class ThrownBjorneunge extends ThrowableItemProjectile {
	private static final double PUSH = 0.5;

	public ThrownBjorneunge(EntityType<? extends ThrownBjorneunge> type, Level level) {
		super(type, level);
	}

	public ThrownBjorneunge(Level level, LivingEntity owner) {
		super(StianEntities.THROWN_BJORNEUNGE, owner, level, new ItemStack(StianItems.BJORNEUNGE));
	}

	@Override
	protected Item getDefaultItem() {
		return StianItems.BJORNEUNGE;
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		Entity target = hitResult.getEntity();
		Vec3 flight = this.getDeltaMovement();
		Vec3 push = new Vec3(flight.x, 0.0, flight.z);
		if (push.lengthSqr() > 1.0E-4) {
			push = push.normalize().scale(PUSH);
			target.push(push.x, 0.15, push.z);
			// Send the push to everyone, the target's own client included (see JinithEntity.launch).
			target.needsSync = true;
			if (target instanceof ServerPlayer player) {
				player.syncVelocity = true;
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel level) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.COPPER_HIT, SoundSource.NEUTRAL, 1.0F, 1.6F);
			this.discard();
		}
	}
}
