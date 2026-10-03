package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenu;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.RequestMarionetteSyncPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncMarionettesToClient(int containerId, List<RequestMarionetteSyncPacket.MarionetteEntry> entries)
        implements CustomPacketPayload {

    public static final Type<SyncMarionettesToClient> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "marionette_sync_to_client"));

    private static final StreamCodec<RegistryFriendlyByteBuf, RequestMarionetteSyncPacket.MarionetteEntry> ENTRY_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, RequestMarionetteSyncPacket.MarionetteEntry::entityId,
                    ByteBufCodecs.VAR_INT, RequestMarionetteSyncPacket.MarionetteEntry::modeOrdinal,
                    ByteBufCodecs.BOOL, RequestMarionetteSyncPacket.MarionetteEntry::shouldAttack,
                    ByteBufCodecs.BOOL, RequestMarionetteSyncPacket.MarionetteEntry::isMarionette,
                    RequestMarionetteSyncPacket.MarionetteEntry::new
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMarionettesToClient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncMarionettesToClient::containerId,
                    ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncMarionettesToClient::entries,
                    SyncMarionettesToClient::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMarionettesToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = ClientHandler.getPlayer();
            if (player != null
                    && player.containerMenu.containerId == packet.containerId()
                    && player.containerMenu instanceof MarionetteMenu menu) {
                menu.applySync(packet.entries());
            }
        });
    }
}