package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.util.data.ClientData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncPlayerTeleportationOnlinePlayersPacket(List<String> playerUUIDs) implements CustomPacketPayload {

    public static final Type<SyncPlayerTeleportationOnlinePlayersPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_tp_online_players"));

    public static final StreamCodec<ByteBuf, SyncPlayerTeleportationOnlinePlayersPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncPlayerTeleportationOnlinePlayersPacket::playerUUIDs,
            SyncPlayerTeleportationOnlinePlayersPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerTeleportationOnlinePlayersPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerTeleportationAbility.setOnlinePlayers(new ArrayList<>(packet.playerUUIDs().stream().map(UUID::fromString).toList()));
        });
    }
}