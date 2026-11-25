package martidi.sunnmore.stian;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.glenn.GlennItems;

public final class StianEntities {
	public static final ResourceKey<EntityType<?>> STIAN_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Sunnmore.id("stian"));
	public static final ResourceKey<EntityType<?>> THROWN_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Sunnmore.id("thrown_bjorneunge"));

	// Player-sized. No loot table: he drops nothing.
	public static final EntityType<StianEntity> STIAN = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		STIAN_KEY,
		EntityType.Builder.of(StianEntity::new, MobCategory.CREATURE)
			.noLootTable()
			.sized(0.6F, 1.95F)
			.eyeHeight(1.62F)
			.clientTrackingRange(8)
			.build(STIAN_KEY)
	);

	/** The can he throws when you hit him. */
	public static final EntityType<ThrownBjorneunge> THROWN_BJORNEUNGE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		THROWN_KEY,
		EntityType.Builder.<ThrownBjorneunge>of(ThrownBjorneunge::new, MobCategory.MISC)
			.noLootTable()
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(THROWN_KEY)
	);

	public static final ResourceKey<Item> STIAN_EGG_KEY = ResourceKey.create(Registries.ITEM, Sunnmore.id("stian_spawn_egg"));

	public static final Item STIAN_SPAWN_EGG = Registry.register(
		BuiltInRegistries.ITEM,
		STIAN_EGG_KEY,
		new SpawnEggItem(new Item.Properties().setId(STIAN_EGG_KEY).spawnEgg(STIAN))
	);

	private StianEntities() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		FabricDefaultAttributeRegistry.register(STIAN, StianEntity.createAttributes());
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(STIAN_SPAWN_EGG));
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> output.accept(STIAN_SPAWN_EGG));
	}
}
