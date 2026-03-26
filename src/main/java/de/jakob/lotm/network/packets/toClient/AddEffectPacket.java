package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddEffectPacket(int index, double x, double y, double z, int entityId)
        implements CustomPacketPayload {

    /** Sentinel value meaning "no entity" — maps to the no-scaling code path. */
    public static final int NO_ENTITY = -1;

    /** Convenience constructor for effects with no entity (original API). */
    public AddEffectPacket(int index, double x, double y, double z) {
        this(index, x, y, z, NO_ENTITY);
    }

    public static final Type<AddEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_effect"));

    public static final StreamCodec<ByteBuf, AddEffectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,    AddEffectPacket::index,
            ByteBufCodecs.DOUBLE, AddEffectPacket::x,
            ByteBufCodecs.DOUBLE, AddEffectPacket::y,
            ByteBufCodecs.DOUBLE, AddEffectPacket::z,
            ByteBufCodecs.INT,    AddEffectPacket::entityId,
            AddEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.addEffect(packet.index(), packet.x(), packet.y(), packet.z(), packet.entityId());
            }
        });
    }
}