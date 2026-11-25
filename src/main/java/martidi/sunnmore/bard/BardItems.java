package martidi.sunnmore.bard;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.glenn.GlennItems;

/**
 * Bård's goods. No recipes: in survival they only come from his shop. Both are in the Sunnmøre tab.
 */
public final class BardItems {
	/** The receipt for a weather purchase. Right-click it for the forecast. */
	public static final Item VAERMELDING = register("vaermelding", VaermeldingItem::new, withLore("vaermelding")
		.useCooldown(1.0F)
		.stacksTo(16));

	/** His homemade bread: better than vanilla bread (5 hunger), as filling as steak. */
	public static final Item HEIMEBAKT_BROD = register("heimebakt_brod", Item::new, withLore("heimebakt_brod")
		.food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));

	private BardItems() {
	}

	/** Called from BardEntities.register, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> {
			output.accept(VAERMELDING);
			output.accept(HEIMEBAKT_BROD);
		});
	}

	private static Item.Properties withLore(String name) {
		return new Item.Properties().component(
			DataComponents.LORE,
			new ItemLore(List.of(Component.translatable("item.sunnmore." + name + ".lore")))
		);
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Sunnmore.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}
}
