package martidi.sunnmore.visits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bård's lure: a small weather station, a pole with three cups on arms. Decorative; FriendVisits
 * finds it as a point of interest. The outline covers the cups; you only bump into the pole.
 */
public class VaerstasjonBlock extends Block {
	private static final VoxelShape OUTLINE = Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0);
	private static final VoxelShape POLE = Block.column(6.0, 0.0, 14.0);

	public VaerstasjonBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return POLE;
	}
}
