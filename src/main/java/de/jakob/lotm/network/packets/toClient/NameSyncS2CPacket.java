package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.shapeShifting.NameUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record NameSyncS2CPacket(UUID playerUuid, String nickname) implements CustomPacketPayload {
    public static final Type<NameSyncS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shape_shifting_name_sync"));

    public static final StreamCodec<FriendlyByteBuf, NameSyncS2CPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NameSyncS2CPacket::playerUuid,
            ByteBufCodecs.STRING_UTF8, NameSyncS2CPacket::nickname,
            NameSyncS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NameSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID uuid = packet.playerUuid();
            String nickname = packet.nickname();

            if (nickname == null || nickname.isEmpty()) {
                NameUtils.mapping.remove(uuid);
            } else {
                NameUtils.mapping.put(uuid, nickname);
            }
        });
    }
}