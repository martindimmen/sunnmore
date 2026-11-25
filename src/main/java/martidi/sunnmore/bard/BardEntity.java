package martidi.sunnmore.bard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import martidi.sunnmore.bard.WeatherForecast.Forecast;
import martidi.sunnmore.bard.goal.CheckForecastGoal;
import martidi.sunnmore.visits.FriendVisits;
import martidi.sunnmore.visits.VisitingFriend;
import org.jspecify.annotations.Nullable;

/**
 * Bård: always checking the weather report. He tells everyone what the weather will do (and he is
 * right, it comes from the game's own countdowns), and strikes harmless lightning next to anyone
 * who hits him. His weather shop is added on top of this.
 */
public class BardEntity extends PathfinderMob implements VisitingFriend {
	private static final EntityDataAccessor<Boolean> DATA_ON_PHONE = SynchedEntityData.defineId(BardEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int STROLL_INTERVAL_TICKS = 240;
	private static final int LINE_COOLDOWN_TICKS = 60;
	/** The same kind of line to the same player at most every 5 seconds, even when forced. */
	private static final int LINE_REPEAT_TICKS = 100;
	private static final int KILLER_PENALTY_TICKS = 5 * 60 * 20;
	private static final double ARRIVAL_RANGE = 64.0;
	private static final int WEATHER_WATCH_INTERVAL = 20;
	private static final int BOLT_TRIES = 8;

	private final Map<String, Long> lastLineBy = new HashMap<>();
	private long lastLineTick = -LINE_COOLDOWN_TICKS;
	private long lastBolt = BardMath.NEVER;
	private int despawnDelay;
	/** Said the "rain is coming" line for this dry spell already. */
	private boolean stressed;
	/** The weather at the last check, so a storm starting can be noticed. Null until the first check. */
	private @Nullable Weather lastSeenWeather;

	public BardEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
		// Priority 1 is staying put while someone trades with him.
		this.goalSelector.addGoal(2, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(3, new CheckForecastGoal(this));
		WaterAvoidingRandomStrollGoal stroll = new WaterAvoidingRandomStrollGoal(this, 0.6);
		stroll.setInterval(STROLL_INTERVAL_TICKS);
		this.goalSelector.addGoal(5, stroll);
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_ON_PHONE, false);
	}

	/** Synced to clients: the model raises the phone while this is true. */
	public boolean isOnPhone() {
		return this.entityData.get(DATA_ON_PHONE);
	}

	public void setOnPhone(boolean onPhone) {
		this.entityData.set(DATA_ON_PHONE, onPhone);
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		// Drops nothing.
	}

