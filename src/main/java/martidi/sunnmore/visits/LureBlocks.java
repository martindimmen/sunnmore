package martidi.sunnmore.visits;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.glenn.GlennItems;

/**
 * The lure blocks. Placed near players, they make their friend visit far more often, and visit
 * right there. Each one is a point of interest, so FriendVisits can find them quickly.
 */
public final class LureBlocks {
	public static final Block GLENNS_PLANTE = registerBlock("glenns_plante", GlennsPlanteBlock::new,
		BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED));

	public static final Block TRENINGSBENK = registerBlock("treningsbenk", TreningsbenkBlock::new,
		BlockBehaviour.Properties.of().strength(1.5F).noOcclusion().sound(SoundType.METAL).pushReaction(PushReaction.POPPED));

	public static final Item GLENNS_PLANTE_ITEM = registerItem("glenns_plante", GLENNS_PLANTE,
		new Item.Properties().component(DataComponents.LORE,
			new ItemLore(List.of(Component.translatable("block.sunnmore.glenns_plante.lore")))));

	public static final Item TRENINGSBENK_ITEM = registerItem("treningsbenk", TRENINGSBENK, new Item.Properties());

	/** One "seat" each, searched within 1 block: we only need to know where the lures are. */
	public static final PoiType GLENN_LURE = PoiHelper.register(Sunnmore.id("glenn_lure"), 1, 1, GLENNS_PLANTE);
	public static final PoiType JINITH_LURE = PoiHelper.register(Sunnmore.id("jinith_lure"), 1, 1, TRENINGSBENK);

	private LureBlocks() {
	}

	/** Called from Sunnmore.onInitialize, after GlennItems (which owns the Sunnmøre tab). */
	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(GlennItems.SUNNMORE_TAB_KEY).register(output -> {
			output.accept(GLENNS_PLANTE_ITEM);
			output.accept(TRENINGSBENK_ITEM);
		});
	}

	private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Sunnmore.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item registerItem(String name, Block block, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Sunnmore.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, properties.setId(key).useBlockDescriptionPrefix()));
	}
}
