package org.freeforums.geforce.securitycraft.main;

import org.freeforums.geforce.securitycraft.tileentity.CustomizableSCTE;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Utils {
	
	public static String getFormattedCoordinates(BlockPos pos){
		return "X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ();
	}
	
	/**
	 * Prints "Method running" whenever this method is called.
	 * Good for checking if methods are being called.
	 */
	public static void checkIfRunning(){
		System.out.println("Method running.");
	}
	
	/**
	 * Prints "Method running" whenever this method is called.
	 * Good for checking if methods are being called.
	 */	
	public static void checkIfRunning(Object... objects){
		String string = "Method running. Args: ";
		
		for(int i = 0; i < objects.length; i++){
			if(i == (objects.length - 1)){
				string += objects[i];
			}else{
				string += objects[i] + " | ";
			}		
		}
		
		System.out.println(string);
	}
	
	public static Material getBlockMaterial(World par1World, BlockPos pos){
		return par1World.getBlockState(pos).getBlock().getMaterial();
	}
	
	public static void closePlayerScreen(){
		Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
		Minecraft.getMinecraft().setIngameFocus();
	}
	
	public static EnumFacing getSideFacingFromIndex(int index){
		return EnumFacing.values()[index];
	}
	
	public static void destroyBlock(World par1World, BlockPos pos, boolean par5){
		par1World.destroyBlock(pos, par5);
	}
	
	public static void destroyBlock(World par1World, int par2, int par3, int par4, boolean par5){
		par1World.destroyBlock(new BlockPos(par2, par3, par4), par5);
	}
	
	public static void setBlock(World par1World, BlockPos pos, Block block){
		par1World.setBlockState(pos, block.getDefaultState());
	}
	
	public static void setBlock(World par1World, int par2, int par3, int par4, Block block){
		setBlock(par1World, new BlockPos(par2, par3, par4), block);
	}
	
	public static Block getBlock(World par1World, BlockPos pos){
		return par1World.getBlockState(pos).getBlock();
	}
	
	public static Block getBlock(World par1World, int par2, int par3, int par4){
		return par1World.getBlockState(new BlockPos(par2, par3, par4)).getBlock();
	}

	public static void setBlockProperty(World par1World, BlockPos pos, PropertyBool property, boolean value) {
		ItemStack[] modules = null;
		if(par1World.getTileEntity(pos) instanceof CustomizableSCTE){
			modules = ((CustomizableSCTE) par1World.getTileEntity(pos)).itemStacks;
		}
		
		TileEntity tileEntity = par1World.getTileEntity(pos);
		par1World.setBlockState(pos, par1World.getBlockState(pos).withProperty(property, value));
		par1World.setTileEntity(pos, tileEntity);
		
		if(modules != null){
			((CustomizableSCTE) par1World.getTileEntity(pos)).itemStacks = modules;
		}
	}
	
	public static void setBlockProperty(World par1World, int par2, int par3, int par4, PropertyBool property, boolean value) {
		ItemStack[] modules = null;
		if(par1World.getTileEntity(new BlockPos(par2, par3, par4)) instanceof CustomizableSCTE){
			modules = ((CustomizableSCTE) par1World.getTileEntity(new BlockPos(par2, par3, par4))).itemStacks;
		}
		
		TileEntity tileEntity = par1World.getTileEntity(new BlockPos(par2, par3, par4));
		par1World.setBlockState(new BlockPos(par2, par3, par4), par1World.getBlockState(new BlockPos(par2, par3, par4)).withProperty(property, value));
		par1World.setTileEntity(new BlockPos(par2, par3, par4), tileEntity);
		
		if(modules != null){
			((CustomizableSCTE) par1World.getTileEntity(new BlockPos(par2, par3, par4))).itemStacks = modules;
		}
	}
	
	public static Comparable getBlockProperty(World par1World, BlockPos pos, PropertyBool property) {
		return par1World.getBlockState(pos).getValue(property);
	}
	
	public static Comparable getBlockProperty(World par1World, int par2, int par3, int par4, PropertyBool property) {
		return par1World.getBlockState(new BlockPos(par2, par3, par4)).getValue(property);
	}

}
