package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSefirotAccommodationPacket(int progressTicks, int totalTicks) implements CustomPacketPayload {
    public static final Type<SyncSefirotAccommodationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_sefirot_accommodation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSefirotAccommodationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncSefirotAccommodationPacket::progressTicks,
                    ByteBufCodecs.INT, SyncSefirotAccommodationPacket::totalTicks,
                    SyncSefirotAccommodationPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSefirotAccommodationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSefirotAccommodation(packet.progressTicks(), packet.totalTicks());
            }
        });
    }
}
