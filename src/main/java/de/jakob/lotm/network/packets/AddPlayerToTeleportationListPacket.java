package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record AddPlayerToTeleportationListPacket(int id, String playerName, UUID playerUUID) implements CustomPacketPayload {
    public static final Type<AddPlayerToTeleportationListPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_player_to_teleportation_list"));

    public static final StreamCodec<FriendlyByteBuf, AddPlayerToTeleportationListPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, AddPlayerToTeleportationListPacket::id,
                    ByteBufCodecs.STRING_UTF8, AddPlayerToTeleportationListPacket::playerName,
                    ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC), AddPlayerToTeleportationListPacket::playerUUID,
                    AddPlayerToTeleportationListPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddPlayerToTeleportationListPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.addPlayerToList(packet.id, packet.playerName, packet.playerUUID);
        });
    }
}