package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilityHotbarPacket(int hotbarIndex) implements CustomPacketPayload {
    
    public static final Type<SyncAbilityHotbarPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_hotbar"));
    
    public static final StreamCodec<ByteBuf, SyncAbilityHotbarPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncAbilityHotbarPacket::hotbarIndex,
        SyncAbilityHotbarPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncAbilityHotbarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleHotbarPacket(packet);
            }
        });
    }
}