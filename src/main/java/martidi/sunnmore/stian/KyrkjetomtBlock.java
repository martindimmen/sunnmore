package martidi.sunnmore.stian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

/**
 * Stian's church plot: holds the materials and marks where the church goes. FACING is the way the
 * church's front faces (towards the player who placed it); the church is built behind the plot.
 * Placing or opening it reports what is missing and what is in the way, and outlines the church.
 */
public class KyrkjetomtBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0);

	public KyrkjetomtBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// The front of the church faces the player.
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof KyrkjetomtBlockEntity plot) {
			player.openMenu(plot);
			if (player instanceof ServerPlayer serverPlayer) {
				ChurchSite.of(serverLevel, pos, state).ifPresent(site -> site.reportTo(serverPlayer, plot));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
		super.setPlacedBy(level, pos, state, by, itemStack);
		if (level instanceof ServerLevel serverLevel && by instanceof ServerPlayer player
			&& level.getBlockEntity(pos) instanceof KyrkjetomtBlockEntity plot) {
			ChurchSite.of(serverLevel, pos, state).ifPresent(site -> site.reportTo(player, plot));
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KyrkjetomtBlockEntity(pos, state);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
