package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record PlayAnimationS2CPacket(int playerId, String animId) implements CustomPacketPayload {
    
    public static final Type<PlayAnimationS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_animation"));

    public static final StreamCodec<ByteBuf, PlayAnimationS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayAnimationS2CPacket::playerId,
            ByteBufCodecs.STRING_UTF8,
            PlayAnimationS2CPacket::animId,
            PlayAnimationS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayAnimationS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.playAnimation(packet);
        });
    }
}