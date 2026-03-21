package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddDirectionalEffectPacket(int index,
                                         double startX, double startY, double startZ,
                                         double endX, double endY, double endZ,
                                         int duration,
                                         int entityId) implements CustomPacketPayload {

    /** Sentinel value meaning "no entity" — maps to the no-scaling code path. */
    public static final int NO_ENTITY = -1;

    /** Convenience constructor for effects with no entity (original API). */
    public AddDirectionalEffectPacket(int index,
                                      double startX, double startY, double startZ,
                                      double endX, double endY, double endZ,
                                      int duration) {
        this(index, startX, startY, startZ, endX, endY, endZ, duration, NO_ENTITY);
    }

    public static final Type<AddDirectionalEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_directional_effect"));

    public static final StreamCodec<ByteBuf, AddDirectionalEffectPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AddDirectionalEffectPacket decode(ByteBuf buf) {
                    int index    = buf.readInt();
                    double startX = buf.readDouble();
                    double startY = buf.readDouble();
                    double startZ = buf.readDouble();
                    double endX   = buf.readDouble();
                    double endY   = buf.readDouble();
                    double endZ   = buf.readDouble();
                    int duration  = buf.readInt();
                    int entityId  = buf.readInt();
                    return new AddDirectionalEffectPacket(index,
                            startX, startY, startZ,
                            endX, endY, endZ,
                            duration, entityId);
                }

                @Override
                public void encode(ByteBuf buf, AddDirectionalEffectPacket packet) {
                    buf.writeInt(packet.index);
                    buf.writeDouble(packet.startX);
                    buf.writeDouble(packet.startY);
                    buf.writeDouble(packet.startZ);
                    buf.writeDouble(packet.endX);
                    buf.writeDouble(packet.endY);
                    buf.writeDouble(packet.endZ);
                    buf.writeInt(packet.duration);
                    buf.writeInt(packet.entityId);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddDirectionalEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.addDirectionalEffect(
                        packet.index(),
                        packet.startX(), packet.startY(), packet.startZ(),
                        packet.endX(), packet.endY(), packet.endZ(),
                        packet.duration(),
                        packet.entityId()
                );
            }
        });
    }
}