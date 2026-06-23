package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.door.PlayerTeleportationAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncPlayerTeleportationOnlinePlayersS2CPacket(List<String> playerUUIDs) implements CustomPacketPayload {

    public static final Type<SyncPlayerTeleportationOnlinePlayersS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_tp_online_players"));

    public static final StreamCodec<ByteBuf, SyncPlayerTeleportationOnlinePlayersS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncPlayerTeleportationOnlinePlayersS2CPacket::playerUUIDs,
            SyncPlayerTeleportationOnlinePlayersS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerTeleportationOnlinePlayersS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerTeleportationAbility.setOnlinePlayers(new ArrayList<>(packet.playerUUIDs().stream().map(UUID::fromString).toList()));
        });
    }
}