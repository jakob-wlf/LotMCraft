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

public record PlayAnimationPacket(int playerId, String animId) implements CustomPacketPayload {
    
    public static final Type<PlayAnimationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_animation"));

    public static final StreamCodec<ByteBuf, PlayAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayAnimationPacket::playerId,
            ByteBufCodecs.STRING_UTF8,
            PlayAnimationPacket::animId,
            PlayAnimationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.playAnimation(packet);
        });
    }
}