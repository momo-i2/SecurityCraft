package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;

public class ReinforcedScaffoldingBlock extends ScaffoldingBlock implements EntityBlock, IReinforcedBlock {
	public ReinforcedScaffoldingBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof Player player)
			NeoForge.EVENT_BUS.post(new OwnershipEvent(level, pos, player));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(pos, state);
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.SCAFFOLDING;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (ctx instanceof EntityCollisionContext ectx && ownsScaffolding(level, pos, ectx.getEntity()))
			return super.getShape(state, level, pos, ctx);
		else
			return state.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (ctx instanceof EntityCollisionContext ectx && ownsScaffolding(level, pos, ectx.getEntity()))
			return super.getCollisionShape(state, level, pos, ctx);
		else
			return Shapes.empty();
	}

	@Override
	public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
		return ownsScaffolding(level, pos, entity);
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
		return super.isLadder(state, level, pos, entity) && ownsScaffolding(level, pos, entity);
	}

	public boolean ownsScaffolding(BlockGetter level, BlockPos pos, Entity entity) {
		return !(entity instanceof LivingEntity) || level.getBlockEntity(pos) instanceof IOwnable ownable && ownable.isOwnedBy(entity);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		int distance = getDistance(level, pos);

		return defaultBlockState().setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER).setValue(DISTANCE, distance).setValue(BOTTOM, isBottom(level, pos, distance));
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int distance = getDistance(level, pos);
		BlockState newState = state.setValue(DISTANCE, distance).setValue(BOTTOM, isBottom(level, pos, distance));

		if (newState.getValue(DISTANCE) == 7) {
			if (state.getValue(DISTANCE) == 7) {
				CompoundTag data = level.getBlockEntity(pos).saveWithoutMetadata(level.registryAccess());
				FallingBlockEntity entity = FallingBlockEntity.fall(level, pos, newState);

				entity.blockData = data;
			}
			else
				level.destroyBlock(pos, true);
		}
		else if (state != newState)
			level.setBlock(pos, newState, 3);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return getDistance(level, pos) < 7;
	}

	public static int getDistance(BlockGetter level, BlockPos pos) {
		BlockPos.MutableBlockPos mutable = pos.mutable().move(Direction.DOWN);
		BlockState mutableState = level.getBlockState(mutable);
		int distance = 7;

		if (mutableState.is(SCContent.REINFORCED_SCAFFOLDING))
			distance = mutableState.getValue(DISTANCE);
		else if (mutableState.isFaceSturdy(level, mutable, Direction.UP))
			return 0;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockState offsetState = level.getBlockState(mutable.setWithOffset(pos, direction));

			if (offsetState.is(SCContent.REINFORCED_SCAFFOLDING)) {
				distance = Math.min(distance, offsetState.getValue(DISTANCE) + 1);

				if (distance == 1)
					break;
			}
		}

		return distance;
	}
}
