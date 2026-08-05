package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent to a client whenever they cross the Grey Fog seal boundary (enter or exit).
 * The client uses this to toggle the blue-grey screen tint overlay.
 */
public record SyncGreyFogStatusPacket(boolean inside) implements CustomPacketPayload {

    public static final Type<SyncGreyFogStatusPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_grey_fog_status"));

    public static final StreamCodec<ByteBuf, SyncGreyFogStatusPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(SyncGreyFogStatusPacket::new, SyncGreyFogStatusPacket::inside);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncGreyFogStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleGreyFogStatus(packet.inside());
            }
        });
    }
}
