package martidi.sunnmore.jopa;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.glenn.GlennItems;

/** The JOPA portal's blocks and the access card. */
public final class JopaBlocks {
	public static final Block JOPA_RAMME = registerBlock("jopa_ramme", JopaFrameBlock::new, BlockBehaviour.Properties.of()
		.strength(3.0F, 6.0F)
		.sound(SoundType.METAL)
		.lightLevel(state -> state.getValue(JopaFrameBlock.LIT) ? 11 : 0));

	/** Like the Nether portal block: no collision, can't be mined, drops nothing, made only by a card. */
	public static final Block JOPA_PORTAL = registerBlock("jopa_portal", JopaPortalBlock::new, BlockBehaviour.Properties.of()
		.noCollision()
		.strength(-1.0F)
		.sound(SoundType.GLASS)
		.lightLevel(state -> 11)
		.pushReaction(PushReaction.IMMOVEABLE)
		.noLootTable());

	public static final Item JOPA_RAMME_ITEM = registerItem("jopa_ramme",
		properties -> new BlockItem(JOPA_RAMME, properties.useBlockDescriptionPrefix()), new Item.Properties());

	public static final Item JOPA_KORT = registerItem("jopa_kort", JopaCardItem::new, new Item.Properties()
		.stacksTo(1)
		.component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.sunnmore.jopa_kort.lore")))));

	private JopaBlocks() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> {
			output.accept(JOPA_KORT);
			output.accept(JOPA_RAMME_ITEM);
		});
	}

	private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Sunnmore.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Sunnmore.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}
}
