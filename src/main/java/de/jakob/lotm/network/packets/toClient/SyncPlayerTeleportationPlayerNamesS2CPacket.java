package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.door.PlayerTeleportationAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SyncPlayerTeleportationPlayerNamesS2CPacket(String uuid, String name) implements CustomPacketPayload {

    public static final Type<SyncPlayerTeleportationPlayerNamesS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_tp_player_names"));

    public static final StreamCodec<ByteBuf, SyncPlayerTeleportationPlayerNamesS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerTeleportationPlayerNamesS2CPacket::uuid,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerTeleportationPlayerNamesS2CPacket::name,
            SyncPlayerTeleportationPlayerNamesS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerTeleportationPlayerNamesS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerTeleportationAbility.setNameForPlayer(UUID.fromString(packet.uuid()), packet.name());
        });
    }
}