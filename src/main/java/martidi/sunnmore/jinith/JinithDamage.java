package martidi.sunnmore.jinith;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import martidi.sunnmore.Sunnmore;

/**
 * Jinith's punch. The damage type itself is data (data/sunnmore/damage_type/jinith_punch.json);
 * it is tagged bypasses_armor, bypasses_shield and bypasses_enchantments, so armor, shields and
 * their durability are never touched.
 */
public final class JinithDamage {
	public static final ResourceKey<DamageType> PUNCH = ResourceKey.create(Registries.DAMAGE_TYPE, Sunnmore.id("jinith_punch"));

	private JinithDamage() {
	}
}
