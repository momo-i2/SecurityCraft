package net.geforcemods.securitycraft.network.server;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.items.BriefcaseItem;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CheckBriefcasePasscode(String passcode) implements CustomPacketPayload {
	public static final Type<CheckBriefcasePasscode> TYPE = new Type<>(new ResourceLocation(SecurityCraft.MODID, "check_briefcase_passcode"));
	//@formatter:off
	public static final StreamCodec<RegistryFriendlyByteBuf, CheckBriefcasePasscode> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, packet -> PasscodeUtils.hashPasscodeWithoutSalt(packet.passcode),
			CheckBriefcasePasscode::new);
	//@formatter:on

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(PlayPayloadContext ctx) {
		Player player = ctx.player().orElseThrow();
		ItemStack briefcase = PlayerUtils.getItemStackFromAnyHand(player, SCContent.BRIEFCASE.get());

		if (!briefcase.isEmpty()) {
			CompoundTag tag = Utils.getTag(briefcase).getUnsafe();
			String tagCode = tag.getString("passcode");

			if (tagCode.length() == 4) //If an old plaintext passcode is encountered, generate and check with the hashed variant
				BriefcaseItem.hashAndSetPasscode(tag, PasscodeUtils.hashPasscodeWithoutSalt(tagCode), p -> BriefcaseItem.checkPasscode(player, briefcase, passcode, PasscodeUtils.bytesToString(p), tag));
			else
				BriefcaseItem.checkPasscode(player, briefcase, passcode, tagCode, tag);
		}
	}
}
