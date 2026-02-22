package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncPlayerTeleportationPlayerNamesPacket(String uuid, String name) implements CustomPacketPayload {

    public static final Type<SyncPlayerTeleportationPlayerNamesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_tp_player_names"));

    public static final StreamCodec<ByteBuf, SyncPlayerTeleportationPlayerNamesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerTeleportationPlayerNamesPacket::uuid,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerTeleportationPlayerNamesPacket::name,
            SyncPlayerTeleportationPlayerNamesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerTeleportationPlayerNamesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerTeleportationAbility.setNameForPlayer(UUID.fromString(packet.uuid()), packet.name());
        });
    }
}