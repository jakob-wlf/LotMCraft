package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncIntrospectMenuS2CPacket(int sequence, String pathway, float sanity) implements CustomPacketPayload {
    public static final Type<SyncIntrospectMenuS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_introspect_menu"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncIntrospectMenuS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SyncIntrospectMenuS2CPacket::sequence,
        ByteBufCodecs.STRING_UTF8, SyncIntrospectMenuS2CPacket::pathway,
        ByteBufCodecs.FLOAT, SyncIntrospectMenuS2CPacket::sanity,
        SyncIntrospectMenuS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncIntrospectMenuS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
                    ClientHandler.handleSyncIntrospectMenuPacket(packet, context.player().getUUID());
                }
        );
    }
}
