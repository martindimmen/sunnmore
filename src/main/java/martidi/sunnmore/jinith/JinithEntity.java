package martidi.sunnmore.jinith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import martidi.sunnmore.visits.FriendVisits;
import martidi.sunnmore.visits.VisitingFriend;

import org.jspecify.annotations.Nullable;

/**
 * Jinith: a small, very buff medicine student. Friendly and heals people near him; punch him and
 * he turns red and beats you to exactly half a heart, then patches you up.
 * A PathfinderMob, not a Monster: no Peaceful despawn, no "monsters nearby" for beds, golems ignore him.
 */
public class JinithEntity extends PathfinderMob implements VisitingFriend {
	private static final EntityDataAccessor<Boolean> DATA_ANGRY = SynchedEntityData.defineId(JinithEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_FLEXING = SynchedEntityData.defineId(JinithEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int STROLL_INTERVAL_TICKS = 240;
	private static final int LINE_COOLDOWN_TICKS = 60;
	private static final int PATCH_UP_TICKS = 200;
	private static final double HEAL_RADIUS = 6.0;
	private static final double PET_CALM_RADIUS = 32.0;
	private static final double ARRIVAL_RANGE = 64.0;
	private static final int HEAL_TICKS = 100;
	private static final int HEAL_LINE_COOLDOWN_TICKS = 1200;

	private final Map<UUID, Long> lastHealLine = new HashMap<>();
	private final Map<String, Long> lastLineBy = new HashMap<>();

	private @Nullable UUID angryAt;
	private long lastProgressTick;
	private static final long NOT_FLEXING = -1L;
	private long flexStartedAt = NOT_FLEXING;
	/** When the scare phase started (target at half a heart, punches do no damage), or NEVER. */
	private long scareStartedAt = JinithMath.NEVER;
	/** Ticks until a visiting Jinith leaves. 0 means he stays (spawn egg, /summon, name tag). */
	private int despawnDelay;
	// Not Long.MIN_VALUE: "now - MIN_VALUE" overflows and would silence every unforced line.
	private long lastLineTick = -LINE_COOLDOWN_TICKS;

	public JinithEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 100.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 8.0)
			// No ATTACK_KNOCKBACK: his launch is set directly (JinithMath.launch), past the attribute's cap of 5.
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
			// Steps up full blocks like an Enderman, so a chase never stalls on a ledge.
			.add(Attributes.STEP_HEIGHT, 1.0)
			// He has to find his way to people he launched ~40 blocks away.
			.add(Attributes.FOLLOW_RANGE, JinithMath.GIVE_UP_DISTANCE);
	}

