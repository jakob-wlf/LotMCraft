package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility.PlayerInfo;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record SyncPlayerListPacket(List<PlayerInfo> players) implements CustomPacketPayload {

    public static final Type<SyncPlayerListPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_list"));

    // StreamCodec for PlayerInfo
    public static final StreamCodec<ByteBuf, PlayerInfo> PLAYER_INFO_CODEC = new StreamCodec<>() {
        @Override
        public PlayerInfo decode(ByteBuf buf) {
            int id = ByteBufCodecs.INT.decode(buf);
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            long mostSigBits = ByteBufCodecs.VAR_LONG.decode(buf);
            long leastSigBits = ByteBufCodecs.VAR_LONG.decode(buf);
            UUID uuid = new UUID(mostSigBits, leastSigBits);
            return new PlayerInfo(id, name, uuid);
        }

        @Override
        public void encode(ByteBuf buf, PlayerInfo playerInfo) {
            ByteBufCodecs.INT.encode(buf, playerInfo.id());
            ByteBufCodecs.STRING_UTF8.encode(buf, playerInfo.name());
            ByteBufCodecs.VAR_LONG.encode(buf, playerInfo.uuid().getMostSignificantBits());
            ByteBufCodecs.VAR_LONG.encode(buf, playerInfo.uuid().getLeastSignificantBits());
        }
    };

    // StreamCodec for the list of PlayerInfo
    public static final StreamCodec<ByteBuf, SyncPlayerListPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncPlayerListPacket decode(ByteBuf buf) {
            List<PlayerInfo> players = PLAYER_INFO_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            return new SyncPlayerListPacket(players);
        }

        @Override
        public void encode(ByteBuf buf, SyncPlayerListPacket packet) {
            PLAYER_INFO_CODEC.apply(ByteBufCodecs.list()).encode(buf, packet.players());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerListPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handlePlayerListPacket(packet);
            }
        });
    }
}