package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerSefirotPacket(String sefirot) implements CustomPacketPayload {

    public static final Type<SyncPlayerSefirotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_sefirot"));

    public static final StreamCodec<ByteBuf, SyncPlayerSefirotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerSefirotPacket::sefirot,
            SyncPlayerSefirotPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerSefirotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SefirahHandler.syncPlayerSefirotToClient(ClientHandler.getPlayer(), packet.sefirot());
        });
    }
}