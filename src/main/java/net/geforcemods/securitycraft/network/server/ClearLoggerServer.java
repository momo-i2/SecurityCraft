package net.geforcemods.securitycraft.network.server;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ClearLoggerServer(BlockPos pos) implements CustomPacketPayload {
	public static final Type<ClearLoggerServer> TYPE = new Type<>(new ResourceLocation(SecurityCraft.MODID, "clear_logger_server"));
	//@formatter:off
	public static final StreamCodec<RegistryFriendlyByteBuf, ClearLoggerServer> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ClearLoggerServer::pos,
			ClearLoggerServer::new);
	//@formatter:on

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(PlayPayloadContext ctx) {
		Player player = ctx.player().orElseThrow();

		if (player.level().getBlockEntity(pos) instanceof UsernameLoggerBlockEntity be && be.isOwnedBy(player)) {
			be.setPlayers(new String[100]);
			be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
		}
	}
}
