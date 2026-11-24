package martidi.sunnmore.visits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Glenn's lure: a potted, very green plant. Purely decorative; FriendVisits finds it as a point of interest. */
public class GlennsPlanteBlock extends Block {
	private static final VoxelShape SHAPE = Block.column(10.0, 0.0, 14.0);

	public GlennsPlanteBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
