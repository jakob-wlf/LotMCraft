package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDiscernmentDataS2CPacket (boolean isDiscerning, int entityId) implements CustomPacketPayload {
    public static final Type<SyncDiscernmentDataS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_discernment_data"));

    public static final StreamCodec<ByteBuf, SyncDiscernmentDataS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SyncDiscernmentDataS2CPacket::isDiscerning,
            ByteBufCodecs.INT,
            SyncDiscernmentDataS2CPacket::entityId,
            SyncDiscernmentDataS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDiscernmentDataS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleDiscernmentDataPacket(packet);
            }
        });
    }
}
