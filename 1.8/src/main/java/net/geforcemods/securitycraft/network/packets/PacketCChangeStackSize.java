package net.geforcemods.securitycraft.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCChangeStackSize implements IMessage
{
	private int slot;
	private int changeBy;

	public PacketCChangeStackSize(){}

	public PacketCChangeStackSize(int slot, int changeBy)
	{
		this.slot = slot;
		this.changeBy = changeBy;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		slot = buf.readInt();
		changeBy = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(slot);
		buf.writeInt(changeBy);
	}

	public static class Handler extends PacketHelper implements IMessageHandler<PacketCChangeStackSize, IMessage>
	{
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(PacketCChangeStackSize message, MessageContext ctx)
		{
			Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(message.slot).stackSize += message.changeBy;
			return null;
		}
	}
}
