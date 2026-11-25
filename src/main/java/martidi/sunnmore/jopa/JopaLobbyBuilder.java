package martidi.sunnmore.jopa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import martidi.sunnmore.jopa.JopaLobby.Cell;
import martidi.sunnmore.jopa.JopaLobby.Part;

/**
 * Builds the JOPA lobby in the JOPA dimension from JopaLobby's layout. Called on every arrival:
 * missing parts are put back, nothing else is touched (air in the layout is left alone, so things
 * creative players build inside stay).
 */
public final class JopaLobbyBuilder {
	/** The middle of the lobby floor. */
	public static final BlockPos ORIGIN = new BlockPos(0, JopaLobby.ORIGIN_Y, 0);

	private JopaLobbyBuilder() {
	}

	public static void ensureBuilt(ServerLevel level) {
		// Walls, floor and roof first, then the portal (without shape updates), then the lit frame:
		// the same order as opening a portal with the card, so nothing closes itself halfway.
		for (Cell cell : JopaLobby.layout()) {
			if (cell.part() != Part.AIR && cell.part() != Part.PORTAL && cell.part() != Part.FRAME) {
				place(level, cell, state(cell.part()), Block.UPDATE_ALL);
			}
		}
		for (Cell cell : JopaLobby.layout()) {
			if (cell.part() == Part.PORTAL) {
				place(level, cell, state(Part.PORTAL), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
		for (Cell cell : JopaLobby.layout()) {
			if (cell.part() == Part.FRAME) {
				place(level, cell, state(Part.FRAME), Block.UPDATE_ALL);
			}
		}
	}

	private static void place(ServerLevel level, Cell cell, BlockState state, int flags) {
		BlockPos pos = ORIGIN.offset(cell.x(), cell.y(), cell.z());
		if (!level.getBlockState(pos).equals(state)) {
			level.setBlock(pos, state, flags);
		}
	}

	private static BlockState state(Part part) {
		return switch (part) {
			case FLOOR -> Blocks.SMOOTH_STONE.defaultBlockState();
			case WALL, ROOF -> Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState();
			case WINDOW -> Blocks.GLASS.defaultBlockState();
			case LIGHT -> Blocks.SEA_LANTERN.defaultBlockState();
			case FRAME -> JopaBlocks.JOPA_RAMME.defaultBlockState().setValue(JopaFrameBlock.LIT, true);
			case PORTAL -> JopaBlocks.JOPA_PORTAL.defaultBlockState().setValue(JopaPortalBlock.AXIS, Direction.Axis.X);
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
