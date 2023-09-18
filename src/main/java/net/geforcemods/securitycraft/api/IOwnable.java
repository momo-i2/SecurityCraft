package net.geforcemods.securitycraft.api;

import java.util.UUID;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.TeamUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * This interface marks a {@link TileEntity} as "ownable". Any TileEntity that implements this interface is able to be
 * destroyed by the Universal Block Remover, and can only be broken or modified by the person who placed it down. <p> Use
 * this to set the owner, preferably in Block.onBlockPlacedBy():
 *
 * <pre>
 * ((IOwnable) world.getTileEntity(x, y, z)).setOwner(player.getGameProfile().getId().toString(), player.getCommandSenderName());
 * </pre>
 *
 * @author Geforce
 */
public interface IOwnable {
	/**
	 * @return An Owner object containing the player's name and UUID. You are responsible for reading and writing the name and
	 *         UUID variables to your TileEntity's CompoundNBT in writeToNBT() and readFromNBT().
	 */
	public Owner getOwner();

	/**
	 * Save a new owner to your Owner object here. <p> The easiest way is to use Owner.set(UUID, name), this method is here
	 * mainly for convenience.
	 *
	 * @param uuid The UUID of the new player.
	 * @param name The name of the new player.
	 */
	public void setOwner(String uuid, String name);

	/**
	 * @return true if the owner of this IOwnable should be invalidated when changed by the Universal Owner Changer
	 */
	default boolean needsValidation() {
		return false;
	}

	/**
	 * Executes after the owner has been changed and invalidates this if it needs validation
	 *
	 * @param world The current world
	 * @param state The IOwnable's state
	 * @param pos The IOwnable's position
	 * @param player The player that changed the owner of the IOwnable
	 */
	default void onOwnerChanged(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		if (needsValidation()) {
			getOwner().setValidated(false);
			PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.get().getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.ownerInvalidated"), TextFormatting.GREEN);
		}

		TileEntity te = (TileEntity) this;

		if (te instanceof LinkableBlockEntity) {
			LinkableBlockEntity linkable = (LinkableBlockEntity) te;

			linkable.createLinkedBlockAction(new ILinkedAction.OwnerChanged(getOwner()), linkable);
		}

		te.setChanged();
	}

	/**
	 * Checks whether the given player owns this IOwnable.
	 *
	 * @param player The player to check ownership of
	 * @return true if the given player owns this IOwnable, false otherwise
	 */
	public default boolean isOwnedBy(PlayerEntity player) {
		if (player == null)
			return false;

		return isOwnedBy(new Owner(player));
	}

	/**
	 * Checks whether the given owner owns this IOwnable.
	 *
	 * @param otherOwner The owner to check ownership of
	 * @return true if the given owner owns this IOwnable, false otherwise
	 */
	public default boolean isOwnedBy(Owner otherOwner) {
		Owner self = getOwner();

		if (ConfigHandler.SERVER.enableTeamOwnership.get() && TeamUtils.areOnSameTeam(self, otherOwner))
			return true;

		String selfUUID = self.getUUID();
		String otherUUID = otherOwner.getUUID();
		String otherName = otherOwner.getName();

		if (otherUUID != null && otherUUID.equals(selfUUID))
			return true;

		return otherName != null && selfUUID.equals("ownerUUID") && otherName.equals(self.getName());
	}

	/**
	 * Checks whether this and ownable entity's owner owns this block entity
	 *
	 * @param entity The entity to check
	 * @return true if the entity's owner owns this block entity, false otherwise
	 */
	public default boolean allowsOwnableEntity(Entity entity) {
		Owner beOwner = getOwner();
		UUID animalOwnerUUID = null;

		if (entity instanceof AbstractHorseEntity)
			animalOwnerUUID = ((AbstractHorseEntity) entity).getOwnerUUID();
		else if (entity instanceof TameableEntity)
			animalOwnerUUID = ((TameableEntity) entity).getOwnerUUID();

		return animalOwnerUUID != null && (animalOwnerUUID.toString().equals(beOwner.getUUID()) || TeamUtils.areOnSameTeam(beOwner, new Owner(entity.level.getPlayerByUUID(animalOwnerUUID))));
	}
}
