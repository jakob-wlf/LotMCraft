package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IsPlayerModelPacket(String entityType, boolean sequenceRestrict) implements CustomPacketPayload {

    public static final Type<IsPlayerModelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "is_player_model"));

    public static final StreamCodec<ByteBuf, IsPlayerModelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            IsPlayerModelPacket::entityType,
            ByteBufCodecs.BOOL,
            IsPlayerModelPacket::sequenceRestrict,
            IsPlayerModelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IsPlayerModelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.isPlayerModel(packet.entityType, packet.sequenceRestrict);
            }
        });
    }
}