package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ReinforcedCobwebBlock extends BaseReinforcedBlock {
	public ReinforcedCobwebBlock(Properties properties, Block vB) {
		super(properties, vB);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
		return VoxelShapes.empty();
	}

	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof PlayerEntity) {
			TileEntity te = world.getBlockEntity(pos);

			if (te instanceof OwnableBlockEntity) {
				if (((OwnableBlockEntity) te).isOwnedBy((PlayerEntity) entity))
					return;
			}
		}

		entity.makeStuckInBlock(state, new Vector3d(0.25D, 0.05D, 0.25D));
	}
}
