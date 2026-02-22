package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.ControllingUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReturnToMainBodyPacket() implements CustomPacketPayload {

    public static final Type<ReturnToMainBodyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "return_to_main_body"));

    public static final StreamCodec<ByteBuf, ReturnToMainBodyPacket> STREAM_CODEC = StreamCodec.unit(new ReturnToMainBodyPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReturnToMainBodyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ControllingUtil.reset(serverPlayer, serverPlayer.serverLevel(), true);
            }
        });
    }
}