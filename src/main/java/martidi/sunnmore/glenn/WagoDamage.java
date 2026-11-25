package martidi.sunnmore.glenn;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import martidi.sunnmore.Sunnmore;

/**
 * The Wago pinch. The damage type itself is data (data/sunnmore/damage_type/wago_pinch.json); it is
 * tagged bypasses_armor and bypasses_enchantments, so armor doesn't help and is never worn down.
 */
public final class WagoDamage {
	public static final ResourceKey<DamageType> PINCH = ResourceKey.create(Registries.DAMAGE_TYPE, Sunnmore.id("wago_pinch"));

	private WagoDamage() {
	}
}
