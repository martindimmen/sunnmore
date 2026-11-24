package martidi.sunnmore.glenn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import martidi.sunnmore.glenn.goal.DanceToJukeboxGoal;
import martidi.sunnmore.glenn.goal.GreetStarerGoal;
import martidi.sunnmore.glenn.goal.StayWhileTradingGoal;
import martidi.sunnmore.glenn.goal.SugarTemptGoal;
import martidi.sunnmore.visits.FriendVisits;
import martidi.sunnmore.visits.VisitingFriend;

import org.jspecify.annotations.Nullable;

/**
 * Glenn: a tall ginger Enderman who is friendly instead of hostile. He greets players who stare at
 * him, sells silly things for sugar, follows sugar, dances to jukeboxes and hops away when hit.
 * The vanilla Enderman supplies the model and sounds; here we remove everything that makes it
 * aggressive and add the rest.
 */
public class GlenndermanEntity extends Enderman implements Merchant, VisitingFriend {
	private static final int LINE_COOLDOWN_TICKS = 60;
	private static final int GREET_COOLDOWN_TICKS = 60 * 20;
	private static final int KILLER_PENALTY_TICKS = 5 * 60 * 20;
	private static final double ARRIVAL_RANGE = 64.0;
	/** Vanilla default is 120 (a 1-in-120 chance per check to start a walk); twice as calm. */
	private static final int STROLL_INTERVAL_TICKS = 240;
	private static final DustParticleOptions GINGER_SPARKLE = new DustParticleOptions(0xE27024, 1.0F);

	private @Nullable Player tradingPlayer;
	private boolean boughtThisVisit;
	private boolean quietStop;
	private @Nullable MerchantOffers offers;
	private long offersDay = -1L;
	/** Ticks until a visiting Glenn leaves. 0 means he stays (spawn egg, /summon, name tag). */
	private int despawnDelay;
	// Not Long.MIN_VALUE: "now - MIN_VALUE" overflows and would silence every unforced line.
	private long lastLineTick = -LINE_COOLDOWN_TICKS;
	private final Map<UUID, Long> lastGreeted = new HashMap<>();

	public GlenndermanEntity(EntityType<? extends Enderman> type, Level level) {
		super(type, level);
		this.xpReward = 0;
		this.setCarriedBlock(Blocks.CAKE.defaultBlockState());
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Enderman.createAttributes()
			.add(Attributes.ATTACK_DAMAGE, 0.0)
			// TemptGoal reads this attribute and crashes without it.
			.add(Attributes.TEMPT_RANGE, 10.0);
	}

	@Override
	protected void registerGoals() {
		// Deliberately no targetSelector goals and no melee goal: Glenn never attacks, and
		// looking at him does not anger him (that stare goal lives in targetSelector).
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new StayWhileTradingGoal(this));
		this.goalSelector.addGoal(2, new DanceToJukeboxGoal(this));
		this.goalSelector.addGoal(3, new GreetStarerGoal(this));
		this.goalSelector.addGoal(4, new SugarTemptGoal(this));
		// Slow and seldom wandering, so he doesn't drift off while friends are around.
		WaterAvoidingRandomStrollGoal stroll = new WaterAvoidingRandomStrollGoal(this, 0.6);
		stroll.setInterval(STROLL_INTERVAL_TICKS);
		this.goalSelector.addGoal(7, stroll);
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	// ---------------------------------------------------------------- friendly body

	@Override
	public boolean isSensitiveToWater() {
		// Vanilla Endermen take damage in rain and water. Glenn can stand in the rain.
		return false;
	}

