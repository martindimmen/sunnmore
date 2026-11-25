package martidi.sunnmore.stian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

import martidi.sunnmore.stian.ChurchBlueprint.Placement;
import martidi.sunnmore.stian.ChurchProgress.Step;

/**
 * One church in the world: the drawing turned to face the way the plot faces. The plot sits just
 * outside the front door; the church is built behind it. Reads progress from the world, checks
 * what is in the way, and places blocks taken from the plot, never anywhere else.
 */
public final class ChurchSite {
	private final ServerLevel level;
	private final BlockPos plot;
	private final StianChurch.Loaded church;
	private final Rotation rotation;

	private ChurchSite(ServerLevel level, BlockPos plot, Direction facing, StianChurch.Loaded church) {
		this.level = level;
		this.plot = plot;
		this.church = church;
		this.rotation = switch (facing) {
			case EAST -> Rotation.CLOCKWISE_90;
			case SOUTH -> Rotation.CLOCKWISE_180;
			case WEST -> Rotation.COUNTERCLOCKWISE_90;
			default -> Rotation.NONE;
		};
	}

	/** The church for a plot block, if the block is a plot and the drawing loaded. */
	public static Optional<ChurchSite> of(ServerLevel level, BlockPos plot, BlockState plotState) {
		if (!(plotState.getBlock() instanceof KyrkjetomtBlock)) {
			return Optional.empty();
		}
		return StianChurch.get().map(church -> new ChurchSite(level, plot, plotState.getValue(KyrkjetomtBlock.FACING), church));
	}

	public BlockPos plot() {
		return this.plot;
	}

	/** Blueprint north is the front, towards the plot; the church grows "south" (backwards) from it. */
	public BlockPos worldPos(int x, int y, int z) {
		Direction back = this.rotation.rotate(Direction.SOUTH);
		Direction east = this.rotation.rotate(Direction.EAST);
		return this.plot.relative(back, z + 1).relative(east, x - this.church.blueprint().width() / 2).above(y);
	}

	public BlockPos worldPos(Placement p) {
		return this.worldPos(p.x(), p.y(), p.z());
	}

	public BlockState state(Placement p) {
		return this.church.states().get(p.block()).rotate(this.rotation);
	}

	/** Done when the position already holds the drawing's block, whichever way it is turned. */
	public boolean done(Placement p) {
		return this.level.getBlockState(this.worldPos(p)).is(this.state(p).getBlock());
	}

	public Step next(KyrkjetomtBlockEntity plotInventory) {
		return ChurchProgress.next(this.church.blueprint().buildOrder(), this::done, plotInventory::count);
	}

	/** Air, plants that get replaced anyway (grass, flowers, snow layers), or the church's own block. */
	private boolean isFree(BlockPos pos, Placement p) {
		BlockState here = this.level.getBlockState(pos);
		return here.isAir() || here.is(BlockTags.REPLACEABLE) || here.is(BlockTags.FLOWERS)
			|| (p != null && here.is(this.state(p).getBlock()));
	}

	/** Everything inside the church's box that he would have to break. He never does: these stop him. */
	public List<BlockPos> obstacles() {
		List<BlockPos> blocked = new ArrayList<>();
		ChurchBlueprint blueprint = this.church.blueprint();
		for (int x = 0; x < blueprint.width(); x++) {
			for (int y = 0; y < blueprint.height(); y++) {
				for (int z = 0; z < blueprint.depth(); z++) {
					BlockPos pos = this.worldPos(x, y, z);
					if (!this.isFree(pos, this.church.at(x, y, z))) {
						blocked.add(pos);
					}
				}
			}
		}
		return blocked;
	}

