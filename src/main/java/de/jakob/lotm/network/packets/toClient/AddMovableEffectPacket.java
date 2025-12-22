package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record AddMovableEffectPacket(UUID effectId, int index, 
                                     double x, double y, double z,
                                     int duration, boolean infinite) implements CustomPacketPayload {
    
    public static final Type<AddMovableEffectPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_movable_effect"));
    
    public static final StreamCodec<ByteBuf, AddMovableEffectPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public AddMovableEffectPacket decode(ByteBuf buf) {
                UUID effectId = new UUID(buf.readLong(), buf.readLong());
                int index = buf.readInt();
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                int duration = buf.readInt();
                boolean infinite = buf.readBoolean();
                return new AddMovableEffectPacket(effectId, index, x, y, z, duration, infinite);
            }

            @Override
            public void encode(ByteBuf buf, AddMovableEffectPacket packet) {
                buf.writeLong(packet.effectId.getMostSignificantBits());
                buf.writeLong(packet.effectId.getLeastSignificantBits());
                buf.writeInt(packet.index);
                buf.writeDouble(packet.x);
                buf.writeDouble(packet.y);
                buf.writeDouble(packet.z);
                buf.writeInt(packet.duration);
                buf.writeBoolean(packet.infinite);
            }
        };
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(AddMovableEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.addMovableEffect(
                    packet.effectId(),
                    packet.index(),
                    packet.x(), packet.y(), packet.z(),
                    packet.duration(),
                    packet.infinite()
                );
            }
        });
    }
}