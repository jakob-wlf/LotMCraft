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

public record SyncControllingDataS2CPacket(boolean isControlling, CompoundTag bodyEntity, int entityId) implements CustomPacketPayload {
    
    public static final Type<SyncControllingDataS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_controlling_data"));
    
    public static final StreamCodec<ByteBuf, SyncControllingDataS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SyncControllingDataS2CPacket::isControlling,
            ByteBufCodecs.COMPOUND_TAG.map(
                    tag -> tag,
                    tag -> tag == null ? new CompoundTag() : tag
            ),
            SyncControllingDataS2CPacket::bodyEntity,
            ByteBufCodecs.INT,
            SyncControllingDataS2CPacket::entityId,
        SyncControllingDataS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncControllingDataS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleControllingDataPacket(packet);
            }
        });
    }
}