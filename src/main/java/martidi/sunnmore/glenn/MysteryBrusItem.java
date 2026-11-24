package martidi.sunnmore.glenn;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Glenn's soda: nobody knows what is in it. Each sip gives one random effect. */
public class MysteryBrusItem extends Item {
	private static final List<Supplier<MobEffectInstance>> EFFECTS = List.of(
		() -> new MobEffectInstance(MobEffects.SPEED, 30 * 20, 1),
		() -> new MobEffectInstance(MobEffects.JUMP_BOOST, 30 * 20, 1),
		() -> new MobEffectInstance(MobEffects.LEVITATION, 5 * 20, 0),
		() -> new MobEffectInstance(MobEffects.NAUSEA, 10 * 20, 0)
	);

	public MysteryBrusItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
		if (!level.isClientSide()) {
			entity.addEffect(EFFECTS.get(entity.getRandom().nextInt(EFFECTS.size())).get());
		}
		return super.finishUsingItem(itemStack, level, entity);
	}
}
