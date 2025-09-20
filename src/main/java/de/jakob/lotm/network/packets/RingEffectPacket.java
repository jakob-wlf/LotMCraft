package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RingEffectPacket(
        double x, double y, double z,
        float maxRadius, int duration,
        float red, float green, float blue, float alpha,
        float ringThickness, float ringHeight, 
        float expansionSpeed, boolean smoothExpansion
) implements CustomPacketPayload {

    public static final Type<RingEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ring_effect"));

    public static final StreamCodec<FriendlyByteBuf, RingEffectPacket> STREAM_CODEC =
            StreamCodec.of(RingEffectPacket::write, RingEffectPacket::read);

    private static void write(FriendlyByteBuf buf, RingEffectPacket packet) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.maxRadius);
        buf.writeInt(packet.duration);
        buf.writeFloat(packet.red);
        buf.writeFloat(packet.green);
        buf.writeFloat(packet.blue);
        buf.writeFloat(packet.alpha);
        buf.writeFloat(packet.ringThickness);
        buf.writeFloat(packet.ringHeight);
        buf.writeFloat(packet.expansionSpeed);
        buf.writeBoolean(packet.smoothExpansion);
    }

    private static RingEffectPacket read(FriendlyByteBuf buf) {
        return new RingEffectPacket(
                buf.readDouble(),  // x
                buf.readDouble(),  // y
                buf.readDouble(),  // z
                buf.readFloat(),   // maxRadius
                buf.readInt(),     // duration
                buf.readFloat(),   // red
                buf.readFloat(),   // green
                buf.readFloat(),   // blue
                buf.readFloat(),   // alpha
                buf.readFloat(),   // ringThickness
                buf.readFloat(),   // ringHeight
                buf.readFloat(),   // expansionSpeed
                buf.readBoolean()  // smoothExpansion
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RingEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Only handle on client side
            if (context.flow().isClientbound() && FMLEnvironment.dist == Dist.CLIENT) {
                ClientHandler.handleRingPacket(packet);
            }
        });
    }
}