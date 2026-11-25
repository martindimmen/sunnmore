package martidi.sunnmore.glenn;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import martidi.sunnmore.Sunnmore;

/**
 * Glenn's made-up goods. In survival they only come from trading with Glenn, except the plain Wago,
 * which anyone can craft from iron and glass. They are listed in the Sunnmøre creative tab.
 */
public final class GlennItems {
	public static final ResourceKey<EquipmentAsset> GINGER_WIG_ASSET =
		ResourceKey.create(EquipmentAssets.ROOT_ID, Sunnmore.id("ginger_wig"));

	/** Chewing gum that teleports you a short way (chorus fruit, but with a smaller range). */
	public static final Item TELEPORT_TYGGIS = register("teleport_tyggis", Item::new, withLore("teleport_tyggis")
		.food(
			new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build(),
			Consumables.defaultFood().consumeSeconds(0.8F).onConsume(new TeleportRandomlyConsumeEffect(10.0F, true)).build()
		)
		.useCooldown(1.0F)
		.stacksTo(16));

	public static final Item MYSTERY_BRUS = register("mystery_brus", MysteryBrusItem::new, withLore("mystery_brus")
		.component(DataComponents.CONSUMABLE, Consumables.defaultDrink().build())
		.stacksTo(16));

	/** A ginger wig. Counts as a disguise, so real Endermen don't mind your stare (see the item tag). */
	public static final Item GINGER_WIG = register("ginger_wig", Item::new, withLore("ginger_wig")
		.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
			.setAsset(GINGER_WIG_ASSET)
			.setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
			.build())
		.rarity(Rarity.UNCOMMON)
		.stacksTo(1));

	/** The scam deal: it is just dirt, but the purple name sells it. */
	public static final Item PREMIUM_JORD = register("premium_jord", Item::new, withLore("premium_jord")
		.rarity(Rarity.EPIC));

	/**
	 * A lever connector that clicks, and sometimes pinches (see WagoItem). Crafted from iron and glass.
	 * It wears out like a tool (durability means it doesn't stack) and is repaired with an iron ingot.
	 */
	public static final Item WAGO = register("wago", WagoItem::new, withLore("wago")
		.durability(WagoMath.WAGO_CLICKS)
		.repairable(Items.IRON_INGOT));

	/** Glenn's Wago: a rare deal, paid in glass and iron (see GlennTrades). Lasts twice as long. */
	public static final Item GLENNDERMAN_WAGO = register("glennderman_wago", WagoItem::new, withLore("glennderman_wago")
		.rarity(Rarity.UNCOMMON)
		.durability(WagoMath.GLENNDERMAN_WAGO_CLICKS)
		.repairable(Items.IRON_INGOT));

	public static final ResourceKey<CreativeModeTab> SUNNMORE_TAB_KEY =
		ResourceKey.create(Registries.CREATIVE_MODE_TAB, Sunnmore.id("sunnmore"));

	public static final CreativeModeTab SUNNMORE_TAB = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		SUNNMORE_TAB_KEY,
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.sunnmore"))
			.icon(() -> new ItemStack(GlennEntities.GLENNDERMAN_SPAWN_EGG))
			.displayItems((parameters, output) -> {
				output.accept(GlennEntities.GLENNDERMAN_SPAWN_EGG);
				output.accept(TELEPORT_TYGGIS);
				output.accept(MYSTERY_BRUS);
				output.accept(GINGER_WIG);
				output.accept(PREMIUM_JORD);
				output.accept(WAGO);
				output.accept(GLENNDERMAN_WAGO);
			})
			.build()
	);

	private GlennItems() {
	}

	/** Called from Sunnmore.onInitialize; touching the class runs the registrations above. */
	public static void register() {
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
