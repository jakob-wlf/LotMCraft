package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SyncOriginalBodyOwnerS2CPacket(int entityId, UUID ownerUUID, String ownerName) implements CustomPacketPayload {
    public static final Type<SyncOriginalBodyOwnerS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_original_body_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncOriginalBodyOwnerS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncOriginalBodyOwnerS2CPacket::entityId,
                    UUIDUtil.STREAM_CODEC, SyncOriginalBodyOwnerS2CPacket::ownerUUID,
                    ByteBufCodecs.STRING_UTF8, SyncOriginalBodyOwnerS2CPacket::ownerName,
                    SyncOriginalBodyOwnerS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncOriginalBodyOwnerS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.handleOriginalBodyOwnerSyncPacket(packet);
            }
        });
    }
}