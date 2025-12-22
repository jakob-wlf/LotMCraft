package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record UpdateMovableEffectPositionPacket(UUID effectId, double x, double y, double z) 
    implements CustomPacketPayload {
    
    public static final Type<UpdateMovableEffectPositionPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_movable_effect_position"));
    
    public static final StreamCodec<ByteBuf, UpdateMovableEffectPositionPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public UpdateMovableEffectPositionPacket decode(ByteBuf buf) {
                UUID effectId = new UUID(buf.readLong(), buf.readLong());
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                return new UpdateMovableEffectPositionPacket(effectId, x, y, z);
            }

            @Override
            public void encode(ByteBuf buf, UpdateMovableEffectPositionPacket packet) {
                buf.writeLong(packet.effectId.getMostSignificantBits());
                buf.writeLong(packet.effectId.getLeastSignificantBits());
                buf.writeDouble(packet.x);
                buf.writeDouble(packet.y);
                buf.writeDouble(packet.z);
            }
        };
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(UpdateMovableEffectPositionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.updateMovableEffectPosition(
                    packet.effectId(),
                    packet.x(), packet.y(), packet.z()
                );
            }
        });
    }
}