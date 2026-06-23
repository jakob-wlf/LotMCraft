package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemoveMovableEffectS2CPacket(UUID effectId) implements CustomPacketPayload {
    
    public static final Type<RemoveMovableEffectS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "remove_movable_effect"));
    
    public static final StreamCodec<ByteBuf, RemoveMovableEffectS2CPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public RemoveMovableEffectS2CPacket decode(ByteBuf buf) {
                UUID effectId = new UUID(buf.readLong(), buf.readLong());
                return new RemoveMovableEffectS2CPacket(effectId);
            }

            @Override
            public void encode(ByteBuf buf, RemoveMovableEffectS2CPacket packet) {
                buf.writeLong(packet.effectId.getMostSignificantBits());
                buf.writeLong(packet.effectId.getLeastSignificantBits());
            }
        };
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(RemoveMovableEffectS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.removeMovableEffect(packet.effectId());
            }
        });
    }
}