	// ---------------------------------------------------------------- getting hit

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		// Melee, or a projectile a player shot. Zombies and falls get no lightning.
		if (hurt && this.isAlive() && source.getEntity() instanceof ServerPlayer player) {
			this.onHitByPlayer(level, player);
		}
		return hurt;
	}

	protected void onHitByPlayer(ServerLevel level, ServerPlayer player) {
		long now = level.getGameTime();
		if (BardMath.canStrike(now, this.lastBolt) && strikeNear(level, player.position(), 1.0, 2.0)) {
			this.lastBolt = now;
		}
		this.sayLine(BardLines.Pool.HIT, player, false);
	}

	/**
	 * A visual-only lightning bolt somewhere between min and max blocks from the centre: flash and
	 * thunder, no damage, no fire, no charged creepers. A visual bolt still de-ages copper and
	 * powers lightning rods, so it never lands on those. Returns whether a bolt struck.
	 */
	public static boolean strikeNear(ServerLevel level, Vec3 centre, double min, double max) {
		for (int attempt = 0; attempt < BOLT_TRIES; attempt++) {
			double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
			double distance = min + level.getRandom().nextDouble() * (max - min);
			Vec3 spot = new Vec3(centre.x + Math.cos(angle) * distance, centre.y, centre.z + Math.sin(angle) * distance);
			if (!isSafeToStrike(level, spot)) {
				continue;
			}
			LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
			if (bolt == null) {
				return false;
			}
			bolt.setVisualOnly(true);
			bolt.snapTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
			return level.addFreshEntity(bolt);
		}
		return false;
	}

	/** The block the bolt "hits" (just below its feet, as LightningBolt.getStrikePosition) must not be copper or a rod. */
	private static boolean isSafeToStrike(ServerLevel level, Vec3 spot) {
		Block block = level.getBlockState(BlockPos.containing(spot.x, spot.y - 1.0E-6, spot.z)).getBlock();
		return !(block instanceof WeatheringCopper)
			&& !(block instanceof LightningRodBlock)
			&& HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block) == null;
	}

	@Override
	public void die(DamageSource source) {
		if (!this.isRemoved() && !this.dead && this.level() instanceof ServerLevel level) {
			this.onDying();
			// Kill credit covers indirect kills too: burning from Fire Aspect, a tamed wolf's last bite.
			ServerPlayer killer = source.getEntity() instanceof ServerPlayer direct ? direct
				: this.getKillCredit() instanceof ServerPlayer credited ? credited : null;
			if (killer != null) {
				this.sayLine(BardLines.Pool.DEATH, killer, true);
				killer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, KILLER_PENALTY_TICKS, 0));
				killer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, KILLER_PENALTY_TICKS, 0));
				level.getServer().getPlayerList().broadcastSystemMessage(
					Component.translatable("chat.sunnmore.bard.killed", killer.getDisplayName()).withStyle(ChatFormatting.RED),
					false
				);
			}
		}
		super.die(source);
	}

	/** Hook for the shop: stop trading when he dies. */
	protected void onDying() {
	}

	// ---------------------------------------------------------------- the weather

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.maybeLeave(level);
		if (this.tickCount % WEATHER_WATCH_INTERVAL == 0) {
			this.watchWeather(level);
		}
	}

	/** Stressed when rain is under a minute away, once per dry spell; panics when a storm starts. */
	private void watchWeather(ServerLevel level) {
		if (!BardWeather.hasSignal(level) || !BardWeather.cycleOn(level)) {
			return;
		}
		Forecast forecast = WeatherForecast.of(BardWeather.read(level.getServer()));
		if (forecast.now() != Weather.CLEAR) {
			this.stressed = false;
		}
		if (BardMath.shouldStress(forecast, this.stressed)) {
			this.stressed = true;
			this.sayLine(BardLines.Pool.STRESS, null, true);
		}
		if (this.lastSeenWeather != null && this.lastSeenWeather != Weather.STORM && forecast.now() == Weather.STORM) {
			this.sayLine(BardLines.Pool.STORM, null, true);
		}
		this.lastSeenWeather = forecast.now();
	}

	/** The weather report, to players nearby. No signal outside the Overworld; nothing to say when it is frozen. */
	public void sayForecast(@Nullable Entity about) {
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		if (!BardWeather.hasSignal(level)) {
			this.sayLine(BardLines.Pool.NO_SIGNAL, about, true);
		} else if (!BardWeather.cycleOn(level)) {
			this.sayLine(BardLines.Pool.FROZEN, about, true);
		} else {
			Forecast forecast = WeatherForecast.of(BardWeather.read(level.getServer()));
			this.speak("FORECAST", () -> BardLines.forecastLine(forecast), about, true);
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
		BardLines.sayNearby(this, BardLines.poolLine(BardLines.Pool.ARRIVE, this.getRandom(), null), null, ARRIVAL_RANGE);
	}

	/** Whether someone has his shop open. A visit never ends in the middle of a trade. */
	public boolean isTrading() {
		return false;
	}

	private void maybeLeave(ServerLevel level) {
		if (this.despawnDelay <= 0 || this.isTrading()) {
			return;
		}
		if (this.hasCustomName()) {
			// Named with a name tag: he moves in for good.
			this.despawnDelay = 0;
			return;
		}
		if (--this.despawnDelay == 0) {
			this.sayLine(BardLines.Pool.LEAVE, null, true);
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

	/** A line from a pool to players nearby; see speak for the cooldowns. Returns whether he said it. */
	public boolean sayLine(BardLines.Pool pool, @Nullable Entity about, boolean force) {
		return this.speak(pool.name(), () -> BardLines.poolLine(pool, this.getRandom(), about), about, force);
	}

	/**
	 * Unless forced, he keeps quiet for a few seconds after any line. Even forced, the same kind of
	 * line to the same player only every 5 seconds, so holding right-click doesn't flood the chat.
	 */
	private boolean speak(String kind, Supplier<Component> body, @Nullable Entity about, boolean force) {
		long now = this.level().getGameTime();
		if (!force && now - this.lastLineTick < LINE_COOLDOWN_TICKS) {
			return false;
		}
		String key = kind + "/" + (about != null ? about.getUUID() : "nobody");
		Long last = this.lastLineBy.get(key);
		if (last != null && now - last < LINE_REPEAT_TICKS) {
			return false;
		}
		this.lastLineTick = now;
		this.lastLineBy.put(key, now);
		BardLines.sayNearby(this, body.get(), about, BardLines.HEARING_RANGE);
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
