package martidi.sunnmore.jopa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * JOPA-ramme: the portal frame. Grey with a card reader; lit (JOPA colours, light 11) while it
 * borders a JOPA portal. It goes grey again by itself once no portal block is next to it, also
 * diagonally, so the corners follow the sides.
 */
public class JopaFrameBlock extends Block {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public JopaFrameBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
		Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (state.getValue(LIT) && !portalNear(level, pos)) {
			return state.setValue(LIT, false);
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	/** A JOPA portal block in the 3x3x3 around pos: the sides touch one, the corners touch one diagonally. */
	private static boolean portalNear(LevelReader level, BlockPos pos) {
		for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (level.getBlockState(near).getBlock() instanceof JopaPortalBlock) {
				return true;
			}
		}
		return false;
	}
}
