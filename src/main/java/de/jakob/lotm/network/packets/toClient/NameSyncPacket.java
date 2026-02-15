package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.shapeShifting.NameStorage;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record NameSyncPacket(UUID playerUuid, String nickname) implements CustomPacketPayload {
    public static final Type<NameSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shape_shifting_name_sync"));

    public static final StreamCodec<FriendlyByteBuf, NameSyncPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NameSyncPacket::playerUuid,
            ByteBufCodecs.STRING_UTF8, NameSyncPacket::nickname,
            NameSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NameSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            NameStorage.setNickname(packet.playerUuid(), packet.nickname());
        });
    }
}