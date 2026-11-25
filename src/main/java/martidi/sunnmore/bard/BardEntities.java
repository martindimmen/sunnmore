package martidi.sunnmore.bard;

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

public final class BardEntities {
	public static final ResourceKey<EntityType<?>> BARD_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Sunnmore.id("bard"));

	// Player-sized. No loot table: he drops nothing.
	public static final EntityType<BardEntity> BARD = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		BARD_KEY,
		EntityType.Builder.of(BardEntity::new, MobCategory.CREATURE)
			.noLootTable()
			.sized(0.6F, 1.95F)
			.eyeHeight(1.62F)
			.clientTrackingRange(8)
			.build(BARD_KEY)
	);

	public static final ResourceKey<Item> BARD_EGG_KEY = ResourceKey.create(Registries.ITEM, Sunnmore.id("bard_spawn_egg"));

	public static final Item BARD_SPAWN_EGG = Registry.register(
		BuiltInRegistries.ITEM,
		BARD_EGG_KEY,
		new SpawnEggItem(new Item.Properties().setId(BARD_EGG_KEY).spawnEgg(BARD))
	);

	private BardEntities() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		FabricDefaultAttributeRegistry.register(BARD, BardEntity.createAttributes());
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(BARD_SPAWN_EGG));
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> output.accept(BARD_SPAWN_EGG));
	}
}