	@Override
	protected void registerGoals() {
		// No targetSelector goals: only the anger code sets his target.
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new JinithPunchGoal(this));
		WaterAvoidingRandomStrollGoal stroll = new WaterAvoidingRandomStrollGoal(this, 0.6);
		stroll.setInterval(STROLL_INTERVAL_TICKS);
		this.goalSelector.addGoal(5, stroll);
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_ANGRY, false);
		entityData.define(DATA_FLEXING, false);
	}

	/** Synced to clients: the renderer shows the red skin while this is true. */
	public boolean isAngry() {
		return this.entityData.get(DATA_ANGRY);
	}

	protected void setAngryFlag(boolean angry) {
		this.entityData.set(DATA_ANGRY, angry);
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	// ---------------------------------------------------------------- the fight

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		// Melee or a projectile they shot: the newest attacker is the one he goes after.
		if (this.isAlive() && source.getEntity() instanceof ServerPlayer player) {
			this.becomeAngryAt(player);
		}
		return hurt;
	}

	public void becomeAngryAt(ServerPlayer player) {
		// Hit during the flex: the flex is over, the fight is back on.
		this.stopFlex();
		boolean newTarget = !player.getUUID().equals(this.angryAt);
		this.angryAt = player.getUUID();
		this.lastProgressTick = this.level().getGameTime();
		this.setAngryFlag(true);
		this.setTarget(player);
		if (newTarget) {
			// A deep, angry grunt as he turns red.
			// Only with the line, so friends taking turns punching him don't get a grunt every time.
			if (this.sayLine(JinithLines.Pool.ANGRY, player, true)) {
				this.playSound(SoundEvents.PIGLIN_BRUTE_ANGRY, 1.5F, 0.9F);
			}
		}
	}

	public @Nullable ServerPlayer angryTarget() {
		if (this.angryAt != null && this.level() instanceof ServerLevel level && level.getPlayerByUUID(this.angryAt) instanceof ServerPlayer player) {
			return player;
		}
		return null;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		// Only the player who punched him; nobody else ever gets hit.
		if (!(target instanceof ServerPlayer player) || !player.getUUID().equals(this.angryAt)) {
			return false;
		}
		DamageSource source = level.damageSources().source(JinithDamage.PUNCH, this);
		if (this.scareStartedAt != JinithMath.NEVER) {
			// Scare phase: he keeps punching, they keep flying, but no damage.
			this.scarePunch(level, player, source);
			return true;
		}
		float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float healthBefore = player.getHealth() + player.getAbsorptionAmount();
		float damage = JinithMath.cappedDamage(baseDamage, player.getHealth(), player.getAbsorptionAmount());
		if (damage <= 0.0F) {
			// Already at half a heart: on to the scare phase.
			this.startScare(level, player);
			this.scarePunch(level, player, source);
			return true;
		}
		boolean hurt = player.hurtServer(level, source, damage);
		if (hurt) {
			this.launch(level, player);
			// A hit Resistance soaked up completely is no progress, or he could hunt forever.
			if (player.getHealth() + player.getAbsorptionAmount() < healthBefore) {
				this.lastProgressTick = level.getGameTime();
			}
			if (JinithMath.isFinishingBlow(baseDamage, damage, player.getHealth())) {
				this.startScare(level, player);
			} else {
				this.sayLine(JinithLines.Pool.HIT, player, false);
			}
		}
		return hurt;
	}

	/** Maximum knockback plus the hit sounds. Used for real punches and scare punches alike. */
	private void launch(ServerLevel level, ServerPlayer player) {
		// Set the speed directly, far past the vanilla knockback cap (~40 blocks).
		double[] launch = JinithMath.launch(player.getX() - this.getX(), player.getZ() - this.getZ());
		player.setDeltaMovement(launch[0], launch[1], launch[2]);
		this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
		// Send the launch to everyone, including the victim's own client (needsSync alone skips
		// the victim when they were hurt in the last half second, e.g. while burning).
		player.needsSync = true;
		player.syncVelocity = true;
		// The landing from his launch never hurts (the wind-charge rule): only his punch counts.
		player.setIgnoreFallDamageFromCurrentImpulse(true, player.position());
		JinithSafety.protect(player);
		this.setLastHurtMob(player);
		// A heavy thud plus the whoosh of a knockback hit.
		this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.2F, 1.0F);
		this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.9F);
	}

	/** A punch with no damage: the player still gets the hurt flash and flies, to scare them. */
	private void scarePunch(ServerLevel level, ServerPlayer player, DamageSource source) {
		level.broadcastDamageEvent(player, source);
		this.launch(level, player);
		this.sayLine(JinithLines.Pool.HIT, player, false);
	}

	/**
	 * The target is at half a heart. For a few seconds he keeps chasing and punching without
	 * damage (to scare them), then he flexes and patches them up. Protected all the way through.
	 */
	private void startScare(ServerLevel level, ServerPlayer player) {
		if (this.scareStartedAt != JinithMath.NEVER) {
			return;
		}
		this.scareStartedAt = level.getGameTime();
		JinithSafety.protectUntil(player, JinithMath.scareProtectionUntil(this.scareStartedAt));
	}

	private void tickScare(ServerLevel level) {
		ServerPlayer player = this.angryTarget();
		if (player == null || !player.isAlive()) {
			this.calmDown(false);
			return;
		}
		if (JinithMath.isScareOver(level.getGameTime(), this.scareStartedAt)) {
			this.startFlex(level, player);
		} else if (this.getTarget() != player && this.isAttackable(level, player)) {
			this.setTarget(player);
		}
	}

	private boolean isAttackable(ServerLevel level, ServerPlayer player) {
		return !player.isCreative() && !player.isSpectator() && level.getDifficulty() != Difficulty.PEACEFUL;
	}

	/**
	 * The player is at half a heart: he stops, stays red and flexes over them for a moment,
	 * then calms down and patches them up (tickFlex). The player is protected the whole time.
	 */
	private void startFlex(ServerLevel level, ServerPlayer player) {
		this.scareStartedAt = JinithMath.NEVER;
		this.setTarget(null);
		this.getNavigation().stop();
		this.flexStartedAt = level.getGameTime();
		this.entityData.set(DATA_FLEXING, true);
		JinithSafety.protectUntil(player, JinithMath.flexProtectionUntil(this.flexStartedAt));
		if (this.sayLine(JinithLines.Pool.FLEX, player, true)) {
			this.playSound(SoundEvents.PIGLIN_BRUTE_AMBIENT, 1.5F, 0.8F);
		}
	}

	private void tickFlex(ServerLevel level) {
		this.getNavigation().stop();
		ServerPlayer player = this.angryTarget();
		if (player != null) {
			this.getLookControl().setLookAt(player, 30.0F, 30.0F);
		}
		if (JinithMath.isFlexOver(level.getGameTime(), this.flexStartedAt)) {
			this.calmDown(true);
		}
	}

	private void stopFlex() {
		this.flexStartedAt = NOT_FLEXING;
		this.entityData.set(DATA_FLEXING, false);
	}

	/** Synced to clients: the model raises both arms while this is true. */
	public boolean isFlexing() {
		return this.entityData.get(DATA_FLEXING);
	}

	/**
	 * The fight is over for the pets too: tamed wolves (and other owned mobs) that joined in when
	 * their owner punched him stop attacking, so the friends don't lose their doctor.
	 */
	private void callOffPets() {
		for (Mob pet : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(PET_CALM_RADIUS),
			mob -> mob instanceof OwnableEntity && mob.getTarget() == this)) {
			pet.setTarget(null);
			if (pet instanceof NeutralMob neutral) {
				neutral.stopBeingAngry();
			}
		}
	}

	/** Ends the fight. With patchUp the medicine student heals the player he just beat up. */
	public void calmDown(boolean patchUp) {
		this.stopFlex();
		this.scareStartedAt = JinithMath.NEVER;
		ServerPlayer player = this.angryTarget();
		this.angryAt = null;
		this.setTarget(null);
		this.setAngryFlag(false);
		this.getNavigation().stop();
		this.callOffPets();
		if (patchUp && player != null && this.level() instanceof ServerLevel level) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PATCH_UP_TICKS, 1));
			level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.8, player.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
			// The "cured" sound: the doctor has fixed you.
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 0.5F, 1.4F);
			JinithSafety.protect(player);
			this.sayLine(JinithLines.Pool.PATCHED_UP, player, true);
		} else {
			this.sayLine(JinithLines.Pool.CALM, player, false);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.maybeLeave(level);
		if (this.isRemoved()) {
			return;
		}
		if (this.flexStartedAt != NOT_FLEXING) {
			this.tickFlex(level);
		} else if (this.scareStartedAt != JinithMath.NEVER) {
			this.tickScare(level);
		} else if (this.angryAt != null) {
			this.checkGiveUp(level);
		} else if (this.tickCount % JinithMath.HEAL_INTERVAL_TICKS == 0) {
			this.healNearbyPlayers(level);
		}
	}

	// ---------------------------------------------------------------- the doctor

	/** Medicine student: hurt players standing near him get Regeneration. Not while he is angry. */
	private void healNearbyPlayers(ServerLevel level) {
		long now = level.getGameTime();
		for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(HEAL_RADIUS),
			p -> p.isAlive() && !p.isSpectator() && p.getHealth() < p.getMaxHealth())) {
			if (this.distanceToSqr(player) > HEAL_RADIUS * HEAL_RADIUS) {
				continue;
			}
			MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
			if (!JinithMath.needsRegenRenewal(regen != null, regen != null ? regen.getDuration() : 0)) {
				continue;
			}
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, HEAL_TICKS, 0));
			level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.8, player.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
			// A soft sparkle when the healing kicks in.
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
			Long lastLine = this.lastHealLine.get(player.getUUID());
			if (lastLine == null || now - lastLine >= HEAL_LINE_COOLDOWN_TICKS) {
				this.lastHealLine.put(player.getUUID(), now);
				this.sayLine(JinithLines.Pool.HEAL, player, false);
			}
		}
	}

	private void checkGiveUp(ServerLevel level) {
		ServerPlayer player = this.angryTarget();
		if (player == null || !player.isAlive()) {
			// Logged out, changed dimension or died some other way.
			this.calmDown(false);
			return;
		}
		if (JinithMath.shouldPatchUp(player.getHealth())) {
			// Down to half a heart by something else (a mob, fire, a fall): straight to the scare phase.
			this.startScare(level, player);
			return;
		}
		boolean attackable = this.isAttackable(level, player);
		if (JinithMath.shouldGiveUp(level.getGameTime(), this.lastProgressTick, this.distanceToSqr(player), true, attackable)) {
			this.calmDown(false);
		} else if (attackable && this.getTarget() != player) {
			this.setTarget(player);
		}
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
		JinithLines.sayNearby(this, JinithLines.Pool.ARRIVE, null, ARRIVAL_RANGE);
	}

	/** A visiting Jinith leaves when his time is up, but never in the middle of a fight. */
	private void maybeLeave(ServerLevel level) {
		if (this.despawnDelay <= 0 || this.angryAt != null) {
			return;
		}
		if (this.hasCustomName()) {
			// Named with a name tag: he moves in for good.
			this.despawnDelay = 0;
			return;
		}
		if (--this.despawnDelay == 0) {
			this.sayLine(JinithLines.Pool.LEAVE, null, true);
			FriendVisits.onVisitorLeft(level, this.getType());
			level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.8, this.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
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

	/**
	 * Says a line to players nearby and returns whether he did. Unless forced, he keeps quiet for a
	 * few seconds after any line. Even forced, the same pool to the same player only every 5 seconds,
	 * so group fights and re-punching don't flood the chat.
	 */
	public boolean sayLine(JinithLines.Pool pool, @Nullable Entity about, boolean force) {
		long now = this.level().getGameTime();
		if (!force && now - this.lastLineTick < LINE_COOLDOWN_TICKS) {
			return false;
		}
		String key = pool.name() + "/" + (about != null ? about.getUUID() : "nobody");
		if (!JinithMath.canRepeatLine(now, this.lastLineBy.getOrDefault(key, JinithMath.NEVER))) {
			return false;
		}
		this.lastLineTick = now;
		this.lastLineBy.put(key, now);
		JinithLines.sayNearby(this, pool, about, JinithLines.HEARING_RANGE);
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
