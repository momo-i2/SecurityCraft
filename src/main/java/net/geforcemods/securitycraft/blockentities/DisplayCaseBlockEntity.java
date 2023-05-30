package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IPasscodeProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.blocks.DisplayCaseBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class DisplayCaseBlockEntity extends CustomizableBlockEntity implements ITickable, IPasscodeProtected, ILockable {
	private AxisAlignedBB renderBoundingBox = Block.FULL_BLOCK_AABB;
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private DisabledOption disabled = new DisabledOption(false);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption(this::getPos);
	private long cooldownEnd = 0;
	private ItemStack displayedStack = ItemStack.EMPTY;
	private boolean shouldBeOpen;
	private float openness;
	private float oOpenness;
	private byte[] passcode;
	private UUID saltKey;
	private IBlockState state;

	@Override
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		renderBoundingBox = new AxisAlignedBB(pos);
	}

	@Override
	public void update() {
		oOpenness = openness;

		if (!shouldBeOpen && openness > 0.0F)
			openness = Math.max(openness - 0.1F, 0.0F);
		else if (shouldBeOpen && openness < 1.0F)
			openness = Math.min(openness + 0.1F, 1.0F);
	}

	@Override
	public void activate(EntityPlayer player) {
		if (!world.isRemote) {
			Block block = world.getBlockState(pos).getBlock();

			if (block instanceof DisplayCaseBlock)
				((DisplayCaseBlock) block).activate(this);
		}
	}

	@Override
	public boolean shouldAttemptCodebreak(IBlockState state, EntityPlayer player) {
		return !isOpen() && IPasscodeProtected.super.shouldAttemptCodebreak(state, player);
	}

	@Override
	public byte[] getPasscode() {
		return passcode == null || passcode.length == 0 ? null : passcode;
	}

	@Override
	public void setPasscode(byte[] passcode) {
		this.passcode = passcode;
		markDirty();
	}

	@Override
	public UUID getSaltKey() {
		return saltKey;
	}

	@Override
	public void setSaltKey(UUID saltKey) {
		this.saltKey = saltKey;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setTag("DisplayedStack", getDisplayedStack().writeToNBT(new NBTTagCompound()));
		tag.setBoolean("ShouldBeOpen", shouldBeOpen);

		if (saltKey != null)
			tag.setUniqueId("saltKey", saltKey);

		if (passcode != null)
			tag.setString("passcode", PasscodeUtils.bytesToString(passcode));

		tag.setLong("cooldownLeft", getCooldownEnd() - System.currentTimeMillis());
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		load(tag, true);
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		load(tag, false);
	}

	public void load(NBTTagCompound tag, boolean forceOpenness) {
		super.readFromNBT(tag);
		setDisplayedStack(new ItemStack(tag.getCompoundTag("DisplayedStack")));
		shouldBeOpen = tag.getBoolean("ShouldBeOpen");
		cooldownEnd = System.currentTimeMillis() + tag.getLong("cooldownLeft");
		loadSaltKey(tag);
		loadPasscode(tag);

		if (forceOpenness)
			forceOpen(shouldBeOpen);
	}

	@Override
	public void startCooldown() {
		if (!isOnCooldown()) {
			IBlockState state = world.getBlockState(pos);

			cooldownEnd = System.currentTimeMillis() + smartModuleCooldown.get() * 50;
			world.notifyBlockUpdate(pos, state, state, 3);
			markDirty();
		}
	}

	@Override
	public long getCooldownEnd() {
		return cooldownEnd;
	}

	@Override
	public boolean isOnCooldown() {
		return System.currentTimeMillis() < getCooldownEnd();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART, ModuleType.HARMING
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage, disabled, smartModuleCooldown
		};
	}

	public boolean sendsMessages() {
		return sendMessage.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public void setDisplayedStack(ItemStack displayedStack) {
		this.displayedStack = displayedStack;
		sync();
	}

	public ItemStack getDisplayedStack() {
		return displayedStack;
	}

	public void setOpen(boolean shouldBeOpen) {
		world.playSound(null, pos, shouldBeOpen ? SCSounds.DISPLAY_CASE_OPEN.event : SCSounds.DISPLAY_CASE_CLOSE.event, SoundCategory.BLOCKS, 1.0F, 1.0F);
		this.shouldBeOpen = shouldBeOpen;
		sync();
	}

	public void forceOpen(boolean open) {
		shouldBeOpen = open;
		oOpenness = openness = open ? 1.0F : 0.0F;
		sync();
	}

	public float getOpenness(float partialTicks) {
		return ClientUtils.lerp(partialTicks, oOpenness, openness);
	}

	public boolean isOpen() {
		return shouldBeOpen;
	}

	@Override
	public void sync() {
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);

			markDirty();
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return renderBoundingBox;
	}

	public void setBlockState(IBlockState state) {
		this.state = state;
	}

	public IBlockState getBlockState() {
		if (state != null)
			return state;

		if (world != null)
			return world.getBlockState(pos);

		return SCContent.displayCase.getDefaultState();
	}
}
