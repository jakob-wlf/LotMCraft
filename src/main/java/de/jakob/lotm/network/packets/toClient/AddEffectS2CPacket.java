package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddEffectS2CPacket(int index, double x, double y, double z, int entityId)
        implements CustomPacketPayload {

    /** Sentinel value meaning "no entity" — maps to the no-scaling code path. */
    public static final int NO_ENTITY = -1;

    /** Convenience constructor for effects with no entity (original API). */
    public AddEffectS2CPacket(int index, double x, double y, double z) {
        this(index, x, y, z, NO_ENTITY);
    }

    public static final Type<AddEffectS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_effect"));

    public static final StreamCodec<ByteBuf, AddEffectS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,    AddEffectS2CPacket::index,
            ByteBufCodecs.DOUBLE, AddEffectS2CPacket::x,
            ByteBufCodecs.DOUBLE, AddEffectS2CPacket::y,
            ByteBufCodecs.DOUBLE, AddEffectS2CPacket::z,
            ByteBufCodecs.INT,    AddEffectS2CPacket::entityId,
            AddEffectS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddEffectS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.addEffect(packet.index(), packet.x(), packet.y(), packet.z(), packet.entityId());
            }
        });
    }
}