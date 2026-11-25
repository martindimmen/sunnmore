package martidi.sunnmore.jopa;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import martidi.sunnmore.jopa.JopaFrame.Rect;

/** Opening a portal in a JOPA frame in the world: JopaFrame finds the shape, this places the blocks. */
public final class JopaPortals {
	private JopaPortals() {
	}

	/** Lights the frame that framePos belongs to. False if there is no complete, empty frame around it. */
	public static boolean tryOpen(ServerLevel level, BlockPos framePos) {
		for (Direction.Axis axis : new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z}) {
			Direction along = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
			for (Direction step : new Direction[] {Direction.UP, Direction.DOWN, along, along.getOpposite()}) {
				BlockPos start = framePos.relative(step);
				int plane = axis == Direction.Axis.X ? start.getZ() : start.getX();
				Optional<Rect> rect = JopaFrame.find(u(axis, start), start.getY(),
					(u, v) -> level.getBlockState(at(axis, plane, u, v)).isAir(),
					(u, v) -> isFrame(level.getBlockState(at(axis, plane, u, v))));
				if (rect.isPresent()) {
					open(level, axis, plane, rect.get());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Fills the frame with portal blocks, then lights the frame. Portal blocks go in first without
	 * shape updates (like vanilla's Nether portal), so the not-yet-lit frame doesn't close them.
	 */
	public static void open(ServerLevel level, Direction.Axis axis, int plane, Rect rect) {
		BlockState portal = JopaBlocks.JOPA_PORTAL.defaultBlockState().setValue(JopaPortalBlock.AXIS, axis);
		for (int u = rect.minU(); u <= rect.maxU(); u++) {
			for (int v = rect.minV(); v <= rect.maxV(); v++) {
				level.setBlock(at(axis, plane, u, v), portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
		for (int[] cell : JopaFrame.ring(rect, (u, v) -> isFrame(level.getBlockState(at(axis, plane, u, v))))) {
			BlockPos pos = at(axis, plane, cell[0], cell[1]);
			level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(JopaFrameBlock.LIT, true));
		}
	}

	private static boolean isFrame(BlockState state) {
		return state.getBlock() instanceof JopaFrameBlock;
	}

	/** u runs along the frame's width: x for a portal on the X axis, z for one on the Z axis. */
	private static int u(Direction.Axis axis, BlockPos pos) {
		return axis == Direction.Axis.X ? pos.getX() : pos.getZ();
	}

	static BlockPos at(Direction.Axis axis, int plane, int u, int v) {
		return axis == Direction.Axis.X ? new BlockPos(u, v, plane) : new BlockPos(plane, v, u);
	}
}