	/**
	 * Takes one item from the plot and places the block, the door with both halves. While boosted,
	 * a mistake turns a stair or a door hinge the wrong way, never a different block. If anything
	 * fails, the item goes back into the plot. Returns whether a block was placed.
	 */
	public boolean place(Placement p, KyrkjetomtBlockEntity plotInventory, boolean mistake) {
		BlockPos pos = this.worldPos(p);
		BlockState state = this.state(p);
		boolean door = state.getBlock() instanceof DoorBlock;
		if (p.item() == null || !this.isFree(pos, null) || (door && !this.isFree(pos.above(), null))) {
			return false;
		}
		if (mistake) {
			state = wrongly(state, this.level.getRandom());
		}
		// A torch or lantern without support would turn into air: check before taking the item.
		BlockState finalState = door ? state : Block.updateFromNeighbourShapes(state, this.level, pos);
		if (!finalState.is(state.getBlock()) || !plotInventory.take(p.item())) {
			return false;
		}
		boolean placed;
		if (door) {
			placed = this.level.setBlock(pos, finalState, Block.UPDATE_ALL)
				&& this.level.setBlock(pos.above(), finalState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		} else {
			placed = this.level.setBlock(pos, finalState, Block.UPDATE_ALL);
		}
		if (!placed) {
			plotInventory.giveBack(p.item());
		}
		return placed;
	}

	/** A tipsy mistake: the same block, turned wrong. */
	private static BlockState wrongly(BlockState state, RandomSource random) {
		if (state.hasProperty(StairBlock.FACING)) {
			return random.nextBoolean()
				? state.setValue(StairBlock.FACING, state.getValue(StairBlock.FACING).getOpposite())
				: state.setValue(StairBlock.HALF, state.getValue(StairBlock.HALF) == Half.TOP ? Half.BOTTOM : Half.TOP);
		}
		if (state.hasProperty(DoorBlock.HINGE)) {
			return state.setValue(DoorBlock.HINGE, state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
		}
		return state;
	}

	/** Particles along the church's ground outline, so players see where it will stand. */
	public void showOutline() {
		ChurchBlueprint blueprint = this.church.blueprint();
		for (int x = 0; x < blueprint.width(); x++) {
			for (int z = 0; z < blueprint.depth(); z++) {
				if (x == 0 || z == 0 || x == blueprint.width() - 1 || z == blueprint.depth() - 1) {
					BlockPos pos = this.worldPos(x, 0, z);
					this.level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 2, 0.1, 0.3, 0.1, 0.0);
				}
			}
		}
	}

	/** The plot's own message to a player: what is in the way, what is missing, or that all is ready. */
	public void reportTo(ServerPlayer player, KyrkjetomtBlockEntity plotInventory) {
		this.showOutline();
		List<BlockPos> blocked = this.obstacles();
		if (!blocked.isEmpty()) {
			BlockPos first = blocked.get(0);
			player.sendSystemMessage(prefix().append(Component.translatable("chat.sunnmore.kyrkjetomt.blocked",
				blocked.size(), first.getX() + " " + first.getY() + " " + first.getZ())));
			return;
		}
		Map<String, Integer> needed = ChurchProgress.stillNeeded(this.church.blueprint().buildOrder(), this::done);
		if (needed.isEmpty()) {
			player.sendSystemMessage(prefix().append(Component.translatable("chat.sunnmore.kyrkjetomt.finished")));
			return;
		}
		MutableComponent list = Component.empty();
		boolean any = false;
		for (Map.Entry<String, Integer> entry : needed.entrySet()) {
			int missing = entry.getValue() - plotInventory.count(entry.getKey());
			if (missing <= 0) {
				continue;
			}
			if (any) {
				list.append(", ");
			}
			list.append(missing + " ").append(Component.translatable(
				BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey())).getDescriptionId()));
			any = true;
		}
		player.sendSystemMessage(prefix().append(any
			? Component.translatable("chat.sunnmore.kyrkjetomt.missing", list)
			: Component.translatable("chat.sunnmore.kyrkjetomt.ready")));
	}

	private static MutableComponent prefix() {
		return Component.literal("[")
			.append(Component.translatable("block.sunnmore.kyrkjetomt").withStyle(ChatFormatting.GOLD))
			.append("] ");
	}
}
