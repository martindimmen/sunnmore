package martidi.sunnmore.stian;

import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.glenn.GlennItems;

/** Stian's beer. Crafted (iron nugget + wheat + sugar); in his plot it speeds up the building. */
public final class StianItems {
	private static final ResourceKey<Item> BJORNEUNGE_KEY = ResourceKey.create(Registries.ITEM, Sunnmore.id("bjorneunge"));

	/** A can of Isbjørn pils. Drink it and the world wobbles for a few seconds. */
	public static final Item BJORNEUNGE = Registry.register(BuiltInRegistries.ITEM, BJORNEUNGE_KEY, new Item(new Item.Properties()
		.setId(BJORNEUNGE_KEY)
		.component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.sunnmore.bjorneunge.lore"))))
		.food(
			new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).alwaysEdible().build(),
			Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 100))).build()
		)
		.stacksTo(16)));

	private StianItems() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> output.accept(BJORNEUNGE));
	}
}
