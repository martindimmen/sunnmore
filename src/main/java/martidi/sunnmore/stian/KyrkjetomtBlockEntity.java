package martidi.sunnmore.stian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import martidi.sunnmore.visits.LureBlocks;

/**
 * The Kyrkjetomt's 27 slots of church materials, like a barrel. Stian takes exactly one item for
 * each block he places; breaking the plot drops what is left (vanilla preRemoveSideEffects).
 */
public class KyrkjetomtBlockEntity extends RandomizableContainerBlockEntity {
	private static final int SIZE = 27;
	private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

	public KyrkjetomtBlockEntity(BlockPos pos, BlockState state) {
		super(LureBlocks.KYRKJETOMT_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.items);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.items);
		}
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.sunnmore.kyrkjetomt");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return ChestMenu.threeRows(containerId, inventory, this);
	}

	/** How many of this item (id like "minecraft:birch_planks") the plot holds. */
	public int count(String itemId) {
		Item item = item(itemId);
		int total = 0;
		for (ItemStack stack : this.items) {
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** Takes one of this item out of the plot. False if there is none. */
	public boolean take(String itemId) {
		Item item = item(itemId);
		for (ItemStack stack : this.items) {
			if (stack.is(item)) {
				stack.shrink(1);
				this.setChanged();
				return true;
			}
		}
		return false;
	}

	/** Puts one item back (a block that couldn't be placed). If the plot is full, it drops on top of it. */
	public void giveBack(String itemId) {
		ItemStack one = new ItemStack(item(itemId));
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);
			if (stack.isEmpty()) {
				this.items.set(i, one);
				this.setChanged();
				return;
			}
			if (ItemStack.isSameItemSameComponents(stack, one) && stack.getCount() < stack.getMaxStackSize()) {
				stack.grow(1);
				this.setChanged();
				return;
			}
		}
		if (this.level != null) {
			BlockPos pos = this.getBlockPos();
			Containers.dropItemStack(this.level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, one);
		}
	}

	private static Item item(String itemId) {
		return BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(itemId));
	}
}
