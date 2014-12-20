package org.freeforums.geforce.securitycraft.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.freeforums.geforce.securitycraft.blocks.BlockKeycardReader;
import org.freeforums.geforce.securitycraft.containers.ContainerKeycardSetup;
import org.freeforums.geforce.securitycraft.main.HelpfulMethods;
import org.freeforums.geforce.securitycraft.main.mod_SecurityCraft;
import org.freeforums.geforce.securitycraft.network.packets.PacketSetKeycardLevel;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeycardReader;
import org.lwjgl.opengl.GL11;

//TODO
public class GuiKeycardSetup extends GuiContainer{
	
	private static final ResourceLocation field_110410_t = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private TileEntityKeycardReader keypadInventory;
	private GuiButton lvOfSecurityButton;
	private int lvOfSecurity = 0;

	public GuiKeycardSetup(InventoryPlayer inventory, TileEntityKeycardReader tile_entity) {
		 super(new ContainerKeycardSetup(inventory, tile_entity));
	     this.keypadInventory = tile_entity;
	}
	
	public void initGui(){
    	super.initGui();

		this.buttonList.add(this.lvOfSecurityButton = new GuiButton(0, this.width / 2 - (48 * 2 - 23), this.height / 2 + 20, 150, 20, ""));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 48, this.height / 2 + 30 + 20, 100, 20, "Save & continue."));
		
		this.updateButtonText();
    }
	
	 /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRendererObj.drawString("Keycard setup", this.xSize / 2 - this.fontRendererObj.getStringWidth("Keycard setup") / 2, 6, 4210752);
        this.fontRendererObj.drawString("Select the level of security that", this.xSize / 2 - this.fontRendererObj.getStringWidth("Select the level of security that") / 2 - 2, 30 - 7, 4210752);
        this.fontRendererObj.drawString("you want. Only players with a", this.xSize / 2 - this.fontRendererObj.getStringWidth("you want. Only players with a") / 2 - 11, 42 - 7, 4210752);
        this.fontRendererObj.drawString("keycard with a security level", this.xSize / 2 - this.fontRendererObj.getStringWidth("keycard with a security level") / 2 - 10, 54 - 7, 4210752);
        this.fontRendererObj.drawString("equal to or higher then the level", this.xSize / 2 - this.fontRendererObj.getStringWidth("equal to or higher then the level") / 2, 66 - 7, 4210752);
        this.fontRendererObj.drawString("you select will be able to use", this.xSize / 2 - this.fontRendererObj.getStringWidth("you select will be able to use") / 2 - 9, 78 - 7, 4210752);
        this.fontRendererObj.drawString("this keycard reader.", this.xSize / 2 - this.fontRendererObj.getStringWidth("you select will be able to use") / 2 - 10, 90 - 7, 4210752);

    }

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(field_110410_t);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}
	
	 private void updateButtonText(){
		 this.lvOfSecurity++;
		 if(this.lvOfSecurity <= 3){
			 this.lvOfSecurityButton.displayString = "Level of security needed: " + this.lvOfSecurity;
		 }else{
			 this.lvOfSecurity = 1;
			 this.lvOfSecurityButton.displayString = "Level of security needed: " + this.lvOfSecurity;

		 }
	 }
	 
	 protected void actionPerformed(GuiButton guibutton){
		switch(guibutton.id){
			case 0:
				this.updateButtonText();
				break;
				
			case 1:
				this.saveLVs();
				break;
		}
	 }

	private void saveLVs() {
		mod_SecurityCraft.network.sendToServer(new PacketSetKeycardLevel(keypadInventory.xCoord, keypadInventory.yCoord, keypadInventory.zCoord, this.lvOfSecurity));
		
		HelpfulMethods.closePlayerScreen();
	}

}
