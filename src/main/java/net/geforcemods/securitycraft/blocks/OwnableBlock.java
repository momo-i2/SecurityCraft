package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.INameSetter;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class OwnableBlock extends Block {
	public OwnableBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof PlayerEntity) {
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (PlayerEntity) placer));
		}

		if (stack.hasCustomHoverName()) {
			TileEntity te = world.getBlockEntity(pos);

			if (te instanceof INameSetter)
				((INameSetter) te).setCustomName(stack.getHoverName());
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new OwnableBlockEntity();
	}
}
