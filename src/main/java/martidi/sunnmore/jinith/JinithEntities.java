package martidi.sunnmore.jinith;

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

public final class JinithEntities {
	public static final ResourceKey<EntityType<?>> JINITH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Sunnmore.id("jinith"));

	// Short and wide. No loot table: he drops nothing.
	public static final EntityType<JinithEntity> JINITH = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		JINITH_KEY,
		EntityType.Builder.of(JinithEntity::new, MobCategory.CREATURE)
			.noLootTable()
			.sized(1.0F, 1.7F)
			.eyeHeight(1.5F)
			.clientTrackingRange(8)
			.build(JINITH_KEY)
	);

	public static final ResourceKey<Item> JINITH_EGG_KEY = ResourceKey.create(Registries.ITEM, Sunnmore.id("jinith_spawn_egg"));

	public static final Item JINITH_SPAWN_EGG = Registry.register(
		BuiltInRegistries.ITEM,
		JINITH_EGG_KEY,
		new SpawnEggItem(new Item.Properties().setId(JINITH_EGG_KEY).spawnEgg(JINITH))
	);

	private JinithEntities() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		FabricDefaultAttributeRegistry.register(JINITH, JinithEntity.createAttributes());
		JinithSafety.register();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(JINITH_SPAWN_EGG));
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> output.accept(JINITH_SPAWN_EGG));
	}
}
