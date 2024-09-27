package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

public class KeycardLockBlockEntity extends KeycardReaderBlockEntity {
	protected BooleanOption exactLevel = new BooleanOption("exactLevel", true);
	private boolean setUp = false;

	public KeycardLockBlockEntity() {
		super(SCContent.KEYCARD_LOCK_BLOCK_ENTITY.get());
	}

	@Override
	public ActionResultType onRightClickWithActionItem(ItemStack stack, Hand hand, PlayerEntity player, boolean isCodebreaker, boolean isKeycardHolder) {
		if (!isSetUp() && isOwnedBy(player)) {
			if (stack.getItem() instanceof KeycardItem) {
				KeycardItem item = (KeycardItem) stack.getItem();
				boolean[] levels = {
						false, false, false, false, false
				};
				String keySuffix;

				if (exactLevel.get()) {
					levels[item.getLevel()] = true;
					keySuffix = "exact";
				}
				else {
					for (int i = item.getLevel(); i < 5; i++) {
						levels[i] = true;
					}

					keySuffix = "above";
				}

				setUp = true;
				setAcceptedLevels(levels);
				setSignature(stack.getOrCreateTag().getInt("signature"));
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getBlockState().getBlock().getDescriptionId()), Utils.localize("messages.securitycraft:keycard_lock.setup_successful." + keySuffix, item.getLevel() + 1), TextFormatting.GREEN);
				return ActionResultType.SUCCESS;
			}
			else {
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(getBlockState().getBlock().getDescriptionId()), Utils.localize("messages.securitycraft:keycard_lock.not_set_up"), TextFormatting.RED);
				return ActionResultType.FAIL;
			}
		}

		return super.onRightClickWithActionItem(stack, hand, player, isCodebreaker, isKeycardHolder);
	}

	public boolean isSetUp() {
		return setUp;
	}

	@Override
	public void reset() {
		super.reset();
		setUp = false;
		acceptedLevels = new boolean[] {
				false, false, false, false, false
		};
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		if (option == exactLevel) {
			boolean[] acceptedLevels = getAcceptedLevels();
			boolean swap = false;

			for (int i = 0; i < acceptedLevels.length; i++) {
				if (swap)
					acceptedLevels[i] = !acceptedLevels[i];
				else if (acceptedLevels[i])
					swap = true;
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);
		tag.putBoolean("set_up", setUp);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);
		setUp = tag.getBoolean("set_up");
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DENYLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendDenylistMessage, signalLength, disabled, exactLevel
		};
	}
}
