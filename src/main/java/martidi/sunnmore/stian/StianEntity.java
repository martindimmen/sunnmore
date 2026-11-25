package martidi.sunnmore.stian;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import martidi.sunnmore.stian.goal.BuildChurchGoal;
import martidi.sunnmore.visits.FriendVisits;
import martidi.sunnmore.visits.VisitingFriend;
import org.jspecify.annotations.Nullable;

/**
 * Stian: a happy drunk who builds churches. Always a bit tipsy (a crooked walk, hiccups, the odd
 * trip onto his backside), always smoking (puffs only, never fire). Builds a church at a nearby
 * Kyrkjetomt from its materials (BuildChurchGoal). Hit him and he throws his can at you.
 */
public class StianEntity extends PathfinderMob implements VisitingFriend {
	private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(StianEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SMOKING = SynchedEntityData.defineId(StianEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int STROLL_INTERVAL_TICKS = 200;
	private static final int LINE_COOLDOWN_TICKS = 60;
	/** The same kind of line to the same player at most every 5 seconds, even when forced. */
	private static final int LINE_REPEAT_TICKS = 100;
	private static final int KILLER_PENALTY_TICKS = 5 * 60 * 20;
	private static final double ARRIVAL_RANGE = 64.0;
	/** How hard the crooked walk pushes sideways. */
	private static final double DRIFT = 0.04;

	private final Map<String, Long> lastLineBy = new HashMap<>();
	private long lastLineTick = -LINE_COOLDOWN_TICKS;
	private long lastThrow = StianMath.NEVER;
	private long nextHiccup;
	private long nextTrip;
	private long nextPuff;
	private long sitUntil;
	private int despawnDelay;
	/** Set by the building goal: true while the plot holds what the next block needs. */
	private boolean building;

	public StianEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new BuildChurchGoal(this));
		WaterAvoidingRandomStrollGoal stroll = new WaterAvoidingRandomStrollGoal(this, 0.6);
		stroll.setInterval(STROLL_INTERVAL_TICKS);
		this.goalSelector.addGoal(5, stroll);
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_SITTING, false);
		entityData.define(DATA_SMOKING, false);
	}

	/** Synced: sitting on his backside after a trip. */
	public boolean isSitting() {
		return this.entityData.get(DATA_SITTING);
	}

	private void setSitting(boolean sitting) {
		this.entityData.set(DATA_SITTING, sitting);
	}

	/** Synced: on a røykepause, hand at his mouth. */
	public boolean isSmoking() {
		return this.entityData.get(DATA_SMOKING);
	}

	public void setSmoking(boolean smoking) {
		this.entityData.set(DATA_SMOKING, smoking);
	}

	public boolean isBuilding() {
		return this.building;
	}

	public void setBuilding(boolean building) {
		this.building = building;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		// Drops nothing.
	}

	// ---------------------------------------------------------------- tipsy and smoking

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.maybeLeave(level);
		long now = level.getGameTime();
		if (this.nextHiccup == 0) {
			this.nextHiccup = now + this.roll(StianMath.HICCUP_MIN, StianMath.HICCUP_MAX);
			this.nextTrip = now + this.roll(StianMath.TRIP_MIN, StianMath.TRIP_MAX);
			this.nextPuff = now + this.roll(StianMath.PUFF_MIN, StianMath.PUFF_MAX);
		}
		if (this.isSitting()) {
			this.getNavigation().stop();
			if (now >= this.sitUntil) {
				this.setSitting(false);
			}
		} else if (now >= this.nextTrip && this.onGround()) {
			this.nextTrip = now + this.roll(StianMath.TRIP_MIN, StianMath.TRIP_MAX);
			this.sitUntil = now + StianMath.SIT_TICKS;
			this.setSitting(true);
			this.getNavigation().stop();
			this.sayLine(StianLines.Pool.TRIP, null, false);
		}
		if (now >= this.nextHiccup) {
			this.nextHiccup = now + this.roll(StianMath.HICCUP_MIN, StianMath.HICCUP_MAX);
			level.sendParticles(ParticleTypes.POOF, this.getX(), this.getEyeY(), this.getZ(), 2, 0.1, 0.05, 0.1, 0.0);
			this.playSound(SoundEvents.PLAYER_BURP, 0.6F, 1.7F);
			if (this.random.nextInt(3) == 0) {
				this.sayLine(StianLines.Pool.HICCUP, null, false);
			}
		}
		if (now >= this.nextPuff) {
			this.nextPuff = now + this.roll(StianMath.PUFF_MIN, StianMath.PUFF_MAX);
			this.puff(level, 2);
		}
		// The crooked walk: a small sideways nudge now and then while he walks.
		if (!this.isSitting() && this.getNavigation().isInProgress() && this.random.nextInt(8) == 0) {
			Vec3 motion = this.getDeltaMovement();
			Vec3 side = new Vec3(-motion.z, 0.0, motion.x);
			if (side.lengthSqr() > 1.0E-6) {
				side = side.normalize().scale(this.random.nextBoolean() ? DRIFT : -DRIFT);
				this.setDeltaMovement(motion.add(side));
			}
		}
	}

	/** Smoke from the corner of his mouth. Particles only: smoking never starts a fire. */
	public void puff(ServerLevel level, int count) {
		Vec3 look = this.getLookAngle();
		level.sendParticles(ParticleTypes.SMOKE, this.getX() + look.x * 0.3, this.getEyeY() - 0.1, this.getZ() + look.z * 0.3,
			count, 0.03, 0.03, 0.03, 0.01);
	}

	public int roll(int min, int max) {
		return StianMath.between(min, max, this.random.nextInt(max - min + 1));
	}

	// ---------------------------------------------------------------- getting hit

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt && this.isAlive() && source.getEntity() instanceof ServerPlayer player && player.level() == level) {
			long now = level.getGameTime();
			if (StianMath.canThrow(now, this.lastThrow)) {
				this.lastThrow = now;
				this.throwCanAt(level, player);
			}
			this.sayLine(StianLines.Pool.HIT, player, false);
		}
		return hurt;
	}

	/** Throws his can at the player: a klonk and a push, no damage, and the can is gone afterwards. */
	private void throwCanAt(ServerLevel level, ServerPlayer target) {
		double dx = target.getX() - this.getX();
		double dz = target.getZ() - this.getZ();
		double dy = target.getEyeY() - 0.6;
		double lift = Math.sqrt(dx * dx + dz * dz) * 0.15;
		this.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
		Projectile.spawnProjectile(new ThrownBjorneunge(level, this), level, new ItemStack(StianItems.BJORNEUNGE),
			can -> can.shoot(dx, dy + lift - can.getY(), dz, 1.2F, 2.0F));
	}

	@Override
	public void die(DamageSource source) {
		if (!this.isRemoved() && !this.dead && this.level() instanceof ServerLevel level) {
			// Kill credit covers indirect kills too: burning from Fire Aspect, a tamed wolf's last bite.
			ServerPlayer killer = source.getEntity() instanceof ServerPlayer direct ? direct
				: this.getKillCredit() instanceof ServerPlayer credited ? credited : null;
			if (killer != null) {
				this.sayLine(StianLines.Pool.DEATH, killer, true);
				killer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, KILLER_PENALTY_TICKS, 0));
				killer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, KILLER_PENALTY_TICKS, 0));
				level.getServer().getPlayerList().broadcastSystemMessage(
					Component.translatable("chat.sunnmore.stian.killed", killer.getDisplayName()).withStyle(ChatFormatting.RED),
					false
				);
			}
		}
		super.die(source);
	}

	// ---------------------------------------------------------------- visits

	@Override
	public void setDespawnDelay(int ticks) {
		this.despawnDelay = ticks;
	}

	@Override
	public boolean isVisiting() {
		return this.despawnDelay > 0 && !this.hasCustomName();
	}

	@Override
	public void sayArrival() {
		StianLines.sayNearby(this, StianLines.line(StianLines.Pool.ARRIVE, this.getRandom(), null, null), null, ARRIVAL_RANGE);
	}

	/** A visit ends when his time is up, but never in the middle of building with materials at hand. */
	private void maybeLeave(ServerLevel level) {
		if (this.despawnDelay <= 0 || this.building) {
			return;
		}
		if (this.hasCustomName()) {
			// Named with a name tag: he moves in for good.
			this.despawnDelay = 0;
			return;
		}
		if (--this.despawnDelay == 0) {
			this.sayLine(StianLines.Pool.LEAVE, null, true);
			FriendVisits.onVisitorLeft(level, this.getType());
			level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.9, this.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
			this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.8F);
			this.discard();
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("DespawnDelay", this.despawnDelay);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.despawnDelay = input.getIntOr("DespawnDelay", 0);
	}

	// ---------------------------------------------------------------- chat

	public boolean sayLine(StianLines.Pool pool, @Nullable Entity about, boolean force) {
		return this.sayLine(pool, about, null, force);
	}

	/**
	 * Unless forced, he keeps quiet for a few seconds after any line. Even forced, the same kind of
	 * line to the same player only every 5 seconds.
	 */
	public boolean sayLine(StianLines.Pool pool, @Nullable Entity about, @Nullable Component arg, boolean force) {
		long now = this.level().getGameTime();
		if (!force && now - this.lastLineTick < LINE_COOLDOWN_TICKS) {
			return false;
		}
		String key = pool.name() + "/" + (about != null ? about.getUUID() : "nobody") + "/" + (arg != null ? arg.getString() : "");
		Long last = this.lastLineBy.get(key);
		if (last != null && now - last < LINE_REPEAT_TICKS) {
			return false;
		}
		this.lastLineTick = now;
		this.lastLineBy.put(key, now);
		StianLines.sayNearby(this, StianLines.line(pool, this.getRandom(), about, arg), about, StianLines.HEARING_RANGE);
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}
}