	@Override
	protected boolean teleport() {
		// The no-argument teleport is the long random one: vanilla uses it in daylight, in water and
		// when hit. Glenn never does that; when hit he does a short hop instead (hopNearby).
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	@Override
	public void setTarget(@Nullable LivingEntity target) {
		// Never pick a target: no anger state and no creepy face.
		super.setTarget(null);
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		// He is a Monster in code, so golems would attack him. This makes every mob leave him alone.
		return false;
	}

	@Override
	public boolean isPreventingPlayerRest(ServerLevel level, Player player) {
		// Without this, beds say "monsters nearby" when Glenn is close.
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		// Drops nothing, not even the cake he carries.
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
		boolean invulnerable = this.isInvulnerableTo(level, source);
		// Projectiles never hurt him (vanilla Enderman rule); melee does.
		boolean hurt = super.hurtServer(level, source, damage);
		if (!invulnerable && this.isAlive() && (projectile || hurt && source.getEntity() != null)) {
			level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getEyeY(), this.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
			this.sayLine(projectile ? GlennLines.Pool.DODGE : GlennLines.Pool.HURT, source.getEntity(), false);
			this.stopTrading();
			this.hopNearby();
		}
		return hurt;
	}

	/** A short teleport, 2 to 6 blocks away, instead of the vanilla 32-block one. */
	private void hopNearby() {
		for (int attempt = 0; attempt < 16; attempt++) {
			double dx = (2.0 + this.random.nextDouble() * 4.0) * (this.random.nextBoolean() ? 1 : -1);
			double dz = (2.0 + this.random.nextDouble() * 4.0) * (this.random.nextBoolean() ? 1 : -1);
			double dy = this.random.nextInt(7) - 3;
			if (this.teleport(this.getX() + dx, this.getY() + dy, this.getZ() + dz)) {
				return;
			}
		}
	}

	@Override
	public void die(DamageSource source) {
		if (!this.isRemoved() && !this.dead && this.level() instanceof ServerLevel level) {
			this.stopTrading();
			// Kill credit covers indirect kills too: burning from Fire Aspect, a tamed wolf's last bite.
			ServerPlayer killer = source.getEntity() instanceof ServerPlayer direct ? direct
				: this.getKillCredit() instanceof ServerPlayer credited ? credited : null;
			if (killer != null) {
				this.sayLine(GlennLines.Pool.DEATH, killer, true);
				killer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, KILLER_PENALTY_TICKS, 0));
				killer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, KILLER_PENALTY_TICKS, 0));
				level.getServer().getPlayerList().broadcastSystemMessage(
					Component.translatable("chat.sunnmore.glenn.killed", killer.getDisplayName()).withStyle(ChatFormatting.RED),
					false
				);
			}
		}
		super.die(source);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			// A faint ginger sparkle now and then (the purple portal particles are removed by EndermanMixin).
			if (this.random.nextInt(20) == 0) {
				this.level().addParticle(GINGER_SPARKLE, this.getRandomX(0.5), this.getY() + 2.4 + this.random.nextDouble() * 0.5,
					this.getRandomZ(0.5), 0.0, 0.0, 0.0);
			}
		} else {
			this.maybeLeave();
		}
	}

	// ---------------------------------------------------------------- chat

	/** Says a line to players nearby. Unless forced, he keeps quiet for a few seconds after the last line. */
	public void sayLine(GlennLines.Pool pool, @Nullable Entity about, boolean force) {
		long now = this.level().getGameTime();
		if (!force && now - this.lastLineTick < LINE_COOLDOWN_TICKS) {
			return;
		}
		this.lastLineTick = now;
		GlennLines.sayNearby(this, pool, about, GlennLines.HEARING_RANGE);
	}

	public boolean canGreet(Player player) {
		Long last = this.lastGreeted.get(player.getUUID());
		return last == null || this.level().getGameTime() - last >= GREET_COOLDOWN_TICKS;
	}

	public void greet(Player player) {
		this.lastGreeted.put(player.getUUID(), this.level().getGameTime());
		boolean regular = GlennLoyalty.level(player) >= 3;
		this.sayLine(regular ? GlennLines.Pool.GREET_REGULAR : GlennLines.Pool.GREET, player, true);
		if (player instanceof ServerPlayer serverPlayer) {
			GlennLoyalty.award(serverPlayer, GlennLoyalty.ADVANCEMENT_MET);
		}
	}

	// ---------------------------------------------------------------- visits

	@Override
	public void setDespawnDelay(int ticks) {
		this.despawnDelay = ticks;
	}

	/** True for a Glenn who came by himself and will leave again (not egg, /summon or name-tagged ones). */
	@Override
	public boolean isVisiting() {
		return this.despawnDelay > 0 && !this.hasCustomName();
	}

	@Override
	public void sayArrival() {
		// Reaches further than normal chat, so players know he is around.
		GlennLines.sayNearby(this, GlennLines.Pool.ARRIVE, null, ARRIVAL_RANGE);
	}

	private void maybeLeave() {
		if (this.despawnDelay <= 0 || this.isTrading()) {
			return;
		}
		if (this.hasCustomName()) {
			// Named with a name tag: he moves in for good.
			this.despawnDelay = 0;
			return;
		}
		if (--this.despawnDelay == 0 && this.level() instanceof ServerLevel level) {
			this.sayLine(GlennLines.Pool.LEAVE, null, true);
			FriendVisits.onVisitorLeft(level, this.getType());
			level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.5, this.getZ(), 60, 0.4, 1.2, 0.4, 0.2);
			this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
			this.discard();
		}
	}

	// ---------------------------------------------------------------- trading

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (held.is(GlennEntities.GLENNDERMAN_SPAWN_EGG) || !this.isAlive() || this.isTrading()) {
			return super.mobInteract(player, hand);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			MerchantOffers current = this.getOffers();
			if (current.stream().allMatch(MerchantOffer::isOutOfStock)) {
				// Not forced: holding right-click repeats this every few ticks.
				this.sayLine(GlennLines.Pool.SOLDOUT, player, false);
				return InteractionResult.CONSUME;
			}
			this.startTrading(serverPlayer);
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	private void startTrading(ServerPlayer player) {
		// Counts as having said hi, so he doesn't greet you right after you close the screen.
		this.lastGreeted.put(player.getUUID(), this.level().getGameTime());
		this.applyStamkundePrices(player);
		this.setTradingPlayer(player);
		this.openTradingScreen(player, this.getDisplayName(), GlennLoyalty.level(player));
	}

	/** Stamkunde discount: (level - 1) sugar off every deal, for whoever is trading right now. */
	private void applyStamkundePrices(Player player) {
		int discount = StamkundeMath.discount(GlennLoyalty.level(player));
		for (MerchantOffer offer : this.getOffers()) {
			offer.setSpecialPriceDiff(-discount);
		}
	}

	private void resetPrices() {
		if (this.offers != null) {
			for (MerchantOffer offer : this.offers) {
				offer.resetSpecialPriceDiff();
			}
		}
	}

	public boolean isTrading() {
		return this.tradingPlayer != null;
	}

	/** Ends trading without a goodbye line (he was hit, died or is leaving). */
	private void stopTrading() {
		this.quietStop = true;
		this.setTradingPlayer(null);
		this.quietStop = false;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		Player previous = this.tradingPlayer;
		this.tradingPlayer = player;
		if (player != null && previous == null) {
			this.boughtThisVisit = false;
		}
		if (player == null && previous != null) {
			this.resetPrices();
			if (this.isAlive() && !this.quietStop) {
				// Closed the screen: a goodbye after a purchase, a jab if you bought nothing.
				if (this.boughtThisVisit) {
					this.sayLine(GlennLines.Pool.FAREWELL, previous, false);
				} else {
					this.sayLine(GlennLines.Pool.NO_BUY, previous, true);
				}
			}
		}
	}

	@Override
	public @Nullable Player getTradingPlayer() {
		return this.tradingPlayer;
	}

	@Override
	public MerchantOffers getOffers() {
		if (!(this.level() instanceof ServerLevel level)) {
			// The client uses its own copy from the trade screen packet.
			return this.offers != null ? this.offers : new MerchantOffers();
		}
		long day = GlennTrades.currentDay(level);
		// Never swap the deals while someone has the screen open.
		if (this.offers == null || (day != this.offersDay && !this.isTrading())) {
			this.offers = GlennTrades.rollLineup(this.random);
			this.offersDay = day;
		}
		return this.offers;
	}

	@Override
	public void overrideOffers(MerchantOffers offers) {
		// Client-side only in vanilla; Glenn's offers always come from the server.
	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
		this.boughtThisVisit = true;
		if (!(this.tradingPlayer instanceof ServerPlayer player) || !(this.level() instanceof ServerLevel level)) {
			return;
		}
		int trades = GlennLoyalty.addTrade(player);
		GlennLoyalty.award(player, GlennLoyalty.ADVANCEMENT_MET);
		if (StamkundeMath.becameStamkunde(trades)) {
			GlennLoyalty.award(player, GlennLoyalty.ADVANCEMENT_STAMKUNDE);
		}
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 2.2, this.getZ(), 8, 0.4, 0.5, 0.4, 0.0);

		boolean leveledUp = StamkundeMath.leveledUp(trades);
		if (leveledUp) {
			this.sayLine(GlennLines.Pool.LEVELUP, player, true);
			this.applyStamkundePrices(player);
		} else if (offer.getResult().is(GlennItems.PREMIUM_JORD)) {
			this.sayLine(GlennLines.Pool.SCAM, player, false);
		} else {
			this.sayLine(GlennLines.Pool.TRADE, player, false);
		}
		if (player.containerMenu instanceof MerchantMenu menu) {
			if (leveledUp) {
				menu.updateSellItem();
			}
			// Refresh the screen: new XP on the progress bar, new prices after a level-up.
			player.sendMerchantOffers(menu.containerId, this.getOffers(), GlennLoyalty.level(player),
				GlennLoyalty.xp(player), this.showProgressBar(), this.canRestock());
		}
	}

	@Override
	public void notifyTradeUpdated(ItemStack itemStack) {
	}

	@Override
	public int getVillagerXp() {
		return this.tradingPlayer != null ? GlennLoyalty.xp(this.tradingPlayer) : 0;
	}

	@Override
	public void overrideXp(int xp) {
	}

	@Override
	public boolean showProgressBar() {
		return true;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.ENDERMAN_AMBIENT;
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tradingPlayer == player && this.isAlive() && player.isWithinEntityInteractionRange(this, 4.0);
	}

	// ---------------------------------------------------------------- saving

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.offers != null) {
			output.store("GlennOffers", MerchantOffers.CODEC, this.offers);
		}
		output.putLong("GlennOffersDay", this.offersDay);
		output.putInt("DespawnDelay", this.despawnDelay);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.offers = input.read("GlennOffers", MerchantOffers.CODEC).orElse(null);
		this.offersDay = input.getLongOr("GlennOffersDay", -1L);
		this.despawnDelay = input.getIntOr("DespawnDelay", 0);
		if (this.getCarriedBlock() == null) {
			// Glenns saved before he got his cake.
			this.setCarriedBlock(Blocks.CAKE.defaultBlockState());
		}
	}
}
