package martidi.sunnmore.glenn;

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

public final class GlennEntities {
	public static final ResourceKey<EntityType<?>> GLENNDERMAN_KEY =
		ResourceKey.create(Registries.ENTITY_TYPE, Sunnmore.id("glennderman"));

	// Same size as the vanilla Enderman; allowed in Peaceful (the default for a builder).
	// No loot table: killing Glenn gets you nothing (and a debuff, see GlenndermanEntity.die).
	public static final EntityType<GlenndermanEntity> GLENNDERMAN = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		GLENNDERMAN_KEY,
		EntityType.Builder.of(GlenndermanEntity::new, MobCategory.CREATURE)
			.noLootTable()
			.sized(0.6F, 2.9F)
			.eyeHeight(2.55F)
			.passengerAttachments(2.80625F)
			.clientTrackingRange(8)
			.build(GLENNDERMAN_KEY)
	);

	public static final ResourceKey<Item> GLENNDERMAN_EGG_KEY =
		ResourceKey.create(Registries.ITEM, Sunnmore.id("glennderman_spawn_egg"));

	public static final Item GLENNDERMAN_SPAWN_EGG = Registry.register(
		BuiltInRegistries.ITEM,
		GLENNDERMAN_EGG_KEY,
		new SpawnEggItem(new Item.Properties().setId(GLENNDERMAN_EGG_KEY).spawnEgg(GLENNDERMAN))
	);

	private GlennEntities() {
	}

	/** Called from Sunnmore.onInitialize; touching the class runs the registrations above. */
	public static void register() {
		FabricDefaultAttributeRegistry.register(GLENNDERMAN, GlenndermanEntity.createAttributes());
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
			.register(output -> output.accept(GLENNDERMAN_SPAWN_EGG));
	}
}
