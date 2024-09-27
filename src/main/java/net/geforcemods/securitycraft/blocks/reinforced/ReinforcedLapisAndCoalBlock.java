package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ReinforcedLapisAndCoalBlock extends OwnableBlock implements IOverlayDisplay, IReinforcedBlock {
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

	public ReinforcedLapisAndCoalBlock() {
		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.LAPIS));
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(VARIANT).getColor();
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
		EnumType[] values = EnumType.values();

		for (EnumType type : values) {
			items.add(new ItemStack(this, 1, type.getMetadata()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos) {
		return new ItemStack(Item.getItemFromBlock(SCContent.reinforcedLapisAndCoalBlocks), 1, getMetaFromState(state));
	}

	@Override
	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos) {
		return state.getBlock() == this;
	}

	@Override
	public List<Block> getVanillaBlocks() {
		return Arrays.asList(Blocks.LAPIS_BLOCK, Blocks.COAL_BLOCK);
	}

	@Override
	public IBlockState convertToReinforcedState(IBlockState state) {
		Block block = state.getBlock();

		if (block == Blocks.LAPIS_BLOCK)
			return getDefaultState().withProperty(VARIANT, EnumType.LAPIS);
		else if (block == Blocks.COAL_BLOCK)
			return getDefaultState().withProperty(VARIANT, EnumType.COAL);
		else
			return state;
	}

	@Override
	public IBlockState convertToVanillaState(IBlockState state) {
		switch (state.getValue(VARIANT)) {
			case LAPIS:
				return Blocks.LAPIS_BLOCK.getDefaultState();
			case COAL:
				return Blocks.COAL_BLOCK.getDefaultState();
			default:
				return state;
		}
	}

	@Override
	public ItemStack convertToReinforcedStack(ItemStack stackToConvert, Block blockToConvert) {
		int index = getVanillaBlocks().indexOf(blockToConvert);

		if (index >= 0)
			return new ItemStack(this, 1, index);
		else
			return ItemStack.EMPTY;
	}

	@Override
	public ItemStack convertToVanillaStack(ItemStack stackToConvert) {
		int meta = stackToConvert.getMetadata();

		if (meta >= 0 && meta <= 1)
			return new ItemStack(getVanillaBlocks().get(meta));
		else
			return ItemStack.EMPTY;
	}

	public enum EnumType implements IStringSerializable {
		LAPIS(0, "lapis", MapColor.LAPIS),
		COAL(1, "coal", MapColor.BLACK);

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final int meta;
		private final String name;
		private final MapColor color;

		private EnumType(int meta, String name, MapColor color) {
			this.meta = meta;
			this.name = name;
			this.color = color;
		}

		public int getMetadata() {
			return meta;
		}

		@Override
		public String toString() {
			return name;
		}

		public static EnumType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length)
				meta = 0;

			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return name;
		}

		public MapColor getColor() {
			return color;
		}

		static {
			for (EnumType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}